package ru.aiefu.timeandwind;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.HashMap;

public class TimeAndWindClient implements ClientModInitializer {

    /**
     * Runs the mod initializer on the client environment.
     */
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(new Identifier(TimeAndWind.MOD_ID, "sync_cycle"), (client, handler, buf, responseSender) -> {
            NbtCompound tag = buf.readNbt();
            ClientWorld world = MinecraftClient.getInstance().world;
            if(tag != null && world != null){
                IDimType dim = (IDimType) world.getDimension();
                dim.setCycleDuration(tag.getLong("dayD"), tag.getLong("nightD"));
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(new Identifier(TimeAndWind.MOD_ID, "sync_config"), (client, handler, buf, responseSender) -> {
            NbtList list = buf.readNbt().getList("tawConfig", 10);
            TimeAndWind.timeDataMap = new HashMap<>();
            for (int i = 0; i < list.size(); ++i) {
                NbtCompound tag = list.getCompound(i);
                String id = tag.getString("id");
                long dayD = tag.getLong("dayD");
                long nightD = tag.getLong("nightD");
                TimeDataStorage storage = new TimeDataStorage(dayD, nightD);
                TimeAndWind.timeDataMap.put(id, storage);
            }
            ClientWorld clientWorld = MinecraftClient.getInstance().world;
            if(clientWorld != null) {
                IDimType dim = (IDimType) clientWorld.getDimension();
                TimeDataStorage storage = TimeAndWind.timeDataMap.get(clientWorld.getRegistryKey().getValue().toString());
                dim.setCycleDuration(storage.dayDuration, storage.nightDuration);
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(new Identifier(TimeAndWind.MOD_ID, "cfg_debug_info"), (client, handler, buf, responseSender) -> {
            String worldId = client.world.getRegistryKey().getValue().toString();
            if(TimeAndWind.timeDataMap.containsKey(worldId)) {
                TimeDataStorage storage = TimeAndWind.timeDataMap.get(worldId);
                client.player.sendSystemMessage(new LiteralText("Client config for current world: Day Duration: " + storage.dayDuration + " Night Duration: " + storage.nightDuration), Util.NIL_UUID);
            } else client.player.sendSystemMessage(new LiteralText("No Data found for current world on client side"), Util.NIL_UUID);
        });
    }
}
