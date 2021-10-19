package ru.aiefu.timeandwindct.network.messages;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import ru.aiefu.timeandwindct.ITimeOperations;

import java.util.function.Supplier;

public class NightSkip implements ITAWPacket{

    private boolean state;
    private int speed;


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
        ClientWorld clientWorld = Minecraft.getInstance().level;
        if(clientWorld != null){
           ITimeOperations ops = ((ITimeOperations)clientWorld);
           ops.setSkipState(state);
           ops.setSpeed(speed);
        }
    }
}
