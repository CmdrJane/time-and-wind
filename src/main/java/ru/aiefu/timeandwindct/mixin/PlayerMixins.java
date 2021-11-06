package ru.aiefu.timeandwindct.mixin;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.aiefu.timeandwindct.TimeAndWindCT;

@Mixin(Player.class)
public abstract class PlayerMixins extends LivingEntity {

    private int restTimer = 0;

    protected PlayerMixins(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Shadow public abstract boolean isSleepingLongEnough();

    @Shadow public abstract void stopSleepInBed(boolean bl, boolean bl2);

    @Redirect(method = "tick", at =@At(value = "INVOKE", target = "net/minecraft/world/entity/player/Player.stopSleepInBed(ZZ)V"))
    private void disableDayCheck(Player playerEntity, boolean bl, boolean updateSleepingPlayers){
        if(!TimeAndWindCT.CONFIG.syncWithSystemTime){
            this.stopSleepInBed(false, true);
        }
    }

    @Inject(method = "startSleepInBed", at =@At("HEAD"))
    private void onPlayerStartedSleeping(BlockPos pos, CallbackInfoReturnable<Either<Player.BedSleepingProblem, Unit>> cir){
        this.restTimer = 0;
    }

    @Inject(method = "tick", at =@At("TAIL"))
    private void onPlayerTick(CallbackInfo ci){
        if(TimeAndWindCT.CONFIG.syncWithSystemTime && this.isSleepingLongEnough()){
            ++restTimer;
            if(restTimer > 60){
                this.heal(1.0F);
                restTimer = 0;
            }
        }
    }
}
