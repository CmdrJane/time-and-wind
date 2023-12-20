package ru.aiefu.timeandwindct.network.messages;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import ru.aiefu.timeandwindct.network.ClientNetworkHandler;

public class NightSkip{

    private final boolean state;
    private final int speed;


    public NightSkip(boolean state, int speed){
        this.state = state;
        this.speed = speed;
    }

    public static NightSkip decode(FriendlyByteBuf buf){
        return new NightSkip(buf.readBoolean(), buf.readInt());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(state);
        buf.writeInt(speed);
    }

    public void handle(CustomPayloadEvent.Context context) {
        if(FMLEnvironment.dist.isClient()){
            ClientNetworkHandler.handleNightSkipPacket(state, speed);
        }
    }
}
