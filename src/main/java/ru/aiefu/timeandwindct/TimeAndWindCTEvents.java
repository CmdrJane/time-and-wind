package ru.aiefu.timeandwindct;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.SleepingTimeCheckEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import ru.aiefu.timeandwindct.network.NetworkHandler;
import ru.aiefu.timeandwindct.network.messages.SyncConfig;

import java.util.Objects;

public class TimeAndWindCTEvents {

    @SubscribeEvent
    public void playerJoin(PlayerEvent.PlayerLoggedInEvent event){
        Player player = event.getEntity();
        if(!Objects.requireNonNull(player.getServer()).isSingleplayerOwner(player.getGameProfile())){
            TimeAndWindCT.LOGGER.info("[Time & Wind] Sending configuration to player");
            NetworkHandler.sendToPlayer(new SyncConfig(), (ServerPlayer) event.getEntity());
        }
    }
    @SubscribeEvent
    public void serverStarting(ServerAboutToStartEvent event){
        TimeAndWindCT.LOGGER.info("Reading time cfg...");
        ConfigurationManager.readTimeData();
        TimeAndWindCT.systemTimeConfig = ConfigurationManager.readGlobalSysTimeCfg();
        TimeAndWindCT.sysTimeMap = ConfigurationManager.readSysTimeCfg();
        if(TimeAndWindCT.CONFIG.syncWithSystemTime) event.getServer().getGameRules().getRule(GameRules.RULE_DOINSOMNIA).set(false, event.getServer());
    }
    @SubscribeEvent
    public void serverStarted(ServerStartedEvent e){
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
