package ru.aiefu.timeandwind.mixin;

import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import ru.aiefu.timeandwind.IDimType;
import svenhjol.charm.module.SleepImprovements;

@Pseudo
@Mixin(SleepImprovements.class)
public class CharmNightSkipInjection {
    @ModifyConstant(method = "tryEndNight", constant = @Constant(longValue = 24000L), remap = false)
    private long patchCharmNightSkip(long l, ServerWorld world){
        return ((IDimType)world).getCycleDuration();
    }
}
