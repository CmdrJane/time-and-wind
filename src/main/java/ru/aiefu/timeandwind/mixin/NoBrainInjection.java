package ru.aiefu.timeandwind.mixin;

import net.minecraft.entity.ai.brain.Brain;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import ru.aiefu.timeandwind.IBrain;

@Mixin(Brain.class)
public class NoBrainInjection implements IBrain {

    private long cycleDuration = 24000L;

    @ModifyConstant(method = "updateActivityFromSchedule", constant = @Constant(longValue = 24000L))
    private long refreshActivitiesPatchTAW(long l){
        return this.cycleDuration;
    }

    @Override
    public void setCycleDuration(long cycleDuration) {
        this.cycleDuration = cycleDuration;
    }
}
