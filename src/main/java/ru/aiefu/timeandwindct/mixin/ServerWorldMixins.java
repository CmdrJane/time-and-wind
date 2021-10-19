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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.aiefu.timeandwindct.ITimeOperations;
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
import java.util.stream.Collectors;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixins extends World implements ITimeOperations {


	protected ServerWorldMixins(ISpawnWorldInfo p_i241925_1_, RegistryKey<World> p_i241925_2_, DimensionType p_i241925_3_, Supplier<IProfiler> p_i241925_4_, boolean p_i241925_5_, boolean p_i241925_6_, long p_i241925_7_) {
		super(p_i241925_1_, p_i241925_2_, p_i241925_3_, p_i241925_4_, p_i241925_5_, p_i241925_6_, p_i241925_7_);
	}

	@Shadow
	public abstract void setDayTime(long p_241114_1_);

	@Shadow private boolean allPlayersSleeping;

	@Shadow @Final private List<ServerPlayerEntity> players;
	protected Ticker timeTicker;

	protected boolean enableNightSkipAcceleration;
	protected int accelerationSpeed = 0;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void attachTimeDataTAW(MinecraftServer p_i241885_1_, Executor p_i241885_2_, SaveFormat.LevelSave p_i241885_3_, IServerWorldInfo p_i241885_4_, RegistryKey<World> p_i241885_5_, DimensionType p_i241885_6_, IChunkStatusListener p_i241885_7_, ChunkGenerator p_i241885_8_, boolean p_i241885_9_, long p_i241885_10_, List<ISpecialSpawner> p_i241885_12_, boolean p_i241885_13_, CallbackInfo ci){
		String worldId = this.dimension().location().toString();
		if (TimeAndWindCT.CONFIG.syncWithSystemTime) {
			this.timeTicker = new SystemTimeTicker(this);
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
			List<ServerPlayerEntity> totalPlayers = this.players.stream().filter(player -> !player.isSpectator() || !player.isCreative()).collect(Collectors.toList());
			int sleepingPlayers = (int) totalPlayers.stream().filter(ServerPlayerEntity::isSleeping).count();
			double factor = (double)sleepingPlayers / totalPlayers.size();
			int threshold = TimeAndWindCT.CONFIG.enableThreshold ? totalPlayers.size() / 100 * TimeAndWindCT.CONFIG.thresholdPercentage : 0;
			if(sleepingPlayers > threshold){
				enableNightSkipAcceleration = true;
				this.accelerationSpeed = TimeAndWindCT.CONFIG.enableThreshold && TimeAndWindCT.CONFIG.flatAcceleration ?
						TimeAndWindCT.CONFIG.accelerationSpeed :
						(int) Math.ceil(TimeAndWindCT.CONFIG.accelerationSpeed * factor);
			} else enableNightSkipAcceleration = false;
			this.players.forEach(player -> NetworkHandler.sendTo(new NightSkip(enableNightSkipAcceleration, accelerationSpeed), player));
			ci.cancel();
		}
	}

	@Redirect(method = "tickTime", at = @At(value = "INVOKE", target = "net/minecraft/world/server/ServerWorld.setDayTime(J)V"))
	private void customTickerTAW(ServerWorld serverWorld, long p_241114_1_) {
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
