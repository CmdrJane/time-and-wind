package ru.aiefu.timeandwindct.mixin;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.TimeCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.aiefu.timeandwindct.TimeAndWindCT;

@Mixin(TimeCommand.class)
public class TimeCommandMixins {
    @Inject(method = "setTime", at =@At("HEAD"), cancellable = true)
    private static void disableTimeSetTAW(CommandSourceStack source, int time, CallbackInfoReturnable<Integer> cir){
        if(TimeAndWindCT.CONFIG.syncWithSystemTime){
            source.sendSuccess(Component.literal("Time set command is disabled while synchronization with system time is enabled"), false);
            cir.setReturnValue(0);
        }
    }

    @Inject(method = "addTime", at =@At("HEAD"), cancellable = true)
    private static void disableTimeAddTAW(CommandSourceStack source, int time, CallbackInfoReturnable<Integer> cir){
        if(TimeAndWindCT.CONFIG.syncWithSystemTime){
            source.sendSuccess(Component.literal("Time add command is disabled while synchronization with system time is enabled"), false);
            cir.setReturnValue(0);
        }
    }
}
