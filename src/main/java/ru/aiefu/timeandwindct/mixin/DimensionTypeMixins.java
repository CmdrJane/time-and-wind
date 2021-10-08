package ru.aiefu.timeandwindct.mixin;

import net.minecraft.world.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.aiefu.timeandwindct.TimeAndWindCT;

@Mixin(DimensionType.class)
public class DimensionTypeMixins {

    @Inject(method = "timeOfDay", at =@At("HEAD"), cancellable = true)
    private void patchSkyAngleTAW(long p_236032_1_, CallbackInfoReturnable<Float> cir){
        if(TimeAndWindCT.CONFIG.patchSkyAngle) {
            double factor = 1.0D / 24000D;
            double d = p_236032_1_ % 24000L * factor - 0.25D;
            if (d < 0)
                ++d;
            cir.setReturnValue((float) d);
        }
    }
}
