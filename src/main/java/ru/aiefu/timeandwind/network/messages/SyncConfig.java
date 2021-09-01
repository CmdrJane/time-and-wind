package ru.aiefu.timeandwind.network.messages;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import ru.aiefu.timeandwind.IDimType;
import ru.aiefu.timeandwind.TimeAndWind;
import ru.aiefu.timeandwind.TimeDataStorage;
import ru.aiefu.timeandwind.network.NetworkHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SyncConfig implements ITAWPacket{

    public SyncConfig(){
    }

    public SyncConfig(PacketBuffer buf){
        if(buf.readableBytes() > 0 ){
            CompoundNBT nbtCMP = buf.readNbt();
            if(nbtCMP != null) {
                ListNBT list = nbtCMP.getList("tawConfig", 10);
                TimeAndWind.timeDataMap = new HashMap<>();
                for (int i = 0; i < list.size(); ++i) {
                    CompoundNBT tag = list.getCompound(i);
                    String id = tag.getString("id");
                    long dayD = tag.getLong("dayD");
                    long nightD = tag.getLong("nightD");
                    TimeDataStorage storage = new TimeDataStorage(dayD, nightD);
                    TimeAndWind.timeDataMap.put(id, storage);
                }
                ClientWorld clientWorld = Minecraft.getInstance().level;
                if (clientWorld != null) {
                    IDimType dim = (IDimType) clientWorld.dimensionType();
                    TimeDataStorage storage = TimeAndWind.timeDataMap.get(clientWorld.dimension().location().toString());
                    dim.setCycleDuration(storage.dayDuration, storage.nightDuration);
                }
                TimeAndWind.LOGGER.info("[Time & Wind] Configuration synchronized");
                return;
            }
        }
        TimeAndWind.LOGGER.warn("[Time & Wind] Sync failed, requesting resync");
        NetworkHandler.sendToServer(new ResyncConfig());

    }
    @Override
    public void encode(PacketBuffer buf) {
        ListNBT listTag = new ListNBT();
        int i = 0;
        for (Map.Entry<String, TimeDataStorage> e : TimeAndWind.timeDataMap.entrySet()) {
            CompoundNBT tag = new CompoundNBT();
            tag.putString("id", e.getKey());
            TimeDataStorage storage = e.getValue();
            tag.putLong("dayD", storage.dayDuration);
            tag.putLong("nightD", storage.nightDuration);
            listTag.add(i, tag);
            ++i;
        }
        CompoundNBT tag = new CompoundNBT();
        tag.put("tawConfig", listTag);
        buf.writeNbt(tag);
    }

    @Override
    public void handle(Supplier<NetworkEvent.Context> context) {
    }
}
