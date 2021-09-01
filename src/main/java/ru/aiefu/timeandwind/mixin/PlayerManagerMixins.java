package ru.aiefu.timeandwind.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.aiefu.timeandwind.TimeAndWind;

//TODO: Move to forge event bus
@Mixin(PlayerList.class)
public class PlayerManagerMixins {
    @Inject(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "net/minecraft/server/network/ServerPlayNetworkHandler.sendPacket(Lnet/minecraft/network/Packet;)V", shift = At.Shift.BEFORE, ordinal = 0))
    private void syncTimeDurationOnJoin(Connection connection, ServerPlayer player, CallbackInfo ci){
        TimeAndWind.sendConfigSyncPacket(player);
    }
}
