package ru.aiefu.timeandwind.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.Schedule;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
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
public abstract class VillagerInjection extends MerchantEntity implements VillagerChecker {

    public VillagerInjection(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    @Redirect(method = "initBrain", at =@At(value = "INVOKE", target = "net/minecraft/entity/ai/brain/Brain.setSchedule(Lnet/minecraft/entity/ai/brain/Schedule;)V", ordinal = 1))
    private void craftPacthedTask(Brain brain, Schedule schedule){
        IBrain brain1 = (IBrain) brain;
        if(!this.world.getDimension().hasFixedTime()) {
            brain1.setCycleDuration(((IDimType) this.world.getDimension()).getCycleDuration());
            brain.setSchedule(TAWScheduler.scheduler.get(this.world.getRegistryKey().getValue().getPath() + "_villager_taw"));
        } else {
            brain1.setCycleDuration(24000L);
            brain.setSchedule(Schedule.VILLAGER_DEFAULT);
        }
    }

    @Redirect(method = "initBrain", at =@At(value = "INVOKE", target = "net/minecraft/entity/ai/brain/Brain.setSchedule(Lnet/minecraft/entity/ai/brain/Schedule;)V", ordinal = 0))
    private void craftPacthedTaskBaby(Brain brain, Schedule schedule){
        IBrain brain1 = (IBrain) brain;
        if(!this.world.getDimension().hasFixedTime()) {
            brain1.setCycleDuration(((IDimType) this.world.getDimension()).getCycleDuration());
            brain.setSchedule(TAWScheduler.scheduler.get(this.world.getRegistryKey().getValue().getPath() + "_villager_baby_taw"));
        } else {
            brain1.setCycleDuration(24000L);
            brain.setSchedule(Schedule.VILLAGER_BABY);
        }
    }


    @ModifyConstant(method = "hasRecentlySlept", constant = @Constant(longValue = 24000L))
    private long hasRecentlySleptPatchTAW(long l){
        return 24000L;
    }
}
