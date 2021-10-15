package ru.aiefu.timeandwindct.network.messages;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import ru.aiefu.timeandwindct.ITimeOperations;
import ru.aiefu.timeandwindct.config.ModConfig;
import ru.aiefu.timeandwindct.TimeAndWindCT;
import ru.aiefu.timeandwindct.config.SystemTimeConfig;
import ru.aiefu.timeandwindct.config.TimeDataStorage;
import ru.aiefu.timeandwindct.network.NetworkHandler;
import ru.aiefu.timeandwindct.tickers.DefaultTicker;
import ru.aiefu.timeandwindct.tickers.SystemTimeTicker;
import ru.aiefu.timeandwindct.tickers.TimeTicker;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SyncConfig implements ITAWPacket{

    public SyncConfig(){
    }

    public SyncConfig(PacketBuffer buf){
        if(buf.readableBytes() > 0 ){
            boolean skyAnglePatch = buf.readBoolean();
            boolean syncWithSysTime = buf.readBoolean();
            String sunrise = buf.readUtf();
            String sunset = buf.readUtf();
            String timeZone = buf.readUtf();

            TimeAndWindCT.CONFIG = new ModConfig(skyAnglePatch, syncWithSysTime);
            TimeAndWindCT.systemTimeConfig = new SystemTimeConfig(sunrise, sunset, timeZone);

            ClientWorld clientWorld = Minecraft.getInstance().level;
            if(clientWorld != null) {
                ITimeOperations timeOps = (ITimeOperations) clientWorld;
                CompoundNBT nbtCMP = buf.readNbt();
                if(syncWithSysTime){
                    timeOps.setTimeTicker(new SystemTimeTicker((ITimeOperations) clientWorld));
                } else if (nbtCMP != null) {
                    ListNBT list = nbtCMP.getList("tawConfig", 10);
                    TimeAndWindCT.timeDataMap = new HashMap<>();
                    for (int i = 0; i < list.size(); ++i) {
                        CompoundNBT tag = list.getCompound(i);
                        String id = tag.getString("id");
                        long dayD = tag.getLong("dayD");
                        long nightD = tag.getLong("nightD");
                        TimeDataStorage storage = new TimeDataStorage(dayD, nightD);
                        TimeAndWindCT.timeDataMap.put(id, storage);
                    }
                    String worldId = clientWorld.dimension().location().toString();
                    if (TimeAndWindCT.timeDataMap.containsKey(worldId)) {
                        TimeDataStorage storage = TimeAndWindCT.timeDataMap.get(worldId);
                        timeOps.setTimeTicker(new TimeTicker(storage.dayDuration, storage.nightDuration));
                    } else timeOps.setTimeTicker(new DefaultTicker());
                } else {
                    timeOps.setTimeTicker(new DefaultTicker());
                }
            }
        }
    }
    @Override
    public void encode(PacketBuffer buf) {
        ModConfig cfg = TimeAndWindCT.CONFIG;
        SystemTimeConfig scfg = TimeAndWindCT.systemTimeConfig;
        buf.writeBoolean(cfg.patchSkyAngle);
        buf.writeBoolean(cfg.syncWithSystemTime);
        buf.writeUtf(scfg.sunrise);
        buf.writeUtf(scfg.sunset);
        buf.writeUtf(scfg.timeZone);
        ListNBT listTag = new ListNBT();
        int i = 0;
        for (Map.Entry<String, TimeDataStorage> e : TimeAndWindCT.timeDataMap.entrySet()) {
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
