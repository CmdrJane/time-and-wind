package aiefu.timeandwind.network;

import aiefu.timeandwind.TimeAndWind;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.resources.ResourceLocation;

public class PacketIDs {
    public static final ResourceLocation s2c_full_sync_id = ResourceLocation.fromNamespaceAndPath(TimeAndWind.MOD_ID, "s2c_full_sync");

    public static void registerPackets(){
        PayloadTypeRegistry.playS2C().register(FullSyncPayload.TYPE, FullSyncPayload.CODEC);
    }
}
