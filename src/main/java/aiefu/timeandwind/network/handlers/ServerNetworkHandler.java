package aiefu.timeandwind.network.handlers;

import aiefu.timeandwind.network.FullSyncPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public class ServerNetworkHandler {

    public static void sendFullSyncToPlayer(ServerPlayer player){
        ServerPlayNetworking.send(player, FullSyncPayload.getPacket());
    }
}
