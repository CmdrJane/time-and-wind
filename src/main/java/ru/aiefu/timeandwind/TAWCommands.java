package ru.aiefu.timeandwind;

import com.google.gson.GsonBuilder;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TAWCommands {
    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher){
        dispatcher.register(CommandManager.literal("taw").then(CommandManager.literal("reload").executes(context -> reloadCfg(context.getSource()))));

        dispatcher.register(CommandManager.literal("taw").then(CommandManager.literal("set-cycle-length").
                then(CommandManager.argument("dimension", DimensionArgumentType.dimension()).then(CommandManager.argument("day_length", LongArgumentType.longArg(1)).
                        then(CommandManager.argument("night_length", LongArgumentType.longArg(1)).executes(context ->
                                setTimeLength(DimensionArgumentType.getDimensionArgument(context, "dimension"), context.getSource(),
                                        LongArgumentType.getLong(context, "day_length"), LongArgumentType.getLong(context,"night_length"))))))));
        dispatcher.register(CommandManager.literal("taw").then(CommandManager.literal("remove-cycle-entry").
                then(CommandManager.argument("dimension", DimensionArgumentType.dimension()).executes(context ->
                        removeConfigEntry(context.getSource(), DimensionArgumentType.getDimensionArgument(context, "dimension"))))));

        dispatcher.register(CommandManager.literal("taw").then(CommandManager.literal("get-current-world-id").executes(context -> printCurrentWorldId(context.getSource()))));

        dispatcher.register(CommandManager.literal("taw").then(CommandManager.literal("parse-worlds-ids").executes(context -> parseWorldsIds(context.getSource()))));

        dispatcher.register(CommandManager.literal("taw").then(CommandManager.literal("get-ambient-darkness").executes(context -> printAmbientDarkness(context.getSource()))));
        dispatcher.register(CommandManager.literal("taw").then(CommandManager.literal("get-light-level").executes(context -> getLightLevel(context.getSource()))));

        dispatcher.register(CommandManager.literal("taw").then(CommandManager.literal("get-time-data").executes(context -> getTimeConfig(context.getSource()))));
    }

    private static int reloadCfg(ServerCommandSource source) throws CommandSyntaxException {
        if(source.hasPermissionLevel(4) || source.getMinecraftServer().isHost(source.getPlayer().getGameProfile())) {
            MinecraftServer server = source.getMinecraftServer();
            int result = IOManager.readTimeData();
            if(result == 0){
                source.sendFeedback(new LiteralText("Unable to reload config"), false);
                return  0;
            }
            source.getMinecraftServer().getWorlds().forEach(serverWorld -> {
                String id = serverWorld.getRegistryKey().getValue().toString();
                if (TimeAndWind.timeDataMap.containsKey(id)) {
                    ((IDimType) serverWorld.getDimension()).setCycleDuration(TimeAndWind.timeDataMap.get(id).dayDuration, TimeAndWind.timeDataMap.get(id).nightDuration);
                } else ((IDimType) serverWorld.getDimension()).resetCycleDuration();
                TAWScheduler.createTAWSchedule(serverWorld.getDimension(), serverWorld.getRegistryKey().getValue().getPath(), "_villager_taw", false);
                TAWScheduler.createTAWSchedule(serverWorld.getDimension(), serverWorld.getRegistryKey().getValue().getPath(), "_villager_baby_taw", true);
                List<Entity> villagers  = serverWorld.getEntitiesByType(EntityType.VILLAGER, entity -> true);
                for(Entity e : villagers){
                    ((VillagerEntity)e).reinitializeBrain(serverWorld);
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

    private static int setTimeLength(ServerWorld targetWorld, ServerCommandSource source, long dayD, long nightD) throws CommandSyntaxException {
        if(source.hasPermissionLevel(4) || source.getMinecraftServer().isHost(source.getPlayer().getGameProfile())) {
            String worldId = targetWorld.getRegistryKey().getValue().toString();
            IOManager.updateTimeData(worldId, dayD, nightD);
            source.sendFeedback(new LiteralText("Configuration entry added, now use /taw reload to apply changes"), false);
        } else source.sendError(new LiteralText("[Time & Wind] Permission level of 4 is required to run this command"));
        return 0;
    }

    private static int removeConfigEntry(ServerCommandSource source, ServerWorld targetWorld) throws CommandSyntaxException {
        if(source.hasPermissionLevel(4) || source.getMinecraftServer().isHost(source.getPlayer().getGameProfile())) {
            String worldId = targetWorld.getRegistryKey().getValue().toString();
            if(TimeAndWind.timeDataMap.containsKey(worldId)){
                TimeAndWind.timeDataMap.remove(worldId);
                IOManager.updateTimeData();
                source.sendFeedback(new LiteralText("Entry removed, now use /taw reload to apply changes"), false);
            } else source.sendError(new LiteralText("Config does not contains settings for " + worldId));
        } else source.sendError(new LiteralText("[Time & Wind] Permission level of 4 is required to run this command"));
        return 0;
    }

    private static int printCurrentWorldId(ServerCommandSource source) throws CommandSyntaxException {
        String id = source.getPlayer().world.getRegistryKey().getValue().toString();
        source.sendFeedback(new LiteralText(id), false);
        ServerPlayNetworking.send(source.getPlayer(), NetworkPacketsID.WORLD_ID_CLIPBOARD, new PacketByteBuf(Unpooled.buffer()).writeString(id));
        return 0;
    }
    private static int printAmbientDarkness(ServerCommandSource source) throws CommandSyntaxException {
        if(source.hasPermissionLevel(4) || source.getMinecraftServer().isHost(source.getPlayer().getGameProfile())) {
            source.sendFeedback(new LiteralText("Ambient Darkness: " + source.getPlayer().world.getAmbientDarkness()), false);
        } else source.sendError(new LiteralText("[Time & Wind] Permission level of 4 is required to run this command"));
        return 0;
    }
    private static int parseWorldsIds(ServerCommandSource source) throws CommandSyntaxException {
        if(source.hasPermissionLevel(4) || source.getMinecraftServer().isHost(source.getPlayer().getGameProfile())) {
            List<String> ids = new ArrayList<>();
            source.getMinecraftServer().getWorlds().forEach(serverWorld -> ids.add(serverWorld.getRegistryKey().getValue().toString()));
            File file = new File("taw-worlds-ids.json");
            IOManager.fileWriter(file, new GsonBuilder().setPrettyPrinting().create().toJson(ids));
            source.sendFeedback(new LiteralText("Saved to " + file.getAbsolutePath()), false);
        } else source.sendError(new LiteralText("[Time & Wind] Permission level of 4 is required to run this command"));
        return 0;
    }
    private static int getLightLevel(ServerCommandSource source) throws CommandSyntaxException {
        if(source.hasPermissionLevel(4) || source.getMinecraftServer().isHost(source.getPlayer().getGameProfile())) {
            source.sendFeedback(new LiteralText("Light Level: " + source.getPlayer().world.getLightLevel(source.getPlayer().getBlockPos())), false);
        } else source.sendError(new LiteralText("[Time & Wind] Permission level of 4 is required to run this command"));
        return 0;
    }

    private static int getTimeConfig(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayer();
        String worldId = player.world.getRegistryKey().getValue().toString();
        if(player.world.getDimension().hasFixedTime()){
            source.sendFeedback(new LiteralText("Current dimension has fixed time, custom configuration is useless"), false);
        } else source.sendFeedback(new LiteralText("Current dimension does not has fixed time, custom configuration should work fine"), false);
        if(TimeAndWind.timeDataMap.containsKey(worldId)) {
            TimeDataStorage storage = TimeAndWind.timeDataMap.get(worldId);
            source.sendFeedback(new LiteralText("Server config for current world: Day Duration: " + storage.dayDuration + " Night Duration: " + storage.nightDuration), true);
            ServerPlayNetworking.send(player, NetworkPacketsID.CFG_DEBUG_INFO, new PacketByteBuf(Unpooled.buffer()));
        } else source.sendError(new LiteralText("No Data found for current world on server side"));
        return 0;
    }

}
