package net.ccbluex.classycreepers.injection.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelCreeper;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SideOnly(Side.CLIENT)
@Mixin(ModelCreeper.class)
public class MixinModelCreeper {
    private static final float DEGREES_PER_RADIAN = 180.0F / (float) Math.PI;
    private static final float ROTATION_POINT_Y = 6.0F;

    private final ResourceLocation topHatTexture = new ResourceLocation("tophat.png");
    private ModelRenderer[] classyParts;

    @Inject(method = "<init>(F)V", at = @At("RETURN"))
    private void init(float modelSize, CallbackInfo callbackInfo) {
        classyParts = new ModelRenderer[] {
            part(9, 0, 4, -8, -4, 1, 1, 8),
            part(9, 0, -5, -8, -4, 1, 1, 8),
            part(9, 0, -5, -8, 4, 10, 1, 1),
            part(9, 0, -5, -8, -5, 10, 1, 1),
            part(10, 0, -4, -16, -4, 8, 6, 8),
            part(0, 22, -4, -10, -4, 8, 2, 8),
            part(0, 0, 1, -7, -5, 2, 1, 1),
            part(0, 0, 1, -4, -5, 2, 1, 1),
            part(0, 0, 0, -6, -5, 1, 2, 1),
            part(0, 0, 3, -6, -5, 1, 2, 1),
            part(0, 0, 4, -6, -4, 1, 1, 1),
            part(0, 0, 4, -5, -3, 1, 2, 1),
            part(0, 0, 4, -3, -2, 1, 2, 1),
            part(0, 0, 4, -1, -1, 1, 1, 1),
            part(-1, 19, 1, -6, -5, 2, 2, 1)
        };
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void render(
        Entity entity,
        float limbSwing,
        float limbSwingAmount,
        float ageInTicks,
        float netHeadYaw,
        float headPitch,
        float scale,
        CallbackInfo callbackInfo
    ) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(topHatTexture);
        for (ModelRenderer part : classyParts) {
            part.render(scale);
        }
    }

    @Inject(method = "setRotationAngles", at = @At("RETURN"))
    private void setRotationAngles(
        float limbSwing,
        float limbSwingAmount,
        float ageInTicks,
        float netHeadYaw,
        float headPitch,
        float scale,
        Entity entity,
        CallbackInfo callbackInfo
    ) {
        for (ModelRenderer part : classyParts) {
            part.rotateAngleY = netHeadYaw / DEGREES_PER_RADIAN;
            part.rotateAngleX = headPitch / DEGREES_PER_RADIAN;
        }
    }

    private ModelRenderer part(int textureU, int textureV, float x, float y, float z, int width, int height, int depth) {
        ModelRenderer part = new ModelRenderer((ModelBase) (Object) this, textureU, textureV);
        part.addBox(x, y, z, width, height, depth, 0.0F);
        part.setRotationPoint(0.0F, ROTATION_POINT_Y, 0.0F);
        return part;
    }
}
