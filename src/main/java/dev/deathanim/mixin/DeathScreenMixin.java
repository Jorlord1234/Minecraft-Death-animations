package dev.deathanim.mixin;

import dev.deathanim.client.DeathAnimationsClient;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DeathScreen.class)
public abstract class DeathScreenMixin {
	@Shadow
	@Final
	private Component causeOfDeath;

	@Inject(method = "init", at = @At("TAIL"))
	private void deathanimations$onInit(CallbackInfo ci) {
		DeathAnimationsClient.onDeathScreen(this.causeOfDeath);
	}
}
