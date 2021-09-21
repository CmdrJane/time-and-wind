package ru.aiefu.timeandwindct;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import ru.aiefu.timeandwindct.packets.CheckConfig;
import ru.aiefu.timeandwindct.packets.SyncConfig;
import ru.aiefu.timeandwindct.packets.WorldIDToClipboard;

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
        INSTANCE.registerMessage(SyncConfig.Handler.class, SyncConfig.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(CheckConfig.Handler.class, CheckConfig.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(WorldIDToClipboard.Handler.class, WorldIDToClipboard.class, nextID(), Side.CLIENT);
    }
}
