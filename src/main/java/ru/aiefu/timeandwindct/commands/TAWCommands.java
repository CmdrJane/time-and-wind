package ru.aiefu.timeandwindct.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;
import ru.aiefu.timeandwindct.*;
import ru.aiefu.timeandwindct.packets.CheckConfig;
import ru.aiefu.timeandwindct.packets.SyncConfig;
import ru.aiefu.timeandwindct.packets.WorldIDToClipboard;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class TAWCommands extends CommandBase {
    @Override
    @Nonnull
    public String getName() {
        return "taw";
    }
    @Override
    @Nonnull
    public String getUsage(@Nonnull ICommandSender sender) {
        return "taw reload add-entry remove-entry debug-mode get-cfg time-values get-world-id";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) {
        if(args.length > 0){
            switch (args[0]){
                case "reload":{
                    this.reloadCfg(server, sender);
                    break;
                }
                case "debug-mode": {
                    switchDebugMod(server, sender, args);
                    break;
                }
                case "add-entry": {
                    addConfigEntry(server, sender, args);
                    break;
                }
                case "remove-entry": {
                    removeConfigEntry(server, sender, args);
                    break;
                }
                case "get-cfg": {
                    this.getTimeConfig(sender, server);
                    break;
                }
                case "time-values": {
                    getCurrentTimeValue(sender, server);
                    break;
                }
                case "get-world-id": {
                    this.printCurrentWorldId(sender, server);
                    break;
                }
                default: {
                    notifyCommandListener(sender, this, getUsage(sender));
                    break;
                }
            }
        } else notifyCommandListener(sender, this, getUsage(sender));
    }

    @Override
    public boolean checkPermission(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender) {
        return true;
    }

    public void switchDebugMod(MinecraftServer server, ICommandSender sender, @Nonnull String[] args){
        if(sender.canUseCommand(4, this.getName()) || server.isSinglePlayer()) {
            if(args.length == 2){
                switch (args[1]){
                    case "true": {
                        TimeAndWindCT.debugMode = true;
                        notifyCommandListener(sender, this, "Debug mode enabled");
                        break;
                    }
                    case "false": {
                        TimeAndWindCT.debugMode = false;
                        notifyCommandListener(sender, this, "Debug mode disabled");
                        break;
                    }
                    default: notifyCommandListener(sender, this, "Invalid argument, can only be true or false");
                }
            } else notifyCommandListener(sender, this, "Wrong syntax, use /taw debug-mode <true/false>");
        } else notifyCommandListener(sender, this, "[Time & Wind] Permission level of 4 is required to run this command");
    }

    public void addConfigEntry(MinecraftServer server, ICommandSender sender, @Nonnull String[] args){
        if(sender.canUseCommand(4, this.getName()) || server.isSinglePlayer()) {
            if (args.length == 4) {
                String worldId = args[1];
                long dayD;
                long nightD;
                try {
                    dayD = Long.parseLong(args[2]);
                    nightD = Long.parseLong(args[3]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    notifyCommandListener(sender, this, "Unable to parse day/night duration, ensure that you typed integer numbers");
                    return;
                }
                for (DimensionType d : DimensionManager.getRegisteredDimensions().keySet()) {
                    if (d.getName().equals(worldId)) {
                        IOManager.updateTimeData(worldId, dayD, nightD);
                        notifyCommandListener(sender, this, "Configuration added, now use /taw reload to apply changes");
                        return;
                    }
                }
                notifyCommandListener(sender, this, "Unable to found world with corresponding id");
            } else notifyCommandListener(sender, this, "Wrong syntax, use /taw add-entry <dimensions-id> <day-length> <night-length>");
        } else notifyCommandListener(sender, this, "[Time & Wind] Permission level of 4 is required to run this command");
    }

    public void removeConfigEntry(MinecraftServer server, ICommandSender sender, @Nonnull String[] args){
        if(sender.canUseCommand(4, this.getName()) || server.isSinglePlayer()) {
            if (args.length == 2) {
                String worldId = args[1];
                if(TimeAndWindCT.timeDataMap.containsKey(worldId)){
                    TimeAndWindCT.timeDataMap.remove(worldId);
                    IOManager.updateTimeData();
                    notifyCommandListener(sender, this, "Entry removed, now use /taw reload to apply changes " + worldId);
                } else notifyCommandListener(sender, this, "Configuration does not contain id: " + worldId);
            } else notifyCommandListener(sender, this, "Wrong syntax, use /taw remove-entry <dimensions-id>");
        } else notifyCommandListener(sender, this, "[Time & Wind] Permission level of 4 is required to run this command");
    }


    public void reloadCfg(MinecraftServer server, ICommandSender sender) {
        if(sender.canUseCommand(4, this.getName()) || server.isSinglePlayer()) {
            int result = IOManager.readTimeData();
            if(result == 0){
                notifyCommandListener(sender, this, "Unable parse config file");
                return;
            }
            Arrays.stream(DimensionManager.getWorlds()).forEach(serverWorld -> {
                String id = serverWorld.provider.getDimensionType().getName();
                if (TimeAndWindCT.timeDataMap.containsKey(id)) {
                    ((ITimeOperations) serverWorld).getTimeTicker().setupCustomTime(TimeAndWindCT.timeDataMap.get(id).dayDuration, TimeAndWindCT.timeDataMap.get(id).nightDuration);
                } else ((ITimeOperations) serverWorld).getTimeTicker().setCustomTicker(false);
            });
            for(EntityPlayerMP player : server.getPlayerList().getPlayers()){
                TAWNetworkHandler.INSTANCE.sendTo(new SyncConfig(), player);
            }
            notifyCommandListener(sender, this, "[Time & Wind] Config reloaded");
        }
        else {
            notifyCommandListener(sender, this, "[Time & Wind] Permission level of 4 is required to run this command");
        }
    }

    public void getTimeConfig(ICommandSender sender, MinecraftServer server) {
        if(TimeAndWindCT.debugMode || sender.canUseCommand(4, this.getName()) || server.isSinglePlayer()) {
            String worldId = sender.getEntityWorld().provider.getDimensionType().getName();
            if (TimeAndWindCT.timeDataMap.containsKey(worldId)) {
                TimeDataStorage storage = TimeAndWindCT.timeDataMap.get(worldId);
                notifyCommandListener(sender, this, "Server config for current world: Day Duration: " + storage.dayDuration + " Night Duration: " + storage.nightDuration);
                if (sender.getCommandSenderEntity() instanceof EntityPlayerMP)
                    TAWNetworkHandler.INSTANCE.sendTo(new CheckConfig(), (EntityPlayerMP) sender.getCommandSenderEntity());
            } else notifyCommandListener(sender, this, "No Data found for current world on server side");
        } else notifyCommandListener(sender, this, "[Time & Wind] Permission level of 4 is required to run this command or debug mode should be enabled");
    }

    public void printCurrentWorldId(ICommandSender sender, MinecraftServer server) {
        if(TimeAndWindCT.debugMode || sender.canUseCommand(4, this.getName()) || server.isSinglePlayer()) {
            String id = sender.getEntityWorld().provider.getDimensionType().getName();
            notifyCommandListener(sender, this, id);
            if (sender.getCommandSenderEntity() instanceof EntityPlayerMP)
                TAWNetworkHandler.INSTANCE.sendTo(new WorldIDToClipboard(id), (EntityPlayerMP) sender.getCommandSenderEntity());
        } else notifyCommandListener(sender, this, "[Time & Wind] Permission level of 4 is required to run this command or debug mode should be enabled");
    }
    public void getCurrentTimeValue(ICommandSender sender, MinecraftServer server){
        if(TimeAndWindCT.debugMode || sender.canUseCommand(4, this.getName()) || server.isSinglePlayer()) {
            TimeTicker ticker = ((ITimeOperations) sender.getEntityWorld()).getTimeTicker();
            notifyCommandListener(sender, this, "State: " + ticker.getState());
            notifyCommandListener(sender, this, "Day Mod: " + ticker.getDayMod() + " Night Mod: " + ticker.getNightMod());
            notifyCommandListener(sender, this, "Day RE: " + ticker.getDayRoundingError() + " Night RE: " + ticker.getNightRoundingError());
            notifyCommandListener(sender, this, "Day Duration: " + ticker.getDayD() + " Night Day: " + ticker.getNightD());
        } else notifyCommandListener(sender, this, "[Time & Wind] Permission level of 4 is required to run this command or debug mode should be enabled");
    }
}
