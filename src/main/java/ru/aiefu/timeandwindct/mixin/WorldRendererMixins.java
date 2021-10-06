package ru.aiefu.timeandwindct.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixins {

    @Shadow private ClientWorld level;
    private float deltaTime;
    private float prevAngle;

    @Inject(method = "renderSky", at =@At("HEAD"))
    private void captureDeltaTime(MatrixStack p_228424_1_, float p_228424_2_, CallbackInfo ci){
        this.deltaTime = p_228424_2_;
    }

    @Redirect(method = "renderSky", at =@At(value = "INVOKE", target = "com/mojang/blaze3d/matrix/MatrixStack.mulPose(Lnet/minecraft/util/math/vector/Quaternion;)V", ordinal = 4))
    private void lerpSkyAngle(MatrixStack matrixStack, Quaternion p_227863_1_){
        float skyAngle = this.level.getTimeOfDay(deltaTime);
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(MathHelper.rotLerp(deltaTime, prevAngle * 360.0F, skyAngle * 360.0F)));
        this.prevAngle = skyAngle;
    }
}
