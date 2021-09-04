package ru.aiefu.timeandwind.mixin;

import net.coderbot.iris.uniforms.WorldTimeUniforms;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import ru.aiefu.timeandwind.IDimType;

@Pseudo
@Mixin(WorldTimeUniforms.class)
public class WorldTimeUniformMixins {
    @ModifyConstant(method = "getWorldDayTime()I", constant = @Constant(longValue = 24000L), remap = false)
    private static long patchTAWWDT(long f){
        return ((IDimType) MinecraftClient.getInstance().world.getDimension()).getCycleDuration();
    }
    @ModifyConstant(method = "getWorldDay()I", constant = @Constant(longValue = 24000L), remap = false)
    private static long patchTAWWD(long l){
        return ((IDimType) MinecraftClient.getInstance().world.getDimension()).getCycleDuration();
    }
}
