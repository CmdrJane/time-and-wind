package ru.aiefu.timeandwindct.mixin;

import com.mojang.datafixers.util.Either;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.aiefu.timeandwindct.TimeAndWindCT;

@Mixin(PlayerEntity.class)
public abstract class PlayerMixins extends LivingEntity {
    @Shadow public abstract boolean isSleepingLongEnough();

    private int sleepTimer = 0;

    protected PlayerMixins(EntityType<? extends LivingEntity> p_i48577_1_, World p_i48577_2_) {
        super(p_i48577_1_, p_i48577_2_);
    }

    @Inject(method = "startSleepInBed", at =@At("HEAD"))
    private void onPlayerStartedSleeping(BlockPos p_213819_1_, CallbackInfoReturnable<Either<PlayerEntity.SleepResult, Unit>> cir){
        this.sleepTimer = 0;
    }
    @Inject(method = "tick", at =@At("TAIL"))
    private void onPlayerTick(CallbackInfo ci){
        if(TimeAndWindCT.CONFIG.syncWithSystemTime && this.isSleepingLongEnough()){
            ++sleepTimer;
            if(sleepTimer > 60){
                this.heal(1.0F);
                sleepTimer = 0;
            }
        }
    }
}
