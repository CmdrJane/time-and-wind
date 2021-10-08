package ru.aiefu.timeandwindct.network.messages;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import ru.aiefu.timeandwindct.ModConfig;
import ru.aiefu.timeandwindct.TimeAndWindCT;

import java.util.function.Supplier;

public class SyncModConfig implements ITAWPacket{
    private ModConfig config;

    public SyncModConfig(){

    }

    public SyncModConfig (PacketBuffer buf){
        if(buf.readableBytes() > 0 ){
            boolean patchAngle = buf.readBoolean();
            boolean syncWithSystemT = buf.readBoolean();
            config = new ModConfig(patchAngle, syncWithSystemT);
        }
    }

    @Override
    public void encode(PacketBuffer buf) {
        ModConfig cfg = TimeAndWindCT.CONFIG;
        buf.writeBoolean(cfg.patchSkyAngle);
        buf.writeBoolean(cfg.syncWithSystemTime);
    }

    @Override
    public void handle(Supplier<NetworkEvent.Context> context) {
        if(config != null){
            TimeAndWindCT.CONFIG = config;
            TimeAndWindCT.LOGGER.info("Mod config synchronized");
        }
    }
}
