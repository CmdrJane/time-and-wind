package ru.aiefu.timeandwind.mixin;

import net.coderbot.iris.mixin.DimensionTypeAccessor;
import net.coderbot.iris.uniforms.HardcodedCustomUniforms;
import net.minecraft.client.MinecraftClient;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.aiefu.timeandwind.IDimType;

@Pseudo
@Mixin(HardcodedCustomUniforms.class)
public class HardcodedCustomUniformsMixins {
    @Inject(method = "getTimeAngle", at = @At("HEAD"), cancellable = true, remap = false)
    private static void calcTAWAngle(CallbackInfoReturnable<Float> cir){

        World world = MinecraftClient.getInstance().world;
        long timeOfDay = world.getTimeOfDay();
        long dayTime = ((DimensionTypeAccessor)world.getDimension()).getFixedTime().orElse(timeOfDay);
        IDimType iDimType = ((IDimType) world.getDimension());
        long dayD = iDimType.getDayDuration();
        long nightD = iDimType.getNightDuration();
        double mod = dayTime % (dayD + nightD);
        double d;
        double f;
        if(mod > dayD){
            mod -= dayD;
            f = 0.5D / nightD;
            d = f * mod + 0.5D;
        }
        else {
            f = 0.5D / dayD;
            d = f * mod;
        }
        cir.setReturnValue((float) d);
        cir.cancel();
    }

}
