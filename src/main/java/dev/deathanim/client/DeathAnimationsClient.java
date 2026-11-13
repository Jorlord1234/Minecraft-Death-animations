package dev.deathanim.client;

import com.mojang.logging.LogUtils;
import dev.deathanim.core.Clip;
import dev.deathanim.core.DeathRouter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(value = DeathAnimationsClient.MOD_ID, dist = Dist.CLIENT)
public class DeathAnimationsClient {
	public static final String MOD_ID = "deathanimations";
	public static final Logger LOGGER = LogUtils.getLogger();

	public DeathAnimationsClient() {
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
		// Step 2 of the plan: prove detection works. Step 3 plays the 3D animation here.
		LOGGER.info("Death Animations: died with message key '{}' -> clip '{}'{}", key, clipId,
			clip == null ? " (none yet)" : " (" + clip.duration + "s)");
	}
}
