package ru.aiefu.timeandwindct.network.messages;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.NetworkEvent;
import ru.aiefu.timeandwindct.network.ClientNetworkHandler;

import java.util.function.Supplier;

public class ConfigDebugInfo implements ITAWPacket{

    public ConfigDebugInfo(){
    }

    public ConfigDebugInfo(PacketBuffer buf){
    }

    @Override
    public void encode(PacketBuffer buf) {
    }

    @Override
    public void handle(Supplier<NetworkEvent.Context> context) {
        if(FMLEnvironment.dist.isClient()) {
            context.get().enqueueWork(ClientNetworkHandler::handleConfigDebugInfoPacket);
            context.get().setPacketHandled(true);
        }
    }
}
