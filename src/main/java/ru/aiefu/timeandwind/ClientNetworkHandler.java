package ru.aiefu.timeandwind;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import java.util.HashMap;

public class ClientNetworkHandler {

    /**
     * Runs the mod initializer on the client environment.
     */
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(new ResourceLocation(TimeAndWind.MOD_ID, "sync_cycle"), (client, handler, buf, responseSender) -> {
            CompoundTag tag = buf.readNbt();
            ClientLevel world = Minecraft.getInstance().level;
            if(tag != null && world != null){
                IDimType dim = (IDimType) world.dimensionType();
                dim.setCycleDuration(tag.getLong("dayD"), tag.getLong("nightD"));
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(new ResourceLocation(TimeAndWind.MOD_ID, "sync_config"), (client, handler, buf, responseSender) -> {
            if(buf.readableBytes() > 0 ){
                CompoundTag nbtCMP = buf.readNbt();
                if(nbtCMP != null) {
                    ListTag list = nbtCMP.getList("tawConfig", 10);
                    TimeAndWind.timeDataMap = new HashMap<>();
                    for (int i = 0; i < list.size(); ++i) {
                        CompoundTag tag = list.getCompound(i);
                        String id = tag.getString("id");
                        long dayD = tag.getLong("dayD");
                        long nightD = tag.getLong("nightD");
                        TimeDataStorage storage = new TimeDataStorage(dayD, nightD);
                        TimeAndWind.timeDataMap.put(id, storage);
                    }
                    ClientLevel clientWorld = Minecraft.getInstance().level;
                    if (clientWorld != null) {
                        IDimType dim = (IDimType) clientWorld.dimensionType();
                        TimeDataStorage storage = TimeAndWind.timeDataMap.get(clientWorld.dimension().location().toString());
                        dim.setCycleDuration(storage.dayDuration, storage.nightDuration);
                    }
                    TimeAndWind.LOGGER.info("[Time & Wind] Configuration synchronized");
                    return;
                }
            }
            TimeAndWind.LOGGER.warn("[Time & Wind] Sync failed, requesting resync");
            ClientPlayNetworking.send(new ResourceLocation(TimeAndWind.MOD_ID, "request_resync"), new FriendlyByteBuf(Unpooled.buffer()));
        });
        ClientPlayNetworking.registerGlobalReceiver(new ResourceLocation(TimeAndWind.MOD_ID, "cfg_debug_info"), (client, handler, buf, responseSender) -> {
            try {
                String worldId = client.level.dimension().location().toString();
                if (TimeAndWind.timeDataMap.containsKey(worldId)) {
                    TimeDataStorage storage = TimeAndWind.timeDataMap.get(worldId);
                    client.player.sendMessage(new TextComponent("Client config for current world: Day Duration: " + storage.dayDuration + " Night Duration: " + storage.nightDuration), Util.NIL_UUID);
                } else
                    client.player.sendMessage(new TextComponent("No Data found for current world on client side"), Util.NIL_UUID);
            } catch (NullPointerException e){
                e.printStackTrace();
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(new ResourceLocation(TimeAndWind.MOD_ID, "world_id_clipboard"), (client, handler, buf, responseSender) -> {
            try{
                if(buf.readableBytes() > 0) {
                    String string = buf.readUtf();
                    client.keyboardHandler.setClipboard(string);
                    client.player.displayClientMessage(new TextComponent("Also copied this to clipboard"), false);
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        });
    }
}
