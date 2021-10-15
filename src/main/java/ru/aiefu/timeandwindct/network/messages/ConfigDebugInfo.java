package ru.aiefu.timeandwindct.network.messages;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.NetworkEvent;
import ru.aiefu.timeandwindct.ITimeOperations;
import ru.aiefu.timeandwindct.TimeAndWindCT;
import ru.aiefu.timeandwindct.config.TimeDataStorage;
import ru.aiefu.timeandwindct.tickers.Ticker;
import ru.aiefu.timeandwindct.tickers.TimeTicker;

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
            ClientPlayerEntity player = Minecraft.getInstance().player;
            String worldId = player.level.dimension().location().toString();
            if (TimeAndWindCT.timeDataMap.containsKey(worldId)) {
                TimeDataStorage storage = TimeAndWindCT.timeDataMap.get(worldId);
                Ticker t = ((ITimeOperations)player.level).getTimeTicker();
                if(t instanceof TimeTicker) {
                    TimeTicker ticker = (TimeTicker) t;
                    player.sendMessage(new StringTextComponent("Client config for current world: Day Duration: " + storage.dayDuration + " Night Duration: " + storage.nightDuration), Util.NIL_UUID);
                    player.sendMessage(new StringTextComponent("[C] Day Mod: " + ticker.getDayMod() + " Night Mod: " + ticker.getNightMod()), Util.NIL_UUID);
                    player.sendMessage(new StringTextComponent("[C] Day RE: " + ticker.getDayRoundingError() + " Night RE: " + ticker.getNightRoundingError()), Util.NIL_UUID);
                }
            } else
                player.sendMessage(new StringTextComponent("No Data found for current world on client side"), Util.NIL_UUID);
            context.get().setPacketHandled(true);
        }
    }
}
