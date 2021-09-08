package ru.aiefu.timeandwind.mixin;

import net.coderbot.iris.uniforms.HardcodedCustomUniforms;
import net.minecraft.client.MinecraftClient;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.aiefu.timeandwind.IDimType;

@Pseudo
@Mixin(HardcodedCustomUniforms.class)
public class HardcodedCustomUniformsMixins {
    @Inject(method = "getTimeAngle", at = @At("HEAD"), cancellable = true, remap = false)
    private static void calcTAWAngle(CallbackInfoReturnable<Float> cir){
        World world = MinecraftClient.getInstance().world;
        if(world.getDimension().hasFixedTime()){
            return;
        }
        IDimType iDimType = ((IDimType) world.getDimension());
        cir.setReturnValue(iDimType.untweakedAngle());
        cir.cancel();
    }
}
