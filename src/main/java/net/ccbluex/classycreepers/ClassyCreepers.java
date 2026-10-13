package net.ccbluex.classycreepers;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.ccbluex.classycreepers.feature.CreeperClassinessFeatureRenderer;
import net.minecraft.client.renderer.entity.CreeperRenderer;
import net.minecraft.world.entity.EntityTypes;

public class ClassyCreepers implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModelLayerRegistry.registerModelLayer(
				CreeperClassinessFeatureRenderer.MODEL_LAYER,
				CreeperClassinessFeatureRenderer::createLayerDefinition
		);

		LivingEntityRenderLayerRegistrationCallback.EVENT.register((entityType, renderer, helper, context) -> {
			if (entityType == EntityTypes.CREEPER && renderer instanceof CreeperRenderer creeperRenderer) {
				helper.register(new CreeperClassinessFeatureRenderer(creeperRenderer, context));
			}
		});
	}
}
