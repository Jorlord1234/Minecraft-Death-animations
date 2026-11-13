package dev.deathanim.core;

/** One sampled moment of an animation. */
public final class Pose {
	private final double[] v = new double[Prop.COUNT];

	public Pose() {
		reset();
	}

	public void reset() {
		for (Prop p : Prop.values()) v[p.ordinal()] = p.def;
	}

	public double get(Prop p) {
		return v[p.ordinal()];
	}

	void set(Prop p, double value) {
		v[p.ordinal()] = value;
	}
}
