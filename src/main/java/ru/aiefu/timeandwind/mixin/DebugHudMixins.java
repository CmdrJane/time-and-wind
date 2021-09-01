package ru.aiefu.timeandwind.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.overlay.DebugOverlayGui;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import ru.aiefu.timeandwind.IDimType;

@Mixin(DebugOverlayGui.class)
public class DebugHudMixins {
    @Shadow @Final private Minecraft minecraft;

    @ModifyConstant(method = "getGameInformation", constant =  @Constant(longValue = 24000L), require = 0)
    private long patchDayCounterTAW (long l){
        return ((IDimType) this.minecraft.level.dimensionType()).getCycleDuration();
    }

}
