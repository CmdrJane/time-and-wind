package ru.aiefu.timeandwindct.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class WorldRendererMixins {
    @Shadow private ClientLevel level;
    private float deltaTime = 0;
    private float prevSkyAngle;

    @Inject(method = "renderSky", at =@At("HEAD"))
    private void captureDelta(PoseStack poseStack, float f, CallbackInfo ci){
        this.deltaTime = f;
    }

    @Redirect(method = "renderSky", at =@At(value = "INVOKE", target = "com/mojang/blaze3d/vertex/PoseStack.mulPose(Lcom/mojang/math/Quaternion;)V", ordinal = 4))
    private void lerpSky(PoseStack matrixStack, Quaternion quaternion){
        float skyAngle = this.level.getTimeOfDay(deltaTime);
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(Mth.rotLerp(deltaTime, prevSkyAngle * 360.0F,skyAngle * 360.0F)));
        prevSkyAngle = skyAngle;
    }

}
