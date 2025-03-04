package aiefu.timeandwind;

import aiefu.timeandwind.config.ModConfig;
import aiefu.timeandwind.config.SystemTimeConfig;
import aiefu.timeandwind.config.TimeDataStorage;
import aiefu.timeandwind.network.PacketIDs;
import aiefu.timeandwind.tickers.SystemTimeTicker;
import aiefu.timeandwind.tickers.Ticker;
import aiefu.timeandwind.tickers.TimeTicker;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

public class TimeAndWind implements ModInitializer {
	public static final String MOD_ID = "time-and-wind";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static MinecraftServer server;

	public static ModConfig config;

	public static HashMap<String, TimeDataStorage> timeConfig;

	public static HashMap<String, SystemTimeConfig> sysTimeConfig;

	public static SystemTimeConfig sysTimeGlobal;

	public static final HashMap<String, Ticker> tickers = new HashMap<>();

	@Override
	public void onInitialize() {
		PacketIDs.registerPackets();
		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			TimeAndWind.server = server;
            try {
				reloadFromDisk();
            } catch (IOException e) {
				LOGGER.trace("Unable to load configuration from disk, shutting down", e);
                throw new RuntimeException(e);
            }
        });
	}

	public static void reloadFromDisk() throws IOException {
		readConfig();
		readTimeData();
		readSysTimeData();
		prepareTickers();
	}

	public static void saveAndReloadTickers() throws IOException {
		saveConfig();
		saveTimeData();
		saveSysTimeData();
		prepareTickers();
		assignTickers(server.getAllLevels());
	}

	public static void reloadTickers(){
		prepareTickers();
		assignTickers(server.getAllLevels());
	}

	public static void readConfig() throws IOException {
		String ymlMain = "./config/time-and-wind/config.yml";
		String jsonMain = "./config/time-and-wind/config.json";
		boolean shouldSave;
		if(Files.exists(Paths.get(ymlMain))){
			ModConfig cfg = TawJsonUtils.readYaml(ymlMain, ModConfig.class);
			shouldSave = patchConfig(TawJsonUtils.gson.toJsonTree(cfg).getAsJsonObject());
		} else if (Files.exists(Paths.get(jsonMain))) {
			shouldSave = patchConfig(TawJsonUtils.loadJsonObject(jsonMain));
		} else {
			config = ModConfig.getDefault();
			shouldSave = true;
		}
		if(shouldSave){
			TawJsonUtils.writeYaml(config, ymlMain);
		}
	}

	public static boolean patchConfig(JsonObject object) {
		boolean shouldSave = false;
		if(object.has("syncWithSystemTime")){
			boolean bl = object.get("syncWithSystemTime").getAsBoolean();
			object.remove("syncWithSystemTime");
			object.addProperty("enableGlobalSystemTime", bl);
			shouldSave = true;
		}
		if(object.has("systemTimePerDimensions")){
			object.remove("systemTimePerDimensions");
			shouldSave = true;
		}
		config = TawJsonUtils.gson.fromJson(object, ModConfig.class);
		return shouldSave;
	}

	public static void saveConfig() throws IOException {
		TawJsonUtils.writeYaml(config, "./config/time-and-wind/config.yml");
	}

	public static void readTimeData() throws IOException {
		String ymlData = "./config/time-and-wind/time-data.yml";
		String jsonData = "./config/time-and-wind/time-data.json";
		if(Files.exists(Paths.get(ymlData))){
			timeConfig = TawJsonUtils.readYaml(ymlData, HashMap.class, String.class, TimeDataStorage.class);
		} else if (Files.exists(Paths.get(jsonData))) {
			timeConfig  = TawJsonUtils.loadJson(jsonData, new TypeToken<HashMap<String, TimeDataStorage>>(){}.getType());
			TawJsonUtils.writeYaml(timeConfig, ymlData);
		} else {
			timeConfig = new HashMap<>();
			timeConfig.put("minecraft:overworld", new TimeDataStorage(12000, 12000));
			TawJsonUtils.writeYaml(timeConfig, ymlData);
		}
	}

	public static void saveTimeData() throws IOException {
		TawJsonUtils.writeYaml(timeConfig, "./config/time-and-wind/time-data.yml");
	}

	public static void readSysTimeData() throws IOException {
		String ymlData = "./config/time-and-wind/system-time-data.json.yml";
		String jsonData = "./config/time-and-wind/system-time-data.json.json";
		if(Files.exists(Paths.get(ymlData))){
			sysTimeConfig = TawJsonUtils.readYaml(ymlData, HashMap.class, String.class, SystemTimeConfig.class);
		} else if (Files.exists(Paths.get(jsonData))) {
			sysTimeConfig  = TawJsonUtils.loadJson(jsonData, new TypeToken<HashMap<String, SystemTimeConfig>>(){}.getType());
			TawJsonUtils.writeYaml(sysTimeConfig, ymlData);
		} else {
			sysTimeConfig = new HashMap<>();
			sysTimeConfig.put("minecraft:overworld", new SystemTimeConfig("5:00", "20:00", "3:00"));
			TawJsonUtils.writeYaml(sysTimeConfig, ymlData);
		}
	}

	public static void saveSysTimeData() throws IOException {
		TawJsonUtils.writeYaml(sysTimeConfig,"./config/time-and-wind/system-time-data.json.yml");
	}

	public static void prepareTickers(){
		tickers.clear();
		if(!config.enableGlobalSystemTime){
			timeConfig.forEach( (k, v) -> {
				tickers.put(k, new TimeTicker(v));
			});
			sysTimeConfig.forEach((k, v) -> {
				tickers.put(k, new SystemTimeTicker(v));
			});
		} else {
			server.getAllLevels().forEach(l -> {
				if(!l.dimensionType().hasFixedTime()){
					tickers.put(l.dimension().location().toString(), new SystemTimeTicker(sysTimeGlobal));
				}
			});
		}
	}

	public static void assignTickers(Iterable<ServerLevel> levels){
		levels.forEach(l -> {
			String id = l.dimension().location().toString();
			Ticker ticker = tickers.get(id);
			if(ticker != null){
				((ITimeOperations)l).taw$setTimeTicker(ticker);
			}
		});
	}
}