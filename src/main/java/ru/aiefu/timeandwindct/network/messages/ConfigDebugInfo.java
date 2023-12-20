package ru.aiefu.timeandwindct.network.messages;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import ru.aiefu.timeandwindct.network.ClientNetworkHandler;

public class ConfigDebugInfo {

    public ConfigDebugInfo(){
    }

    public static ConfigDebugInfo decode(FriendlyByteBuf buf){
        return new ConfigDebugInfo();
    }


    public void encode(FriendlyByteBuf buf) {
    }

    public void handle(CustomPayloadEvent.Context context) {
        if(FMLEnvironment.dist.isClient()) {
            ClientNetworkHandler.handleConfigDebugInfoPacket();
        }
    }
}
