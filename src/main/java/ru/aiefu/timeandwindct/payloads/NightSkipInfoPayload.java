package ru.aiefu.timeandwindct.payloads;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import ru.aiefu.timeandwindct.TimeAndWindCT;

public record NightSkipInfoPayload(boolean skipState, int accelerationSpeed) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<NightSkipInfoPayload> ID = new CustomPacketPayload.Type<>(
        ResourceLocation.fromNamespaceAndPath(TimeAndWindCT.MOD_ID, "nskip_info")
    );

    public static final StreamCodec<FriendlyByteBuf, NightSkipInfoPayload> CODEC = CustomPacketPayload.codec(
        NightSkipInfoPayload::write,
        NightSkipInfoPayload::new
    );

    private NightSkipInfoPayload(FriendlyByteBuf buf) {
        this(buf.readBoolean(), buf.readInt());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(skipState);
        buf.writeInt(accelerationSpeed);
    }

    @Override
    @NotNull
    public Type<NightSkipInfoPayload> type() {
        return ID;
    }
}
