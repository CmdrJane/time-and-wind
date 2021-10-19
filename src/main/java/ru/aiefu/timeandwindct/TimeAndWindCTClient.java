package ru.aiefu.timeandwindct;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
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
                boolean nightSkip = buf.readBoolean();
                int speed = buf.readInt();
                boolean threshold = buf.readBoolean();
                int percentage = buf.readInt();
                boolean flatS = buf.readBoolean();
                String sunrise = buf.readString();
                String sunset = buf.readString();
                String timeZone = buf.readString();
                NbtCompound nbtCMP = buf.readNbt();

                TimeAndWindCT.CONFIG = new ModConfig(skyAnglePatch, syncWithSysTime, nightSkip, speed, threshold, percentage, flatS);
                TimeAndWindCT.systemTimeConfig = new SystemTimeConfig(sunrise, sunset, timeZone);
                ClientWorld clientWorld = MinecraftClient.getInstance().world;
                if(clientWorld != null) {
                    ITimeOperations timeOps = (ITimeOperations) clientWorld;
                    if(syncWithSysTime){
                        timeOps.setTimeTicker(new SystemTimeTicker((ITimeOperations) clientWorld));
                    }
                    else if (nbtCMP != null) {
                        NbtList list = nbtCMP.getList("tawConfig", 10);
                        TimeAndWindCT.timeDataMap = new HashMap<>();
                        for (int i = 0; i < list.size(); ++i) {
                            NbtCompound tag = list.getCompound(i);
                            String id = tag.getString("id");
                            long dayD = tag.getLong("dayD");
                            long nightD = tag.getLong("nightD");
                            TimeDataStorage storage = new TimeDataStorage(dayD, nightD);
                            TimeAndWindCT.timeDataMap.put(id, storage);
                        }
                        String worldId = clientWorld.getRegistryKey().getValue().toString();
                         if (TimeAndWindCT.timeDataMap.containsKey(worldId)) {
                            TimeDataStorage storage = TimeAndWindCT.timeDataMap.get(worldId);
                            timeOps.setTimeTicker(new TimeTicker(storage.dayDuration, storage.nightDuration));
                        } else timeOps.setTimeTicker(new DefaultTicker());
                    } else timeOps.setTimeTicker(new DefaultTicker());
                    TimeAndWindCT.LOGGER.info("[Time & Wind] Configuration synchronized");
                }
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(NetworkPacketsID.CFG_DEBUG_INFO, (client, handler, buf, responseSender) -> {
            try {
                String worldId = client.world.getRegistryKey().getValue().toString();
                if(((ITimeOperations)client.world).getTimeTicker() instanceof SystemTimeTicker){
                    return;
                }
                if (TimeAndWindCT.timeDataMap.containsKey(worldId)) {
                    TimeDataStorage storage = TimeAndWindCT.timeDataMap.get(worldId);
                    client.player.sendSystemMessage(new LiteralText("Client config for current world: Day Duration: " + storage.dayDuration + " Night Duration: " + storage.nightDuration), Util.NIL_UUID);
                } else
                    client.player.sendSystemMessage(new LiteralText("No Data found for current world on client side"), Util.NIL_UUID);
            } catch (NullPointerException e){
                e.printStackTrace();
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(NetworkPacketsID.WORLD_ID_CLIPBOARD, (client, handler, buf, responseSender) -> {
            try{
                if(buf.readableBytes() > 0) {
                    String string = buf.readString();
                    client.keyboard.setClipboard(string);
                    client.player.sendMessage(new LiteralText("Also copied this to clipboard"), false);
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(NetworkPacketsID.SETUP_TIME, (client, handler, buf, responseSender) -> {
            ClientWorld clientWorld = MinecraftClient.getInstance().world;
            if (clientWorld != null) {
                String worldId = clientWorld.getRegistryKey().getValue().toString();
                if (TimeAndWindCT.CONFIG.syncWithSystemTime) {
                    ((ITimeOperations)clientWorld).setTimeTicker(new SystemTimeTicker((ITimeOperations) clientWorld));
                }
                else if (TimeAndWindCT.timeDataMap != null && TimeAndWindCT.timeDataMap.containsKey(worldId)) {
                    TimeDataStorage storage = TimeAndWindCT.timeDataMap.get(worldId);
                    ((ITimeOperations)clientWorld).setTimeTicker(new TimeTicker(storage.dayDuration, storage.nightDuration));
                } else ((ITimeOperations)clientWorld).setTimeTicker(new DefaultTicker());
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
