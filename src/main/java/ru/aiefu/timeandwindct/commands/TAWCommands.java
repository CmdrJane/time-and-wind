package ru.aiefu.timeandwindct.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
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
        return "taw reload/get-cfg/time-values/get-world-id";
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
                case "get-cfg": {
                    this.getTimeConfig(sender);
                    break;
                }
                case "time-values": {
                    getCurrentTimeValue(sender);
                    break;
                }
                case "get-world-id": {
                    this.printCurrentWorldId(sender);
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
                }
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

    public void getTimeConfig(ICommandSender sender) {
        String worldId = sender.getEntityWorld().provider.getDimensionType().getName();
        if(TimeAndWindCT.timeDataMap.containsKey(worldId)) {
            TimeDataStorage storage = TimeAndWindCT.timeDataMap.get(worldId);
            notifyCommandListener(sender, this, "Server config for current world: Day Duration: " + storage.dayDuration + " Night Duration: " + storage.nightDuration);
            if(sender.getCommandSenderEntity() instanceof EntityPlayerMP )
                TAWNetworkHandler.INSTANCE.sendTo(new CheckConfig(), (EntityPlayerMP) sender.getCommandSenderEntity());
        } else notifyCommandListener(sender, this, "No Data found for current world on server side");
    }

    public void printCurrentWorldId(ICommandSender sender) {
        String id = sender.getEntityWorld().provider.getDimensionType().getName();
        notifyCommandListener(sender, this, id);
        if(sender.getCommandSenderEntity() instanceof EntityPlayerMP )
            TAWNetworkHandler.INSTANCE.sendTo(new WorldIDToClipboard(id), (EntityPlayerMP) sender.getCommandSenderEntity());

    }
    public void getCurrentTimeValue(ICommandSender sender){
       TimeTicker ticker = ((ITimeOperations) sender.getEntityWorld()).getTimeTicker();
       notifyCommandListener(sender, this, "Day Mod: " + ticker.getDayMod() + " Night Mod: " + ticker.getNightMod());
       notifyCommandListener(sender, this, "Day RE: " + ticker.getDayRoundingError() + " Night RE: " + ticker.getNightRoundingError());
       notifyCommandListener(sender, this, "Day Duration: " + ticker.getDayD() + " Night Day: " + ticker.getNightD());
    }
}
