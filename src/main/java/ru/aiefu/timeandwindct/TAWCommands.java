package ru.aiefu.timeandwindct;

import com.google.gson.GsonBuilder;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.DimensionArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import ru.aiefu.timeandwindct.config.TimeDataStorage;
import ru.aiefu.timeandwindct.network.NetworkHandler;
import ru.aiefu.timeandwindct.network.messages.ConfigDebugInfo;
import ru.aiefu.timeandwindct.network.messages.SyncConfig;
import ru.aiefu.timeandwindct.network.messages.WorldKeyToClipboard;
import ru.aiefu.timeandwindct.tickers.DefaultTicker;
import ru.aiefu.timeandwindct.tickers.SystemTimeTicker;
import ru.aiefu.timeandwindct.tickers.Ticker;
import ru.aiefu.timeandwindct.tickers.TimeTicker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TAWCommands {
    public static void registerCommands(CommandDispatcher<CommandSource> dispatcher){
        dispatcher.register(Commands.literal("taw").then(Commands.literal("reload").executes(context -> reloadCfg(context.getSource()))));

        dispatcher.register(Commands.literal("taw").then(Commands.literal("enable-debug").then(Commands.argument("boolean", BoolArgumentType.bool()).executes(context ->
               switchDebug(context.getSource(), BoolArgumentType.getBool(context, "boolean")) ))));

        dispatcher.register(Commands.literal("taw").then(Commands.literal("set-cycle-length").
                then(Commands.argument("dimension", DimensionArgument.dimension()).then(Commands.argument("day_length", LongArgumentType.longArg(1)).
                        then(Commands.argument("night_length", LongArgumentType.longArg(1)).executes(context ->
                                setTimeLength(DimensionArgument.getDimension(context, "dimension"), context.getSource(),
                                        LongArgumentType.getLong(context, "day_length"), LongArgumentType.getLong(context,"night_length"))))))));

        dispatcher.register(Commands.literal("taw").then(Commands.literal("remove-cycle-entry").
                then(Commands.argument("dimension", DimensionArgument.dimension()).executes(context ->
                        removeConfigEntry(context.getSource(), DimensionArgument.getDimension(context, "dimension"))))));

        dispatcher.register(Commands.literal("taw").then(Commands.literal("get-current-world-id").executes(context -> printCurrentWorldId(context.getSource()))));

        dispatcher.register(Commands.literal("taw").then(Commands.literal("parse-worlds-ids").executes(context -> parseWorldsIds(context.getSource()))));

        dispatcher.register(Commands.literal("taw").then(Commands.literal("get-ambient-darkness").executes(context -> printAmbientDarkness(context.getSource()))));
        dispatcher.register(Commands.literal("taw").then(Commands.literal("get-light-level").executes(context -> getLightLevel(context.getSource()))));

        dispatcher.register(Commands.literal("taw").then(Commands.literal("get-time-data").executes(context -> getTimeConfig(context.getSource()))));
    }

    private static int reloadCfg(CommandSource source) throws CommandSyntaxException {
        if(source.hasPermission(4) || source.getServer().isSingleplayerOwner(source.getPlayerOrException().getGameProfile())) {
            MinecraftServer server = source.getServer();
            int result = IOManager.readTimeData();
            if(result == 0){
                source.sendFailure(new StringTextComponent("Unable to reload config"));
                return 0;
            }
            source.getServer().getAllLevels().forEach(serverWorld -> {
                String id = serverWorld.dimension().location().toString();
                if(TimeAndWindCT.CONFIG.syncWithSystemTime){
                    ((ITimeOperations) serverWorld).setTimeTicker(new SystemTimeTicker((ITimeOperations) serverWorld));
                }
                else if (TimeAndWindCT.timeDataMap.containsKey(id)) {
                    TimeDataStorage storage = TimeAndWindCT.timeDataMap.get(id);
                    ((ITimeOperations) serverWorld).setTimeTicker(new TimeTicker(storage.dayDuration, storage.nightDuration));
                } else ((ITimeOperations) serverWorld).setTimeTicker(new DefaultTicker());
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

    private static int removeConfigEntry(CommandSource source, ServerWorld targetWorld) throws CommandSyntaxException {
        if(source.hasPermission(4) || source.getServer().isSingleplayerOwner(source.getPlayerOrException().getGameProfile())) {
            String worldId = targetWorld.dimension().location().toString();
            if(TimeAndWindCT.timeDataMap.containsKey(worldId)){
                TimeAndWindCT.timeDataMap.remove(worldId);
                IOManager.updateTimeData();
                source.sendSuccess(new StringTextComponent("Entry removed, now use /taw reload to apply changes"), false);
            } else source.sendFailure(new StringTextComponent("Config does not contains settings for " + worldId));
        } else source.sendFailure(new StringTextComponent("[Time & Wind] Permission level of 4 is required to run this command"));
        return 0;
    }

    private static int switchDebug(CommandSource source, boolean bl) throws CommandSyntaxException {
        if(source.hasPermission(4) || source.getServer().isSingleplayerOwner(source.getPlayerOrException().getGameProfile())) {
            TimeAndWindCT.debugMode = bl;
            source.sendSuccess(new StringTextComponent("[Time & Wind] Set debug mod to " + bl), false);
        } else source.sendFailure(new StringTextComponent("[Time & Wind] Permission level of 4 is required to run this command"));
        return 0;
    }

    private static int setTimeLength(ServerWorld targetWorld, CommandSource source, long dayD, long nightD) throws CommandSyntaxException {
        if(source.hasPermission(4) || source.getServer().isSingleplayerOwner(source.getPlayerOrException().getGameProfile())) {
            String worldId = targetWorld.dimension().location().toString();
            IOManager.updateTimeData(worldId, dayD, nightD);
            source.sendSuccess(new StringTextComponent("Configuration entry added, now use /taw reload to apply changes"), false);
        } else source.sendFailure(new StringTextComponent("[Time & Wind] Permission level of 4 is required to run this command"));
        return 0;
    }

    private static int printCurrentWorldId(CommandSource source) throws CommandSyntaxException {
        if(TimeAndWindCT.debugMode || source.hasPermission(4) || source.getServer().isSingleplayerOwner(source.getPlayerOrException().getGameProfile())) {
            String id = source.getPlayerOrException().level.dimension().location().toString();
            source.sendSuccess(new StringTextComponent(id), false);
            NetworkHandler.sendTo(new WorldKeyToClipboard(id), source.getPlayerOrException());
        } else source.sendFailure(new StringTextComponent("[Time & Wind] Permission level of 4 is required to run this command"));
        return 0;
    }
    private static int printAmbientDarkness(CommandSource source) throws CommandSyntaxException {
        if(TimeAndWindCT.debugMode || source.hasPermission(4) || source.getServer().isSingleplayerOwner(source.getPlayerOrException().getGameProfile())) {
            source.sendSuccess(new StringTextComponent("Ambient Darkness: " + source.getPlayerOrException().level.getSkyDarken()), false);
        }
        return 0;
    }
    private static int parseWorldsIds(CommandSource source) throws CommandSyntaxException {
        if(source.hasPermission(4) || source.getServer().isSingleplayerOwner(source.getPlayerOrException().getGameProfile())) {
            List<String> ids = new ArrayList<>();
            source.getServer().getAllLevels().forEach(serverWorld -> ids.add(serverWorld.dimension().location().toString()));
            File file = new File("taw-worlds-ids.json");
            IOManager.fileWriter(file, new GsonBuilder().setPrettyPrinting().create().toJson(ids));
            source.sendSuccess(new StringTextComponent("Saved to " + file.getAbsolutePath()), false);
        } else source.sendFailure(new StringTextComponent("[Time & Wind] Permission level of 4 is required to run this command"));
        return 0;
    }
    private static int getLightLevel(CommandSource source) throws CommandSyntaxException {
        if(TimeAndWindCT.debugMode || source.hasPermission(4) || source.getServer().isSingleplayerOwner(source.getPlayerOrException().getGameProfile())) {
            source.sendSuccess(new StringTextComponent("Light Level: " + source.getPlayerOrException().level.getMaxLocalRawBrightness(source.getPlayerOrException().blockPosition())), false);
        }
        return 0;
    }

    private static int getTimeConfig(CommandSource source) throws CommandSyntaxException {
        if(TimeAndWindCT.debugMode || source.hasPermission(4) || source.getServer().isSingleplayerOwner(source.getPlayerOrException().getGameProfile())) {
            ServerPlayerEntity player = source.getPlayerOrException();
            String worldId = player.level.dimension().location().toString();
            if (player.level.dimensionType().hasFixedTime()) {
                source.sendSuccess(new StringTextComponent("Current dimension has fixed time, custom configuration is useless"), false);
            } else source.sendSuccess(new StringTextComponent("Current dimension does not has fixed time, custom configuration should work fine"), false);

            Ticker t = ((ITimeOperations) player.level).getTimeTicker();
            if (t instanceof TimeTicker && TimeAndWindCT.timeDataMap.containsKey(worldId)) {
                TimeDataStorage storage = TimeAndWindCT.timeDataMap.get(worldId);
                source.sendSuccess(new StringTextComponent("Server config for current world: Day Duration: " + storage.dayDuration + " Night Duration: " + storage.nightDuration), true);
                TimeTicker ticker = (TimeTicker) t;
                source.sendSuccess(new StringTextComponent("[S] Day Mod: " + ticker.getDayMod() + " Night Mod: " + ticker.getNightMod()), false);
                source.sendSuccess(new StringTextComponent("[S] Day RE: " + ticker.getDayRoundingError() + " Night RE: " + ticker.getNightRoundingError()), false);
                NetworkHandler.sendTo(new ConfigDebugInfo(), player);

            } else if(t instanceof SystemTimeTicker){
                SystemTimeTicker stt = (SystemTimeTicker) t;
                String sunrise = TimeAndWindCT.getFormattedTime(stt.getSunrise() / 1000);
                String sunset = TimeAndWindCT.getFormattedTime(stt.getSunset() / 1000);
                String dayD = TimeAndWindCT.getFormattedTime(stt.getDayD() / 1000);
                String nightD = TimeAndWindCT.getFormattedTime(stt.getNightD() / 1000);
                source.sendSuccess(new StringTextComponent("Sunrise are at: " + sunrise + " and sunset are at: " + sunset + " in timezone: " + TimeAndWindCT.systemTimeConfig.timeZone), false);
                source.sendSuccess(new StringTextComponent("Day Length are: " + dayD + " and Night Length are: " + nightD), false);
            } else if (t instanceof DefaultTicker) source.sendSuccess(new StringTextComponent("This world uses default time ticker"), false);
            else source.sendFailure(new StringTextComponent("No Data found for current world on server side"));
        }
        return 0;
    }

}
