package ru.aiefu.timeandwindct;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.aiefu.timeandwindct.config.ModConfig;
import ru.aiefu.timeandwindct.config.SystemTimeConfig;
import ru.aiefu.timeandwindct.config.TimeDataStorage;
import ru.aiefu.timeandwindct.payloads.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Objects;

public class TimeAndWindCT implements ModInitializer {
    public static final String MOD_ID = "tawct";
    public static final Logger LOGGER = LogManager.getLogger();
    public static HashMap<String, TimeDataStorage> timeDataMap;
    public static HashMap<String, SystemTimeConfig> sysTimeMap;
    public static ModConfig modConfig;
    public static SystemTimeConfig systemTimeConfig;
    public static boolean debugMode = false;

    @Override
    public void onInitialize() {
        craftPaths();
        registerPayloads();

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            ConfigurationManager.readTimeData();
            systemTimeConfig = ConfigurationManager.readGlobalSysTimeCfg();
            sysTimeMap = ConfigurationManager.readSysTimeCfg();
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            if (modConfig.syncWithSystemTime) {
                server.getGameRules().getRule(GameRules.RULE_DOINSOMNIA).set(false, server);
            }
        });

        CommandRegistrationCallback.EVENT.register(
            (dispatcher, ra, env) -> TAWCommands.registerCommands(dispatcher)
        );
    }

    private void registerPayloads() {
        PayloadTypeRegistry.playS2C().register(CfgDebugInfoPayload.ID, CfgDebugInfoPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(NightSkipInfoPayload.ID, NightSkipInfoPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SetupTimeLocalPayload.ID, SetupTimeLocalPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncConfigPayload.ID, SyncConfigPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(WorldIdClipboardPayload.ID, WorldIdClipboardPayload.CODEC);
    }

    public void craftPaths() {
        File file = new File("./config/time-and-wind");
        file.mkdirs();

        if (!Files.exists(Paths.get("./config/time-and-wind/time-data.json"))) {
            ConfigurationManager.genTimeData();
        }

        if (!Files.exists(Paths.get("./config/time-and-wind/config.json"))) {
            ConfigurationManager.generateModConfig();
        }

        if (!Files.exists(Paths.get("./config/time-and-wind/system-time-data-global.json"))) {
            ConfigurationManager.generateSysTimeCfg();
        }

        if (!Files.exists(Paths.get("./config/time-and-wind/system-time-data.json"))) {
            ConfigurationManager.generateMapSysTime();
        }

        modConfig = ConfigurationManager.readModConfig();
    }

    public static String getFormattedTime(long ms) {
        long seconds = ms;
        long hours = seconds / 3600;
        seconds -= (hours * 3600);
        long minutes = seconds / 60;
        seconds -= (minutes * 60);

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static void sendConfigSyncPacket(ServerPlayer player) {
        if (!Objects.requireNonNull(player.getServer()).isSingleplayerOwner(player.getGameProfile())) {
            ServerPlayNetworking.send(player, new SyncConfigPayload(
                TimeAndWindCT.modConfig,
                TimeAndWindCT.systemTimeConfig,
                TimeAndWindCT.timeDataMap,
                TimeAndWindCT.sysTimeMap
            ));

            LOGGER.info("[Time & Wind] Sending config to player");
        } else {
            ServerPlayNetworking.send(player, new SetupTimeLocalPayload());
        }
    }
}
