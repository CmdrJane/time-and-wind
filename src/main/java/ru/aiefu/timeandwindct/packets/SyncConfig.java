package ru.aiefu.timeandwindct.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.apache.logging.log4j.Logger;
import ru.aiefu.timeandwindct.ITimeOperations;
import ru.aiefu.timeandwindct.TimeAndWindCT;
import ru.aiefu.timeandwindct.TimeDataStorage;
import ru.aiefu.timeandwindct.TimeTicker;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class SyncConfig implements IMessage {

    private HashMap<String, TimeDataStorage> timeMap = new HashMap<>();

    @Override
    public void fromBytes(ByteBuf buf) {
        int size = buf.readInt();
        this.timeMap = new HashMap<>();
        for(int i = 0; i < size; ++i){
            int charSeqL = buf.readInt();
            String worldId = buf.readCharSequence(charSeqL, StandardCharsets.UTF_8).toString();
            long dayD = buf.readLong();
            long nightD = buf.readLong();
            this.timeMap.put(worldId, new TimeDataStorage(dayD, nightD));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(TimeAndWindCT.timeDataMap.keySet().size());
        for(Map.Entry<String, TimeDataStorage> s : TimeAndWindCT.timeDataMap.entrySet()){
            String charSeq = s.getKey();
            buf.writeInt(charSeq.length());
            buf.writeCharSequence(charSeq, StandardCharsets.UTF_8);
            buf.writeLong(s.getValue().dayDuration);
            buf.writeLong(s.getValue().nightDuration);
        }
    }
    public static class Handler implements IMessageHandler<SyncConfig, IMessage>{

        @Override
        public IMessage onMessage(SyncConfig message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(SyncConfig message, MessageContext ctx){
            HashMap<String, TimeDataStorage> map = message.timeMap;
            TimeAndWindCT.timeDataMap = map;
            if(ctx.side.isClient()){
                WorldClient client = Minecraft.getMinecraft().world;
                if(client != null){
                    Logger logger = TimeAndWindCT.getLogger();
                    String worldId = client.provider.getDimensionType().getName();
                    if(map.containsKey(worldId)) {
                        TimeDataStorage storage = map.get(worldId);
                        ((ITimeOperations) Minecraft.getMinecraft().world).getTimeTicker().setupCustomTime(storage.dayDuration, storage.nightDuration);
                        TimeTicker timeOps = ((ITimeOperations) client).getTimeTicker();
                        logger.info("DayMod: " + timeOps.getDayMod() + " NightMod: " + timeOps.getNightMod());
                        logger.info("D RE: " + timeOps.getDayRoundingError() + " N RE: " + timeOps.getNightRoundingError());
                        logger.info("Day: " + timeOps.getDayD() + " Night: " + timeOps.getNightD());
                        logger.info(TimeAndWindCT.timeDataMap);
                        logger.info("Configuration synchronized");
                    } else ((ITimeOperations) Minecraft.getMinecraft().world).getTimeTicker().setCustomTicker(false);
                }
            }
        }
    }
}
