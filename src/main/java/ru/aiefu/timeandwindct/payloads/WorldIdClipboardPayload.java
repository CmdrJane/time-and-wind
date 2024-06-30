package ru.aiefu.timeandwindct.payloads;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import ru.aiefu.timeandwindct.TimeAndWindCT;

public record WorldIdClipboardPayload(String worldId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<WorldIdClipboardPayload> ID = new CustomPacketPayload.Type<>(
        ResourceLocation.fromNamespaceAndPath(TimeAndWindCT.MOD_ID, "world_id_clipboard")
    );

    public static final StreamCodec<FriendlyByteBuf, WorldIdClipboardPayload> CODEC = CustomPacketPayload.codec(
        WorldIdClipboardPayload::write,
        WorldIdClipboardPayload::new
    );

    private WorldIdClipboardPayload(FriendlyByteBuf buf) {
        this(buf.readUtf());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(worldId);
    }

    @Override
    @NotNull
    public Type<WorldIdClipboardPayload> type() {
        return ID;
    }
}
