package aiefu.timeandwind.mixin;

import aiefu.timeandwind.TimeAndWind;
import aiefu.timeandwind.tickers.SkipTicker;
import aiefu.timeandwind.tickers.Ticker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import aiefu.timeandwind.ITimeOperations;

@Mixin(Minecraft.class)
public class MinecraftMixins {

    @Inject(method = "setLevel", at = @At("TAIL"))
    private void attachTimeDataClientSide(ClientLevel clientLevel, ReceivingLevelScreen.Reason reason, CallbackInfo ci){
        ITimeOperations ito = (ITimeOperations) clientLevel;
        if(TimeAndWind.config.enableGlobalSystemTime){
            ito.taw$setTimeTicker(new SkipTicker());
        } else{
            if(!clientLevel.dimensionType().hasFixedTime()){
                Ticker ticker = TimeAndWind.tickers.get(clientLevel.dimension().location().toString());
                if(ticker != null){
                    ito.taw$setTimeTicker(ticker);
                }
            }
        }
    }
}
