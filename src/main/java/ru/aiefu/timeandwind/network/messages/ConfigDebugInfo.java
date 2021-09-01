package ru.aiefu.timeandwind.network.messages;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.loading.FMLEnvironment;
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
        if(FMLEnvironment.dist.isClient()) {
            ClientPlayerEntity player = Minecraft.getInstance().player;
            String worldId = player.level.dimension().location().toString();
            if (TimeAndWind.timeDataMap.containsKey(worldId)) {
                TimeDataStorage storage = TimeAndWind.timeDataMap.get(worldId);
                player.sendMessage(new StringTextComponent("Client config for current world: Day Duration: " + storage.dayDuration + " Night Duration: " + storage.nightDuration), Util.NIL_UUID);
            } else
                player.sendMessage(new StringTextComponent("No Data found for current world on client side"), Util.NIL_UUID);
            context.get().setPacketHandled(true);
        }
    }
}
