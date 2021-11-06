package ru.aiefu.timeandwindct;

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
                boolean skyAnglePatch = buf.readBoolean();
                boolean syncWithSysTime = buf.readBoolean();
                boolean sysTimePerDim = buf.readBoolean();
                boolean nightSkip = buf.readBoolean();
                int speed = buf.readInt();
                boolean threshold = buf.readBoolean();
                int percentage = buf.readInt();
                boolean flatS = buf.readBoolean();
                String sunrise = buf.readUtf();
                String sunset = buf.readUtf();
                String timeZone = buf.readUtf();

                HashMap<String, TimeDataStorage> map = new HashMap<>();
                HashMap<String, SystemTimeConfig> sysMap = new HashMap<>();

                int k = buf.readInt();
                for (int i = 0; i < k; i++) {
                    map.put(buf.readUtf(), new TimeDataStorage(buf.readLong(), buf.readLong()));
                }

                int r = buf.readInt();
                for (int i = 0; i < r; i++) {
                   sysMap.put(buf.readUtf(), new SystemTimeConfig(buf.readUtf(), buf.readUtf(), buf.readUtf()));
                }

                TimeAndWindCT.timeDataMap = map;
                TimeAndWindCT.sysTimeMap = sysMap;

                TimeAndWindCT.CONFIG = new ModConfig(skyAnglePatch, syncWithSysTime, sysTimePerDim, nightSkip, speed, threshold, percentage, flatS);
                TimeAndWindCT.systemTimeConfig = new SystemTimeConfig(sunrise, sunset, timeZone);
                TimeAndWindCT.LOGGER.info("[Time & Wind] Configuration synchronized");
                ClientLevel clientWorld = Minecraft.getInstance().level;
                if(clientWorld != null) {
                    String worldId = clientWorld.dimension().location().toString();
                    ITimeOperations timeOps = (ITimeOperations) clientWorld;
                    if(syncWithSysTime){
                        if(sysTimePerDim && sysMap.containsKey(worldId)) {
                            timeOps.setTimeTicker(new SystemTimeTicker((ITimeOperations) clientWorld, sysMap.get(worldId)));
                        } else timeOps.setTimeTicker(new SystemTimeTicker((ITimeOperations) clientWorld, TimeAndWindCT.systemTimeConfig));
                        TimeAndWindCT.LOGGER.info("[Time & Wind] System time ticker synchronized");
                    }
                    else {
                         if (map.containsKey(worldId)) {
                            TimeDataStorage storage = map.get(worldId);
                            timeOps.setTimeTicker(new TimeTicker(storage.dayDuration, storage.nightDuration));
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
                client.player.displayClientMessage(new TextComponent("Also copied this to clipboard"), false);
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
                    timeOps.setTimeTicker(new TimeTicker(storage.dayDuration, storage.nightDuration));
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
