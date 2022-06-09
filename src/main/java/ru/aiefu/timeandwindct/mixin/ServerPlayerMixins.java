package ru.aiefu.timeandwindct.mixin;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.aiefu.timeandwindct.TimeAndWindCT;

import java.util.List;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixins extends Player{


    public ServerPlayerMixins(Level level, BlockPos blockPos, float f, GameProfile gameProfile, @Nullable ProfilePublicKey profilePublicKey) {
        super(level, blockPos, f, gameProfile, profilePublicKey);
    }

    @Inject(method = "startSleepInBed", at = @At(value = "FIELD", target = "net/minecraft/world/entity/player/Player$BedSleepingProblem.NOT_POSSIBLE_NOW:Lnet/minecraft/world/entity/player/Player$BedSleepingProblem;"), cancellable = true)
    private void patchSleep(BlockPos pos, CallbackInfoReturnable<Either<Player.BedSleepingProblem, Unit>> cir){
        if(TimeAndWindCT.CONFIG.syncWithSystemTime) cir.setReturnValue(patchSleepMechanics(pos));
    }

    private Either<Player.BedSleepingProblem, Unit> patchSleepMechanics(BlockPos pos){
        if (!this.isCreative()) {
            Vec3 vec3d = Vec3.atBottomCenterOf(pos);
            List<Monster> list = this.level.getEntitiesOfClass(Monster.class, new AABB(vec3d.x() - 8.0D, vec3d.y() - 5.0D, vec3d.z() - 8.0D, vec3d.x() + 8.0D, vec3d.y() + 5.0D, vec3d.z() + 8.0D), (hostileEntity) -> hostileEntity.isPreventingPlayerRest(this));
            if (!list.isEmpty()) {
                return Either.left(Player.BedSleepingProblem.NOT_SAFE);
            }
        }

        Either<Player.BedSleepingProblem, Unit> either = super.startSleepInBed(pos).ifRight((unit) -> {
            this.awardStat(Stats.SLEEP_IN_BED);
            CriteriaTriggers.SLEPT_IN_BED.trigger((ServerPlayer) (Object)this);
        });
        ((ServerLevel)this.level).updateSleepingPlayerList();
        return either;
    }
}
