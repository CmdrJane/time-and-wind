package ru.aiefu.timeandwindct.mixin;

import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.aiefu.timeandwindct.TimeAndWindCT;


@Mixin(PlayerList.class)
public class PlayerManagerMixins {
    @Inject(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "net/minecraft/server/level/ServerLevel.getGameRules ()Lnet/minecraft/world/level/GameRules;", shift = At.Shift.BEFORE))
    private void syncTimeDurationOnJoin(Connection connection, ServerPlayer player, CallbackInfo ci){
        TimeAndWindCT.sendConfigSyncPacket(player);
    }
}
