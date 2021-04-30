package ru.aiefu.timeandwind.mixin;

import net.minecraft.entity.passive.VillagerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(VillagerEntity.class)
public class VillagerInjection {
    @ModifyConstant(method = "hasRecentlyWorkedAndSlept", constant = @Constant(longValue = 24000L))
    private long hasRecentlyWorkedAndSleptPatchTAW(long l){
        return 24000L;
    }
}
