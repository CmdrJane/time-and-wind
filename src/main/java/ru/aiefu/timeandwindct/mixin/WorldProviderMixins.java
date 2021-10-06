package ru.aiefu.timeandwindct.mixin;

import net.minecraft.world.WorldProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.aiefu.timeandwindct.TimeAndWindCT;

@Mixin(WorldProvider.class)
public class WorldProviderMixins {

    @Inject(method = "calculateCelestialAngle", at =@At("HEAD"), cancellable = true)
    private void patchSkyAngle(long worldTime, float partialTicks, CallbackInfoReturnable<Float> cir){
        if(TimeAndWindCT.CONFIG.patchSkyAngle) {
            double factor = 1.0D / 24000D;
            double d = worldTime % 24000L * factor - 0.25D;
            if (d < 0)
                ++d;
            cir.setReturnValue((float) d);
        }
    }
}
