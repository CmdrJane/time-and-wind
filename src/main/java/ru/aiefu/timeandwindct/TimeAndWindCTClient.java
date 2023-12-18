package ru.aiefu.timeandwindct;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
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
                TimeAndWindCT.CONFIG = ConfigurationManager.gson_pretty.fromJson(buf.readUtf(), ModConfig.class);
                TimeAndWindCT.systemTimeConfig = ConfigurationManager.gson_pretty.fromJson(buf.readUtf(), SystemTimeConfig.class);

                HashMap<String, TimeDataStorage> map = (HashMap<String, TimeDataStorage>) buf.readMap(FriendlyByteBuf::readUtf, packetByteBuf -> new TimeDataStorage(packetByteBuf.readInt(), packetByteBuf.readInt()));
                HashMap<String, SystemTimeConfig> sysMap = (HashMap<String, SystemTimeConfig>) buf.readMap(FriendlyByteBuf::readUtf, packetByteBuf -> new SystemTimeConfig(buf.readUtf(), buf.readUtf(), buf.readUtf()));
                TimeAndWindCT.timeDataMap = map;
                TimeAndWindCT.sysTimeMap = sysMap;

                TimeAndWindCT.LOGGER.info("[Time & Wind] Configuration synchronized");
                ClientLevel clientWorld = Minecraft.getInstance().level;
                if(clientWorld != null) {
                    String worldId = clientWorld.dimension().location().toString();
                    ITimeOperations timeOps = (ITimeOperations) clientWorld;
                    if(TimeAndWindCT.CONFIG.syncWithSystemTime){
                        if(TimeAndWindCT.CONFIG.systemTimePerDimensions && sysMap.containsKey(worldId)) {
                            timeOps.time_and_wind_custom_ticker$setTimeTicker(new SystemTimeTicker((ITimeOperations) clientWorld, sysMap.get(worldId)));
                        } else timeOps.time_and_wind_custom_ticker$setTimeTicker(new SystemTimeTicker((ITimeOperations) clientWorld, TimeAndWindCT.systemTimeConfig));
                        TimeAndWindCT.LOGGER.info("[Time & Wind] System time ticker synchronized");
                    }
                    else {
                         if (map.containsKey(worldId)) {
                            TimeDataStorage storage = map.get(worldId);
                            timeOps.time_and_wind_custom_ticker$setTimeTicker(new TimeTicker(storage.dayDuration, storage.nightDuration, clientWorld));
                             TimeAndWindCT.LOGGER.info("[Time & Wind] Custom time ticker for world " + worldId + " synchronized");
                        } else timeOps.time_and_wind_custom_ticker$setTimeTicker(new DefaultTicker());
                    }
                }
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(NetworkPacketsID.CFG_DEBUG_INFO, (client, handler, buf, responseSender) -> {
            if(client.level != null && client.player != null) {
                String worldId = client.level.dimension().location().toString();
                if (((ITimeOperations) client.level).time_and_wind_custom_ticker$getTimeTicker() instanceof SystemTimeTicker) {
                    return;
                }
                if(TimeAndWindCT.timeDataMap == null){
                    client.player.sendSystemMessage(Component.literal("[Client Side] TimeDataMap is NULL, this is a bug"));
                }
                else if (TimeAndWindCT.timeDataMap.containsKey(worldId)) {
                    TimeDataStorage storage = TimeAndWindCT.timeDataMap.get(worldId);
                    client.player.sendSystemMessage(Component.literal("Client config for current world: Day Duration: " + storage.dayDuration + " Night Duration: " + storage.nightDuration));
                } else
                    client.player.sendSystemMessage(Component.literal("No Data found for current world on client side"));
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(NetworkPacketsID.WORLD_ID_CLIPBOARD, (client, handler, buf, responseSender) -> {

            if(buf.readableBytes() > 0 && client.player != null) {
                String string = buf.readUtf();
                client.keyboardHandler.setClipboard(string);
                client.player.displayClientMessage(Component.literal("Also copied this to clipboard"), false);
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(NetworkPacketsID.SETUP_TIME, (client, handler, buf, responseSender) -> {
            ClientLevel clientWorld = Minecraft.getInstance().level;
            if (clientWorld != null) {
                String worldId = clientWorld.dimension().location().toString();
                ITimeOperations timeOps = (ITimeOperations) clientWorld;
                if (TimeAndWindCT.CONFIG.syncWithSystemTime) {
                    if(TimeAndWindCT.CONFIG.systemTimePerDimensions && TimeAndWindCT.sysTimeMap.containsKey(worldId)) {
                        timeOps.time_and_wind_custom_ticker$setTimeTicker(new SystemTimeTicker((ITimeOperations) clientWorld, TimeAndWindCT.sysTimeMap.get(worldId)));
                    } else timeOps.time_and_wind_custom_ticker$setTimeTicker(new SystemTimeTicker((ITimeOperations) clientWorld, TimeAndWindCT.systemTimeConfig));
                }
                else if (TimeAndWindCT.timeDataMap != null && TimeAndWindCT.timeDataMap.containsKey(worldId)) {
                    TimeDataStorage storage = TimeAndWindCT.timeDataMap.get(worldId);
                    timeOps.time_and_wind_custom_ticker$setTimeTicker(new TimeTicker(storage.dayDuration, storage.nightDuration, clientWorld));
                } else timeOps.time_and_wind_custom_ticker$setTimeTicker(new DefaultTicker());
                TimeAndWindCT.LOGGER.info("[Time & Wind] Timedata reloaded on client");
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(NetworkPacketsID.NIGHT_SKIP_INFO, (client, handler, buf, responseSender) -> {
            if(buf.readableBytes() > 0) {
                ClientLevel world = Minecraft.getInstance().level;
                if(world != null){
                    ITimeOperations ops = (ITimeOperations) world;
                    ops.time_and_wind_custom_ticker$setSkipState(buf.readBoolean());
                    ops.time_and_wind_custom_ticker$setSpeed(buf.readInt());
                }
            }
        });
    }
}
