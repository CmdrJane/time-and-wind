package ru.aiefu.timeandwindct.mixin;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderGlobal.class)
public class RenderGlobalMixins {
    @Shadow private WorldClient world;

    private float deltaTime;
    private float prevSkyAngle;

    @Inject(method = "renderSky(FI)V", at =@At("HEAD"))
    private void captureDelta(float partialTicks, int pass, CallbackInfo ci){
        this.deltaTime = partialTicks;
    }

    @Redirect(method = "renderSky(FI)V", at =@At(value = "INVOKE", target = "net/minecraft/client/renderer/GlStateManager.rotate(FFFF)V", ordinal = 4))
    private void lerpSkyAngle(float angle, float x, float y, float z){
        float skyAngle = this.world.getCelestialAngle(deltaTime);
        GlStateManager.rotate(lerpAngleDegrees(deltaTime, prevSkyAngle * 360.0F, skyAngle * 360.0F), 1.0F, 0.0F, 0.0F);
        this.prevSkyAngle = skyAngle;
    }

    private float lerpAngleDegrees(float delta, float start, float end) {
        return start + delta * MathHelper.wrapDegrees(end - start);
    }
}
