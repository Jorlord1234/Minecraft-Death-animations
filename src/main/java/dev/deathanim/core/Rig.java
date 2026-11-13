package dev.deathanim.core;

/**
 * Turns a sampled {@link Pose} into final body and limb values by layering on the
 * repeating motions (walking, hopping, flailing). Same maths as the browser preview.
 * Units: pixels (120 = player height), degrees. Negative y is up.
 */
public final class Rig {
	public double x, y, rot, sx, sy, armF, armB, legF, legB;

	public void solve(Pose p, double t) {
		armF = p.get(Prop.ARM_F);
		armB = p.get(Prop.ARM_B);
		legF = p.get(Prop.LEG_F);
		legB = p.get(Prop.LEG_B);
		y = p.get(Prop.Y);
		x = p.get(Prop.X);
		rot = p.get(Prop.ROT);
		sx = p.get(Prop.SX);
		sy = p.get(Prop.SY);

		double walk = Math.sin(t * 14) * p.get(Prop.WALK);
		legF += walk * 28;
		legB -= walk * 28;
		armF -= walk * 18;
		armB += walk * 18;

		double hop = Math.sin(t * 11);
		double hopAmt = p.get(Prop.HOP);
		y -= hopAmt * Math.abs(hop) * 30;
		armF += hopAmt * hop * 25;
		armB -= hopAmt * hop * 25;

		double flail = p.get(Prop.FLAIL);
		armF += flail * Math.sin(t * 10) * 45;
		armB += flail * Math.sin(t * 10 + 2) * 45;
		legF += flail * Math.sin(t * 10 + 1) * 25;
		legB -= flail * Math.sin(t * 10 + 1) * 25;
	}
}
