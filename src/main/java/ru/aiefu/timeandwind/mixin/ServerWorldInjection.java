package ru.aiefu.timeandwind.mixin;

import net.minecraft.profiler.IProfiler;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.ISpawnWorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import ru.aiefu.timeandwind.IDimType;

import java.util.function.Supplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorldInjection extends World {


    protected ServerWorldInjection(ISpawnWorldInfo p_i241925_1_, RegistryKey<World> p_i241925_2_, DimensionType p_i241925_3_, Supplier<IProfiler> p_i241925_4_, boolean p_i241925_5_, boolean p_i241925_6_, long p_i241925_7_) {
        super(p_i241925_1_, p_i241925_2_, p_i241925_3_, p_i241925_4_, p_i241925_5_, p_i241925_6_, p_i241925_7_);
    }

    @ModifyConstant(method = "tick(Ljava/util/function/BooleanSupplier;)V", constant = @Constant(longValue = 24000L))
    private long patchNightSkip(long l){
        return ((IDimType)this.dimensionType()).getCycleDuration();
    }

}
