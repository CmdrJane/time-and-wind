package ru.aiefu.timeandwindct;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.GameRules;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.SleepingTimeCheckEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import ru.aiefu.timeandwindct.network.NetworkHandler;
import ru.aiefu.timeandwindct.network.messages.SyncConfig;

public class TimeAndWindCTEvents {

    @SubscribeEvent
    public void playerJoin(PlayerEvent.PlayerLoggedInEvent event){
        NetworkHandler.sendTo(new SyncConfig(), (ServerPlayerEntity) event.getPlayer());
        TimeAndWindCT.LOGGER.info("[Time & Wind] Sending configuration to player");
    }
    @SubscribeEvent
    public void serverStarting(FMLServerAboutToStartEvent event){
        TimeAndWindCT.LOGGER.info("Reading time cfg...");
        IOManager.readTimeData();
    }
    @SubscribeEvent
    public void serverStarted(FMLServerStartedEvent e){
        if(TimeAndWindCT.CONFIG.syncWithSystemTime){
            e.getServer().getGameRules().getRule(GameRules.RULE_DOINSOMNIA).set(false, e.getServer());
        }
    }
    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event){
        TAWCommands.registerCommands(event.getDispatcher());
    }
    @SubscribeEvent
    public void enableSleepingAtDay(SleepingTimeCheckEvent e){
        if(TimeAndWindCT.CONFIG.syncWithSystemTime) e.setResult(Event.Result.ALLOW);
    }
}
