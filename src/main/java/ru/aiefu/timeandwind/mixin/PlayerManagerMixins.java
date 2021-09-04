package ru.aiefu.timeandwind.mixin;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.aiefu.timeandwind.TimeAndWind;


@Mixin(PlayerManager.class)
public class PlayerManagerMixins {
    @Inject(method = "onPlayerConnect", at = @At("HEAD"))
    private void syncTimeDurationOnJoin(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci){
        TimeAndWind.sendConfigSyncPacket(player);
    }
}
