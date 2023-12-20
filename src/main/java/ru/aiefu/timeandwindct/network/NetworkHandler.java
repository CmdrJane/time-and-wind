package ru.aiefu.timeandwindct.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.SimpleChannel;
import ru.aiefu.timeandwindct.TimeAndWindCT;
import ru.aiefu.timeandwindct.network.messages.ConfigDebugInfo;
import ru.aiefu.timeandwindct.network.messages.NightSkip;
import ru.aiefu.timeandwindct.network.messages.SyncConfig;
import ru.aiefu.timeandwindct.network.messages.WorldKeyToClipboard;



public class NetworkHandler{
    private static final int PROTOCOL_VERSION = 1;
    public static final SimpleChannel network = ChannelBuilder.named(new ResourceLocation(TimeAndWindCT.MOD_ID, "main"))
            .clientAcceptedVersions(Channel.VersionTest.exact(PROTOCOL_VERSION))
            .serverAcceptedVersions(Channel.VersionTest.exact(PROTOCOL_VERSION))
            .networkProtocolVersion(PROTOCOL_VERSION).simpleChannel();
    private static int id = 0;

    public static void setup(){
        network.messageBuilder(SyncConfig.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncConfig::encode).decoder(SyncConfig::decode).consumerMainThread(SyncConfig::handle).add();
        network.messageBuilder(NightSkip.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(NightSkip::encode).decoder(NightSkip::decode).consumerMainThread(NightSkip::handle).add();
        network.messageBuilder(ConfigDebugInfo.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ConfigDebugInfo::encode).decoder(ConfigDebugInfo::decode).consumerMainThread(ConfigDebugInfo::handle).add();
        network.messageBuilder(WorldKeyToClipboard.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(WorldKeyToClipboard::encode).decoder(WorldKeyToClipboard::decode).consumerMainThread(WorldKeyToClipboard::handle).add();
    }

    public static void sendToPlayer(Object packet, ServerPlayer player){
        network.send(packet, player.connection.getConnection());
    }
}
