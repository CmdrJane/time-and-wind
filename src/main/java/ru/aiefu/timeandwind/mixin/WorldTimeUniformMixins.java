package ru.aiefu.timeandwind.mixin;

import net.coderbot.iris.uniforms.WorldTimeUniforms;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.aiefu.timeandwind.IDimType;

import java.util.Objects;

@Pseudo
@Mixin(WorldTimeUniforms.class)
public class WorldTimeUniformMixins {
    @Inject(method = "getWorldDayTime", at = @At("HEAD"), cancellable = true, remap = false)
    private static void timeHackTAW(CallbackInfoReturnable<Integer> cir){
        ClientWorld world = getWorld();
        if(world.getDimension().hasFixedTime()){
            return;
        }
        IDimType dimType = ((IDimType) world.getDimension());
        cir.setReturnValue(dimType.calculateIrisWorldDayTime(world.getTimeOfDay()));
        cir.cancel();

    }

    @ModifyConstant(method = "getWorldDay()I", constant = @Constant(longValue = 24000L), remap = false)
    private static long patchTAWWD(long l){
        return ((IDimType) MinecraftClient.getInstance().world.getDimension()).getCycleDuration();
    }

    @Shadow
    private static ClientWorld getWorld() {
        return Objects.requireNonNull(MinecraftClient.getInstance().world);
    }
}
