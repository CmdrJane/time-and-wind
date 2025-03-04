package aiefu.timeandwind.mixin;

import aiefu.timeandwind.TimeAndWind;
import aiefu.timeandwind.tickers.DefaultTicker;
import aiefu.timeandwind.tickers.Ticker;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.SleepStatus;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import aiefu.timeandwind.ITimeOperations;

import java.util.List;
import java.util.function.Supplier;

@Mixin(ServerLevel.class)
public abstract class ServerWorldMixins extends Level implements ITimeOperations {

	protected ServerWorldMixins(WritableLevelData writableLevelData, ResourceKey<Level> resourceKey, RegistryAccess registryAccess, Holder<DimensionType> holder, Supplier<ProfilerFiller> supplier, boolean bl, boolean bl2, long l, int i) {
		super(writableLevelData, resourceKey, registryAccess, holder, supplier, bl, bl2, l, i);
	}

	@Shadow public abstract void setDayTime(long l);

	@Shadow protected abstract void wakeUpAllPlayers();

	@Shadow @Final private SleepStatus sleepStatus;
	@Shadow @Final
	List<ServerPlayer> players;

	@Shadow public abstract void resetWeatherCycle();

	@Shadow public abstract List<ServerPlayer> players();

	@Unique
	protected Ticker timeTicker = new DefaultTicker();

	@Redirect(method = "tick", at =@At(value = "INVOKE", target = "net/minecraft/server/players/SleepStatus.areEnoughDeepSleeping (ILjava/util/List;)Z", ordinal = 0),
			slice = @Slice(from = @At(value = "INVOKE", target = "net/minecraft/server/level/ServerLevel.advanceWeatherCycle ()V", ordinal = 0),
					to = @At(value = "INVOKE", target = "net/minecraft/server/level/ServerLevel.wakeUpAllPlayers ()V", ordinal = 0)))
	private boolean blockSleepCheckTAW(SleepStatus instance, int i, List<ServerPlayer> list){
		return (!TimeAndWind.config.enableNightSkipAcceleration || timeTicker.allowSleeping());
	}

	@Unique
	private void syncTime(){
		this.players().forEach(p -> p.connection.send(new ClientboundSetTimePacket(this.getGameTime(), this.getDayTime(), this.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT))));
	}

	@Redirect(method = "tickTime", at = @At(value = "INVOKE", target = "net/minecraft/server/level/ServerLevel.setDayTime(J)V"))
	private void customTickerTAW(ServerLevel world, long timeOfDay) {
		this.timeTicker.tick(this);
		int i = this.getGameRules().getInt(GameRules.RULE_PLAYERS_SLEEPING_PERCENTAGE);
		if (this.canAccelerate() && this.sleepStatus.areEnoughSleeping(i) && this.sleepStatus.areEnoughDeepSleeping(i, this.players)) {
			this.timeTicker.accelerate((ServerLevel) (Object) this, TimeAndWind.config.accelerationSpeed);
			this.syncTime();
			if(this.isDay()){
				this.wakeUpAllPlayers();
				this.resetWeatherCycle();
			}
		}
	}


	@Unique
	private boolean canAccelerate(){
		return TimeAndWind.config.enableNightSkipAcceleration && timeTicker.canAccelerate();
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
