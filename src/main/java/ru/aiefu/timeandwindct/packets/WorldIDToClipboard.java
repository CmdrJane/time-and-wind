package ru.aiefu.timeandwindct.packets;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.nio.charset.StandardCharsets;

public class WorldIDToClipboard implements IMessage {

    private String worldId;

    public WorldIDToClipboard() {
    }

    public WorldIDToClipboard(String id){
        this.worldId = id;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int charsLength = buf.readInt();
        this.worldId = buf.readCharSequence(charsLength, StandardCharsets.UTF_8).toString();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        int charsLength = worldId.length();
        buf.writeInt(charsLength);
        buf.writeCharSequence(worldId, StandardCharsets.UTF_8);
    }

    public static class Handler implements IMessageHandler<WorldIDToClipboard, IMessage>{

        @Override
        public IMessage onMessage(WorldIDToClipboard message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(WorldIDToClipboard message, MessageContext ctx){
            if(ctx.side.isClient()){
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(new StringSelection(message.worldId), null);
            }
        }
    }
}
