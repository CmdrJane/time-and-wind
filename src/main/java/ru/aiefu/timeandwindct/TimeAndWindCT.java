package ru.aiefu.timeandwindct;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.aiefu.timeandwindct.config.ModConfig;
import ru.aiefu.timeandwindct.config.SystemTimeConfig;
import ru.aiefu.timeandwindct.config.TimeDataStorage;

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
	public static ModConfig CONFIG;
	public static SystemTimeConfig systemTimeConfig;
	public static boolean debugMode = false;

	@Override
	public void onInitialize() {
		craftPaths();
		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
					ConfigurationManager.readTimeData();
					systemTimeConfig = ConfigurationManager.readGlobalSysTimeCfg();
					sysTimeMap = ConfigurationManager.readSysTimeCfg();
		});
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			if(CONFIG.syncWithSystemTime) server.getGameRules().getRule(GameRules.RULE_DOINSOMNIA).set(false, server);

		});
		CommandRegistrationCallback.EVENT.register((dispatcher, ra, env) -> TAWCommands.registerCommands(dispatcher));
	}

	public void craftPaths(){
		File file = new File("./config/time-and-wind");
		file.mkdirs();
		if(!Files.exists(Paths.get("./config/time-and-wind/time-data.json"))){
			ConfigurationManager.genTimeData();
		}
		if(!Files.exists(Paths.get("./config/time-and-wind/config.json"))){
			ConfigurationManager.generateModConfig();
		}
		if(!Files.exists(Paths.get("./config/time-and-wind/system-time-data-global.json"))){
			ConfigurationManager.generateSysTimeCfg();
		}
		if(!Files.exists(Paths.get("./config/time-and-wind/system-time-data.json"))){
			ConfigurationManager.generateMapSysTime();
		}
		CONFIG = ConfigurationManager.readModConfig();
	}

	public static String getFormattedTime(long ms){
		long seconds = ms;
		long hours = seconds / 3600;
		seconds -= (hours * 3600);
		long minutes = seconds / 60;
		seconds -= (minutes * 60);
		return String.format("%02d:%02d:%02d", hours, minutes, seconds);
	}

	public static void sendConfigSyncPacket(ServerPlayer player){
		if(!Objects.requireNonNull(player.getServer()).isSingleplayerOwner(player.getGameProfile())) {
			FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

			String cfgJson = ConfigurationManager.gson_pretty.toJson(TimeAndWindCT.CONFIG);
			String cfgsJson = ConfigurationManager.gson_pretty.toJson(TimeAndWindCT.systemTimeConfig);

			buf.writeUtf(cfgJson);
			buf.writeUtf(cfgsJson);

			buf.writeMap(TimeAndWindCT.timeDataMap, FriendlyByteBuf::writeUtf, (packetByteBuf, timeDataStorage) -> {
				packetByteBuf.writeInt(timeDataStorage.dayDuration);
				packetByteBuf.writeInt(timeDataStorage.nightDuration);
			});
			buf.writeMap(TimeAndWindCT.sysTimeMap, FriendlyByteBuf::writeUtf, (packetByteBuf, systemTimeConfig1) -> {
				packetByteBuf.writeUtf(systemTimeConfig1.sunrise);
				packetByteBuf.writeUtf(systemTimeConfig1.sunset);
				packetByteBuf.writeUtf(systemTimeConfig1.timeZone);
			});
			ServerPlayNetworking.send(player, NetworkPacketsID.SYNC_CONFIG, buf);
			LOGGER.info("[Time & Wind] Sending config to player");
		} else ServerPlayNetworking.send(player, NetworkPacketsID.SETUP_TIME, new FriendlyByteBuf(Unpooled.buffer()));
	}
}
