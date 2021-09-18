package ru.aiefu.timeandwindct;

import com.google.gson.GsonBuilder;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TAWCommands {
    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher){
        dispatcher.register(CommandManager.literal("taw").then(CommandManager.literal("reload").executes(context -> reloadCfg(context.getSource()))));
        dispatcher.register(CommandManager.literal("taw").then(CommandManager.literal("get-current-world-id").executes(context -> printCurrentWorldId(context.getSource()))));

        dispatcher.register(CommandManager.literal("taw").then(CommandManager.literal("parse-worlds-ids").executes(context -> parseWorldsIds(context.getSource()))));

        dispatcher.register(CommandManager.literal("taw").then(CommandManager.literal("get-ambient-darkness").executes(context -> printAmbientDarkness(context.getSource()))));
        dispatcher.register(CommandManager.literal("taw").then(CommandManager.literal("get-light-level").executes(context -> getLightLevel(context.getSource()))));

        dispatcher.register(CommandManager.literal("taw").then(CommandManager.literal("get-time-data").executes(context -> getTimeConfig(context.getSource()))));
        dispatcher.register(CommandManager.literal("taw").then(CommandManager.literal("time-checker-values").executes(context -> getTimeChecker(context.getSource()))));
        dispatcher.register(CommandManager.literal("taw").then(CommandManager.literal("system-time").executes(context -> getSystemTime(context.getSource()))));
    }

    public static int reloadCfg(ServerCommandSource source) throws CommandSyntaxException {
        if(source.hasPermissionLevel(4) || source.getServer().isHost(source.getPlayer().getGameProfile())) {
            MinecraftServer server = source.getServer();
            IOManager.readTimeData();
            source.getServer().getWorlds().forEach(serverWorld -> {
                String id = serverWorld.getRegistryKey().getValue().toString();
                if (TimeAndWindCT.timeDataMap.containsKey(id)) {
                    ((ITimeOperations) serverWorld).getTimeTicker().setupCustomTime(TimeAndWindCT.timeDataMap.get(id).dayDuration, TimeAndWindCT.timeDataMap.get(id).nightDuration);
                }
            });
            for(ServerPlayerEntity player : server.getPlayerManager().getPlayerList()){
               TimeAndWindCT.sendConfigSyncPacket(player);
            }
            source.sendFeedback(new LiteralText("[Time & Wind] Config reloaded"), true);
        }
        else {
            source.sendError(new LiteralText("[Time & Wind] Permission level of 4 is required to run this command"));
        }
        return 0;
    }
    public static int printCurrentWorldId(ServerCommandSource source) throws CommandSyntaxException {
        String id = source.getPlayer().world.getRegistryKey().getValue().toString();
        source.sendFeedback(new LiteralText(id), false);
        ServerPlayNetworking.send(source.getPlayer(), NetworkPacketsID.WORLD_ID_CLIPBOARD, new PacketByteBuf(Unpooled.buffer()).writeString(id));
        return 0;
    }
    public static int printAmbientDarkness(ServerCommandSource source) throws CommandSyntaxException {
        source.sendFeedback(new LiteralText("Ambient Darkness: " + source.getPlayer().world.getAmbientDarkness()), false);
        return 0;
    }
    public static int parseWorldsIds(ServerCommandSource source) throws CommandSyntaxException {
        if(source.hasPermissionLevel(4) || source.getServer().isHost(source.getPlayer().getGameProfile())) {
            List<String> ids = new ArrayList<>();
            source.getServer().getWorlds().forEach(serverWorld -> ids.add(serverWorld.getRegistryKey().getValue().toString()));
            File file = new File("taw-worlds-ids.json");
            new IOManager().fileWriter(file, new GsonBuilder().setPrettyPrinting().create().toJson(ids));
            source.sendFeedback(new LiteralText("Saved to " + file.getAbsolutePath()), false);
        } else source.sendError(new LiteralText("[Time & Wind] Permission level of 4 is required to run this command"));
        return 0;
    }
    public static int getLightLevel(ServerCommandSource source) throws CommandSyntaxException {
        source.sendFeedback(new LiteralText("Light Level: " + source.getPlayer().world.getLightLevel(source.getPlayer().getBlockPos())), false);
        return 0;
    }

    public static int getTimeConfig(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayer();
        String worldId = player.world.getRegistryKey().getValue().toString();
        if(player.world.getDimension().hasFixedTime()){
            source.sendFeedback(new LiteralText("Current dimension has fixed time, custom configuration is useless"), false);
        } else source.sendFeedback(new LiteralText("Current dimension does not has fixed time, custom configuration should work fine"), false);
        if(TimeAndWindCT.timeDataMap.containsKey(worldId)) {
            TimeDataStorage storage = TimeAndWindCT.timeDataMap.get(worldId);
            source.sendFeedback(new LiteralText("Server config for current world: Day Duration: " + storage.dayDuration + " Night Duration: " + storage.nightDuration), true);
            ServerPlayNetworking.send(player, NetworkPacketsID.CFG_DEBUG_INFO, new PacketByteBuf(Unpooled.buffer()));
        } else source.sendError(new LiteralText("No Data found for current world on server side"));
        return 0;
    }

    public static int getTimeChecker(ServerCommandSource source) {
        TimeTicker ticker = ((ITimeOperations) source.getWorld()).getTimeTicker();
        source.sendFeedback(new LiteralText("Day: " + ticker.getDayD() + " Night: " + ticker.getNightD()), false);
        source.sendFeedback(new LiteralText("Day Mod: " + ticker.getDayMod() + " Night Mod: " + ticker.getNightMod()), false);
        source.sendFeedback(new LiteralText("Day RE: " + ticker.getDayRoundingError() + " Night RE: " + ticker.getNightRoundingError()), false);
        return 0;
    }

    public static int getSystemTime(ServerCommandSource source){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalDateTime time = LocalDateTime.now();
        source.sendFeedback(new LiteralText(time.format(formatter)), false);
        return 0;
    }

}