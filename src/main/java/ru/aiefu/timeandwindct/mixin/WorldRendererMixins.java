package ru.aiefu.timeandwindct.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.aiefu.timeandwindct.ITimeOperations;

@Mixin(LevelRenderer.class)
public class WorldRendererMixins {
    @Shadow private ClientLevel level;
    private ITimeOperations timeOps;
    private float deltaTime = 0;

    /*
    @Inject(method = "renderSky", at =@At("HEAD"))
    private void captureDelta(PoseStack poseStack, Matrix4f matrix4f, float f, Camera camera, boolean bl, Runnable runnable, CallbackInfo ci){
        this.deltaTime = f;
    }

     */

    @Inject(method = "setLevel", at =@At("HEAD"))
    private void setLevelCastI(ClientLevel clientLevel, CallbackInfo ci){
        if(clientLevel != null){
            timeOps = (ITimeOperations) clientLevel;
        } else timeOps = null;
    }

    /*
    @Redirect(method = "renderSky", at =@At(value = "INVOKE", target = "com/mojang/blaze3d/vertex/PoseStack.mulPose(Lcom/mojang/math/Quaternion;)V", ordinal = 4))
    private void lerpSky(PoseStack matrixStack, Quaternion quaternion){
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(Mth.rotLerp(deltaTime, timeOps.getPrevSkyAngle() * 360.0F,this.level.getTimeOfDay(deltaTime) * 360.0F)));
    }
     */

}
