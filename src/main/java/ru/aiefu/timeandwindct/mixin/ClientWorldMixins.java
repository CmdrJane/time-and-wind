package ru.aiefu.timeandwindct.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.aiefu.timeandwindct.ITimeOperations;
import ru.aiefu.timeandwindct.TimeAndWindCT;
import ru.aiefu.timeandwindct.config.TimeDataStorage;
import ru.aiefu.timeandwindct.tickers.DefaultTicker;
import ru.aiefu.timeandwindct.tickers.SystemTimeTicker;
import ru.aiefu.timeandwindct.tickers.Ticker;
import ru.aiefu.timeandwindct.tickers.TimeTicker;

import java.util.function.Supplier;

@Mixin(ClientLevel.class)
public abstract class ClientWorldMixins extends Level implements ITimeOperations {


    protected ClientWorldMixins(WritableLevelData writableLevelData, ResourceKey<Level> resourceKey, RegistryAccess registryAccess, Holder<DimensionType> holder, Supplier<ProfilerFiller> supplier, boolean bl, boolean bl2, long l, int i) {
        super(writableLevelData, resourceKey, registryAccess, holder, supplier, bl, bl2, l, i);
    }

    @Shadow public abstract void setDayTime(long l);

    @Unique
    protected Ticker timeTicker;

    @Unique
    private boolean skipState = false;
    @Unique
    private int speed = 0;
    @Unique

    @Inject(method = "<init>", at = @At("TAIL"))
    private void attachTimeDataTAW(ClientPacketListener clientPacketListener, ClientLevel.ClientLevelData clientLevelData, ResourceKey<Level> resourceKey, Holder<DimensionType> holder, int i, int j, Supplier<ProfilerFiller> supplier, LevelRenderer levelRenderer, boolean bl, long l, CallbackInfo ci){
        String worldId = resourceKey.location().toString();
        if(holder.value().hasFixedTime()){
            this.timeTicker = new DefaultTicker();
        }
        else if(TimeAndWindCT.CONFIG.syncWithSystemTime){
            if(TimeAndWindCT.CONFIG.systemTimePerDimensions && TimeAndWindCT.sysTimeMap != null && TimeAndWindCT.sysTimeMap.containsKey(worldId)) this.timeTicker = new SystemTimeTicker(this, TimeAndWindCT.sysTimeMap.get(worldId));
            else this.timeTicker = new SystemTimeTicker(this, TimeAndWindCT.systemTimeConfig);
        }
        else if (TimeAndWindCT.timeDataMap != null && TimeAndWindCT.timeDataMap.containsKey(worldId)) {
            TimeDataStorage storage = TimeAndWindCT.timeDataMap.get(worldId);
            this.timeTicker = new TimeTicker(storage.dayDuration, storage.nightDuration, this);
        } else this.timeTicker = new DefaultTicker();
    }

    @Redirect(method = "tickTime", at = @At(value = "INVOKE", target = "net/minecraft/client/multiplayer/ClientLevel.setDayTime(J)V"))
    private void customTickerTAW(ClientLevel clientWorld, long timeOfDay) {
        this.timeTicker.tick(this, skipState, speed);
    }

    @Override
    public Ticker time_and_wind_custom_ticker$getTimeTicker() {
        return this.timeTicker;
    }

    @Override
    public void time_and_wind_custom_ticker$setTimeTicker(Ticker timeTicker) {
        this.timeTicker = timeTicker;
    }

    @Override
    public void time_and_wind_custom_ticker$setTimeOfDayTAW(long time) {
        this.setDayTime(time);
    }

    @Override
    public long time_and_wind_custom_ticker$getTimeTAW() {
        return this.levelData.getGameTime();
    }

    @Override
    public long time_and_wind_custom_ticker$getTimeOfDayTAW() {
        return this.levelData.getDayTime();
    }

    public boolean time_and_wind_custom_ticker$isClient() {
        return this.isClientSide();
    }

    @Override
    public void time_and_wind_custom_ticker$setSkipState(boolean bl) {
        this.skipState = bl;
    }

    @Override
    public void time_and_wind_custom_ticker$setSpeed(int speed) {
        this.speed = speed;
    }

    @Override
    public void time_and_wind_custom_ticker$wakeUpAllPlayersTAW() {
        skipState = false;
    }
}
