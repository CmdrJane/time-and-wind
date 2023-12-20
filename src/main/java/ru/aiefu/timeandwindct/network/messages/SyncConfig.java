package ru.aiefu.timeandwindct.network.messages;

import com.google.common.collect.Maps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import ru.aiefu.timeandwindct.ConfigurationManager;
import ru.aiefu.timeandwindct.TimeAndWindCT;
import ru.aiefu.timeandwindct.config.SystemTimeConfig;
import ru.aiefu.timeandwindct.config.TimeDataStorage;
import ru.aiefu.timeandwindct.network.ClientNetworkHandler;

import java.util.HashMap;

public class SyncConfig {

    protected String cfgJson;
    protected String sysTimeJson;

    protected HashMap<String, TimeDataStorage> timeMap;
    protected HashMap<String, SystemTimeConfig> sysTimeMap;

    public SyncConfig(){
    }

    public SyncConfig(String cfgJson, String sysTimeJson, HashMap<String, TimeDataStorage> timeMap, HashMap<String, SystemTimeConfig> sysTimeMap) {
        this.cfgJson = cfgJson;
        this.sysTimeJson = sysTimeJson;
        this.timeMap = timeMap;
        this.sysTimeMap = sysTimeMap;
    }

    public static SyncConfig decode(FriendlyByteBuf buf){
        String cfgJson = buf.readUtf();
        String sysTimeJson = buf.readUtf();

        int i = buf.readVarInt();
        HashMap<String, TimeDataStorage> timeMap = Maps.newHashMapWithExpectedSize(i);
        for (int j = 0; j < i; j++) {
            timeMap.put(buf.readUtf(), new TimeDataStorage(buf.readVarInt(), buf.readVarInt()));
        }
        i = buf.readVarInt();
        HashMap<String, SystemTimeConfig> sysTimeMap = Maps.newHashMapWithExpectedSize(i);
        for (int j = 0; j < i; j++) {
            sysTimeMap.put(buf.readUtf(), new SystemTimeConfig(buf.readUtf(), buf.readUtf(), buf.readUtf()));
        }
        return new SyncConfig(cfgJson, sysTimeJson, timeMap, sysTimeMap);
    }

    public void encode(FriendlyByteBuf buf) {

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

    public void handle(CustomPayloadEvent.Context ctx) {
        if(FMLEnvironment.dist.isClient()){
            ClientNetworkHandler.handleConfigSyncPacket(cfgJson, sysTimeJson, timeMap, sysTimeMap);
        }
    }
}
