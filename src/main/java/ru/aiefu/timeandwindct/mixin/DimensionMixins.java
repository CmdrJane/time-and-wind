package ru.aiefu.timeandwindct.mixin;

import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.aiefu.timeandwindct.TimeAndWindCT;

import java.util.OptionalLong;

@Mixin(DimensionType.class)
public class DimensionMixins {
    @Shadow @Final private OptionalLong fixedTime;

    private final double factor = 1.0D / 24000D;

    @Inject(method = "timeOfDay", at =@At("HEAD"), cancellable = true)
    private void patchSkyAngleTAW(long time, CallbackInfoReturnable<Float> cir){
        if(TimeAndWindCT.CONFIG.patchSkyAngle && fixedTime.isEmpty()) {
            double d = time % 24000L * factor - 0.25D;
            if (d < 0)
                ++d;
            cir.setReturnValue((float) d);
        }
    }
}
