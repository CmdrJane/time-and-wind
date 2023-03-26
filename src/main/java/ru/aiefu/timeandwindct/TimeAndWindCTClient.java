package ru.aiefu.timeandwindct;

import com.google.gson.Gson;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.TextComponent;
import ru.aiefu.timeandwindct.config.ModConfig;
import ru.aiefu.timeandwindct.config.SystemTimeConfig;
import ru.aiefu.timeandwindct.config.TimeDataStorage;
import ru.aiefu.timeandwindct.tickers.DefaultTicker;
import ru.aiefu.timeandwindct.tickers.SystemTimeTicker;
import ru.aiefu.timeandwindct.tickers.TimeTicker;

import java.util.HashMap;

public class TimeAndWindCTClient implements ClientModInitializer {

    /**
     * Runs the mod initializer on the client environment.
     */
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(NetworkPacketsID.SYNC_CONFIG, (client, handler, buf, responseSender) -> {
            if(buf.readableBytes() > 0 ){
                TimeAndWindCT.CONFIG = new Gson().fromJson(buf.readUtf(), ModConfig.class);
                TimeAndWindCT.systemTimeConfig = new Gson().fromJson(buf.readUtf(), SystemTimeConfig.class);

                int c1 = buf.readInt();
                HashMap<String, TimeDataStorage> map = new HashMap<>();

                for (int i = 0; i < c1; i++) {
                    String key = buf.readUtf();
                    int dayD = buf.readInt();
                    int nightD = buf.readInt();
                    map.put(key, new TimeDataStorage(dayD, nightD));
                }

                int c2 = buf.readInt();
                HashMap<String, SystemTimeConfig> sysMap = new HashMap<>();

                for (int i = 0; i < c2; i++) {
                    String key = buf.readUtf();
                    String sunrise = buf.readUtf();
                    String sunset = buf.readUtf();
                    String timezone = buf.readUtf();
                    sysMap.put(key, new SystemTimeConfig(sunrise, sunset, timezone));
                }

                TimeAndWindCT.timeDataMap = map;
                TimeAndWindCT.sysTimeMap = sysMap;

                TimeAndWindCT.LOGGER.info("[Time & Wind] Configuration synchronized");
                ClientLevel clientWorld = Minecraft.getInstance().level;
                if(clientWorld != null) {
                    String worldId = clientWorld.dimension().location().toString();
                    ITimeOperations timeOps = (ITimeOperations) clientWorld;
                    if(TimeAndWindCT.CONFIG.syncWithSystemTime){
                        if(TimeAndWindCT.CONFIG.systemTimePerDimensions && sysMap.containsKey(worldId)) {
                            timeOps.setTimeTicker(new SystemTimeTicker((ITimeOperations) clientWorld, sysMap.get(worldId)));
                        } else timeOps.setTimeTicker(new SystemTimeTicker((ITimeOperations) clientWorld, TimeAndWindCT.systemTimeConfig));
                        TimeAndWindCT.LOGGER.info("[Time & Wind] System time ticker synchronized");
                    }
                    else {
                         if (map.containsKey(worldId)) {
                            TimeDataStorage storage = map.get(worldId);
                            timeOps.setTimeTicker(new TimeTicker(storage.dayDuration, storage.nightDuration, clientWorld));
                             TimeAndWindCT.LOGGER.info("[Time & Wind] Custom time ticker for world " + worldId + " synchronized");
                        } else timeOps.setTimeTicker(new DefaultTicker());
                    }
                }
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(NetworkPacketsID.CFG_DEBUG_INFO, (client, handler, buf, responseSender) -> {
            if(client.level != null && client.player != null) {
                String worldId = client.level.dimension().location().toString();
                if (((ITimeOperations) client.level).getTimeTicker() instanceof SystemTimeTicker) {
                    return;
                }
                if(TimeAndWindCT.timeDataMap == null){
                    client.player.sendMessage(new TextComponent("[Client Side] TimeDataMap is NULL, this is a bug"), Util.NIL_UUID);
                }
                else if (TimeAndWindCT.timeDataMap.containsKey(worldId)) {
                    TimeDataStorage storage = TimeAndWindCT.timeDataMap.get(worldId);
                    client.player.sendMessage(new TextComponent("Client config for current world: Day Duration: " + storage.dayDuration + " Night Duration: " + storage.nightDuration), Util.NIL_UUID);
                } else
                    client.player.sendMessage(new TextComponent("No Data found for current world on client side"), Util.NIL_UUID);
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(NetworkPacketsID.WORLD_ID_CLIPBOARD, (client, handler, buf, responseSender) -> {

            if(buf.readableBytes() > 0 && client.player != null) {
                String string = buf.readUtf();
                client.keyboardHandler.setClipboard(string);
                client.player.sendMessage(new TextComponent("Also copied this to clipboard"), Util.NIL_UUID);
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(NetworkPacketsID.SETUP_TIME, (client, handler, buf, responseSender) -> {
            ClientLevel clientWorld = Minecraft.getInstance().level;
            if (clientWorld != null) {
                String worldId = clientWorld.dimension().location().toString();
                ITimeOperations timeOps = (ITimeOperations) clientWorld;
                if (TimeAndWindCT.CONFIG.syncWithSystemTime) {
                    if(TimeAndWindCT.CONFIG.systemTimePerDimensions && TimeAndWindCT.sysTimeMap.containsKey(worldId)) {
                        timeOps.setTimeTicker(new SystemTimeTicker((ITimeOperations) clientWorld, TimeAndWindCT.sysTimeMap.get(worldId)));
                    } else timeOps.setTimeTicker(new SystemTimeTicker((ITimeOperations) clientWorld, TimeAndWindCT.systemTimeConfig));
                }
                else if (TimeAndWindCT.timeDataMap != null && TimeAndWindCT.timeDataMap.containsKey(worldId)) {
                    TimeDataStorage storage = TimeAndWindCT.timeDataMap.get(worldId);
                    timeOps.setTimeTicker(new TimeTicker(storage.dayDuration, storage.nightDuration, clientWorld));
                } else timeOps.setTimeTicker(new DefaultTicker());
                TimeAndWindCT.LOGGER.info("[Time & Wind] Timedata reloaded on client");
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(NetworkPacketsID.NIGHT_SKIP_INFO, (client, handler, buf, responseSender) -> {
            if(buf.readableBytes() > 0) {
                ClientLevel world = Minecraft.getInstance().level;
                if(world != null){
                    ITimeOperations ops = (ITimeOperations) world;
                    ops.setSkipState(buf.readBoolean());
                    ops.setSpeed(buf.readInt());
                }
            }
        });
    }
}
