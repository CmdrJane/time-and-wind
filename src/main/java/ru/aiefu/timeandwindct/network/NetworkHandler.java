package ru.aiefu.timeandwindct.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import ru.aiefu.timeandwindct.TimeAndWindCT;
import ru.aiefu.timeandwindct.network.messages.*;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Function;

public class NetworkHandler{
    private static final String PROTOCOL_VERSION = Integer.toString(1);
    public static final SimpleChannel network = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(TimeAndWindCT.MOD_ID, "main"))
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .simpleChannel();
    private static int id = 0;

    public static void setup(){
        registerPacket(SyncConfig.class, SyncConfig::new, NetworkDirection.PLAY_TO_CLIENT);
        registerPacket(ConfigDebugInfo.class, ConfigDebugInfo::new, NetworkDirection.PLAY_TO_CLIENT);
        registerPacket(WorldKeyToClipboard.class, WorldKeyToClipboard::new, NetworkDirection.PLAY_TO_CLIENT);

        registerPacket(ResyncConfig.class, ResyncConfig::new, NetworkDirection.PLAY_TO_SERVER);
    }


    public static <MSG extends ITAWPacket> void registerPacket(Class<MSG> clazz, Function<PacketBuffer, MSG> decoder, @Nullable NetworkDirection direction) {
        network.registerMessage(id++, clazz, ITAWPacket::encode, decoder, ITAWPacket::handle, Optional.ofNullable(direction));
    }
    public static void sendTo(Object msg, ServerPlayerEntity player) {
        if (!(player instanceof FakePlayer)) {
            network.sendTo(msg, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
        }
    }
    public static void sendToServer(Object msg) {
        network.sendToServer(msg);
    }
}
