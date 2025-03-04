package aiefu.timeandwind.mixin;

import aiefu.timeandwind.tickers.DefaultTicker;
import aiefu.timeandwind.tickers.Ticker;
import net.minecraft.client.multiplayer.ClientLevel;
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
import org.spongepowered.asm.mixin.injection.Redirect;
import aiefu.timeandwind.ITimeOperations;

import java.util.function.Supplier;

@Mixin(ClientLevel.class)
public abstract class ClientWorldMixins extends Level implements ITimeOperations {
    @Unique
    protected Ticker timeTicker = new DefaultTicker();

    protected ClientWorldMixins(WritableLevelData writableLevelData, ResourceKey<Level> resourceKey, RegistryAccess registryAccess, Holder<DimensionType> holder, Supplier<ProfilerFiller> supplier, boolean bl, boolean bl2, long l, int i) {
        super(writableLevelData, resourceKey, registryAccess, holder, supplier, bl, bl2, l, i);
    }

    @Shadow public abstract void setDayTime(long l);

    @Redirect(method = "tickTime", at = @At(value = "INVOKE", target = "net/minecraft/client/multiplayer/ClientLevel.setDayTime(J)V"))
    private void customTickerTAW(ClientLevel clientWorld, long timeOfDay) {
        this.timeTicker.tick(this);
    }

    @Override
    public Ticker taw$getTimeTicker() {
        return this.timeTicker;
    }

    @Override
    public void taw$setTimeTicker(Ticker timeTicker) {
        this.timeTicker = timeTicker;
    }

    @Override
    public void taw$setTimeOfDayTAW(long time) {
        this.setDayTime(time);
    }

    @Override
    public long taw$getTimeTAW() {
        return this.levelData.getGameTime();
    }

    @Override
    public long taw$getTimeOfDayTAW() {
        return this.levelData.getDayTime();
    }

    @Override
    public boolean taw$isClient() {
        return this.isClientSide();
    }
}
