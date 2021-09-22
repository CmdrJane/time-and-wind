package ru.aiefu.timeandwindct;

import com.google.gson.GsonBuilder;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;
import ru.aiefu.timeandwindct.network.NetworkHandler;
import ru.aiefu.timeandwindct.network.messages.ConfigDebugInfo;
import ru.aiefu.timeandwindct.network.messages.SyncConfig;
import ru.aiefu.timeandwindct.network.messages.WorldKeyToClipboard;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TAWCommands {
    public static void registerCommands(CommandDispatcher<CommandSource> dispatcher){
        dispatcher.register(Commands.literal("taw").then(Commands.literal("reload").executes(context -> reloadCfg(context.getSource()))));
        dispatcher.register(Commands.literal("taw").then(Commands.literal("get-current-world-id").executes(context -> printCurrentWorldId(context.getSource()))));

        dispatcher.register(Commands.literal("taw").then(Commands.literal("parse-worlds-ids").executes(context -> parseWorldsIds(context.getSource()))));

        dispatcher.register(Commands.literal("taw").then(Commands.literal("get-ambient-darkness").executes(context -> printAmbientDarkness(context.getSource()))));
        dispatcher.register(Commands.literal("taw").then(Commands.literal("get-light-level").executes(context -> getLightLevel(context.getSource()))));

        dispatcher.register(Commands.literal("taw").then(Commands.literal("get-time-data").executes(context -> getTimeConfig(context.getSource()))));
    }

    public static int reloadCfg(CommandSource source) throws CommandSyntaxException {
        if(source.hasPermission(4) || source.getServer().isSingleplayerOwner(source.getPlayerOrException().getGameProfile())) {
            MinecraftServer server = source.getServer();
            IOManager.readTimeData();
            source.getServer().getAllLevels().forEach(serverWorld -> {
                String id = serverWorld.dimension().location().toString();
                if (TimeAndWindCT.timeDataMap.containsKey(id)) {
                    ((ITimeOperations) serverWorld).getTimeTicker().setupCustomTime(TimeAndWindCT.timeDataMap.get(id).dayDuration, TimeAndWindCT.timeDataMap.get(id).nightDuration);
                }
            });
            for(ServerPlayerEntity player : server.getPlayerList().getPlayers()){
                NetworkHandler.sendTo(new SyncConfig(), player);
            }
            source.sendSuccess(new StringTextComponent("[Time & Wind] Config reloaded"), true);
        }
        else {
            source.sendFailure(new StringTextComponent("[Time & Wind] Permission level of 4 is required to run this command"));
        }
        return 0;
    }
    public static int printCurrentWorldId(CommandSource source) throws CommandSyntaxException {
        String id = source.getPlayerOrException().level.dimension().location().toString();
        source.sendSuccess(new StringTextComponent(id), false);
        NetworkHandler.sendTo(new WorldKeyToClipboard(id), source.getPlayerOrException());
        return 0;
    }
    public static int printAmbientDarkness(CommandSource source) throws CommandSyntaxException {
        source.sendSuccess(new StringTextComponent("Ambient Darkness: " + source.getPlayerOrException().level.getSkyDarken()), false);
        return 0;
    }
    public static int parseWorldsIds(CommandSource source) throws CommandSyntaxException {
        if(source.hasPermission(4) || source.getServer().isSingleplayerOwner(source.getPlayerOrException().getGameProfile())) {
            List<String> ids = new ArrayList<>();
            source.getServer().getAllLevels().forEach(serverWorld -> ids.add(serverWorld.dimension().location().toString()));
            File file = new File("taw-worlds-ids.json");
            new IOManager().fileWriter(file, new GsonBuilder().setPrettyPrinting().create().toJson(ids));
            source.sendSuccess(new StringTextComponent("Saved to " + file.getAbsolutePath()), false);
        } else source.sendFailure(new StringTextComponent("[Time & Wind] Permission level of 4 is required to run this command"));
        return 0;
    }
    public static int getLightLevel(CommandSource source) throws CommandSyntaxException {
        source.sendSuccess(new StringTextComponent("Light Level: " + source.getPlayerOrException().level.getMaxLocalRawBrightness(source.getPlayerOrException().blockPosition())), false);
        return 0;
    }

    public static int getTimeConfig(CommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrException();
        String worldId = player.level.dimension().location().toString();
        if(player.level.dimensionType().hasFixedTime()){
            source.sendSuccess(new StringTextComponent("Current dimension has fixed time, custom configuration is useless"), false);
        } else source.sendSuccess(new StringTextComponent("Current dimension does not has fixed time, custom configuration should work fine"), false);
        if(TimeAndWindCT.timeDataMap.containsKey(worldId)) {
            TimeDataStorage storage = TimeAndWindCT.timeDataMap.get(worldId);
            source.sendSuccess(new StringTextComponent("Server config for current world: Day Duration: " + storage.dayDuration + " Night Duration: " + storage.nightDuration), true);
            TimeTicker ticker = ((ITimeOperations) player.level).getTimeTicker();
            source.sendSuccess(new StringTextComponent("[S] Day Mod: " + ticker.getDayMod() + " Night Mod: " + ticker.getNightMod()), false);
            source.sendSuccess(new StringTextComponent("[S] Day RE: " + ticker.getDayRoundingError() + " Night RE: " + ticker.getNightRoundingError()), false);
            NetworkHandler.sendTo(new ConfigDebugInfo(), player);
        } else source.sendFailure(new StringTextComponent("No Data found for current world on server side"));
        return 0;
    }

}
