package ru.aiefu.timeandwind.mixin;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import ru.aiefu.timeandwind.IDimType;
import ru.aiefu.timeandwind.TimeAndWind;

import java.util.Optional;

@Mixin(PlayerManager.class)
public class PlayerManagerMixins {
    @Inject(method = "onPlayerConnect", at = @At("RETURN"))
    private void syncTimeDurationOnJoin(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci){
        IDimType dim = (IDimType) player.world.getDimension();
        TimeAndWind.sendTimeSyncPacket(player, dim.getDayDuration(), dim.getNightDuration());
    }
    @Inject(method = "respawnPlayer", at =@At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void syncTimeDurationOnRespawn(ServerPlayerEntity player, boolean alive, CallbackInfoReturnable<ServerPlayerEntity> cir, BlockPos blockPos, float f, boolean bl, ServerWorld serverWorld, Optional optional2, ServerPlayerInteractionManager serverPlayerInteractionManager2, ServerWorld serverWorld2, ServerPlayerEntity serverPlayerEntity){
        IDimType dim = (IDimType) serverPlayerEntity.world.getDimension();
        TimeAndWind.sendTimeSyncPacket(serverPlayerEntity, dim.getDayDuration(), dim.getNightDuration());
    }
}
