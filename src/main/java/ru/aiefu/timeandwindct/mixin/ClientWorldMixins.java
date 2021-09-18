package ru.aiefu.timeandwindct.mixin;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
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

import java.util.function.Supplier;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixins extends World implements ITimeOperations {

    protected ClientWorldMixins(MutableWorldProperties properties, RegistryKey<World> registryRef, DimensionType dimensionType, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed) {
        super(properties, registryRef, dimensionType, profiler, isClient, debugWorld, seed);
    }

    @Shadow
    public abstract void setTimeOfDay(long timeOfDay);

    protected TimeTicker timeTicker = new TimeTicker();

    @Inject(method = "<init>", at = @At("TAIL"))
    private void attachTimeDataTAW(ClientPlayNetworkHandler networkHandler, ClientWorld.Properties properties, RegistryKey<World> registryRef, DimensionType dimensionType, int loadDistance, Supplier<Profiler> profiler, WorldRenderer worldRenderer, boolean debugWorld, long seed, CallbackInfo ci){
        String worldId = this.getRegistryKey().getValue().toString();
        if (TimeAndWindCT.timeDataMap.containsKey(worldId)) {
            TimeDataStorage storage = TimeAndWindCT.timeDataMap.get(worldId);
            this.timeTicker.setupCustomTime(storage.dayDuration, storage.nightDuration);
        }
    }

    @Redirect(method = "tickTime", at = @At(value = "INVOKE", target = "net/minecraft/client/world/ClientWorld.setTimeOfDay(J)V"))
    private void customTickerTAW(ClientWorld clientWorld, long timeOfDay) {
        timeTicker.tickTime((ITimeOperations) clientWorld, timeOfDay);
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
        this.setTimeOfDay(time);
    }

    @Override
    public long getTimeTAW() {
        return this.properties.getTime();
    }

    @Override
    public long getTimeOfDayTAW() {
        return this.properties.getTimeOfDay();
    }
}
