package ru.aiefu.timeandwindct.network.messages;

import com.google.common.collect.Maps;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.NetworkEvent;
import ru.aiefu.timeandwindct.ConfigurationManager;
import ru.aiefu.timeandwindct.TimeAndWindCT;
import ru.aiefu.timeandwindct.config.SystemTimeConfig;
import ru.aiefu.timeandwindct.config.TimeDataStorage;
import ru.aiefu.timeandwindct.network.ClientNetworkHandler;

import java.util.HashMap;
import java.util.function.Supplier;

public class SyncConfig implements ITAWPacket{

    protected String cfgJson;
    protected String sysTimeJson;

    protected HashMap<String, TimeDataStorage> timeMap;
    protected HashMap<String, SystemTimeConfig> sysTimeMap;

    public SyncConfig(){

    }

    public SyncConfig(PacketBuffer buf){
        if(buf.readableBytes() > 0 ){
            this.cfgJson = buf.readUtf();
            this.sysTimeJson = buf.readUtf();

            int i = buf.readVarInt();
            this.timeMap = Maps.newHashMapWithExpectedSize(i);
            for (int j = 0; j < i; j++) {
                this.timeMap.put(buf.readUtf(), new TimeDataStorage(buf.readVarInt(), buf.readVarInt()));
            }
            i = buf.readVarInt();
            this.sysTimeMap = Maps.newHashMapWithExpectedSize(i);
            for (int j = 0; j < i; j++) {
                this.sysTimeMap.put(buf.readUtf(), new SystemTimeConfig(buf.readUtf(), buf.readUtf(), buf.readUtf()));
            }
        }
    }
    @Override
    public void encode(PacketBuffer buf) {

        String cfgJson = ConfigurationManager.gson_pretty.toJson(TimeAndWindCT.CONFIG);
        String cfgsJson = ConfigurationManager.gson_pretty.toJson(TimeAndWindCT.systemTimeConfig);

        buf.writeUtf(cfgJson);
        buf.writeUtf(cfgsJson);

        HashMap<String, TimeDataStorage> timeMap = TimeAndWindCT.timeDataMap;
        buf.writeVarInt(timeMap.size());
        timeMap.forEach((key, value) -> {
            buf.writeUtf(key);
            buf.writeVarInt(value.dayDuration);
            buf.writeVarInt(value.nightDuration);
        });

        HashMap<String, SystemTimeConfig> sysTime = TimeAndWindCT.sysTimeMap;
        buf.writeVarInt(sysTime.size());
        sysTime.forEach((key, value) -> {
            buf.writeUtf(key);
            buf.writeUtf(value.sunrise);
            buf.writeUtf(value.sunset);
            buf.writeUtf(value.timeZone);
        });
    }

    @Override
    public void handle(Supplier<NetworkEvent.Context> context) {
        if(FMLEnvironment.dist.isClient()){
            context.get().enqueueWork(() -> ClientNetworkHandler.handleConfigSyncPacket(cfgJson, sysTimeJson, timeMap, sysTimeMap));
            context.get().setPacketHandled(true);
        }
    }
}
