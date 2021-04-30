package ru.aiefu.timeandwind.mixin;

import net.minecraft.entity.ai.brain.Brain;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Brain.class)
public class NoBrainInjection {
    @ModifyConstant(method = "refreshActivities", constant = @Constant(longValue = 24000L))
    private long refreshActivitiesPatchTAW(long l){
        return 72000L;
    }
}
