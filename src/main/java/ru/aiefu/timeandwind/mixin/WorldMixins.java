package ru.aiefu.timeandwind.mixin;

import net.minecraft.profiler.IProfiler;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.storage.ISpawnWorldInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.aiefu.timeandwind.IDimType;
import ru.aiefu.timeandwind.TAWScheduler;
import ru.aiefu.timeandwind.TimeAndWind;
import ru.aiefu.timeandwind.TimeDataStorage;

import java.util.function.Supplier;

@Mixin(World.class)
public class WorldMixins {
    @Shadow
    @Final
    private DimensionType dimensionType;
    @Shadow
    @Final
    private RegistryKey<World> dimension;

    @Inject(method = "<init>", at =@At("RETURN"))
    private void attachTimeData(ISpawnWorldInfo p_i241925_1_, RegistryKey<World> p_i241925_2_, DimensionType p_i241925_3_, Supplier<IProfiler> p_i241925_4_, boolean p_i241925_5_, boolean p_i241925_6_, long p_i241925_7_, CallbackInfo ci){

        String worldId = this.dimension.location().toString();
        if (TimeAndWind.timeDataMap.containsKey(worldId)) {
            TimeDataStorage storage = TimeAndWind.timeDataMap.get(worldId);
            ((IDimType) this.dimension).setCycleDuration(storage.dayDuration, storage.nightDuration);
        }
        TAWScheduler.createTAWSchedule(this.dimensionType, this.dimension.location().getPath(), "_villager_taw", false);
        TAWScheduler.createTAWSchedule(this.dimensionType, this.dimension.location().getPath(), "_villager_baby_taw", true);
    }
}
