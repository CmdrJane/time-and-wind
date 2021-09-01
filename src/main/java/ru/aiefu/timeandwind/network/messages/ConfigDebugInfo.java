package ru.aiefu.timeandwind.network.messages;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import ru.aiefu.timeandwind.TimeAndWind;
import ru.aiefu.timeandwind.TimeDataStorage;

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
        PlayerEntity player = context.get().getSender();
        if(player != null && player.level.isClientSide){
            String worldId = player.level.dimension().location().toString();
            if (TimeAndWind.timeDataMap.containsKey(worldId)) {
                TimeDataStorage storage = TimeAndWind.timeDataMap.get(worldId);
                player.sendMessage(new StringTextComponent("Client config for current world: Day Duration: " + storage.dayDuration + " Night Duration: " + storage.nightDuration), Util.NIL_UUID);
            } else player.sendMessage(new StringTextComponent("No Data found for current world on client side"), Util.NIL_UUID);
        }
    }
}
