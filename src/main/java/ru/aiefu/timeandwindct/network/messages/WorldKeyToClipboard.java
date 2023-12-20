package ru.aiefu.timeandwindct.network.messages;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import ru.aiefu.timeandwindct.network.ClientNetworkHandler;

public class WorldKeyToClipboard{
    protected String worldId;

    public WorldKeyToClipboard(String string){
        this.worldId = string;
    }

    public static WorldKeyToClipboard decode(FriendlyByteBuf buf){
        return new WorldKeyToClipboard(buf.readUtf());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(worldId);
    }

    public void handle(CustomPayloadEvent.Context context) {
        if(FMLEnvironment.dist.isClient()) {
            ClientNetworkHandler.handleWorldIdToClipboardPacket(worldId);
        }
    }
}
