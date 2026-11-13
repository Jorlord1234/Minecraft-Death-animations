package dev.deathanim.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.deathanim.core.Clip;
import dev.deathanim.core.Prop;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Loads the clip JSON files bundled in the mod jar. */
public final class ClipLibrary {
	private static final String[] IDS = {"fall", "stalagmite", "anvil", "cactus", "fire", "drown"};
	private static final Map<String, Clip> CLIPS = new HashMap<>();

	private ClipLibrary() {}

	public static void load() {
		CLIPS.clear();
		for (String id : IDS) {
			String path = "/assets/deathanimations/clips/" + id + ".json";
			try (InputStream in = ClipLibrary.class.getResourceAsStream(path)) {
				if (in == null) throw new IOException("missing " + path);
				JsonObject root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
				List<Clip.Key> keys = new ArrayList<>();
				for (JsonElement el : root.getAsJsonArray("keys")) {
					JsonObject k = el.getAsJsonObject();
					Map<Prop, Double> set = new EnumMap<>(Prop.class);
					for (Map.Entry<String, JsonElement> e : k.getAsJsonObject("set").entrySet()) {
						set.put(Prop.fromKey(e.getKey()), e.getValue().getAsDouble());
					}
					boolean easeIn = k.has("ease") && "in".equals(k.get("ease").getAsString());
					keys.add(new Clip.Key(k.get("t").getAsDouble(), set, easeIn));
				}
				CLIPS.put(id, new Clip(id, root.get("duration").getAsDouble(), keys));
			} catch (IOException | RuntimeException e) {
				DeathAnimationsClient.LOGGER.error("Could not load death clip '{}'", id, e);
			}
		}
	}

	public static Clip get(String id) {
		return CLIPS.get(id);
	}

	/** Ids of the clips that loaded, in a stable order. */
	public static List<String> ids() {
		List<String> out = new ArrayList<>();
		for (String id : IDS) {
			if (CLIPS.containsKey(id)) out.add(id);
		}
		return out;
	}

	public static int count() {
		return CLIPS.size();
	}
}
