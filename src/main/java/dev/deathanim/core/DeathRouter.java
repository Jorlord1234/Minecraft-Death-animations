package dev.deathanim.core;

/** Maps a vanilla death-message translation key to the animation that should play. */
public final class DeathRouter {
	private DeathRouter() {}

	/** @return a clip id, or null when no animation exists for this death yet. */
	public static String route(String key) {
		if (key == null) return null;
		if (matches(key, "death.attack.stalagmite")) return "stalagmite";
		if (matches(key, "death.attack.anvil")) return "anvil";
		if (matches(key, "death.attack.cactus")) return "cactus";
		if (matches(key, "death.attack.inFire") || matches(key, "death.attack.onFire")) return "fire";
		if (matches(key, "death.attack.drown")) return "drown";
		if (matches(key, "death.attack.fall") || key.startsWith("death.fell.")) return "fall";
		return null;
	}

	/** Exact match, or base followed by a dot (so ".player" variants match but "fallingBlock" does not match "fall"). */
	private static boolean matches(String key, String base) {
		return key.equals(base) || key.startsWith(base + ".");
	}
}
