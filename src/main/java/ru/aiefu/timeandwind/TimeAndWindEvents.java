package ru.aiefu.timeandwind;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import ru.aiefu.timeandwind.network.NetworkHandler;
import ru.aiefu.timeandwind.network.messages.SyncConfig;

public class TimeAndWindEvents {
    @SubscribeEvent
    public void playerJoin(PlayerEvent.PlayerLoggedInEvent event){
        NetworkHandler.sendTo(new SyncConfig(), (ServerPlayerEntity) event.getPlayer());
        TimeAndWind.LOGGER.info("[Time & Wind] Sending configuration to player");
    }
    @SubscribeEvent
    public void serverStarting(FMLServerStartingEvent event){
        TimeAndWind.LOGGER.info("Reading time cfg...");
        IOManager.readTimeData();
    }
    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event){
        TAWCommands.registerCommands(event.getDispatcher());
    }
}
