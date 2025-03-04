package aiefu.timeandwind;

import aiefu.timeandwind.network.handlers.ClientNetworkHandler;
import net.fabricmc.api.ClientModInitializer;

public class TimeAndWindClient implements ClientModInitializer {


    @Override
    public void onInitializeClient() {
        ClientNetworkHandler.registerGlobalReceivers();
    }
}
