package net.ccbluex.classycreepers.feature;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.monster.creeper.CreeperModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import net.minecraft.resources.Identifier;

public final class CreeperClassinessFeatureRenderer
		extends RenderLayer<CreeperRenderState, CreeperModel> {
	private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("classycreepers", "tophat.png");
	public static final ModelLayerLocation MODEL_LAYER = new ModelLayerLocation(
			Identifier.fromNamespaceAndPath("classycreepers", "top_hat"),
			"main"
	);

	private final TopHatModel model;

	public CreeperClassinessFeatureRenderer(
			RenderLayerParent<CreeperRenderState, CreeperModel> renderer,
			EntityRendererProvider.Context context
	) {
		super(renderer);
		this.model = new TopHatModel(context.bakeLayer(MODEL_LAYER));
	}

	public static LayerDefinition createLayerDefinition() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();

		CubeListBuilder classyAccessories = CubeListBuilder.create()
				// Hat brim
				.texOffs(9, 0).addBox(4.0F, -8.0F, -4.0F, 1.0F, 1.0F, 8.0F)
				.texOffs(9, 0).addBox(-5.0F, -8.0F, -4.0F, 1.0F, 1.0F, 8.0F)
				.texOffs(9, 0).addBox(-5.0F, -8.0F, 4.0F, 10.0F, 1.0F, 1.0F)
				.texOffs(9, 0).addBox(-5.0F, -8.0F, -5.0F, 10.0F, 1.0F, 1.0F)
				// Hat crown and band
				.texOffs(10, 0).addBox(-4.0F, -16.0F, -4.0F, 8.0F, 6.0F, 8.0F)
				.texOffs(0, 22).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 2.0F, 8.0F)
				// Monocle frame and chain
				.texOffs(0, 0).addBox(1.0F, -7.0F, -5.0F, 2.0F, 1.0F, 1.0F)
				.texOffs(0, 0).addBox(1.0F, -4.0F, -5.0F, 2.0F, 1.0F, 1.0F)
				.texOffs(0, 0).addBox(0.0F, -6.0F, -5.0F, 1.0F, 2.0F, 1.0F)
				.texOffs(0, 0).addBox(3.0F, -6.0F, -5.0F, 1.0F, 2.0F, 1.0F)
				.texOffs(0, 0).addBox(4.0F, -6.0F, -4.0F, 1.0F, 1.0F, 1.0F)
				.texOffs(0, 0).addBox(4.0F, -5.0F, -3.0F, 1.0F, 2.0F, 1.0F)
				.texOffs(0, 0).addBox(4.0F, -3.0F, -2.0F, 1.0F, 2.0F, 1.0F)
				.texOffs(0, 0).addBox(4.0F, -1.0F, -1.0F, 1.0F, 1.0F, 1.0F)
				.texOffs(-1, 19).addBox(1.0F, -6.0F, -5.0F, 2.0F, 2.0F, 1.0F);

		root.addOrReplaceChild("classy_accessories", classyAccessories, PartPose.offset(0.0F, 6.0F, 0.0F));
		return LayerDefinition.create(mesh, 64, 32);
	}

	@Override
	public void submit(
			PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector,
			int packedLight,
			CreeperRenderState renderState,
			float yRot,
			float xRot
	) {
		renderColoredCutoutModel(
				this.model,
				TEXTURE,
				poseStack,
				submitNodeCollector,
				packedLight,
				renderState,
				-1,
				0
		);
	}

	private static final class TopHatModel extends EntityModel<CreeperRenderState> {
		private static final float DEGREES_TO_RADIANS = (float) (Math.PI / 180.0);
		private final ModelPart accessories;

		private TopHatModel(ModelPart root) {
			super(root);
			this.accessories = root.getChild("classy_accessories");
		}

		@Override
		public void setupAnim(CreeperRenderState renderState) {
			super.setupAnim(renderState);
			this.accessories.yRot = renderState.yRot * DEGREES_TO_RADIANS;
			this.accessories.xRot = renderState.xRot * DEGREES_TO_RADIANS;
		}
	}
}
