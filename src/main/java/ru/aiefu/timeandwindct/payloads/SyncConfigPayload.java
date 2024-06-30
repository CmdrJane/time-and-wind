package ru.aiefu.timeandwindct.payloads;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import ru.aiefu.timeandwindct.ConfigurationManager;
import ru.aiefu.timeandwindct.TimeAndWindCT;
import ru.aiefu.timeandwindct.config.ModConfig;
import ru.aiefu.timeandwindct.config.SystemTimeConfig;
import ru.aiefu.timeandwindct.config.TimeDataStorage;

import java.util.HashMap;

public record SyncConfigPayload(
    ModConfig modConfig,
    SystemTimeConfig systemTimeConfig,
    HashMap<String, TimeDataStorage> timeDataMap,
    HashMap<String, SystemTimeConfig> sysTimeMap
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncConfigPayload> ID = new CustomPacketPayload.Type<>(
        ResourceLocation.fromNamespaceAndPath(TimeAndWindCT.MOD_ID, "sync_config")
    );

    public static final StreamCodec<FriendlyByteBuf, SyncConfigPayload> CODEC = CustomPacketPayload.codec(
        SyncConfigPayload::write,
        SyncConfigPayload::new
    );

    private SyncConfigPayload(FriendlyByteBuf buf) {
        this(
            ConfigurationManager.gson_pretty.fromJson(buf.readUtf(), ModConfig.class),
            ConfigurationManager.gson_pretty.fromJson(buf.readUtf(), SystemTimeConfig.class),
            (HashMap<String, TimeDataStorage>) buf.readMap(
                FriendlyByteBuf::readUtf,
                packetByteBuf -> new TimeDataStorage(packetByteBuf.readInt(), packetByteBuf.readInt())
            ),
            (HashMap<String, SystemTimeConfig>) buf.readMap(
                FriendlyByteBuf::readUtf,
                packetByteBuf -> new SystemTimeConfig(buf.readUtf(), buf.readUtf(), buf.readUtf())
            )
        );
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(ConfigurationManager.gson_pretty.toJson(modConfig));
        buf.writeUtf(ConfigurationManager.gson_pretty.toJson(systemTimeConfig));

        buf.writeMap(timeDataMap, FriendlyByteBuf::writeUtf, (packetByteBuf, timeDataStorage) -> {
            packetByteBuf.writeInt(timeDataStorage.dayDuration);
            packetByteBuf.writeInt(timeDataStorage.nightDuration);
        });

        buf.writeMap(sysTimeMap, FriendlyByteBuf::writeUtf, (packetByteBuf, systemTimeConfig1) -> {
            packetByteBuf.writeUtf(systemTimeConfig1.sunrise);
            packetByteBuf.writeUtf(systemTimeConfig1.sunset);
            packetByteBuf.writeUtf(systemTimeConfig1.timeZone);
        });
    }

    @Override
    public @NotNull Type<SyncConfigPayload> type() {
        return ID;
    }
}
