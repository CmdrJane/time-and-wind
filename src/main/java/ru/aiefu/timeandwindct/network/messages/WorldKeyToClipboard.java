package ru.aiefu.timeandwindct.network.messages;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.NetworkEvent;

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
            Minecraft client = Minecraft.getInstance();
            client.keyboardHandler.setClipboard(worldId);
            client.player.displayClientMessage(new StringTextComponent("Also copied this to clipboard"), false);
        }
    }
}
