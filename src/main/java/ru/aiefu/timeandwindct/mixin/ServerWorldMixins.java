package ru.aiefu.timeandwindct.mixin;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
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
import java.util.stream.Collectors;

@Mixin(ServerLevel.class)
public abstract class ServerWorldMixins extends Level implements ITimeOperations {



	@Shadow @Final
	List<ServerPlayer> players;

	protected ServerWorldMixins(WritableLevelData writableLevelData, ResourceKey<Level> resourceKey, DimensionType dimensionType, Supplier<ProfilerFiller> supplier, boolean bl, boolean bl2, long l) {
		super(writableLevelData, resourceKey, dimensionType, supplier, bl, bl2, l);
	}


	@Shadow public abstract void setDayTime(long l);


	@Shadow protected abstract void wakeUpAllPlayers();

	@Shadow private boolean allPlayersSleeping;

	@Shadow protected abstract void stopWeather();

	protected Ticker timeTicker;

	protected boolean enableNightSkipAcceleration = false;
	protected int accelerationSpeed = 0;

	private boolean shouldUpdateNSkip = true;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void attachTimeDataTAW(MinecraftServer minecraftServer, Executor executor, LevelStorageSource.LevelStorageAccess levelStorageAccess, ServerLevelData serverLevelData, ResourceKey resourceKey, DimensionType dimensionType, ChunkProgressListener chunkProgressListener, ChunkGenerator chunkGenerator, boolean bl, long l, List list, boolean bl2, CallbackInfo ci){
		String worldId = this.dimension().location().toString();
		if(this.dimensionType().hasFixedTime()){
			this.timeTicker = new DefaultTicker();
		}
		else if(TimeAndWindCT.CONFIG.syncWithSystemTime){
			if(TimeAndWindCT.CONFIG.systemTimePerDimensions && TimeAndWindCT.sysTimeMap.containsKey(worldId)) this.timeTicker = new SystemTimeTicker(this, TimeAndWindCT.sysTimeMap.get(worldId));
			else this.timeTicker = new SystemTimeTicker(this, TimeAndWindCT.systemTimeConfig);
		}
		else if (TimeAndWindCT.timeDataMap.containsKey(worldId)) {
			TimeDataStorage storage = TimeAndWindCT.timeDataMap.get(worldId);
			this.timeTicker = new TimeTicker(storage.dayDuration, storage.nightDuration, this);
		} else this.timeTicker = new DefaultTicker();
	}

	@Inject(method = "updateSleepingPlayerList", at =@At("HEAD"), cancellable = true)
	private void patchNightSkip(CallbackInfo ci){
		if(TimeAndWindCT.CONFIG.syncWithSystemTime){
			ci.cancel();
		} else if (TimeAndWindCT.CONFIG.enableNightSkipAcceleration){
			this.allPlayersSleeping = false;
			List<ServerPlayer> totalPlayers = this.players.stream().filter(player -> !player.isSpectator() || !player.isCreative()).collect(Collectors.toList());
			if(totalPlayers.size() > 0) {
				int sleepingPlayers = (int) totalPlayers.stream().filter(ServerPlayer::isSleeping).count();
				int threshold = TimeAndWindCT.CONFIG.enableThreshold ? totalPlayers.size() / 100 * TimeAndWindCT.CONFIG.thresholdPercentage : 0;
				if (sleepingPlayers > threshold) {
					enableNightSkipAcceleration = true;
					this.accelerationSpeed = TimeAndWindCT.CONFIG.accelerationSpeed;
				} else enableNightSkipAcceleration = false;
			} else enableNightSkipAcceleration = false;
			if(this.shouldUpdateNSkip) {
				FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
				buf.writeBoolean(enableNightSkipAcceleration);
				buf.writeInt(accelerationSpeed);
				this.players.forEach(player -> ServerPlayNetworking.send(player, NetworkPacketsID.NIGHT_SKIP_INFO, buf));
			}
			ci.cancel();
		}
	}

	@Inject(method = "addPlayer", at =@At("HEAD"))
	private void onPlayerJoin(ServerPlayer player, CallbackInfo ci){
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
		buf.writeBoolean(enableNightSkipAcceleration);
		buf.writeInt(accelerationSpeed);
		ServerPlayNetworking.send(player, NetworkPacketsID.NIGHT_SKIP_INFO, buf);
	}

	@Inject(method = "wakeUpAllPlayers", at =@At("HEAD"))
	private void preventPacketsSpam(CallbackInfo ci){
		this.enableNightSkipAcceleration = false;
		this.shouldUpdateNSkip = false;
	}

	@Inject(method = "wakeUpAllPlayers", at =@At("TAIL"))
	private void preventPacketsSpamEnd(CallbackInfo ci){
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
		buf.writeBoolean(enableNightSkipAcceleration);
		buf.writeInt(accelerationSpeed);
		this.players.forEach(player -> ServerPlayNetworking.send(player, NetworkPacketsID.NIGHT_SKIP_INFO, buf));
		this.shouldUpdateNSkip = true;

		if (this.getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE)) {
			this.stopWeather();
		}
	}

	@Redirect(method = "tickTime", at = @At(value = "INVOKE", target = "net/minecraft/server/level/ServerLevel.setDayTime(J)V"))
	private void customTickerTAW(ServerLevel world, long timeOfDay) {
		this.timeTicker.tick(this, enableNightSkipAcceleration, accelerationSpeed);
	}

	@Override
	public void wakeUpAllPlayersTAW() {
		wakeUpAllPlayers();
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

	}

	@Override
	public void setSpeed(int speed) {

	}
}
