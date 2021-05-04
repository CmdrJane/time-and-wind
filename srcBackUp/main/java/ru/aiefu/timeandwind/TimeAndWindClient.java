package ru.aiefu.timeandwind;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;

public class TimeAndWindClient implements ClientModInitializer {

    /**
     * Runs the mod initializer on the client environment.
     */
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(new Identifier(TimeAndWind.MOD_ID, "sync_cycle"), (client, handler, buf, responseSender) -> {
            CompoundTag tag = buf.readCompoundTag();
            ClientWorld world = MinecraftClient.getInstance().world;
            if(tag != null && world != null){
                IDimType dim = (IDimType) world.getDimension();
                dim.setCycleDuration(tag.getLong("dayD"), tag.getLong("nightD"));
            }
        });
    }
}
