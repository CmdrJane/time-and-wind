package ru.aiefu.timeandwindct.payloads;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import ru.aiefu.timeandwindct.TimeAndWindCT;

public record CfgDebugInfoPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<CfgDebugInfoPayload> ID = new CustomPacketPayload.Type<>(
        ResourceLocation.fromNamespaceAndPath(TimeAndWindCT.MOD_ID, "cfg_debug_info")
    );

    public static final StreamCodec<Object, CfgDebugInfoPayload> CODEC = StreamCodec.unit(new CfgDebugInfoPayload());

    @Override
    @NotNull
    public Type<CfgDebugInfoPayload> type() {
        return ID;
    }
}
