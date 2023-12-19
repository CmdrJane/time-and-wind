package ru.aiefu.timeandwindct;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.DimensionArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.GameRules;
import net.minecraft.world.server.ServerWorld;
import ru.aiefu.timeandwindct.config.ModConfig;
import ru.aiefu.timeandwindct.config.TimeDataStorage;
import ru.aiefu.timeandwindct.network.NetworkHandler;
import ru.aiefu.timeandwindct.network.messages.ConfigDebugInfo;
import ru.aiefu.timeandwindct.network.messages.SyncConfig;
import ru.aiefu.timeandwindct.network.messages.WorldKeyToClipboard;
import ru.aiefu.timeandwindct.tickers.DefaultTicker;
import ru.aiefu.timeandwindct.tickers.SystemTimeTicker;
import ru.aiefu.timeandwindct.tickers.Ticker;
import ru.aiefu.timeandwindct.tickers.TimeTicker;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TAWCommands {
    public static void registerCommands(CommandDispatcher<CommandSource> dispatcher){
        dispatcher.register(Commands.literal("taw").then(Commands.literal("reload").executes(context -> reloadCfg(context.getSource()))));
        dispatcher.register(Commands.literal("taw").then(Commands.literal("enable-debug").then(Commands.argument("boolean", BoolArgumentType.bool()).executes(context ->
                enableDebug(context.getSource(), BoolArgumentType.getBool(context, "boolean"))))));

        dispatcher.register(Commands.literal("taw").then(Commands.literal("set-cycle-length").
                then(Commands.argument("dimension", DimensionArgument.dimension()).then(Commands.argument("day_length", IntegerArgumentType.integer(1)).
                then(Commands.argument("night_length", IntegerArgumentType.integer(1)).executes(context ->
                        setTimeLength(DimensionArgument.getDimension(context, "dimension"), context.getSource(),
                                IntegerArgumentType.getInteger(context, "day_length"), IntegerArgumentType.getInteger(context,"night_length"))))))));
        dispatcher.register(Commands.literal("taw").then(Commands.literal("remove-cycle-entry").
                then(Commands.argument("dimension", DimensionArgument.dimension()).executes(context ->
                        removeConfigEntry(context.getSource(), DimensionArgument.getDimension(context, "dimension"))))));

        dispatcher.register(Commands.literal("taw").then(Commands.literal("switch-system-time-sync").
                then(Commands.argument("ssyncstate", BoolArgumentType.bool()).executes(context ->
                        switchSystemTimeSyncState(context.getSource(), BoolArgumentType.getBool(context,"ssyncstate"))))));

        dispatcher.register(Commands.literal("taw").then(Commands.literal("switch-sys-time-per-dimension").
                then(Commands.argument("per-dim-state", BoolArgumentType.bool()).executes(context ->
                        switchSystemTimePerDim(context.getSource(), BoolArgumentType.getBool(context,"per-dim-state"))))));

        dispatcher.register(Commands.literal("taw").then(Commands.literal("set-system-time-properties").then(Commands.argument("dimension", DimensionArgument.dimension())
                .then(Commands.argument("sunrise", StringArgumentType.string()).then(Commands.argument("sunset", StringArgumentType.string())
                        .then(Commands.argument("timezone", StringArgumentType.string()).executes(context ->
                                setSysSyncTimeProperties(DimensionArgument.getDimension(context, "dimension"), context.getSource(), StringArgumentType.getString(context, "sunrise"),
                                        StringArgumentType.getString(context, "sunset"), StringArgumentType.getString(context, "timezone")))))))));
        dispatcher.register(Commands.literal("taw").then(Commands.literal("set-global-sys-time-properties").then(Commands.argument("sunrise", StringArgumentType.string())
                .then(Commands.argument("sunset", StringArgumentType.string()).then(Commands.argument("timezone", StringArgumentType.string())
                        .executes(context -> setGlobalSysTimeProps(context.getSource(), StringArgumentType.getString(context, "sunrise"),
                                StringArgumentType.getString(context, "sunset"), StringArgumentType.getString(context, "timezone"))))))));

        dispatcher.register(Commands.literal("taw").then(Commands.literal("get-current-world-id").executes(context -> printCurrentWorldId(context.getSource()))));

        dispatcher.register(Commands.literal("taw").then(Commands.literal("parse-worlds-ids").executes(context -> parseWorldsIds(context.getSource()))));

        dispatcher.register(Commands.literal("taw").then(Commands.literal("get-ambient-darkness").executes(context -> printAmbientDarkness(context.getSource()))));
        dispatcher.register(Commands.literal("taw").then(Commands.literal("get-light-level").executes(context -> getLightLevel(context.getSource()))));

        dispatcher.register(Commands.literal("taw").then(Commands.literal("get-time-data").executes(context -> getTimeConfig(context.getSource()))));

        dispatcher.register(Commands.literal("taw").then(Commands.literal("time-ticker-values").executes(context -> getTimeChecker(context.getSource()))));
        dispatcher.register(Commands.literal("taw").then(Commands.literal("system-time").executes(context -> getSystemTime(context.getSource()))));
    }

    private static int enableDebug(CommandSource source, boolean bl) throws CommandSyntaxException {
        if(source.hasPermission(4) || source.getServer().isSingleplayerOwner(source.getPlayerOrException().getGameProfile())) {
            TimeAndWindCT.debugMode = bl;
            source.sendSuccess(new StringTextComponent("[Time & Wind] Set debug mode to " + bl), true);
        } else source.sendFailure(new StringTextComponent("[Time & Wind] Permission level of 4 is required to run this command"));
        return 0;
    }

    private static int setTimeLength(ServerWorld targetWorld, CommandSource source, int dayD, int nightD) throws CommandSyntaxException {
        if(source.hasPermission(4) || source.getServer().isSingleplayerOwner(source.getPlayerOrException().getGameProfile())) {
            String worldId = targetWorld.dimension().location().toString();
            ConfigurationManager.updateTimeData(worldId, dayD, nightD);
            source.sendSuccess(new StringTextComponent("Configuration entry added, now use /taw reload to apply changes"), false);
        } else source.sendFailure(new StringTextComponent("[Time & Wind] Permission level of 4 is required to run this command"));
        return 0;
    }



    private static int setSysSyncTimeProperties(ServerWorld targetDimension, CommandSource source, String sunrise, String sunset, String timeZone) throws CommandSyntaxException {
        if(source.hasPermission(4) || source.getServer().isSingleplayerOwner(source.getPlayerOrException().getGameProfile())) {
            String worldId = targetDimension.dimension().location().toString();
            if(checkFormat(sunrise) && checkFormat(sunset) && checkFormat(timeZone)){
                ConfigurationManager.updateMapSysTime(worldId, sunrise, sunset, timeZone);
                source.sendSuccess(new StringTextComponent("Configuration entry added, now use /taw reload to apply changes"), false);
            } else source.sendFailure(new StringTextComponent("Error, sunrise, sunset or timezone param contains non numeric symbols"));
        } source.sendFailure(new StringTextComponent("[Time & Wind] Permission level of 4 is required to run this command"));
        return 0;
    }

    private static int setGlobalSysTimeProps(CommandSource source, String sunrise, String sunset, String timezone) throws CommandSyntaxException {
        if(source.hasPermission(4) || source.getServer().isSingleplayerOwner(source.getPlayerOrException().getGameProfile())) {
            if(checkFormat(sunrise) && checkFormat(sunset) && checkFormat(timezone)){
                ConfigurationManager.updateGlobalSysTimeCfg(sunrise, sunset, timezone);
                source.sendSuccess(new StringTextComponent("Configuration entry added, now use /taw reload to apply changes"), false);
            } else source.sendFailure(new StringTextComponent("Error, sunrise, sunset or timezone param contains non numeric symbols"));
        } source.sendFailure(new StringTextComponent("[Time & Wind] Permission level of 4 is required to run this command"));
        return 0;
    }

    private static boolean checkFormat(String time){
        int k = time.indexOf(":");
        if(k == -1) return false;
        String hour = time.substring(0, k);
        String min = time.substring(k + 1);
        return hour.matches("[0-9]+") && min.matches("[0-9]+");
    }

    private static int removeConfigEntry(CommandSource source, ServerWorld targetWorld) throws CommandSyntaxException {
        if(source.hasPermission(4) || source.getServer().isSingleplayerOwner(source.getPlayerOrException().getGameProfile())) {
            String worldId = targetWorld.dimension().location().toString();
            if(TimeAndWindCT.timeDataMap.containsKey(worldId)){
                TimeAndWindCT.timeDataMap.remove(worldId);
                ConfigurationManager.updateTimeData();
                source.sendSuccess(new StringTextComponent("Entry removed, now use /taw reload to apply changes"), false);
            } else source.sendFailure(new StringTextComponent("Config does not contains settings for " + worldId));
        } else source.sendFailure(new StringTextComponent("[Time & Wind] Permission level of 4 is required to run this command"));
        return 0;
    }

    private static int switchSystemTimeSyncState(CommandSource source, boolean state) throws CommandSyntaxException {
        if(source.hasPermission(4) || source.getServer().isSingleplayerOwner(source.getPlayerOrException().getGameProfile())) {
            source.getServer().getGameRules().getRule(GameRules.RULE_DOINSOMNIA).set(!state, source.getServer());
            ModConfig cfg = TimeAndWindCT.CONFIG.copy();
            cfg.syncWithSystemTime = state;
            ConfigurationManager.updateModConfig(cfg);
            source.sendSuccess(new StringTextComponent("SysTimeSync state switched to" + state + " now use /taw reload to apply changes"), false);
        } else source.sendFailure(new StringTextComponent("[Time & Wind] Permission level of 4 is required to run this command"));
        return 0;
    }

    private static int switchSystemTimePerDim(CommandSource source, boolean state) throws CommandSyntaxException {
        if(source.hasPermission(4) || source.getServer().isSingleplayerOwner(source.getPlayerOrException().getGameProfile())) {
            ModConfig cfg = TimeAndWindCT.CONFIG.copy();
            cfg.syncWithSystemTime = state;
            ConfigurationManager.updateModConfig(cfg);
            source.sendSuccess(new StringTextComponent("SystemTimePerDimension state switched to" + state + " now use /taw reload to apply changes"), false);
        }
        return 0;
    }

    private static int reloadCfg(CommandSource source) throws CommandSyntaxException {
        if(source.hasPermission(4) || source.getServer().isSingleplayerOwner(source.getPlayerOrException().getGameProfile())) {
            MinecraftServer server = source.getServer();
            int result = ConfigurationManager.readTimeData();
            TimeAndWindCT.CONFIG = ConfigurationManager.readModConfig();
            TimeAndWindCT.systemTimeConfig = ConfigurationManager.readGlobalSysTimeCfg();
            TimeAndWindCT.sysTimeMap = ConfigurationManager.readSysTimeCfg();
            if(result == 0){
                source.sendSuccess(new StringTextComponent("Unable to reload config"), false);
                return 0;
            }
            for(ServerWorld serverWorld : source.getServer().getAllLevels()){
                String id = serverWorld.dimension().location().toString();
                if(TimeAndWindCT.CONFIG.syncWithSystemTime){
                    if(TimeAndWindCT.CONFIG.systemTimePerDimensions && TimeAndWindCT.sysTimeMap.containsKey(id))
                    ((ITimeOperations) serverWorld).time_and_wind_custom_ticker$setTimeTicker(new SystemTimeTicker((ITimeOperations) serverWorld, TimeAndWindCT.sysTimeMap.get(id)));
                    else ((ITimeOperations) serverWorld).time_and_wind_custom_ticker$setTimeTicker(new SystemTimeTicker((ITimeOperations) serverWorld, TimeAndWindCT.systemTimeConfig));
                }
                else if (TimeAndWindCT.timeDataMap.containsKey(id)) {
                    TimeDataStorage storage = TimeAndWindCT.timeDataMap.get(id);
                    ((ITimeOperations) serverWorld).time_and_wind_custom_ticker$setTimeTicker(new TimeTicker(storage.dayDuration, storage.nightDuration, serverWorld));
                } else ((ITimeOperations) serverWorld).time_and_wind_custom_ticker$setTimeTicker(new DefaultTicker());
            }
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
    private static int printCurrentWorldId(CommandSource source) throws CommandSyntaxException {
        if(TimeAndWindCT.debugMode || source.hasPermission(4) || source.getServer().isSingleplayerOwner(source.getPlayerOrException().getGameProfile())) {
            ServerPlayerEntity player = source.getPlayerOrException();
            String id = player.level.dimension().location().toString();
            source.sendSuccess(new StringTextComponent(id), false);
            NetworkHandler.sendTo(new WorldKeyToClipboard(id), player);
        }
        return 0;
    }
    private static int printAmbientDarkness(CommandSource source) throws CommandSyntaxException {
        if(TimeAndWindCT.debugMode || source.hasPermission(4) || source.getServer().isSingleplayerOwner(source.getPlayerOrException().getGameProfile())) {
            ServerPlayerEntity player = source.getPlayerOrException();
            source.sendSuccess(new StringTextComponent("Ambient Darkness: " + player.level.getSkyDarken()), false);
        }
        return 0;
    }
    private static int parseWorldsIds(CommandSource source) throws CommandSyntaxException {
        if(source.hasPermission(4) || source.getServer().isSingleplayerOwner(source.getPlayerOrException().getGameProfile())) {
            List<String> ids = new ArrayList<>();
            source.getServer().getAllLevels().forEach(serverWorld -> ids.add(serverWorld.dimension().location().toString()));
            try(FileWriter writer = ConfigurationManager.getFileWriter("./taw-worlds-ids.json")) {
                ConfigurationManager.gson_pretty.toJson(ids, writer);
            } catch (IOException e){
                e.printStackTrace();
            }
            source.sendSuccess(new StringTextComponent("Saved to minecraft root folder"), false);
        } else source.sendFailure(new StringTextComponent("[Time & Wind] Permission level of 4 is required to run this command"));
        return 0;
    }
    private static int getLightLevel(CommandSource source) throws CommandSyntaxException {
        if(TimeAndWindCT.debugMode || source.hasPermission(4) || source.getServer().isSingleplayerOwner(source.getPlayerOrException().getGameProfile())) {
            ServerPlayerEntity player = source.getPlayerOrException();
            source.sendSuccess(new StringTextComponent("Light Level: " + player.level.getMaxLocalRawBrightness(player.blockPosition())), false);
        }
        return 0;
    }

    private static int getTimeConfig(CommandSource source) throws CommandSyntaxException {
        if(TimeAndWindCT.debugMode || source.hasPermission(4) || source.getServer().isSingleplayerOwner(source.getPlayerOrException().getGameProfile())) {
            ServerPlayerEntity player = source.getPlayerOrException();
            String worldId = player.level.dimension().location().toString();
            if (player.level.dimensionType().hasFixedTime()) {
                source.sendSuccess(new StringTextComponent("Current dimension have fixed time, custom configuration is useless"), false);
            } else
                source.sendSuccess(new StringTextComponent("Current dimension does not have fixed time, custom configuration should work fine"), false);
            if (TimeAndWindCT.timeDataMap.containsKey(worldId)) {
                TimeDataStorage storage = TimeAndWindCT.timeDataMap.get(worldId);
                source.sendSuccess(new StringTextComponent("Server config for current world: Day Duration: " + storage.dayDuration + " Night Duration: " + storage.nightDuration), true);
                NetworkHandler.sendTo(new ConfigDebugInfo(), player);
            } else source.sendFailure(new StringTextComponent("No Data found for current world on server side"));
        }
        return 0;
    }

    private static int getTimeChecker(CommandSource source) throws CommandSyntaxException {
        if(TimeAndWindCT.debugMode || source.hasPermission(4) || source.getServer().isSingleplayerOwner(source.getPlayerOrException().getGameProfile())) {
            Ticker t = ((ITimeOperations) source.getLevel()).time_and_wind_custom_ticker$getTimeTicker();
            if(t instanceof TimeTicker) {
                TimeTicker ticker = (TimeTicker) t;
                source.sendSuccess(new StringTextComponent("Day: " + ticker.getDayD() + " Night: " + ticker.getNightD()), false);
                source.sendSuccess(new StringTextComponent("Day Mod: " + ticker.getDayMod() + " Night Mod: " + ticker.getNightMod()), false);
                source.sendSuccess(new StringTextComponent("Day RE: " + ticker.getDayRoundingError() + " Night RE: " + ticker.getNightRoundingError()), false);
            } else if(t instanceof SystemTimeTicker){
                SystemTimeTicker stt = (SystemTimeTicker) t;
                String sunrise = TimeAndWindCT.getFormattedTime(stt.getSunrise() / 1000);
                String sunset = TimeAndWindCT.getFormattedTime(stt.getSunset() / 1000);
                String dayD = TimeAndWindCT.getFormattedTime(stt.getDayD() / 1000);
                String nightD = TimeAndWindCT.getFormattedTime(stt.getNightD() / 1000);
                source.sendSuccess(new StringTextComponent("Time is synced with system time"), false);
                source.sendSuccess(new StringTextComponent("Sunrise are at: " + sunrise + " and sunset are at: " + sunset + " in timezone: " + TimeAndWindCT.systemTimeConfig.timeZone), false);
                source.sendSuccess(new StringTextComponent("Day Length are: " + dayD + " and Night Length are: " + nightD), false);
            } else source.sendFailure(new StringTextComponent("This world uses default time ticker"));
        } else source.sendFailure(new StringTextComponent("[Time & Wind] Permission level of 4 is required to run this command"));
        return 0;
    }

    private static int getSystemTime(CommandSource source) throws CommandSyntaxException {
        if(source.hasPermission(4) || source.getServer().isSingleplayerOwner(source.getPlayerOrException().getGameProfile())) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            LocalDateTime time = LocalDateTime.now();
            source.sendSuccess(new StringTextComponent(time.format(formatter)), false);
        } else source.sendFailure(new StringTextComponent("[Time & Wind] Permission level of 4 is required to run this command"));
        return 0;
    }

}
