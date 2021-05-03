package ru.aiefu.timeandwind.mixin;

import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Quaternion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.aiefu.timeandwind.IDimType;

@Mixin(WorldRenderer.class)
public class WorldRendererMixins {
    @Shadow
    private ClientWorld world;
    private float deltaTime = 0;

    @Inject(method = "renderSky", at =@At("HEAD"))
    private void captureDelta(MatrixStack matrices, float tickDelta, CallbackInfo ci){
        this.deltaTime = tickDelta;
    }

    @Redirect(method = "renderSky", at =@At(value = "INVOKE", target = "net/minecraft/client/util/math/MatrixStack.multiply(Lnet/minecraft/util/math/Quaternion;)V", ordinal = 4))
    private void lerpSky(MatrixStack matrixStack, Quaternion quaternion){
        matrixStack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(MathHelper.lerpAngleDegrees(deltaTime, ((IDimType)this.world.getDimension()).getPrevAngle() * 360.0F ,this.world.getSkyAngle(deltaTime) * 360.0F)));
    }

}
