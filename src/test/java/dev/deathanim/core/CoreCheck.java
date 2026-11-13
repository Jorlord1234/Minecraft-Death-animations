package dev.deathanim.core;

import java.util.List;
import java.util.Map;

/** Plain-Java check (no Minecraft needed): the sampler must match the browser prototype. */
public final class CoreCheck {
	static Clip.Key k(double t, Object... kv) {
		Map<Prop, Double> m = new java.util.EnumMap<>(Prop.class);
		boolean in = false;
		for (int i = 0; i < kv.length; i += 2) {
			if (kv[i].equals("ease")) { in = true; continue; }
			m.put(Prop.fromKey((String) kv[i]), ((Number) kv[i + 1]).doubleValue());
		}
		return new Clip.Key(t, m, in);
	}

	static int failures = 0;

	static void eq(String what, double got, double want) {
		if (Math.abs(got - want) > 1e-4) {
			failures++;
			System.out.println("FAIL " + what + ": got " + got + " want " + want);
		}
	}

	public static void main(String[] args) {
		Clip c = new Clip("stalagmite", 5.2, List.of(
			k(0, "y", -240, "armF", -165, "armB", -150),
			k(0.5, "y", -26, "legF", -22, "legB", 22, "ease", 0),
			k(0.55, "eyes", 1, "flash", 0.9, "armF", -70, "armB", -50),
			k(0.9, "flash", 0),
			k(1.8, "tint", 0.3),
			k(2.4, "tint", 0.9),
			k(2.9, "y", -36, "legF", -3, "legB", 3, "armF", -90, "armB", -90, "tint", 1),
			k(3.5),
			k(4.1, "x", 28, "y", -10, "rot", 90, "armF", -165, "armB", -165, "legF", 0, "legB", 0, "stars", 1, "ease", 0),
			k(4.3, "rot", 83, "y", -22, "eyes", 2),
			k(4.6, "rot", 90, "y", -10),
			k(5.2)));
		// t, y, legF, rot, tint, flash, eyes, x  (values produced by the prototype's JS)
		double[][] g = {
			{0, -240, 0, 0, 0, 0, 0, 0}, {0.25, -186.5, -5.5, 0, 0, 0, 0, 0},
			{0.5, -26, -22, 0, 0, 0, 0, 0}, {0.52, -26, -22, 0, 0, 0.3168, 0, 0},
			{0.7, -26, -22, 0, 0, 0.545773, 1, 0}, {2.1, -26, -22, 0, 0.6, 0, 1, 0},
			{2.7, -32.48, -9.688, 0, 0.9648, 0, 1, 0}, {3.0, -36, -3, 0, 1, 0, 1, 0},
			{3.8, -29.5, -2.25, 22.5, 1, 0, 1, 7}, {4.2, -16, 0, 86.5, 1, 0, 1, 28},
			{4.45, -16, 0, 86.5, 1, 0, 2, 28}, {5.2, -10, 0, 90, 1, 0, 2, 28}, {9, -10, 0, 90, 1, 0, 2, 28}};
		Pose p = new Pose();
		for (double[] r : g) {
			c.sample(r[0], p);
			String at = "t=" + r[0] + " ";
			eq(at + "y", p.get(Prop.Y), r[1]);
			eq(at + "legF", p.get(Prop.LEG_F), r[2]);
			eq(at + "rot", p.get(Prop.ROT), r[3]);
			eq(at + "tint", p.get(Prop.TINT), r[4]);
			eq(at + "flash", p.get(Prop.FLASH), r[5]);
			eq(at + "eyes", p.get(Prop.EYES), r[6]);
			eq(at + "x", p.get(Prop.X), r[7]);
		}
		// Rig: walking and hopping layers
		Pose w = new Pose();
		w.set(Prop.WALK, 1);
		w.set(Prop.HOP, 1);
		Rig rig = new Rig();
		rig.solve(w, Math.PI / 28); // sin(14t) = 1
		eq("walk legF", rig.legF, 28);
		eq("walk legB", rig.legB, -28);
		eq("walk armF", rig.armF, -18 + 25 * Math.sin(Math.PI / 28 * 11));
		rig.solve(w, Math.PI / 22); // sin(11t) = 1
		eq("hop y", rig.y, -30);
		Pose idle = new Pose();
		rig.solve(idle, 1.234);
		eq("idle legF", rig.legF, 0);
		eq("idle y", rig.y, 0);
		String[][] routes = {
			{"death.attack.fall", "fall"}, {"death.attack.fall.player", "fall"}, {"death.fell.accident.ladder", "fall"},
			{"death.attack.fallingBlock", null}, {"death.attack.stalagmite", "stalagmite"},
			{"death.attack.anvil", "anvil"}, {"death.attack.cactus.player", "cactus"},
			{"death.attack.inFire", "fire"}, {"death.attack.onFire.player", "fire"},
			{"death.attack.drown", "drown"}, {"death.attack.lava", null}, {null, null}};
		for (String[] r : routes) {
			String got = DeathRouter.route(r[0]);
			if (!java.util.Objects.equals(got, r[1])) { failures++; System.out.println("FAIL route " + r[0] + " -> " + got); }
		}
		System.out.println(failures == 0 ? "ALL CHECKS PASSED" : failures + " FAILURES");
		if (failures > 0) System.exit(1);
	}
}
