package dev.deathanim.client;

import dev.deathanim.core.Clip;
import dev.deathanim.core.DeathRouter;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeathAnimationsClient implements ClientModInitializer {
	public static final String MOD_ID = "deathanimations";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
		ClipLibrary.load();
		LOGGER.info("Death Animations: loaded {} clips", ClipLibrary.count());
	}

	/** Called by DeathScreenMixin every time the death screen opens. */
	public static void onDeathScreen(Component cause) {
		String key = null;
		if (cause != null && cause.getContents() instanceof TranslatableContents t) {
			key = t.getKey();
		}
		String clipId = DeathRouter.route(key);
		Clip clip = clipId == null ? null : ClipLibrary.get(clipId);
		// Step 2 of the plan: just prove detection works. Step 3 will play the 3D animation here.
		LOGGER.info("Death Animations: died with message key '{}' -> clip '{}'{}", key, clipId,
			clip == null ? " (none yet)" : " (" + clip.duration + "s)");
	}
}
