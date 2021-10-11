package ru.aiefu.timeandwindct.mixin;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.aiefu.timeandwindct.TimeAndWindCT;


@Mixin(PlayerManager.class)
public class PlayerManagerMixins {
    @Inject(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "net/minecraft/server/world/ServerWorld.getGameRules()Lnet/minecraft/world/GameRules;", shift = At.Shift.BEFORE))
    private void syncTimeDurationOnJoin(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci){
        TimeAndWindCT.sendConfigSyncPacket(player);
        TimeAndWindCT.sendModConfigSyncPacket(player);
    }
}
