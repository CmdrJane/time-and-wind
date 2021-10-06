package ru.aiefu.timeandwindct.mixin;

import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.aiefu.timeandwindct.ITimeOperations;
import ru.aiefu.timeandwindct.TimeAndWindCT;
import ru.aiefu.timeandwindct.TimeDataStorage;
import ru.aiefu.timeandwindct.TimeTicker;


@Mixin(WorldServer.class)
public abstract class ServerWorldMixins extends World implements ITimeOperations {

	protected ServerWorldMixins(ISaveHandler saveHandlerIn, WorldInfo info, WorldProvider providerIn, Profiler profilerIn, boolean client) {
		super(saveHandlerIn, info, providerIn, profilerIn, client);
	}

	protected TimeTicker timeTicker = new TimeTicker();

	@Inject(method = "<init>", at = @At("TAIL"))
	private void attachTimeDataTAW(MinecraftServer server, ISaveHandler saveHandlerIn, WorldInfo info, int dimensionId, Profiler profilerIn, CallbackInfo ci){
		String worldId = this.provider.getDimensionType().getName();
		if (TimeAndWindCT.timeDataMap.containsKey(worldId)) {
			TimeDataStorage storage = TimeAndWindCT.timeDataMap.get(worldId);
			this.timeTicker.setupCustomTime(storage.dayDuration, storage.nightDuration);
		} else this.timeTicker.setCustomTicker(false);
	}

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "net/minecraft/world/WorldServer.setWorldTime(J)V", ordinal = 1))
	private void customTickerTAW(WorldServer worldServer, long time) {
		this.timeTicker.tickTime((ITimeOperations) worldServer, time);
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
		this.setWorldTime(time);
	}

	@Override
	public long getTimeTAW() {
		return this.worldInfo.getWorldTotalTime();
	}

	@Override
	public long getTimeOfDayTAW() {
		return this.getWorldTime();
	}
}
