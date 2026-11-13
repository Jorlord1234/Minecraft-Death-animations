package dev.deathanim.client;

import com.mojang.brigadier.arguments.StringArgumentType;
import dev.deathanim.core.Clip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

/** /deathanim to list animations, /deathanim <name> to play one. */
@EventBusSubscriber(modid = DeathAnimationsClient.MOD_ID, value = Dist.CLIENT)
public final class AnimCommand {
	private static volatile Clip pending;

	private AnimCommand() {}

	@SubscribeEvent
	static void onRegisterCommands(RegisterClientCommandsEvent event) {
		event.getDispatcher().register(
			Commands.literal("deathanim")
				.executes(ctx -> {
					say("Animations: " + String.join(", ", ClipLibrary.ids()));
					return 1;
				})
				.then(Commands.argument("clip", StringArgumentType.word())
					.suggests((ctx, builder) -> SharedSuggestionProvider.suggest(ClipLibrary.ids(), builder))
					.executes(ctx -> {
						String id = StringArgumentType.getString(ctx, "clip");
						Clip clip = ClipLibrary.get(id);
						if (clip == null) {
							say("No animation called '" + id + "'. Try: " + String.join(", ", ClipLibrary.ids()));
							return 0;
						}
						pending = clip; // opened on the next tick, after the chat screen has closed
						return 1;
					})));
	}

	@SubscribeEvent
	static void onClientTick(ClientTickEvent.Post event) {
		Clip clip = pending;
		if (clip == null) return;
		Minecraft mc = Minecraft.getInstance();
		if (mc.screen instanceof ChatScreen) return;
		pending = null;
		mc.setScreen(new DeathAnimationScreen(clip));
	}

	private static void say(String text) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player != null) mc.player.displayClientMessage(Component.literal(text), false);
	}
}
