package ru.aiefu.timeandwindct.network.messages;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.NetworkEvent;
import ru.aiefu.timeandwindct.network.ClientNetworkHandler;

import java.util.function.Supplier;

public class WorldKeyToClipboard implements ITAWPacket{
    protected String worldId;

    public WorldKeyToClipboard(String string){
        this.worldId = string;
    }

    public WorldKeyToClipboard(PacketBuffer buf){
        this.worldId = buf.readUtf();
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeUtf(worldId);
    }

    @Override
    public void handle(Supplier<NetworkEvent.Context> context) {
        if(FMLEnvironment.dist.isClient()) {
            context.get().enqueueWork(() -> ClientNetworkHandler.handleWorldIdToClipboardPacket(worldId));
            context.get().setPacketHandled(true);
        }
    }
}
