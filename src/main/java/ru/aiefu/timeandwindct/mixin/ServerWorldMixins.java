package ru.aiefu.timeandwindct.mixin;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.profiler.IProfiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.listener.IChunkStatusListener;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.ISpecialSpawner;
import net.minecraft.world.storage.IServerWorldInfo;
import net.minecraft.world.storage.ISpawnWorldInfo;
import net.minecraft.world.storage.SaveFormat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.aiefu.timeandwindct.ITimeOperations;
import ru.aiefu.timeandwindct.SleepStatus;
import ru.aiefu.timeandwindct.TimeAndWindCT;
import ru.aiefu.timeandwindct.config.TimeDataStorage;
import ru.aiefu.timeandwindct.network.NetworkHandler;
import ru.aiefu.timeandwindct.network.messages.NightSkip;
import ru.aiefu.timeandwindct.tickers.DefaultTicker;
import ru.aiefu.timeandwindct.tickers.SystemTimeTicker;
import ru.aiefu.timeandwindct.tickers.Ticker;
import ru.aiefu.timeandwindct.tickers.TimeTicker;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixins extends World implements ITimeOperations {

	protected ServerWorldMixins(ISpawnWorldInfo p_i241925_1_, RegistryKey<World> p_i241925_2_, DimensionType p_i241925_3_, Supplier<IProfiler> p_i241925_4_, boolean p_i241925_5_, boolean p_i241925_6_, long p_i241925_7_) {
		super(p_i241925_1_, p_i241925_2_, p_i241925_3_, p_i241925_4_, p_i241925_5_, p_i241925_6_, p_i241925_7_);
	}

	@Shadow public abstract void setDayTime(long l);

	@Shadow protected abstract void wakeUpAllPlayers();

	@Shadow @Final
	private
	List<ServerPlayerEntity> players;

	@Shadow protected abstract void stopWeather();

	@Unique
	protected Ticker timeTicker;

	@Unique
	private boolean shouldUpdate = true;

	@Unique
	private boolean skipState = false;

	@Unique
	private SleepStatus sleepStatus = new SleepStatus();

	@Inject(method = "<init>", at = @At("TAIL"))
	private void attachTimeDataTAW(MinecraftServer p_i241885_1_, Executor p_i241885_2_, SaveFormat.LevelSave p_i241885_3_, IServerWorldInfo p_i241885_4_, RegistryKey<World> key, DimensionType type, IChunkStatusListener p_i241885_7_, ChunkGenerator p_i241885_8_, boolean p_i241885_9_, long p_i241885_10_, List<ISpecialSpawner> p_i241885_12_, boolean p_i241885_13_, CallbackInfo ci){
		String worldId = key.location().toString();
		if(type.hasFixedTime()){
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

	@Redirect(method = "tick", at = @At(value = "FIELD", target = "net/minecraft/world/server/ServerWorld.allPlayersSleeping : Z", ordinal = 0))
	private boolean blockSleepCheckTAW(ServerWorld instance){
		return !(TimeAndWindCT.CONFIG.syncWithSystemTime || TimeAndWindCT.CONFIG.enableNightSkipAcceleration);
	}

	@Inject(method = "updateSleepingPlayerList", at = @At("TAIL"))
	private void syncSkipStateTAW(CallbackInfo ci){
		if(!players.isEmpty()){
			this.sleepStatus.update(this.players);
		}
		if(this.canAccelerate()){
			if(this.sleepStatus.areEnoughSleeping(TimeAndWindCT.CONFIG.thresholdPercentage)){
				if(!skipState){
					skipState = true;
					if(shouldUpdate) this.broadcastSkipState(true);
				}
			} else{
				if(skipState){
					skipState = false;
					if(shouldUpdate) this.broadcastSkipState(false);
				}
			}
		}
	}

	@Inject(method = "addPlayer", at =@At("HEAD"))
	private void onPlayerJoin(ServerPlayerEntity p_217448_1_, CallbackInfo ci){
		if(this.canAccelerate()){
			NetworkHandler.sendTo(new NightSkip(this.skipState, TimeAndWindCT.CONFIG.accelerationSpeed), p_217448_1_);
		}
	}

	@Redirect(method = "tickTime", at = @At(value = "INVOKE", target = "net/minecraft/world/server/ServerWorld.setDayTime (J)V"))
	private void customTickerTAW(ServerWorld instance, long p_241114_1_) {
		this.timeTicker.tick(this);
		if(this.skipState){
			this.timeTicker.accelerate((ServerWorld) (Object) this, TimeAndWindCT.CONFIG.accelerationSpeed);
			if(this.isDay()){
				this.skipState = false;
				this.shouldUpdate = false;
				this.broadcastSkipState(false);
				this.wakeUpAllPlayers();
				this.stopWeather();
				this.shouldUpdate = true;
			}
		}
	}

	@Unique
	private void broadcastSkipState(boolean bl){
		this.players.forEach(p -> {
			NetworkHandler.sendTo(new NightSkip(bl, TimeAndWindCT.CONFIG.accelerationSpeed), p);
		});
	}

	@Unique
	private boolean canAccelerate(){
		return TimeAndWindCT.CONFIG.enableNightSkipAcceleration && !TimeAndWindCT.CONFIG.syncWithSystemTime;
	}

	@Override
	public void time_and_wind_custom_ticker$wakeUpAllPlayersTAW() {
		this.wakeUpAllPlayers();
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

	@Override
	public boolean time_and_wind_custom_ticker$isClient() {
		return this.isClientSide();
	}

	@Override
	public void time_and_wind_custom_ticker$setSkipState(boolean bl) {

	}

	@Override
	public void time_and_wind_custom_ticker$setSpeed(int speed) {

	}
}
