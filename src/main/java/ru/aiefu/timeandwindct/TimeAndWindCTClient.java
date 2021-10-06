package ru.aiefu.timeandwindct;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Util;

import java.util.HashMap;

public class TimeAndWindCTClient implements ClientModInitializer {

    /**
     * Runs the mod initializer on the client environment.
     */
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(NetworkPacketsID.SYNC_CONFIG, (client, handler, buf, responseSender) -> {
            if(buf.readableBytes() > 0 ){
                NbtCompound nbtCMP = buf.readNbt();
                if(nbtCMP != null) {
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
                    ClientWorld clientWorld = MinecraftClient.getInstance().world;
                    if (clientWorld != null) {
                        String worldId = clientWorld.getRegistryKey().getValue().toString();
                        if(TimeAndWindCT.timeDataMap.containsKey(worldId)) {
                            TimeDataStorage storage = TimeAndWindCT.timeDataMap.get(worldId);
                            ((ITimeOperations) MinecraftClient.getInstance().world).getTimeTicker().setupCustomTime(storage.dayDuration, storage.nightDuration);
                        } else ((ITimeOperations) MinecraftClient.getInstance().world).getTimeTicker().setCustomTicker(false);
                    }
                    TimeAndWindCT.LOGGER.info("[Time & Wind] Configuration synchronized");
                }
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(NetworkPacketsID.CFG_DEBUG_INFO, (client, handler, buf, responseSender) -> {
            try {
                String worldId = client.world.getRegistryKey().getValue().toString();
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
            if (clientWorld != null && TimeAndWindCT.timeDataMap != null) {
                String worldId = clientWorld.getRegistryKey().getValue().toString();
                if(TimeAndWindCT.timeDataMap.containsKey(worldId)) {
                    TimeDataStorage storage = TimeAndWindCT.timeDataMap.get(worldId);
                    ((ITimeOperations) MinecraftClient.getInstance().world).getTimeTicker().setupCustomTime(storage.dayDuration, storage.nightDuration);
                } else ((ITimeOperations) MinecraftClient.getInstance().world).getTimeTicker().setCustomTicker(false);
            }
            TimeAndWindCT.LOGGER.info("[Time & Wind] Timedata reloaded on client");
        });
    }
}
