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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

@Mod("timeandwindct")
public class TimeAndWindCT {
	public static final String MOD_ID = "timeandwindct";
	public static final Logger LOGGER = LogManager.getLogger();

	public static HashMap<String, TimeDataStorage> timeDataMap;
	public static HashMap<String, SystemTimeConfig> sysTimeMap;

	public static ModConfig CONFIG;

	public static SystemTimeConfig systemTimeConfig;
	public static boolean debugMode = false;

	public TimeAndWindCT() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		MinecraftForge.EVENT_BUS.register(new TimeAndWindCTEvents());
	}

	public void setup(final FMLCommonSetupEvent event) {
		LOGGER.info("[Time & Wind] Initializing...");
		craftPaths();
		NetworkHandler.setup();
		LOGGER.info("[Time & Wind] I'm in time control now!");
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
}
