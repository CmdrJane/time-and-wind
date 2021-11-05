package ru.aiefu.timeandwindct.mixin;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.Spawner;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;
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

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixins extends World implements ITimeOperations {

	protected ServerWorldMixins(MutableWorldProperties properties, RegistryKey<World> registryRef, DimensionType dimensionType, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed) {
		super(properties, registryRef, dimensionType, profiler, isClient, debugWorld, seed);
	}

	@Shadow
	public abstract void setTimeOfDay(long timeOfDay);

	@Shadow @Final
	List<ServerPlayerEntity> players;
	protected Ticker timeTicker;

	protected boolean enableNightSkipAcceleration = false;
	protected int accelerationSpeed = 0;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void attachTimeDataTAW(MinecraftServer server, Executor workerExecutor, LevelStorage.Session session, ServerWorldProperties properties, RegistryKey<World> worldKey, DimensionType dimensionType, WorldGenerationProgressListener worldGenerationProgressListener, ChunkGenerator chunkGenerator, boolean debugWorld, long seed, List<Spawner> spawners, boolean shouldTickTime, CallbackInfo ci){
		String worldId = this.getRegistryKey().getValue().toString();
		if(this.getDimension().hasFixedTime()){
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

	@Inject(method = "updateSleepingPlayers", at =@At("HEAD"), cancellable = true)
	private void patchNightSkip(CallbackInfo ci){
		if(TimeAndWindCT.CONFIG.syncWithSystemTime){
			ci.cancel();
		} else if (TimeAndWindCT.CONFIG.enableNightSkipAcceleration){
			List<ServerPlayerEntity> totalPlayers = this.players.stream().filter(player -> !player.isSpectator() || !player.isCreative()).collect(Collectors.toList());
			if(totalPlayers.size() > 0) {
				int sleepingPlayers = (int) totalPlayers.stream().filter(ServerPlayerEntity::isSleeping).count();
				double factor = (double) sleepingPlayers / totalPlayers.size();
				int threshold = TimeAndWindCT.CONFIG.enableThreshold ? totalPlayers.size() / 100 * TimeAndWindCT.CONFIG.thresholdPercentage : 0;
				if (sleepingPlayers > threshold) {
					enableNightSkipAcceleration = true;
					this.accelerationSpeed = TimeAndWindCT.CONFIG.enableThreshold && TimeAndWindCT.CONFIG.flatAcceleration ?
							TimeAndWindCT.CONFIG.accelerationSpeed :
							(int) Math.ceil(TimeAndWindCT.CONFIG.accelerationSpeed * factor);
				} else enableNightSkipAcceleration = false;
			} else enableNightSkipAcceleration = false;
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			buf.writeBoolean(enableNightSkipAcceleration);
			buf.writeInt(accelerationSpeed);
			this.players.forEach(player -> ServerPlayNetworking.send(player, NetworkPacketsID.NIGHT_SKIP_INFO, buf));
			ci.cancel();
		}
	}

	@Inject(method = "addPlayer", at =@At("HEAD"))
	private void onPlayerJoin(ServerPlayerEntity player, CallbackInfo ci){
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeBoolean(enableNightSkipAcceleration);
		buf.writeInt(accelerationSpeed);
		ServerPlayNetworking.send(player, NetworkPacketsID.NIGHT_SKIP_INFO, buf);
	}

	@Redirect(method = "tickTime", at = @At(value = "INVOKE", target = "net/minecraft/server/world/ServerWorld.setTimeOfDay(J)V"))
	private void customTickerTAW(ServerWorld world, long timeOfDay) {
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

	@Override
	public boolean isClientSide() {
		return this.isClient();
	}

	@Override
	public void setSkipState(boolean bl) {

	}

	@Override
	public void setSpeed(int speed) {

	}
}
