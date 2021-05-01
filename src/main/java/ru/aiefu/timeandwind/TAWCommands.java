package ru.aiefu.timeandwind;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

public class TAWCommands {
    public static void reloadCfgReg(CommandDispatcher<ServerCommandSource> dispatcher){
        dispatcher.register(CommandManager.literal("taw").then(CommandManager.literal("reload").executes(context -> reloadCfg(context.getSource()))));
    }

    public static int reloadCfg(ServerCommandSource source) throws CommandSyntaxException {
        if(source.hasPermissionLevel(4) || (source.getMinecraftServer().isSinglePlayer() && source.getMinecraftServer().isHost(source.getPlayer().getGameProfile()))) {
            IOManager.readTimeData();
            source.getMinecraftServer().getWorlds().forEach(serverWorld -> {
                String id = serverWorld.getRegistryKey().getValue().toString();
                if (TimeAndWind.timeDataMap.containsKey(id)) {
                    ((IDimType) serverWorld.getDimension()).setCycleDuration(TimeAndWind.timeDataMap.get(id).dayDuration, TimeAndWind.timeDataMap.get(id).nightDuration);
                }
            });
            for(ServerPlayerEntity player : source.getMinecraftServer().getPlayerManager().getPlayerList()){
                IDimType dim = (IDimType) player.world.getDimension();
                TimeAndWind.sendTimeSyncPacket(player, dim.getDayDuration(), dim.getNightDuration());
            }
            source.sendFeedback(new LiteralText("[Time & Wind] Config reloaded"), true);
        }
        else {
            source.sendError(new LiteralText("[Time & Wind] Permission level of 4 is required to run this command"));
        }
        return 0;
    }

}
