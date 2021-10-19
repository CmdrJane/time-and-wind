package ru.aiefu.timeandwindct.mixin;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import net.minecraft.resources.DataPackRegistries;
import net.minecraft.resources.ResourcePackList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.chunk.listener.IChunkStatusListenerFactory;
import net.minecraft.world.storage.IServerConfiguration;
import net.minecraft.world.storage.SaveFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.aiefu.timeandwindct.ITimeOperations;
import ru.aiefu.timeandwindct.TimeAndWindCT;
import ru.aiefu.timeandwindct.tickers.SystemTimeTicker;
import ru.aiefu.timeandwindct.tickers.Ticker;

import java.net.Proxy;
import java.util.function.BooleanSupplier;

@Mixin(IntegratedServer.class)
public abstract class IntegratedServerMixins extends MinecraftServer {

    @Shadow private boolean paused;
    private boolean shouldUpdate = false;

    public IntegratedServerMixins(Thread p_i232576_1_, DynamicRegistries.Impl p_i232576_2_, SaveFormat.LevelSave p_i232576_3_, IServerConfiguration p_i232576_4_, ResourcePackList p_i232576_5_, Proxy p_i232576_6_, DataFixer p_i232576_7_, DataPackRegistries p_i232576_8_, MinecraftSessionService p_i232576_9_, GameProfileRepository p_i232576_10_, PlayerProfileCache p_i232576_11_, IChunkStatusListenerFactory p_i232576_12_) {
        super(p_i232576_1_, p_i232576_2_, p_i232576_3_, p_i232576_4_, p_i232576_5_, p_i232576_6_, p_i232576_7_, p_i232576_8_, p_i232576_9_, p_i232576_10_, p_i232576_11_, p_i232576_12_);
    }

    @Inject(method = "tickServer", at =@At("TAIL"))
    private void recalculateTime(BooleanSupplier p_71217_1_, CallbackInfo ci){
        if(TimeAndWindCT.CONFIG.syncWithSystemTime) {
            if (this.paused) {
                this.shouldUpdate = true;
            } else if (shouldUpdate) {
                shouldUpdate = false;
                this.getAllLevels().forEach(serverWorld -> {
                    Ticker t = ((ITimeOperations) serverWorld).getTimeTicker();
                    if (t instanceof SystemTimeTicker) {
                        ((SystemTimeTicker) t).updateTime((ITimeOperations) serverWorld);
                    }
                });
                TimeAndWindCT.LOGGER.info("Game unpaused, time corrected");
            }
        }
    }
}
