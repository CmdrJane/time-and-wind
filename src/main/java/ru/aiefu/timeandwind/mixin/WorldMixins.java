package ru.aiefu.timeandwind.mixin;

import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
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
    @Shadow @Final private DimensionType dimension;

    @Shadow
    @Final
    private RegistryKey<World> registryKey;

    @Inject(method = "<init>", at =@At("RETURN"))
    private void attachTimeData(MutableWorldProperties properties, RegistryKey<World> registryRef, DimensionType dimensionType, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed, CallbackInfo ci){
        if(this instanceof StructureWorldAccess) {
            String worldId = this.registryKey.getValue().toString();
            if (TimeAndWind.timeDataMap.containsKey(worldId)) {
                TimeDataStorage storage = TimeAndWind.timeDataMap.get(worldId);
                ((IDimType) this.dimension).setCycleDuration(storage.dayDuration, storage.nightDuration);
            }
            TAWScheduler.createTAWSchedule(this.dimension, this.registryKey.getValue().getPath(), "_villager_taw", false);
            TAWScheduler.createTAWSchedule(this.dimension, this.registryKey.getValue().getPath(), "_villager_baby_taw", true);
        }
    }
}
