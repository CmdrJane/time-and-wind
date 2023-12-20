package ru.aiefu.timeandwindct.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import ru.aiefu.timeandwindct.ConfigurationManager;
import ru.aiefu.timeandwindct.ITimeOperations;
import ru.aiefu.timeandwindct.TimeAndWindCT;
import ru.aiefu.timeandwindct.config.ModConfig;
import ru.aiefu.timeandwindct.config.SystemTimeConfig;
import ru.aiefu.timeandwindct.config.TimeDataStorage;
import ru.aiefu.timeandwindct.tickers.DefaultTicker;
import ru.aiefu.timeandwindct.tickers.SystemTimeTicker;
import ru.aiefu.timeandwindct.tickers.Ticker;
import ru.aiefu.timeandwindct.tickers.TimeTicker;

import java.util.HashMap;

public class ClientNetworkHandler {
    public static void handleConfigSyncPacket(String cfgJson, String cfgJson2, HashMap<String, TimeDataStorage> timeMap, HashMap<String, SystemTimeConfig> sysTimeMap){
        TimeAndWindCT.CONFIG = ConfigurationManager.gson_pretty.fromJson(cfgJson, ModConfig.class);
        TimeAndWindCT.systemTimeConfig = ConfigurationManager.gson_pretty.fromJson(cfgJson2, SystemTimeConfig.class);

        TimeAndWindCT.timeDataMap = timeMap;
        TimeAndWindCT.sysTimeMap = sysTimeMap;

        TimeAndWindCT.LOGGER.info("[Time & Wind] Configuration synchronized");

        ClientLevel clientWorld = Minecraft.getInstance().level;
        if(clientWorld != null) {
            String worldId = clientWorld.dimension().location().toString();
            ITimeOperations timeOps = (ITimeOperations) clientWorld;
            if (TimeAndWindCT.CONFIG.syncWithSystemTime) {
                SystemTimeConfig sysTimeStorage = sysTimeMap.get(worldId);
                if (TimeAndWindCT.CONFIG.systemTimePerDimensions && sysTimeStorage != null) {
                    timeOps.time_and_wind_custom_ticker$setTimeTicker(new SystemTimeTicker((ITimeOperations) clientWorld, sysTimeStorage));
                } else
                    timeOps.time_and_wind_custom_ticker$setTimeTicker(new SystemTimeTicker((ITimeOperations) clientWorld, TimeAndWindCT.systemTimeConfig));
                TimeAndWindCT.LOGGER.info("[Time & Wind] System time ticker synchronized");
            } else {
                TimeDataStorage storage = timeMap.get(worldId);
                if (storage != null) {
                    timeOps.time_and_wind_custom_ticker$setTimeTicker(new TimeTicker(storage.dayDuration, storage.nightDuration, clientWorld));
                    TimeAndWindCT.LOGGER.info("[Time & Wind] Custom time ticker for world " + worldId + " synchronized");
                } else timeOps.time_and_wind_custom_ticker$setTimeTicker(new DefaultTicker());
            }
        }
    }

    public static void handleNightSkipPacket(boolean state, int speed){
        ClientLevel clientWorld = Minecraft.getInstance().level;
        if(clientWorld != null){
            ITimeOperations ops = ((ITimeOperations)clientWorld);
            ops.time_and_wind_custom_ticker$setSkipState(state);
            ops.time_and_wind_custom_ticker$setSpeed(speed);
        }
    }

    public static void handleWorldIdToClipboardPacket(String worldId){
        Minecraft client = Minecraft.getInstance();
        client.keyboardHandler.setClipboard(worldId);
        client.player.displayClientMessage(Component.literal("Also copied this to clipboard"), false);
    }

    public static void handleConfigDebugInfoPacket(){
        LocalPlayer player = Minecraft.getInstance().player;
        String worldId = player.level().dimension().location().toString();
        if (TimeAndWindCT.timeDataMap.containsKey(worldId)) {
            TimeDataStorage storage = TimeAndWindCT.timeDataMap.get(worldId);
            Ticker t = ((ITimeOperations)player.level()).time_and_wind_getTimeTicker();
            if(t instanceof TimeTicker ticker) {
                player.displayClientMessage(Component.literal("Client config for current world: Day Duration: " + storage.dayDuration + " Night Duration: " + storage.nightDuration), false);
                player.displayClientMessage(Component.literal("[C] Day Mod: " + ticker.getDayMod() + " Night Mod: " + ticker.getNightMod()), false);
                player.displayClientMessage(Component.literal("[C] Day RE: " + ticker.getDayRoundingError() + " Night RE: " + ticker.getNightRoundingError()), false);
            }
            System.out.println("TEST");
        } else
            player.displayClientMessage(Component.literal("No Data found for current world on client side"), false);
    }
}
