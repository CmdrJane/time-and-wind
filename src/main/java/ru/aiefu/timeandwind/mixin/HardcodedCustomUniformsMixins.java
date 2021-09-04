package ru.aiefu.timeandwind.mixin;

import net.coderbot.iris.uniforms.HardcodedCustomUniforms;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import ru.aiefu.timeandwind.IDimType;

@Pseudo
@Mixin(HardcodedCustomUniforms.class)
public class HardcodedCustomUniformsMixins {
    @ModifyConstant(method = "getTimeAngle()F", constant = @Constant(floatValue = 24000.0F), remap = false)
    private static float patchTAWTA(float f){
        return (float) ((IDimType) MinecraftClient.getInstance().world.getDimension()).getCycleDuration();
    }
    @ModifyConstant(method = "getWorldDayTime()I", constant = @Constant(longValue = 24000L), remap = false)
    private static long patchTAWWDT(long l){
        return ((IDimType) MinecraftClient.getInstance().world.getDimension()).getCycleDuration();
    }
}
