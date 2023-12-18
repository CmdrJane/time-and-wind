package ru.aiefu.timeandwindct.mixin;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.players.SleepStatus;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.aiefu.timeandwindct.ITimeOperations;
import ru.aiefu.timeandwindct.NetworkPacketsID;
import ru.aiefu.timeandwindct.TimeAndWindCT;
import ru.aiefu.timeandwindct.config.TimeDataStorage;
import ru.aiefu.timeandwindct.tickers.DefaultTicker;
import ru.aiefu.timeandwindct.tickers.SystemTimeTicker;
import ru.aiefu.timeandwindct.tickers.Ticker;
import ru.aiefu.timeandwindct.tickers.TimeTicker;

import java.util.List;
import java.util.concurrent.Executor;
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

	@Shadow protected abstract void resetWeatherCycle();

	@Unique
	protected Ticker timeTicker;

	@Unique
	private boolean shouldUpdate = true;

	@Unique
	private boolean skipState = false;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void attachTimeDataTAW(MinecraftServer minecraftServer, Executor executor, LevelStorageSource.LevelStorageAccess levelStorageAccess, ServerLevelData serverLevelData, ResourceKey<Level> resourceKey, LevelStem levelStem, ChunkProgressListener chunkProgressListener, boolean bl, long l, List<CustomSpawner> list, boolean bl2, RandomSequences randomSequences, CallbackInfo ci){
		String worldId = resourceKey.location().toString();
		if(levelStem.type().value().hasFixedTime()){
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

	@Redirect(method = "tick", at =@At(value = "INVOKE", target = "net/minecraft/server/players/SleepStatus.areEnoughDeepSleeping (ILjava/util/List;)Z", ordinal = 0),
			slice = @Slice(from = @At(value = "INVOKE", target = "net/minecraft/server/level/ServerLevel.advanceWeatherCycle ()V", ordinal = 0),
					to = @At(value = "INVOKE", target = "net/minecraft/server/level/ServerLevel.wakeUpAllPlayers ()V", ordinal = 0)))
	private boolean blockSleepCheckTAW(SleepStatus instance, int i, List<ServerPlayer> list){
		return !(TimeAndWindCT.CONFIG.syncWithSystemTime || TimeAndWindCT.CONFIG.enableNightSkipAcceleration);
	}

	@Inject(method = "updateSleepingPlayerList", at = @At("TAIL"))
	private void syncSkipStateTAW(CallbackInfo ci){
		int i = this.getGameRules().getInt(GameRules.RULE_PLAYERS_SLEEPING_PERCENTAGE);
		if(this.canAccelerate()){
			if(this.sleepStatus.areEnoughSleeping(i)){
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
	private void onPlayerJoin(ServerPlayer player, CallbackInfo ci){
		if(this.canAccelerate()){
			FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
			buf.writeBoolean(skipState);
			buf.writeInt(TimeAndWindCT.CONFIG.accelerationSpeed);
			ServerPlayNetworking.send(player, NetworkPacketsID.NIGHT_SKIP_INFO, buf);
		}
	}

	@Redirect(method = "tickTime", at = @At(value = "INVOKE", target = "net/minecraft/server/level/ServerLevel.setDayTime(J)V"))
	private void customTickerTAW(ServerLevel world, long timeOfDay) {
		this.timeTicker.tick(this);
		if(this.skipState){
			this.timeTicker.accelerate((ServerLevel) (Object) this, TimeAndWindCT.CONFIG.accelerationSpeed);
			if(this.isDay()){
				this.skipState = false;
				this.shouldUpdate = false;
				this.broadcastSkipState(false);
				this.wakeUpAllPlayers();
				this.resetWeatherCycle();
				this.shouldUpdate = true;
			}
		}
	}

	@Unique
	private void broadcastSkipState(boolean bl){
		this.players.forEach(p -> {
			FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
			buf.writeBoolean(bl);
			buf.writeInt(TimeAndWindCT.CONFIG.accelerationSpeed);
			ServerPlayNetworking.send(p, NetworkPacketsID.NIGHT_SKIP_INFO, buf);
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
