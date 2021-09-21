package ru.aiefu.timeandwindct;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import ru.aiefu.timeandwindct.packets.SyncConfig;

public class TAWNetworkHandler {
    private static int packetId = 0;

    public static SimpleNetworkWrapper INSTANCE = null;

    public TAWNetworkHandler() {
    }

    public static int nextID() {
        return packetId++;
    }

    public static void registerMessages(String channelName) {
        INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(channelName);
        registerMessages();
    }

    public static void registerMessages() {
        // Register messages which are sent from the client to the server here:
        INSTANCE.registerMessage(SyncConfig.Handler.class, SyncConfig.class, nextID(), Side.CLIENT);
    }
}
