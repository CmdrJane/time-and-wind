package ru.aiefu.timeandwind.network.messages;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import ru.aiefu.timeandwind.TimeAndWind;
import ru.aiefu.timeandwind.network.NetworkHandler;

import java.util.function.Supplier;

public class ResyncConfig implements ITAWPacket{

    public ResyncConfig(){
    }

    public ResyncConfig(PacketBuffer buf){
    }

    @Override
    public void encode(PacketBuffer buf) {

    }

    @Override
    public void handle(Supplier<NetworkEvent.Context> context) {
        NetworkHandler.sendTo(new SyncConfig(new PacketBuffer(Unpooled.buffer())), context.get().getSender());
        TimeAndWind.LOGGER.warn("[Time & Wind] Player requested config resync, this shouldn't happen");
        TimeAndWind.LOGGER.info("[Time & Wind] Sending resync packet");
    }
}
