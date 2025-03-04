package aiefu.timeandwind.network;

import aiefu.timeandwind.TimeAndWind;
import aiefu.timeandwind.config.ModConfig;
import aiefu.timeandwind.config.SystemTimeConfig;
import aiefu.timeandwind.config.TimeDataStorage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public record FullSyncPayload(ModConfig config, SystemTimeConfig sysTimeGlobal, HashMap<String, TimeDataStorage> timeData, HashMap<String, SystemTimeConfig> sysTimeData) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<FullSyncPayload> TYPE = new CustomPacketPayload.Type<>(PacketIDs.s2c_full_sync_id);

    public static final StreamCodec<RegistryFriendlyByteBuf, FullSyncPayload> CODEC = CustomPacketPayload.codec(FullSyncPayload::write, FullSyncPayload::new);

    private FullSyncPayload(FriendlyByteBuf buf){
        this(new ModConfig(buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readInt()),
                new SystemTimeConfig(buf.readUtf(), buf.readUtf(), buf.readUtf()),
                (HashMap<String, TimeDataStorage>) buf.readMap(FriendlyByteBuf::readUtf, buffer -> new TimeDataStorage(buffer.readInt(), buffer.readInt())),
                (HashMap<String, SystemTimeConfig>) buf.readMap(FriendlyByteBuf::readUtf, buffer -> new SystemTimeConfig(buffer.readUtf(), buffer.readUtf(), buffer.readUtf())));
    }

    public void write(FriendlyByteBuf buf){
        buf.writeBoolean(config.patchSkyAngle);
        buf.writeBoolean(config.enableGlobalSystemTime);
        buf.writeBoolean(config.enableNightSkipAcceleration);
        buf.writeInt(config.accelerationSpeed);

        buf.writeUtf(sysTimeGlobal.sunrise);
        buf.writeUtf(sysTimeGlobal.sunset);
        buf.writeUtf(sysTimeGlobal.timeZone);

        buf.writeMap(timeData, FriendlyByteBuf::writeUtf, (buffer, target) -> {
          buffer.writeInt(target.dayDuration);
          buffer.writeInt(target.nightDuration);
        });

        buf.writeMap(sysTimeData, FriendlyByteBuf::writeUtf, (buffer, target) -> {
            buffer.writeUtf(target.sunrise);
            buffer.writeUtf(target.sunset);
            buffer.writeUtf(target.timeZone);
        });

    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static FullSyncPayload getPacket(){
        return new FullSyncPayload(TimeAndWind.config, TimeAndWind.sysTimeGlobal, TimeAndWind.timeConfig, TimeAndWind.sysTimeConfig);
    }
}
