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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

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
					IOManager.readTimeData();
					systemTimeConfig = IOManager.readGlobalSysTimeCfg();
					sysTimeMap = IOManager.readSysTimeCfg();
		});
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			if(CONFIG.syncWithSystemTime) server.getGameRules().getRule(GameRules.RULE_DOINSOMNIA).set(false, server);

		});
		CommandRegistrationCallback.EVENT.register((dispatcher, ra, env) -> TAWCommands.registerCommands(dispatcher));
	}

	public void craftPaths(){
		try{
			if(!Files.isDirectory(Paths.get("./config"))){
				Files.createDirectory(Paths.get("./config"));
			}
			if(!Files.isDirectory(Paths.get("./config/time-and-wind"))){
				Files.createDirectory(Paths.get("./config/time-and-wind"));
			}
			if(!Files.exists(Paths.get("./config/time-and-wind/time-data.json"))){
				IOManager.genTimeData();
			}
			if(!Files.exists(Paths.get("./config/time-and-wind/config.json"))){
				IOManager.generateModConfig();
			}
			if(!Files.exists(Paths.get("./config/time-and-wind/system-time-data.json"))){
				IOManager.generateSysTimeCfg();
			}
			if(!Files.exists(Paths.get("./config/time-and-wind/system-time-data-global.json"))){
				IOManager.generateSysTimeCfg();
			}
			if(!Files.exists(Paths.get("./config/time-and-wind/system-time-data.json"))){
				IOManager.generateMapSysTime();
			}
			CONFIG = IOManager.readModConfig();
		}
		catch (IOException e){
			e.printStackTrace();
		}
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
		if(!player.getServer().isSingleplayerOwner(player.getGameProfile())) {
			FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

			ModConfig cfg = TimeAndWindCT.CONFIG;
			SystemTimeConfig cfgs = TimeAndWindCT.systemTimeConfig;
			buf.writeBoolean(cfg.patchSkyAngle);
			buf.writeBoolean(cfg.syncWithSystemTime);
			buf.writeBoolean(cfg.systemTimePerDimensions);
			buf.writeBoolean(cfg.enableNightSkipAcceleration);
			buf.writeInt(cfg.accelerationSpeed);
			buf.writeBoolean(cfg.enableThreshold);
			buf.writeInt(cfg.thresholdPercentage);

			buf.writeUtf(cfgs.sunrise);
			buf.writeUtf(cfgs.sunset);
			buf.writeUtf(cfgs.timeZone);

			buf.writeMap(TimeAndWindCT.timeDataMap, FriendlyByteBuf::writeUtf, (packetByteBuf, timeDataStorage) -> {
				packetByteBuf.writeLong(timeDataStorage.dayDuration);
				packetByteBuf.writeLong(timeDataStorage.nightDuration);
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
