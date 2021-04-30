package ru.aiefu.timeandwind;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

public class TimeAndWind implements ModInitializer {
	public static final String MOD_ID = "timeandwind";
	public static HashMap<String, TimeDataStorage> timeDataMap;

	@Override
	public void onInitialize() {
		craftPaths();
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			server.getWorlds().forEach(serverWorld -> {
				String id = serverWorld.getRegistryKey().getValue().toString();
				if(timeDataMap.containsKey(id)){
					((IDimType)serverWorld.getDimension()).setCycleDuration(timeDataMap.get(id).dayDuration, timeDataMap.get(id).nightDuration);
				}
			});
		});
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
			ioManager.readTimeData();

		}
		catch (IOException e){
			e.printStackTrace();
		}
	}

}
