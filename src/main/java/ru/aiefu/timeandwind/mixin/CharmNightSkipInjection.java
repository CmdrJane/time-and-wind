package ru.aiefu.timeandwind.mixin;

import net.minecraft.server.level.ServerLevel;
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
    private long patchCharmNightSkip(long l, ServerLevel world){
        return ((IDimType)world.dimensionType()).getCycleDuration();
    }
}
