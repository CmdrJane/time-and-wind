package ru.aiefu.timeandwindct.mixin;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.aiefu.timeandwindct.ITimeOperations;
import ru.aiefu.timeandwindct.TimeAndWindCT;
import ru.aiefu.timeandwindct.TimeDataStorage;
import ru.aiefu.timeandwindct.TimeTicker;

@Mixin(WorldClient.class)
public abstract class ClientWorldMixins extends World implements ITimeOperations {

    protected ClientWorldMixins(ISaveHandler saveHandlerIn, WorldInfo info, WorldProvider providerIn, Profiler profilerIn, boolean client) {
        super(saveHandlerIn, info, providerIn, profilerIn, client);
    }

    @Shadow
    public abstract void setWorldTime(long time);

    protected TimeTicker timeTicker = new TimeTicker();

    @Inject(method = "<init>", at = @At("TAIL"))
    private void attachTimeDataTAW(NetHandlerPlayClient netHandler, WorldSettings settings, int dimension, EnumDifficulty difficulty, Profiler profilerIn, CallbackInfo ci){
        String worldId = this.provider.getDimensionType().getName();
        if (TimeAndWindCT.timeDataMap != null && TimeAndWindCT.timeDataMap.containsKey(worldId)) {
            TimeDataStorage storage = TimeAndWindCT.timeDataMap.get(worldId);
            this.timeTicker.setupCustomTime(storage.dayDuration, storage.nightDuration);
        }
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "net/minecraft/client/multiplayer/WorldClient.setWorldTime(J)V"))
    private void customTickerTAW(WorldClient worldClient, long time) {
        this.timeTicker.tickTime((ITimeOperations) worldClient, time);
    }

    @Override
    public TimeTicker getTimeTicker() {
        return this.timeTicker;
    }

    @Override
    public void setTimeTicker(TimeTicker timeTicker) {
        this.timeTicker = timeTicker;
    }

    @Override
    public void setTimeOfDayTAW(long time) {
        this.setWorldTime(time);
    }

    @Override
    public long getTimeTAW() {
        return this.getTotalWorldTime();
    }

    @Override
    public long getTimeOfDayTAW() {
        return this.getWorldTime();
    }
}
