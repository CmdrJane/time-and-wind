package ru.aiefu.timeandwind;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import java.util.List;

public class TAWCommands {
    public static void reloadCfgReg(CommandDispatcher<ServerCommandSource> dispatcher){
        dispatcher.register(CommandManager.literal("taw").then(CommandManager.literal("reload").executes(context -> reloadCfg(context.getSource()))));
    }

    public static int reloadCfg(ServerCommandSource source) throws CommandSyntaxException {
        if(source.hasPermissionLevel(4) || source.getMinecraftServer().isHost(source.getPlayer().getGameProfile())) {
            MinecraftServer server = source.getMinecraftServer();
            IOManager.readTimeData();
            source.getMinecraftServer().getWorlds().forEach(serverWorld -> {
                String id = serverWorld.getRegistryKey().getValue().toString();
                if (TimeAndWind.timeDataMap.containsKey(id)) {
                    ((IDimType) serverWorld.getDimension()).setCycleDuration(TimeAndWind.timeDataMap.get(id).dayDuration, TimeAndWind.timeDataMap.get(id).nightDuration);
                }
                TAWScheduler.createTAWSchedule(serverWorld.getDimension(), serverWorld.getRegistryKey().getValue().getPath(), "_villager_taw", false);
                TAWScheduler.createTAWSchedule(serverWorld.getDimension(), serverWorld.getRegistryKey().getValue().getPath(), "_villager_baby_taw", true);
                List<? extends VillagerEntity> villagers  = serverWorld.getEntitiesByType(EntityType.VILLAGER, entity -> true);
                for(VillagerEntity e : villagers){
                    e.reinitializeBrain(serverWorld);
                }
            });
            for(ServerPlayerEntity player : server.getPlayerManager().getPlayerList()){
               TimeAndWind.sendConfigSyncPacket(player);
            }
            source.sendFeedback(new LiteralText("[Time & Wind] Config reloaded"), true);
        }
        else {
            source.sendError(new LiteralText("[Time & Wind] Permission level of 4 is required to run this command"));
        }
        return 0;
    }

}
