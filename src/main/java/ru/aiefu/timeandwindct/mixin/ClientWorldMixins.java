package ru.aiefu.timeandwindct.mixin;

import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.storage.ISpawnWorldInfo;
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

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixins extends World implements ITimeOperations {


    protected ClientWorldMixins(ISpawnWorldInfo p_i241925_1_, RegistryKey<World> p_i241925_2_, DimensionType p_i241925_3_, Supplier<IProfiler> p_i241925_4_, boolean p_i241925_5_, boolean p_i241925_6_, long p_i241925_7_) {
        super(p_i241925_1_, p_i241925_2_, p_i241925_3_, p_i241925_4_, p_i241925_5_, p_i241925_6_, p_i241925_7_);
    }

    @Shadow
    public abstract void setDayTime(long p_72877_1_);

    protected Ticker timeTicker;

    private boolean skipState = false;
    private int speed = 0;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void attachTimeDataTAW(ClientPlayNetHandler p_i242067_1_, ClientWorld.ClientWorldInfo p_i242067_2_, RegistryKey<World> p_i242067_3_, DimensionType p_i242067_4_, int p_i242067_5_, Supplier<IProfiler> p_i242067_6_, WorldRenderer p_i242067_7_, boolean p_i242067_8_, long p_i242067_9_, CallbackInfo ci){
        String worldId = this.dimension().location().toString();
        if(TimeAndWindCT.CONFIG.syncWithSystemTime){
            this.timeTicker = new SystemTimeTicker(this);
        }
        else if (TimeAndWindCT.timeDataMap != null && TimeAndWindCT.timeDataMap.containsKey(worldId)) {
            TimeDataStorage storage = TimeAndWindCT.timeDataMap.get(worldId);
            this.timeTicker = new TimeTicker(storage.dayDuration, storage.nightDuration);
        } else this.timeTicker = new DefaultTicker();
    }

    @Redirect(method = "tickTime", at = @At(value = "INVOKE", target = "net/minecraft/client/world/ClientWorld.setDayTime(J)V"))
    private void customTickerTAW0(ClientWorld clientWorld, long p_72877_1_) {
        timeTicker.tick((ITimeOperations) clientWorld, skipState, speed);
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

    @Override
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
}
