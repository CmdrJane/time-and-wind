package ru.aiefu.timeandwindct.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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


    protected ClientWorldMixins(WritableLevelData writableLevelData, ResourceKey<Level> resourceKey, Holder<DimensionType> holder, Supplier<ProfilerFiller> supplier, boolean bl, boolean bl2, long l, int i) {
        super(writableLevelData, resourceKey, holder, supplier, bl, bl2, l, i);
    }

    @Shadow public abstract void setDayTime(long l);



    protected Ticker timeTicker;

    private boolean skipState = false;
    private int speed = 0;
    private float prevSkyAngle = 0;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void attachTimeDataTAW(ClientPacketListener clientPacketListener, ClientLevel.ClientLevelData clientLevelData, ResourceKey resourceKey, Holder holder, int i, int j, Supplier supplier, LevelRenderer levelRenderer, boolean bl, long l, CallbackInfo ci){
        String worldId = this.dimension().location().toString();
        if(this.dimensionType().hasFixedTime()){
            this.timeTicker = new DefaultTicker();
        }
        else if(TimeAndWindCT.CONFIG.syncWithSystemTime){
            if(TimeAndWindCT.CONFIG.systemTimePerDimensions && TimeAndWindCT.sysTimeMap.containsKey(worldId)) this.timeTicker = new SystemTimeTicker(this, TimeAndWindCT.sysTimeMap.get(worldId));
            else this.timeTicker = new SystemTimeTicker(this, TimeAndWindCT.systemTimeConfig);
        }
        else if (TimeAndWindCT.timeDataMap != null && TimeAndWindCT.timeDataMap.containsKey(worldId)) {
            TimeDataStorage storage = TimeAndWindCT.timeDataMap.get(worldId);
            this.timeTicker = new TimeTicker(storage.dayDuration, storage.nightDuration, this);
        } else this.timeTicker = new DefaultTicker();
    }

    @Redirect(method = "tickTime", at = @At(value = "INVOKE", target = "net/minecraft/client/multiplayer/ClientLevel.setDayTime(J)V"))
    private void customTickerTAW(ClientLevel clientWorld, long timeOfDay) {
        this.prevSkyAngle = getTimeOfDay(1.0F);
        timeTicker.tick(this, skipState, speed);
    }

    @Override
    public Ticker getTimeTicker() {
        return this.timeTicker;
    }

    @Override
    public void setTimeTicker(Ticker timeTicker) {
        this.timeTicker = timeTicker;
    }

    @Override
    public void setTimeOfDayTAW(long time) {
        this.setDayTime(time);
    }

    @Override
    public long getTimeTAW() {
        return this.levelData.getGameTime();
    }

    @Override
    public long getTimeOfDayTAW() {
        return this.levelData.getDayTime();
    }

    public boolean isClient() {
        return this.isClientSide();
    }

    @Override
    public void setSkipState(boolean bl) {
        this.skipState = bl;
    }

    @Override
    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public float getPrevSkyAngle(){
        return prevSkyAngle;
    }

    @Override
    public void wakeUpAllPlayersTAW() {

    }
}
