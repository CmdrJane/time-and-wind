package ru.aiefu.timeandwindct.network.messages;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public interface ITAWPacket {
    void encode(PacketBuffer buf);
    void handle(Supplier<NetworkEvent.Context> context);
}
