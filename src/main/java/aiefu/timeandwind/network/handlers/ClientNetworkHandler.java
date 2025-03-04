package aiefu.timeandwind.network.handlers;

import aiefu.timeandwind.TimeAndWind;
import aiefu.timeandwind.network.FullSyncPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class ClientNetworkHandler {

    public static void registerGlobalReceivers(){
        ClientPlayNetworking.registerGlobalReceiver(FullSyncPayload.TYPE, (p, ctx) -> {
            ctx.client().execute(() -> {
                TimeAndWind.config = p.config();
                TimeAndWind.sysTimeGlobal = p.sysTimeGlobal();
                TimeAndWind.timeConfig = p.timeData();
                TimeAndWind.sysTimeConfig = p.sysTimeData();
                TimeAndWind.reloadTickers();
            });
        });
    }
}
