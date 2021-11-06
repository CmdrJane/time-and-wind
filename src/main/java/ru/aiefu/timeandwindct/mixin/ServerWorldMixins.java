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
import net.minecraft.world.level.CustomSpawner;
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

	protected ServerWorldMixins(WritableLevelData properties, ResourceKey<Level> registryRef, DimensionType dimensionType, Supplier<ProfilerFiller> profiler, boolean isClient, boolean debugWorld, long seed) {
		super(properties, registryRef, dimensionType, profiler, isClient, debugWorld, seed);
	}

	@Shadow @Final
	private List<ServerPlayer> players;

	@Shadow public abstract void setDayTime(long l);

	@Shadow private boolean allPlayersSleeping;
	protected Ticker timeTicker;

	protected boolean enableNightSkipAcceleration = false;
	protected int accelerationSpeed = 0;

	private boolean shouldUpdateNSkip = true;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void attachTimeDataTAW(MinecraftServer server, Executor workerExecutor, LevelStorageSource.LevelStorageAccess session, ServerLevelData properties, ResourceKey<Level> worldKey, DimensionType dimensionType, ChunkProgressListener worldGenerationProgressListener, ChunkGenerator chunkGenerator, boolean debugWorld, long seed, List<CustomSpawner> spawners, boolean shouldTickTime, CallbackInfo ci){
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
			this.timeTicker = new TimeTicker(storage.dayDuration, storage.nightDuration);
		} else this.timeTicker = new DefaultTicker();
	}

	@Inject(method = "updateSleepingPlayerList", at =@At("HEAD"), cancellable = true)
	private void patchNightSkip(CallbackInfo ci){
		if(TimeAndWindCT.CONFIG.syncWithSystemTime){
			this.allPlayersSleeping = false;
			ci.cancel();
		} else if (TimeAndWindCT.CONFIG.enableNightSkipAcceleration){
			this.allPlayersSleeping = false;
			List<ServerPlayer> totalPlayers = this.players.stream().filter(player -> !player.isSpectator() || !player.isCreative()).collect(Collectors.toList());
			if(totalPlayers.size() > 0) {
				int sleepingPlayers = (int) totalPlayers.stream().filter(ServerPlayer::isSleeping).count();
				double factor = (double) sleepingPlayers / totalPlayers.size();
				int threshold = TimeAndWindCT.CONFIG.enableThreshold ? totalPlayers.size() / 100 * TimeAndWindCT.CONFIG.thresholdPercentage : 0;
				if (sleepingPlayers > threshold) {
					enableNightSkipAcceleration = true;
					this.accelerationSpeed = TimeAndWindCT.CONFIG.enableThreshold && TimeAndWindCT.CONFIG.flatAcceleration ?
							TimeAndWindCT.CONFIG.accelerationSpeed :
							(int) Math.ceil(TimeAndWindCT.CONFIG.accelerationSpeed * factor);
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
		this.shouldUpdateNSkip = false;
	}

	@Inject(method = "wakeUpAllPlayers", at =@At("TAIL"))
	private void preventPacketsSpamEnd(CallbackInfo ci){
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
		buf.writeBoolean(enableNightSkipAcceleration);
		buf.writeInt(accelerationSpeed);
		this.players.forEach(player -> ServerPlayNetworking.send(player, NetworkPacketsID.NIGHT_SKIP_INFO, buf));
		this.shouldUpdateNSkip = true;
	}

	@Redirect(method = "tickTime", at = @At(value = "INVOKE", target = "net/minecraft/server/level/ServerLevel.setDayTime(J)V"))
	private void customTickerTAW(ServerLevel world, long timeOfDay) {
		this.timeTicker.tick(this, enableNightSkipAcceleration, accelerationSpeed);
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
