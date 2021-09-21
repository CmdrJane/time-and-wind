package ru.aiefu.timeandwindct.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import ru.aiefu.timeandwindct.TimeAndWindCT;
import ru.aiefu.timeandwindct.TimeDataStorage;

public class CheckConfig implements IMessage {
    @Override
    public void fromBytes(ByteBuf buf) {

    }

    @Override
    public void toBytes(ByteBuf buf) {

    }
    public static class Handler implements IMessageHandler<CheckConfig, IMessage>{

        @Override
        public IMessage onMessage(CheckConfig message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(CheckConfig message, MessageContext ctx){
            if(ctx.side.isClient()){
                EntityPlayerSP player = Minecraft.getMinecraft().player;
                String worldId = Minecraft.getMinecraft().world.provider.getDimensionType().getName();
                if(TimeAndWindCT.timeDataMap.containsKey(worldId)){
                    TimeDataStorage storage = TimeAndWindCT.timeDataMap.get(worldId);
                    player.sendStatusMessage( new TextComponentString("Client config: Day Duration: "
                            + storage.dayDuration + " Night Duration: " + storage.nightDuration), false);
                } else player.sendStatusMessage(new TextComponentString("Unable to find configuration for this world"), false);
            }
        }
    }
}
