package ru.aiefu.timeandwindct;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Util;
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
                String sunrise = buf.readString();
                String sunset = buf.readString();
                String timeZone = buf.readString();

                HashMap<String, TimeDataStorage> map = (HashMap<String, TimeDataStorage>) buf.readMap(PacketByteBuf::readString, packetByteBuf -> new TimeDataStorage(packetByteBuf.readLong(), packetByteBuf.readLong()));
                HashMap<String, SystemTimeConfig> sysMap = (HashMap<String, SystemTimeConfig>) buf.readMap(PacketByteBuf::readString, packetByteBuf -> new SystemTimeConfig(buf.readString(), buf.readString(), buf.readString()));
                TimeAndWindCT.timeDataMap = map;
                TimeAndWindCT.sysTimeMap = sysMap;


                TimeAndWindCT.CONFIG = new ModConfig(skyAnglePatch, syncWithSysTime, sysTimePerDim, nightSkip, speed, threshold, percentage, flatS);
                TimeAndWindCT.systemTimeConfig = new SystemTimeConfig(sunrise, sunset, timeZone);
                TimeAndWindCT.LOGGER.info("[Time & Wind] Configuration synchronized");
                ClientWorld clientWorld = MinecraftClient.getInstance().world;
                if(clientWorld != null) {
                    String worldId = clientWorld.getRegistryKey().getValue().toString();
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
            if(client.world != null && client.player != null) {
                String worldId = client.world.getRegistryKey().getValue().toString();
                if (((ITimeOperations) client.world).getTimeTicker() instanceof SystemTimeTicker) {
                    return;
                }
                if(TimeAndWindCT.timeDataMap == null){
                    client.player.sendSystemMessage(new LiteralText("[Client Side] TimeDataMap is NULL, this is a bug"), Util.NIL_UUID);
                }
                else if (TimeAndWindCT.timeDataMap.containsKey(worldId)) {
                    TimeDataStorage storage = TimeAndWindCT.timeDataMap.get(worldId);
                    client.player.sendSystemMessage(new LiteralText("Client config for current world: Day Duration: " + storage.dayDuration + " Night Duration: " + storage.nightDuration), Util.NIL_UUID);
                } else
                    client.player.sendSystemMessage(new LiteralText("No Data found for current world on client side"), Util.NIL_UUID);
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(NetworkPacketsID.WORLD_ID_CLIPBOARD, (client, handler, buf, responseSender) -> {

            if(buf.readableBytes() > 0 && client.player != null) {
                String string = buf.readString();
                client.keyboard.setClipboard(string);
                client.player.sendMessage(new LiteralText("Also copied this to clipboard"), false);
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(NetworkPacketsID.SETUP_TIME, (client, handler, buf, responseSender) -> {
            ClientWorld clientWorld = MinecraftClient.getInstance().world;
            if (clientWorld != null) {
                String worldId = clientWorld.getRegistryKey().getValue().toString();
                ITimeOperations timeOps = (ITimeOperations) clientWorld;
                if (TimeAndWindCT.CONFIG.syncWithSystemTime) {
                    timeOps.setTimeTicker(new SystemTimeTicker((ITimeOperations) clientWorld, TimeAndWindCT.sysTimeMap.get(worldId)));
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
                ClientWorld world = MinecraftClient.getInstance().world;
                if(world != null){
                    ITimeOperations ops = (ITimeOperations) world;
                    ops.setSkipState(buf.readBoolean());
                    ops.setSpeed(buf.readInt());
                }
            }
        });
    }
}
