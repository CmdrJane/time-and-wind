package ru.aiefu.timeandwindct;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.aiefu.timeandwindct.config.ModConfig;
import ru.aiefu.timeandwindct.config.SystemTimeConfig;
import ru.aiefu.timeandwindct.config.TimeDataStorage;
import ru.aiefu.timeandwindct.network.NetworkHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

@Mod("timeandwindct")
public class TimeAndWindCT {
	public static final String MOD_ID = "timeandwindct";
	public static final Logger LOGGER = LogManager.getLogger();
	public static HashMap<String, TimeDataStorage> timeDataMap;

	public static ModConfig CONFIG;
	public static SystemTimeConfig systemTimeConfig;
	public static boolean debugMode = false;

	public TimeAndWindCT() {
		// Register the setup method for modloading
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

		MinecraftForge.EVENT_BUS.register(new TimeAndWindCTEvents());
	}
	public void setup(final FMLCommonSetupEvent event) {
		LOGGER.info("[Time & Wind] Initializing...");
		craftPaths();
		NetworkHandler.setup();
		//CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> TAWCommands.registerCommands(dispatcher));
		LOGGER.info("[Time & Wind] I'm in time control now!");
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
			CONFIG = IOManager.readModConfig();
			systemTimeConfig = IOManager.readSysTimeCfg();
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
}
