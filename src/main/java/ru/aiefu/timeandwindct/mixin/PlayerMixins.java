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
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.aiefu.timeandwindct.TimeAndWindCT;

@Mixin(PlayerEntity.class)
public abstract class PlayerMixins extends LivingEntity {

    private int sleepTimer = 0;

    protected PlayerMixins(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Shadow public abstract void wakeUp(boolean bl, boolean updateSleepingPlayers);

    @Shadow public abstract boolean isSleepingLongEnough();

    @Redirect(method = "tick", at =@At(value = "INVOKE", target = "net/minecraft/entity/player/PlayerEntity.wakeUp(ZZ)V"))
    private void disableDayCheck(PlayerEntity playerEntity, boolean bl, boolean updateSleepingPlayers){
        if(!TimeAndWindCT.CONFIG.syncWithSystemTime){
            this.wakeUp(false, true);
        }
    }

    @Inject(method = "trySleep", at =@At("HEAD"))
    private void onPlayerStartedSleeping(BlockPos pos, CallbackInfoReturnable<Either<PlayerEntity.SleepFailureReason, Unit>> cir){
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
