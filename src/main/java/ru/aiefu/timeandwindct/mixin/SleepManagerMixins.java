package ru.aiefu.timeandwindct.mixin;

import net.minecraft.server.players.SleepStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.aiefu.timeandwindct.TimeAndWindCT;

@Mixin(SleepStatus.class)
public class SleepManagerMixins {

    @Inject(method = "areEnoughSleeping", at =@At("HEAD"), cancellable = true)
    private void disableNightSkip(int percentage, CallbackInfoReturnable<Boolean> cir){
        if(TimeAndWindCT.CONFIG.syncWithSystemTime || TimeAndWindCT.CONFIG.enableNightSkipAcceleration){
            cir.setReturnValue(false);
        }
    }
}
