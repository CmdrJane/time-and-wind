package ru.aiefu.timeandwind.mixin;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import ru.aiefu.timeandwind.IDimType;

import java.util.function.Supplier;


@Mixin(ServerWorld.class)
public abstract class ServerWorldInjection extends World {
    protected ServerWorldInjection(MutableWorldProperties properties, RegistryKey<World> registryRef, DimensionType dimensionType, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed) {
        super(properties, registryRef, dimensionType, profiler, isClient, debugWorld, seed);
    }

    @ModifyConstant(method = "tick(Ljava/util/function/BooleanSupplier;)V", constant = @Constant(longValue = 24000L))
    private long patchNightSkip(long l){
        return ((IDimType)this).getCycleDuration();
    }

}
