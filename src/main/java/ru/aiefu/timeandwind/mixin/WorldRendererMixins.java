package ru.aiefu.timeandwind.mixin;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.aiefu.timeandwind.IDimType;
@Mixin(WorldRenderer.class)
public class WorldRendererMixins {
    //TODO: Possibly delete this
    @Shadow
    private ClientWorld world;
    private float deltaTime = 0;

    @Inject(method = "renderSky", at =@At("HEAD"))
    private void captureDelta(PoseStack matrices, float tickDelta, CallbackInfo ci){
        this.deltaTime = tickDelta;
    }

    @Redirect(method = "renderSky", at =@At(value = "INVOKE", target = "net/minecraft/client/util/math/MatrixStack.multiply(Lnet/minecraft/util/math/Quaternion;)V", ordinal = 4))
    private void lerpSky(PoseStack matrixStack, Quaternion quaternion){
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(Mth.rotLerp(deltaTime, ((IDimType)this.world.dimensionType()).getPrevAngle() * 360.0F ,this.world.getTimeOfDay(deltaTime) * 360.0F)));
    }

}
