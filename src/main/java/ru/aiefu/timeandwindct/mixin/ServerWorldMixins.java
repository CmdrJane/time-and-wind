package ru.aiefu.timeandwindct.mixin;

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

import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixins extends World implements ITimeOperations {


	protected ServerWorldMixins(ISpawnWorldInfo p_i241925_1_, RegistryKey<World> p_i241925_2_, DimensionType p_i241925_3_, Supplier<IProfiler> p_i241925_4_, boolean p_i241925_5_, boolean p_i241925_6_, long p_i241925_7_) {
		super(p_i241925_1_, p_i241925_2_, p_i241925_3_, p_i241925_4_, p_i241925_5_, p_i241925_6_, p_i241925_7_);
	}

	@Shadow
	public abstract void setDayTime(long p_241114_1_);

	protected TimeTicker timeTicker = new TimeTicker();

	@Inject(method = "<init>", at = @At("TAIL"))
	private void attachTimeDataTAW(MinecraftServer p_i241885_1_, Executor p_i241885_2_, SaveFormat.LevelSave p_i241885_3_, IServerWorldInfo p_i241885_4_, RegistryKey<World> p_i241885_5_, DimensionType p_i241885_6_, IChunkStatusListener p_i241885_7_, ChunkGenerator p_i241885_8_, boolean p_i241885_9_, long p_i241885_10_, List<ISpecialSpawner> p_i241885_12_, boolean p_i241885_13_, CallbackInfo ci){
		String worldId = this.dimension().location().toString();
		if (TimeAndWindCT.timeDataMap.containsKey(worldId)) {
			TimeDataStorage storage = TimeAndWindCT.timeDataMap.get(worldId);
			this.timeTicker.setupCustomTime(storage.dayDuration, storage.nightDuration);
		} else this.timeTicker.setCustomTicker(false);
	}

	@Redirect(method = "tickTime", at = @At(value = "INVOKE", target = "net/minecraft/world/server/ServerWorld.setDayTime(J)V"))
	private void customTickerTAW(ServerWorld world, long timeOfDay) {
		this.timeTicker.tickTime((ITimeOperations) world, timeOfDay);
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
}
