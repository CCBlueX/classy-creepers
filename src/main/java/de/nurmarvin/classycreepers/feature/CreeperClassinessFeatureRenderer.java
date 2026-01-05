package de.nurmarvin.classycreepers.feature;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.CreeperEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.util.Identifier;
import java.util.Arrays;
import java.util.List;

public class CreeperClassinessFeatureRenderer extends FeatureRenderer<CreeperEntity, CreeperEntityModel<CreeperEntity>> {
  private static final Identifier TOP_HAT = new Identifier("classycreepers", "tophat.png");

  private final ModelPart hat;
  private final ModelPart hat2;
  private final ModelPart brim1;
  private final ModelPart brim2;
  private final ModelPart brim3;
  private final ModelPart brim4;
  private final ModelPart mono1;
  private final ModelPart mono2;
  private final ModelPart mono3;
  private final ModelPart mono4;
  private final ModelPart mono5;
  private final ModelPart mono6;
  private final ModelPart mono7;
  private final ModelPart mono8;
  private final ModelPart monoGlass;
  private final List<ModelPart> modelParts;

  public CreeperClassinessFeatureRenderer(FeatureRendererContext<CreeperEntity, CreeperEntityModel<CreeperEntity>> context) {
    super(context);

    CreeperEntityModel<CreeperEntity> model = new CreeperEntityModel<>(1.0f);

    this.brim1 = new ModelPart(model, 9, 0);
    this.brim1.addCuboid(4.0f, -8.0f, -4.0f, 1.0f, 1.0f, 8.0f, 0.0f);
    this.brim1.setPivot(0.0f, 6.0f, 0.0f);

    this.brim2 = new ModelPart(model, 9, 0);
    this.brim2.addCuboid(-5.0f, -8.0f, -4.0f, 1.0f, 1.0f, 8.0f, 0.0f);
    this.brim2.setPivot(0.0f, 6.0f, 0.0f);

    this.brim3 = new ModelPart(model, 9, 0);
    this.brim3.addCuboid(-5.0f, -8.0f, 4.0f, 10.0f, 1.0f, 1.0f, 0.0f);
    this.brim3.setPivot(0.0f, 6.0f, 0.0f);

    this.brim4 = new ModelPart(model, 9, 0);
    this.brim4.addCuboid(-5.0f, -8.0f, -5.0f, 10.0f, 1.0f, 1.0f, 0.0f);
    this.brim4.setPivot(0.0f, 6.0f, 0.0f);

    this.hat = new ModelPart(model, 10, 0);
    this.hat.addCuboid(-4.0f, -16.0f, -4.0f, 8.0f, 6.0f, 8.0f, 0.0f);
    this.hat.setPivot(0.0f, 6.0f, 0.0f);

    this.hat2 = new ModelPart(model, 0, 22);
    this.hat2.addCuboid(-4.0f, -10.0f, -4.0f, 8.0f, 2.0f, 8.0f, 0.0f);
    this.hat2.setPivot(0.0f, 6.0f, 0.0f);

    this.mono1 = new ModelPart(model, 0, 0);
    this.mono1.addCuboid(1.0f, -7.0f, -5.0f, 2.0f, 1.0f, 1.0f, 0.0f);
    this.mono1.setPivot(0.0f, 6.0f, 0.0f);

    this.mono2 = new ModelPart(model, 0, 0);
    this.mono2.addCuboid(1.0f, -4.0f, -5.0f, 2.0f, 1.0f, 1.0f, 0.0f);
    this.mono2.setPivot(0.0f, 6.0f, 0.0f);

    this.mono3 = new ModelPart(model, 0, 0);
    this.mono3.addCuboid(0.0f, -6.0f, -5.0f, 1.0f, 2.0f, 1.0f, 0.0f);
    this.mono3.setPivot(0.0f, 6.0f, 0.0f);

    this.mono4 = new ModelPart(model, 0, 0);
    this.mono4.addCuboid(3.0f, -6.0f, -5.0f, 1.0f, 2.0f, 1.0f, 0.0f);
    this.mono4.setPivot(0.0f, 6.0f, 0.0f);

    this.mono5 = new ModelPart(model, 0, 0);
    this.mono5.addCuboid(4.0f, -6.0f, -4.0f, 1.0f, 1.0f, 1.0f, 0.0f);
    this.mono5.setPivot(0.0f, 6.0f, 0.0f);

    this.mono6 = new ModelPart(model, 0, 0);
    this.mono6.addCuboid(4.0f, -5.0f, -3.0f, 1.0f, 2.0f, 1.0f, 0.0f);
    this.mono6.setPivot(0.0f, 6.0f, 0.0f);

    this.mono7 = new ModelPart(model, 0, 0);
    this.mono7.addCuboid(4.0f, -3.0f, -2.0f, 1.0f, 2.0f, 1.0f, 0.0f);
    this.mono7.setPivot(0.0f, 6.0f, 0.0f);

    this.mono8 = new ModelPart(model, 0, 0);
    this.mono8.addCuboid(4.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 0.0f);
    this.mono8.setPivot(0.0f, 6.0f, 0.0f);

    this.monoGlass = new ModelPart(model, -1, 19);
    this.monoGlass.addCuboid(1.0f, -6.0f, -5.0f, 2.0f, 2.0f, 1.0f, 0.0f);
    this.monoGlass.setPivot(0.0f, 6.0f, 0.0f);

    this.modelParts = Arrays.asList(
        this.hat, this.hat2, this.brim1, this.brim2, this.brim3,
        this.brim4, this.mono1, this.mono2, this.mono3, this.mono4,
        this.mono5, this.mono6, this.mono7, this.mono8, this.monoGlass
    );
  }

  @Override
  public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
      int light, CreeperEntity entity, float limbAngle,
      float limbDistance, float tickDelta, float customAngle,
      float headYaw, float headPitch) {
    VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(TOP_HAT));

    for (ModelPart modelPart : modelParts) {
      this.setAngles(modelPart, headYaw, headPitch);
      modelPart.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV);
    }
  }

  private void setAngles(ModelPart modelPart, float headYaw, float headPitch) {
    modelPart.yaw = headYaw / 57.295776f;
    modelPart.pitch = headPitch / 57.295776f;
  }
}