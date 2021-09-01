package ru.aiefu.timeandwind;

import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.aiefu.timeandwind.network.NetworkHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

@Mod("timeandwind")
public class TimeAndWind {
	public static final String MOD_ID = "timeandwind";
	public static final Logger LOGGER = LogManager.getLogger();
	public static HashMap<String, TimeDataStorage> timeDataMap;

	public TimeAndWind() {
		// Register the setup method for modloading
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

		MinecraftForge.EVENT_BUS.register(new TimeAndWindEvents());
	}
	public void setup(final FMLCommonSetupEvent event) {
		LOGGER.info("[Time & Wind] Initializing...");
		craftPaths();
		registerReceivers();
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
			IOManager ioManager = new IOManager();
			if(!Files.exists(Paths.get("./config/time-and-wind/time-data.json"))){
				ioManager.genTimeData();
			}
		}
		catch (IOException e){
			e.printStackTrace();
		}
	}

	public static String get24TimeFormat(World world){
		if(world != null){
			double duration = ((IDimType) world.dimensionType()).getCycleDuration();
			double currentTime =  world.getDayTime() % duration;
			double tickInHours = duration / 24;
			double ticksInMinute = tickInHours / 60;
			int hours = (int) Math.floor(currentTime / tickInHours);
			int minutes = (int) Math.floor((currentTime - tickInHours * hours) / ticksInMinute);
			String mm = "0" + minutes;
			mm = mm.substring(mm.length() - 2);
			hours += 6;
			if(hours > 23){
				hours -= 24;
			}
			return hours + ":" + mm;
		}
		return "NaN";
	}

	public static int [] get24TimeFormatRaw(World world){
		if(world != null){
			double duration = ((IDimType) world.dimensionType()).getCycleDuration();
			double currentTime =  world.getDayTime() % duration;
			double tickInHours = duration / 24;
			double ticksInMinute = tickInHours / 60;
			int hours = (int) Math.floor(currentTime / tickInHours);
			int minutes = (int) Math.floor((currentTime - tickInHours * hours) / ticksInMinute);
			hours += 6;
			if(hours > 23){
				hours -= 24;
			}
			return new int[]{hours, minutes};
		}
		return new int[]{0,0};
	}
	private void registerReceivers(){

	}
}
