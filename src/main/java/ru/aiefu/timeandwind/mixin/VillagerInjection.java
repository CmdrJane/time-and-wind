package ru.aiefu.timeandwind.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.schedule.Schedule;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import ru.aiefu.timeandwind.IBrain;
import ru.aiefu.timeandwind.IDimType;
import ru.aiefu.timeandwind.TAWScheduler;
import ru.aiefu.timeandwind.VillagerChecker;

@Mixin(VillagerEntity.class)
public abstract class VillagerInjection extends AbstractVillagerEntity implements VillagerChecker {


    public VillagerInjection(EntityType<? extends AbstractVillagerEntity> p_i50185_1_, World p_i50185_2_) {
        super(p_i50185_1_, p_i50185_2_);
    }

    @Redirect(method = "registerBrainGoals", at =@At(value = "INVOKE", target = "net/minecraft/entity/ai/brain/Brain.setSchedule(Lnet/minecraft/entity/ai/brain/schedule/Schedule;)V", ordinal = 1))
    private void craftPacthedTask(Brain brain, Schedule schedule){
        IBrain brain1 = (IBrain) brain;
        if(!this.level.dimensionType().hasFixedTime()) {
            brain1.setCycleDuration(((IDimType) this.level.dimensionType()).getCycleDuration());
            brain.setSchedule(TAWScheduler.scheduler.get(this.level.dimension().location().getPath() + "_villager_taw"));
        } else {
            brain1.setCycleDuration(24000L);
            brain.setSchedule(Schedule.VILLAGER_DEFAULT);
        }
    }

    @Redirect(method = "registerBrainGoals", at =@At(value = "INVOKE", target = "net/minecraft/entity/ai/brain/Brain.setSchedule(Lnet/minecraft/entity/ai/brain/schedule/Schedule;)V", ordinal = 0))
    private void craftPacthedTaskBaby(Brain brain, Schedule schedule){
        IBrain brain1 = (IBrain) brain;
        if(!this.level.dimensionType().hasFixedTime()) {
            brain1.setCycleDuration(((IDimType) this.level.dimensionType()).getCycleDuration());
            brain.setSchedule(TAWScheduler.scheduler.get(this.level.dimension().location().getPath() + "_villager_baby_taw"));
        } else {
            brain1.setCycleDuration(24000L);
            brain.setSchedule(Schedule.VILLAGER_BABY);
        }
    }


    @ModifyConstant(method = "golemSpawnConditionsMet", constant = @Constant(longValue = 24000L))
    private long hasRecentlySleptPatchTAW(long l){
        return ((IDimType)this.level.dimensionType()).getCycleDuration();
    }
}
