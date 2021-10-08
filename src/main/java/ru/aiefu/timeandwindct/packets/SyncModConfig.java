package ru.aiefu.timeandwindct.packets;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import ru.aiefu.timeandwindct.ModConfig;
import ru.aiefu.timeandwindct.TimeAndWindCT;

public class SyncModConfig implements IMessage {
    private ModConfig config;

    @Override
    public void fromBytes(ByteBuf buf) {
        boolean skyAnglePatch = buf.readBoolean();
        boolean syncWithSystemTime = buf.readBoolean();
        config = new ModConfig(skyAnglePatch, syncWithSystemTime);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ModConfig cfg = TimeAndWindCT.CONFIG;
        buf.writeBoolean(cfg.patchSkyAngle);
        buf.writeBoolean(cfg.syncWithSystemTime);
    }
    public static class Handler implements IMessageHandler<SyncModConfig, IMessage> {

        @Override
        public IMessage onMessage(SyncModConfig message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }
        private void handle(SyncModConfig message, MessageContext ctx){
            if(ctx.side.isClient()){
                TimeAndWindCT.CONFIG = message.config;
                TimeAndWindCT.getLogger().info("[Time & Wind] Mod config synchronized");
            }
        }
    }
}
