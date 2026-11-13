package dev.deathanim.client;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.deathanim.core.Clip;
import dev.deathanim.core.Pose;
import dev.deathanim.core.Prop;
import dev.deathanim.core.Rig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.joml.Quaternionf;
import org.lwjgl.glfw.GLFW;

/**
 * Plays a death clip on a stage using the player's own skin, in 3D.
 * First version: body motion, tint, ash and flash. Props (stalagmite, anvil, flames...) come next.
 */
public class DeathAnimationScreen extends Screen {
	/** Which way the player is turned. 90 = side-on facing right, 180 = facing the camera. */
	private static final float VIEW_YAW = 125f;
	/** Blocks per prototype pixel (player is 1.8 blocks = 120 px). */
	private static final float K = 1.8f / 120f;

	private final Clip clip;
	private final Pose pose = new Pose();
	private final Rig rig = new Rig();
	private long startNanos = System.nanoTime();

	public DeathAnimationScreen(Clip clip) {
		super(Component.literal("Death animation: " + clip.id));
		this.clip = clip;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public boolean keyPressed(int key, int scancode, int mods) {
		if (key == GLFW.GLFW_KEY_SPACE) {
			startNanos = System.nanoTime();
			return true;
		}
		return super.keyPressed(key, scancode, mods);
	}

	@Override
	public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
		double t = (System.nanoTime() - startNanos) / 1e9;
		if (t > clip.duration + 0.8) {
			startNanos = System.nanoTime();
			t = 0;
		}
		double animTime = Math.min(t, clip.duration);
		clip.sample(animTime, pose);
		rig.solve(pose, animTime);

		int groundY = (int) (height * 0.78);
		g.fill(0, 0, width, groundY, 0xFFBFE0F2);
		g.fill(0, groundY, width, height, 0xFF7A5230);
		g.fill(0, groundY, width, groundY + 4, 0xFF5D9C3B);

		drawPlayer(g, groundY);

		double flash = pose.get(Prop.FLASH);
		if (flash > 0.01) {
			g.fill(0, 0, width, height, ((int) (flash * 255) << 24) | 0xFFFFFF);
		}
		g.drawCenteredString(font, title, width / 2, 8, 0xFF202020);
		g.drawCenteredString(font, Component.literal("Space: replay    Esc: close"), width / 2, height - 14, 0xFF202020);
	}

	private void drawPlayer(GuiGraphics g, int groundY) {
		Minecraft mc = Minecraft.getInstance();
		AbstractClientPlayer player = mc.player;
		if (player == null) return;
		PlayerRenderer renderer = (PlayerRenderer) mc.getEntityRenderDispatcher().getRenderer(player);
		PlayerModel<AbstractClientPlayer> m = renderer.getModel();
		ResourceLocation skin = renderer.getTextureLocation(player);

		// pose the bones
		m.head.resetPose();
		m.hat.resetPose();
		m.body.resetPose();
		m.jacket.resetPose();
		m.leftArm.resetPose();
		m.rightArm.resetPose();
		m.leftSleeve.resetPose();
		m.rightSleeve.resetPose();
		m.leftLeg.resetPose();
		m.rightLeg.resetPose();
		m.leftPants.resetPose();
		m.rightPants.resetPose();
		m.setAllVisible(true);
		m.young = false;
		m.leftArm.xRot = (float) Math.toRadians(rig.armF);
		m.rightArm.xRot = (float) Math.toRadians(rig.armB);
		m.leftLeg.xRot = (float) Math.toRadians(rig.legF);
		m.rightLeg.xRot = (float) Math.toRadians(rig.legB);
		m.hat.copyFrom(m.head);
		m.jacket.copyFrom(m.body);
		m.leftSleeve.copyFrom(m.leftArm);
		m.rightSleeve.copyFrom(m.rightArm);
		m.leftPants.copyFrom(m.leftLeg);
		m.rightPants.copyFrom(m.rightLeg);

		// colour: red tint and ash grey
		double tint = pose.get(Prop.TINT) * 0.6;
		double ash = pose.get(Prop.ASH);
		double r = 1, gr = 1 - tint, b = 1 - tint;
		double avg = (r + gr + b) / 3;
		double dark = 1 - 0.55 * ash;
		r = (r + (avg - r) * ash) * dark;
		gr = (gr + (avg - gr) * ash) * dark;
		b = (b + (avg - b) * ash) * dark;
		int color = (255 << 24) | ((int) (r * 255) << 16) | ((int) (gr * 255) << 8) | (int) (b * 255);

		// place and render (same set-up as the inventory paper doll)
		float scale = height * 0.24f;
		PoseStack ps = g.pose();
		ps.pushPose();
		ps.translate(width / 2f, groundY, 50f);
		ps.scale(scale, scale, -scale);
		ps.mulPose(new Quaternionf().rotateZ((float) Math.PI));
		ps.mulPose(Axis.YP.rotationDegrees(180f - VIEW_YAW));
		// animation moves the whole body, pivoting on the feet (forward is -z here)
		ps.translate(0, -rig.y * K, -rig.x * K);
		ps.mulPose(Axis.XP.rotationDegrees((float) -rig.rot));
		ps.scale((float) rig.sx, (float) rig.sy, (float) rig.sx);
		// same flips the vanilla player renderer applies
		ps.scale(-1f, -1f, 1f);
		ps.scale(0.9375f, 0.9375f, 0.9375f);
		ps.translate(0, -1.501f, 0);

		Lighting.setupForEntityInInventory();
		VertexConsumer vc = g.bufferSource().getBuffer(RenderType.entityCutoutNoCull(skin));
		m.renderToBuffer(ps, vc, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, color);
		g.flush();
		ps.popPose();
		Lighting.setupFor3DItems();
	}
}
