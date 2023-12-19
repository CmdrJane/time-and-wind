package ru.aiefu.timeandwindct.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
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

        ClientWorld clientWorld = Minecraft.getInstance().level;
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
        ClientWorld clientWorld = Minecraft.getInstance().level;
        if(clientWorld != null){
            ITimeOperations ops = ((ITimeOperations)clientWorld);
            ops.time_and_wind_custom_ticker$setSkipState(state);
            ops.time_and_wind_custom_ticker$setSpeed(speed);
        }
    }

    public static void handleWorldIdToClipboardPacket(String worldId){
        Minecraft client = Minecraft.getInstance();
        client.keyboardHandler.setClipboard(worldId);
        client.player.displayClientMessage(new StringTextComponent("Also copied this to clipboard"), false);
    }

    public static void handleConfigDebugInfoPacket(){
        ClientPlayerEntity player = Minecraft.getInstance().player;
        String worldId = player.level.dimension().location().toString();
        if (TimeAndWindCT.timeDataMap.containsKey(worldId)) {
            TimeDataStorage storage = TimeAndWindCT.timeDataMap.get(worldId);
            Ticker t = ((ITimeOperations)player.level).time_and_wind_custom_ticker$getTimeTicker();
            if(t instanceof TimeTicker) {
                TimeTicker ticker = (TimeTicker) t;
                player.sendMessage(new StringTextComponent("Client config for current world: Day Duration: " + storage.dayDuration + " Night Duration: " + storage.nightDuration), Util.NIL_UUID);
                player.sendMessage(new StringTextComponent("[C] Day Mod: " + ticker.getDayMod() + " Night Mod: " + ticker.getNightMod()), Util.NIL_UUID);
                player.sendMessage(new StringTextComponent("[C] Day RE: " + ticker.getDayRoundingError() + " Night RE: " + ticker.getNightRoundingError()), Util.NIL_UUID);
            }
        } else
            player.sendMessage(new StringTextComponent("No Data found for current world on client side"), Util.NIL_UUID);
    }
}
