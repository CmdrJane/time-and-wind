package ru.aiefu.timeandwind.mixin;


import net.johnvictorfs.simple_utilities.hud.GameInfoHud;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.aiefu.timeandwind.TimeAndWind;

@Pseudo
@Mixin(GameInfoHud.class)
public class SimpleUtilsMixins {
    @Inject(method = "parseTime", at =@At("HEAD"), cancellable = true, remap = false)
    private static void patchTimeParserTAW(long time, CallbackInfoReturnable<String> cir){
        cir.setReturnValue(TimeAndWind.get24TimeFormat(MinecraftClient.getInstance().world));
        return;
    }
}
