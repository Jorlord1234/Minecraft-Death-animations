package dev.deathanim.core;

/**
 * Every value an animation can drive. Units match the browser prototype:
 * pixels (player = 120 px tall), degrees, and 0..1 amounts.
 * Rotations: positive limb angle swings the limb backwards, negative swings it forwards.
 */
public enum Prop {
	X("x", 0), Y("y", 0), ROT("rot", 0), SX("sx", 1), SY("sy", 1),
	ARM_F("armF", 0), ARM_B("armB", 0), LEG_F("legF", 0), LEG_B("legB", 0),
	EYES("eyes", 0), TINT("tint", 0), WALK("walk", 0), HOP("hop", 0), FLAIL("flail", 0),
	STARS("stars", 0), FIRE("fire", 0), SPIKES("spikes", 0), ASH("ash", 0),
	OBJ_Y("oy", -430), FLASH("flash", 0), BUBBLES("bub", 0);

	public static final int COUNT = values().length;
	public final String key;
	public final double def;

	Prop(String key, double def) {
		this.key = key;
		this.def = def;
	}

	public static Prop fromKey(String key) {
		for (Prop p : values()) {
			if (p.key.equals(key)) return p;
		}
		throw new IllegalArgumentException("Unknown animation property: " + key);
	}
}
