package dev.deathanim.client;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;

import java.util.HashMap;
import java.util.Map;

/** Cached single-cuboid models, used to build props out of vanilla block textures. */
final class Boxes {
	private static final Map<String, ModelPart> CACHE = new HashMap<>();

	private Boxes() {}

	/** A box w x h x d (in 1/16 blocks) standing on y=0, centred on x and z. Model space is y-down. */
	static ModelPart get(float w, float h, float d) {
		return CACHE.computeIfAbsent(w + "x" + h + "x" + d, key -> {
			MeshDefinition mesh = new MeshDefinition();
			mesh.getRoot().addOrReplaceChild("b",
				CubeListBuilder.create().texOffs(0, 0).addBox(-w / 2f, -h, -d / 2f, w, h, d),
				PartPose.ZERO);
			return LayerDefinition.create(mesh, 16, 16).bakeRoot().getChild("b");
		});
	}
}
