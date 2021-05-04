package ru.aiefu.timeandwind.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.aiefu.timeandwind.IDimType;
import ru.aiefu.timeandwind.TimeAndWind;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixins extends PlayerEntity {
    private int syncTickTimer = 0;
    public ServerPlayerEntityMixins(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Inject(method = "moveToWorld", at =@At("TAIL"))
    private void syncTimeOnMoveToWorld(ServerWorld destination, CallbackInfoReturnable<Entity> cir){
        IDimType dim = (IDimType) destination.getDimension();
        TimeAndWind.sendTimeSyncPacket((ServerPlayerEntity) (Object) this, dim.getDayDuration(), dim.getNightDuration());
    }

    @Inject(method = "teleport", at =@At("TAIL"))
    private void syncTimeOnTeleportation(ServerWorld targetWorld, double x, double y, double z, float yaw, float pitch, CallbackInfo ci){
       IDimType dim = (IDimType) targetWorld.getDimension();
       TimeAndWind.sendTimeSyncPacket((ServerPlayerEntity) (Object) this, dim.getDayDuration(), dim.getNightDuration());
    }

    @Inject(method = "tick", at =@At("TAIL"))
    private void syncTimeOnTick(CallbackInfo ci){
        ++this.syncTickTimer;
        if(this.syncTickTimer >= 300){
            this.syncTickTimer = 0;
            IDimType dim = (IDimType) this.world.getDimension();
            TimeAndWind.sendTimeSyncPacket((ServerPlayerEntity) (Object) this, dim.getDayDuration(), dim.getNightDuration());
        }
    }
}
