package ru.aiefu.timeandwindct.mixin;

import com.mojang.datafixers.DataFixer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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
    @Shadow
    private boolean paused;
    @Unique
    private boolean shouldUpdate = false;

    public IntegratedServerMixins(Thread thread, LevelStorageSource.LevelStorageAccess levelStorageAccess, PackRepository packRepository, WorldStem worldStem, Proxy proxy, DataFixer dataFixer, Services services, ChunkProgressListenerFactory chunkProgressListenerFactory) {
        super(thread, levelStorageAccess, packRepository, worldStem, proxy, dataFixer, services, chunkProgressListenerFactory);
    }


    @Inject(method = "tickServer", at =@At("TAIL"))
    private void recalculateTime(BooleanSupplier p_71217_1_, CallbackInfo ci){
        if(TimeAndWindCT.CONFIG.syncWithSystemTime) {
            if (this.paused) {
                this.shouldUpdate = true;
            } else if (shouldUpdate) {
                shouldUpdate = false;
                this.getAllLevels().forEach(serverWorld -> {
                    Ticker t = ((ITimeOperations) serverWorld).time_and_wind_getTimeTicker();
                    if (t instanceof SystemTimeTicker) {
                        ((SystemTimeTicker) t).updateTime((ITimeOperations) serverWorld);
                    }
                });
                TimeAndWindCT.LOGGER.info("Game unpaused, time corrected");
            }
        }
    }
}
