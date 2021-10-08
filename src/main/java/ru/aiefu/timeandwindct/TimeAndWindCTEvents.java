package ru.aiefu.timeandwindct;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import ru.aiefu.timeandwindct.network.NetworkHandler;
import ru.aiefu.timeandwindct.network.messages.SyncConfig;
import ru.aiefu.timeandwindct.network.messages.SyncModConfig;

public class TimeAndWindCTEvents {
    @SubscribeEvent
    public void playerJoin(PlayerEvent.PlayerLoggedInEvent event){
        NetworkHandler.sendTo(new SyncConfig(), (ServerPlayerEntity) event.getPlayer());
        NetworkHandler.sendTo(new SyncModConfig(), (ServerPlayerEntity) event.getPlayer());
        TimeAndWindCT.LOGGER.info("[Time & Wind] Sending configuration to player");
    }
    @SubscribeEvent
    public void serverStarting(FMLServerAboutToStartEvent event){
        TimeAndWindCT.LOGGER.info("Reading time cfg...");
        IOManager.readTimeData();
    }
    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event){
        TAWCommands.registerCommands(event.getDispatcher());
    }
}
