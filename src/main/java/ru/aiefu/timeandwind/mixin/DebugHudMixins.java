package ru.aiefu.timeandwind.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import ru.aiefu.timeandwind.IDimType;

@Mixin(DebugHud.class)
public class DebugHudMixins {
    @Shadow @Final private MinecraftClient client;

    @ModifyConstant(method = "getLeftText", constant =  @Constant(longValue = 24000L), require = 0)
    private long patchDayCounterTAW (long l){
        return ((IDimType) this.client.world.getDimension()).getCycleDuration();
    }

}
