package ru.aiefu.timeandwind.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.Schedule;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import ru.aiefu.timeandwind.TAWSchedule;

@Mixin(VillagerEntity.class)
public abstract class VillagerInjection extends MerchantEntity {

    public VillagerInjection(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    @Redirect(method = "initBrain", at =@At(value = "INVOKE", target = "net/minecraft/entity/ai/brain/Brain.setSchedule(Lnet/minecraft/entity/ai/brain/Schedule;)V"))
    private void craftPacthedTask(Brain brain, Schedule schedule){
        brain.setSchedule(TAWSchedule.createTAWSchedule(this.world.getDimension(), "villager_taw_patch"));
    }

    @ModifyConstant(method = "hasRecentlyWorkedAndSlept", constant = @Constant(longValue = 24000L))
    private long hasRecentlyWorkedAndSleptPatchTAW(long l){
        return 24000L;
    }
}
