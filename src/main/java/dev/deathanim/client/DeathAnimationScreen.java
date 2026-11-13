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
 * Plays a death clip on a stage using the player's own skin, in 3D, with a setting and props
 * that fit the death (cave, underwater, desert...).
 */
public class DeathAnimationScreen extends Screen {
	/** Which way the player is turned. 90 = side-on facing right, 180 = facing the camera. */
	private static final float VIEW_YAW = 125f;
	/** Blocks per prototype pixel (player is 1.8 blocks = 120 px). */
	private static final double K = 1.8 / 120.0;
	private static final int WHITE = 0xFFFFFFFF;
	/** Depth for flat overlays so they draw in front of the 3D scene. */
	private static final float OVERLAY_Z = 400f;

	private static final ResourceLocation DRIPSTONE = block("dripstone_block");
	private static final ResourceLocation DARK = block("gray_concrete");
	private static final ResourceLocation LIGHT = block("light_gray_concrete");
	private static final ResourceLocation CACTUS = block("cactus_side");
	private static final ResourceLocation ORANGE = block("orange_concrete");
	private static final ResourceLocation YELLOW = block("yellow_concrete");

	private enum Env { SURFACE, CAVE, UNDERWATER, DESERT }

	/** Flame positions on the body: x (sideways), y (up), z, in blocks. */
	private static final double[][] FLAMES = {
		{-0.2, 0.0, 0.1}, {0.2, 0.0, -0.1}, {0.0, 0.0, 0.25}, {0.0, 0.0, -0.25},
		{-0.22, 0.85, 0.0}, {0.22, 0.85, 0.0}, {0.0, 1.2, 0.2}, {0.0, 1.65, 0.0}
	};

	private final Clip clip;
	private final Env env;
	private final Pose pose = new Pose();
	private final Rig rig = new Rig();
	private long startNanos = System.nanoTime();
	private double animTime;

	public DeathAnimationScreen(Clip clip) {
		super(Component.literal("Death animation: " + clip.id));
		this.clip = clip;
		this.env = switch (clip.id) {
			case "stalagmite" -> Env.CAVE;
			case "drown" -> Env.UNDERWATER;
			case "cactus" -> Env.DESERT;
			default -> Env.SURFACE;
		};
	}

	private static ResourceLocation block(String name) {
		return ResourceLocation.withDefaultNamespace("textures/block/" + name + ".png");
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
		animTime = Math.min(t, clip.duration);
		clip.sample(animTime, pose);
		rig.solve(pose, animTime);

		int groundY = (int) (height * 0.78);
		float scale = height * (env == Env.UNDERWATER ? 0.15f : 0.24f); // pixels per block
		drawBackground(g, groundY, scale);
		drawStage(g, groundY, scale);
		drawOverlays(g, groundY, scale);

		g.drawCenteredString(font, title, width / 2, 8, 0xFFFFFFFF);
		g.drawCenteredString(font, Component.literal("Space: replay    Esc: close"), width / 2, height - 14, 0xFFFFFFFF);
	}

	// ---------------------------------------------------------------- 2D setting

	private void drawBackground(GuiGraphics g, int groundY, float scale) {
		switch (env) {
			case SURFACE -> {
				g.fill(0, 0, width, groundY, 0xFFBFE0F2);
				g.fill(0, groundY, width, height, 0xFF7A5230);
				g.fill(0, groundY, width, groundY + 4, 0xFF5D9C3B);
			}
			case DESERT -> {
				g.fill(0, 0, width, groundY, 0xFFBFE0F2);
				g.fill(0, groundY, width, height, 0xFFDBCB8B);
				g.fill(0, groundY, width, groundY + 4, 0xFFE9DCA6);
			}
			case CAVE -> {
				g.fill(0, 0, width, height, 0xFF15151B);
				g.fill(0, 0, width, (int) (height * 0.1), 0xFF2B2B33);
				g.fill(0, groundY, width, height, 0xFF4A4A52);
				g.fill(0, groundY, width, groundY + 4, 0xFF62626C);
			}
			case UNDERWATER -> {
				int surfaceY = surfaceY(groundY, scale);
				g.fill(0, 0, width, surfaceY, 0xFFBFE0F2);
				g.fill(0, surfaceY, width, groundY, 0xFF2A6FC0);
				g.fill(0, groundY, width, height, 0xFFDCCB8F);
				g.fill(0, groundY, width, groundY + 4, 0xFFE9DCA6);
				g.fill(0, surfaceY - 1, width, surfaceY + 2, 0xFFFFFFFF);
			}
		}
	}

	private static int surfaceYOffsetPx(float scale) {
		return (int) (250 * K * scale);
	}

	private int surfaceY(int groundY, float scale) {
		return groundY - surfaceYOffsetPx(scale);
	}

	/** Flat effects drawn in front of everything: water tint, bubbles, flash. */
	private void drawOverlays(GuiGraphics g, int groundY, float scale) {
		g.pose().pushPose();
		g.pose().translate(0, 0, OVERLAY_Z);
		if (env == Env.UNDERWATER) {
			g.fill(0, surfaceY(groundY, scale), width, groundY, 0x552F7FD0);
			double bub = pose.get(Prop.BUBBLES);
			if (bub > 0.01) {
				int a = (int) (bub * 0.8 * 255) << 24;
				for (int i = 0; i < 6; i++) {
					double rise = (animTime * 55 + i * 45) % 230;
					int bx = (int) (width / 2f + (-26 + i * 10 + Math.sin(animTime * 3 + i) * 5) * K * scale);
					int by = (int) (groundY - rise * K * scale);
					int s = Math.max(3, (int) ((3 + i % 3) * 2 * K * scale));
					g.fill(bx, by, bx + s, by + 1, a | 0xFFFFFF);
					g.fill(bx, by + s - 1, bx + s, by + s, a | 0xFFFFFF);
					g.fill(bx, by, bx + 1, by + s, a | 0xFFFFFF);
					g.fill(bx + s - 1, by, bx + s, by + s, a | 0xFFFFFF);
				}
			}
		}
		double flash = pose.get(Prop.FLASH);
		if (flash > 0.01) {
			g.fill(0, 0, width, height, ((int) (flash * 255) << 24) | 0xFFFFFF);
		}
		g.pose().popPose();
	}

	// ---------------------------------------------------------------- 3D stage

	private void drawStage(GuiGraphics g, int groundY, float scale) {
		Minecraft mc = Minecraft.getInstance();
		AbstractClientPlayer player = mc.player;
		if (player == null) return;
		PlayerRenderer renderer = (PlayerRenderer) mc.getEntityRenderDispatcher().getRenderer(player);
		PlayerModel<AbstractClientPlayer> m = renderer.getModel();
		ResourceLocation skin = renderer.getTextureLocation(player);
		poseBones(m);

		PoseStack ps = g.pose();
		ps.pushPose();
		ps.translate(width / 2f, groundY, 50f);
		ps.scale(scale, scale, -scale);
		ps.mulPose(new Quaternionf().rotateZ((float) Math.PI)); // y-up, like the inventory paper doll
		ps.mulPose(Axis.YP.rotationDegrees(180f - VIEW_YAW));   // turn the whole scene; forward is -z
		Lighting.setupForEntityInInventory();

		drawScenery(g, ps, groundY, scale);

		// the player: the animation moves the body, pivoting on the feet
		ps.pushPose();
		ps.translate(0, -rig.y * K, -rig.x * K);
		ps.mulPose(Axis.XP.rotationDegrees((float) -rig.rot));
		ps.scale((float) rig.sx, (float) rig.sy, (float) rig.sx);
		drawBodyEffects(g, ps);

		ps.pushPose();
		ps.scale(-1f, -1f, 1f);                 // same flips the vanilla player renderer applies
		ps.scale(0.9375f, 0.9375f, 0.9375f);
		ps.translate(0, -1.501f, 0);
		VertexConsumer vc = g.bufferSource().getBuffer(RenderType.entityCutoutNoCull(skin));
		m.renderToBuffer(ps, vc, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, bodyColor());
		ps.popPose();
		ps.popPose();

		g.flush();
		ps.popPose();
		Lighting.setupFor3DItems();
	}

	private void poseBones(PlayerModel<AbstractClientPlayer> m) {
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
	}

	/** Red tint and ash grey as a colour multiplier. */
	private int bodyColor() {
		double tint = pose.get(Prop.TINT) * 0.6;
		double ash = pose.get(Prop.ASH);
		double r = 1, gr = 1 - tint, b = 1 - tint;
		double avg = (r + gr + b) / 3;
		double dark = 1 - 0.55 * ash;
		r = (r + (avg - r) * ash) * dark;
		gr = (gr + (avg - gr) * ash) * dark;
		b = (b + (avg - b) * ash) * dark;
		return (255 << 24) | ((int) (r * 255) << 16) | ((int) (gr * 255) << 8) | (int) (b * 255);
	}

	/** Props that sit in the world, not on the player. */
	private void drawScenery(GuiGraphics g, PoseStack ps, int groundY, float scale) {
		switch (clip.id) {
			case "stalagmite" -> {
				float[] w = {8f, 6.5f, 5f, 3.5f, 2f}; // a stepped, pointed rock about one block tall
				for (int i = 0; i < 5; i++) {
					box(g, ps, DRIPSTONE, w[i], 3.4f, w[i], 0, i * 3.4 / 16.0, 0, 1, 1, 1);
				}
				double ceiling = (groundY - height * 0.1) / scale; // blocks above the floor
				double[][] hanging = {{-1.6, -1.2}, {1.4, 0.9}, {-0.3, -2.3}, {2.3, -1.0}, {-2.2, 1.0}};
				float[] hw = {6f, 4.5f, 3f, 1.8f};
				for (double[] h : hanging) {
					for (int i = 0; i < 4; i++) {
						box(g, ps, DRIPSTONE, hw[i], 3.4f, hw[i], h[0], ceiling - (i + 1) * 3.4 / 16.0, h[1], 1, 1, 1);
					}
				}
			}
			case "anvil" -> {
				double ay = -pose.get(Prop.OBJ_Y) * K; // underside of the anvil, in blocks
				box(g, ps, DARK, 10f, 2.5f, 10f, 0, ay, 0, 1, 1, 1);
				box(g, ps, DARK, 5f, 3.5f, 6f, 0, ay + 2.5 / 16.0, 0, 1, 1, 1);
				box(g, ps, LIGHT, 8f, 3.5f, 16f, 0, ay + 6.0 / 16.0, 0, 1, 1, 1);
			}
			case "cactus" -> {
				double cz = -48 * K;
				box(g, ps, CACTUS, 6f, 20f, 6f, 0, 0, cz, 1, 1, 1);
				box(g, ps, CACTUS, 5f, 2.5f, 2.5f, -5.5 / 16.0, 9.0 / 16.0, cz, 1, 1, 1);
				box(g, ps, CACTUS, 2.5f, 5f, 2.5f, -6.75 / 16.0, 11.5 / 16.0, cz, 1, 1, 1);
				box(g, ps, CACTUS, 5f, 2.5f, 2.5f, 5.5 / 16.0, 5.0 / 16.0, cz, 1, 1, 1);
				box(g, ps, CACTUS, 2.5f, 4f, 2.5f, 6.75 / 16.0, 7.5 / 16.0, cz, 1, 1, 1);
			}
			default -> { }
		}
	}

	/** Props that ride on the player: flames and circling stars. Drawn in the player's own frame. */
	private void drawBodyEffects(GuiGraphics g, PoseStack ps) {
		double fire = pose.get(Prop.FIRE);
		if (fire > 0.02) {
			for (int i = 0; i < FLAMES.length; i++) {
				double[] f = FLAMES[i];
				double flicker = (0.85 + 0.25 * Math.sin(animTime * 18 + i * 2)) * fire;
				box(g, ps, ORANGE, 4f, 6f, 4f, f[0], f[1], f[2], fire, flicker, fire);
				box(g, ps, YELLOW, 2.5f, 5f, 2.5f, f[0], f[1] + 6.0 / 16.0 * flicker, f[2], fire, flicker, fire);
			}
		}
		if (pose.get(Prop.STARS) > 0.01) {
			for (int i = 0; i < 3; i++) {
				double a = animTime * 4 + i * 2.09;
				box(g, ps, YELLOW, 2.5f, 2.5f, 2.5f, Math.cos(a) * 0.3, 1.95, Math.sin(a) * 0.3, 1, 1, 1);
			}
		}
	}

	/** Draws a textured box standing at (x, y, z) in blocks, in the current frame (y is up). */
	private void box(GuiGraphics g, PoseStack ps, ResourceLocation tex, float w, float h, float d,
					 double x, double y, double z, double sx, double sy, double sz) {
		ps.pushPose();
		ps.translate(x, y, z);
		ps.scale((float) -sx, (float) -sy, (float) sz); // model space is y-down
		VertexConsumer vc = g.bufferSource().getBuffer(RenderType.entityCutoutNoCull(tex));
		Boxes.get(w, h, d).render(ps, vc, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, WHITE);
		ps.popPose();
	}
}
