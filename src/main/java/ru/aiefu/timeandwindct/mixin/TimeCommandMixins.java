package ru.aiefu.timeandwindct.mixin;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.TimeCommand;
import net.minecraft.text.LiteralText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.aiefu.timeandwindct.TimeAndWindCT;

@Mixin(TimeCommand.class)
public class TimeCommandMixins {
    @Inject(method = "executeSet", at =@At("HEAD"), cancellable = true)
    private static void disableTimeSetTAW(ServerCommandSource source, int time, CallbackInfoReturnable<Integer> cir){
        if(TimeAndWindCT.CONFIG.syncWithSystemTime){
            source.sendFeedback(new LiteralText("Time set command is disabled while synchronization with system time is enabled"), false);
            cir.setReturnValue(0);
        }
    }

    @Inject(method = "executeAdd", at =@At("HEAD"), cancellable = true)
    private static void disableTimeAddTAW(ServerCommandSource source, int time, CallbackInfoReturnable<Integer> cir){
        if(TimeAndWindCT.CONFIG.syncWithSystemTime){
            source.sendFeedback(new LiteralText("Time add command is disabled while synchronization with system time is enabled"), false);
            cir.setReturnValue(0);
        }
    }
}
