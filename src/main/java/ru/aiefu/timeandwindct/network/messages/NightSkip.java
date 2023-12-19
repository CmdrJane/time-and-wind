package ru.aiefu.timeandwindct.network.messages;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.NetworkEvent;
import ru.aiefu.timeandwindct.network.ClientNetworkHandler;

import java.util.function.Supplier;

public class NightSkip implements ITAWPacket{

    private final boolean state;
    private final int speed;


    public NightSkip(boolean state, int speed){
        this.state = state;
        this.speed = speed;
    }

    public NightSkip(PacketBuffer buf){
        this.state = buf.readBoolean();
        this.speed = buf.readInt();
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeBoolean(state);
        buf.writeInt(speed);
    }

    @Override
    public void handle(Supplier<NetworkEvent.Context> context) {
        if(FMLEnvironment.dist.isClient()){
            context.get().enqueueWork(() -> ClientNetworkHandler.handleNightSkipPacket(state, speed));
            context.get().setPacketHandled(true);
        }
    }
}
