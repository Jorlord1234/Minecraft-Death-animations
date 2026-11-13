package dev.deathanim.core;

import java.util.List;
import java.util.Map;

/**
 * A keyframed animation. Keyframes are written sparsely (only what changes);
 * unspecified values carry over from the previous keyframe.
 */
public final class Clip {
	public record Key(double t, Map<Prop, Double> set, boolean easeIn) {}

	public final String id;
	public final double duration;
	private final double[] times;
	private final double[][] full;
	private final boolean[] easeIn;

	public Clip(String id, double duration, List<Key> keys) {
		if (keys.isEmpty()) throw new IllegalArgumentException("Clip " + id + " has no keys");
		this.id = id;
		this.duration = duration;
		int n = keys.size();
		times = new double[n];
		full = new double[n][];
		easeIn = new boolean[n];
		double[] running = new double[Prop.COUNT];
		for (Prop p : Prop.values()) running[p.ordinal()] = p.def;
		for (int i = 0; i < n; i++) {
			Key k = keys.get(i);
			if (i > 0 && k.t() < times[i - 1]) {
				throw new IllegalArgumentException("Clip " + id + ": key times must not go backwards");
			}
			for (Map.Entry<Prop, Double> e : k.set().entrySet()) running[e.getKey().ordinal()] = e.getValue();
			times[i] = k.t();
			full[i] = running.clone();
			easeIn[i] = k.easeIn();
		}
	}

	/** Fills {@code out} with the pose at time {@code t} (seconds). */
	public void sample(double t, Pose out) {
		int last = times.length - 1;
		int i = 0;
		while (i < last && times[i + 1] <= t) i++;
		int j = Math.min(i + 1, last);
		double u = times[j] > times[i] ? (t - times[i]) / (times[j] - times[i]) : 1;
		u = Math.max(0, Math.min(1, u));
		u = easeIn[j] ? u * u : u * u * (3 - 2 * u);
		for (Prop p : Prop.values()) {
			int k = p.ordinal();
			out.set(p, full[i][k] + (full[j][k] - full[i][k]) * u);
		}
		// eyes are a discrete face texture, so they snap instead of blending
		out.set(Prop.EYES, full[i][Prop.EYES.ordinal()]);
	}
}
