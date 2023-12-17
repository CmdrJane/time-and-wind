package ru.aiefu.timeandwindct;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import ru.aiefu.timeandwindct.config.ModConfig;
import ru.aiefu.timeandwindct.config.SystemTimeConfig;
import ru.aiefu.timeandwindct.config.TimeDataStorage;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class ConfigurationManager {
    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public static void genTimeData(){
        try(FileWriter writer = getFileWriter("./config/time-and-wind/time-data.json")) {
            gson.toJson(new HashMapOf<>("minecraft:overworld", new TimeDataStorage()), writer);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void generateSysTimeCfg(){
        try(FileWriter writer = getFileWriter("./config/time-and-wind/system-time-data-global.json")) {
            gson.toJson(new SystemTimeConfig("5:00", "20:00", "3:00"), writer);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void generateMapSysTime(){
        try(FileWriter writer = getFileWriter("./config/time-and-wind/system-time-data.json")) {
            gson.toJson(new HashMapOf<>("minecraft:overworld", new SystemTimeConfig("5:00", "20:00", "3:00")), writer);
        } catch (IOException e){
            e.printStackTrace();
        }
    }



    public static void updateModConfig(ModConfig config){
        try(FileWriter writer = getFileWriter("./config/time-and-wind/config.json")) {
            gson.toJson(config, writer);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void updateMapSysTime(String worldId, String sunrise, String sunset, String timeZone){
        TimeAndWindCT.sysTimeMap.put(worldId, new SystemTimeConfig(sunrise, sunset, timeZone));
        try(FileWriter writer = getFileWriter("./config/time-and-wind/system-time-data.json")) {
            gson.toJson(TimeAndWindCT.sysTimeMap, writer);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void updateGlobalSysTimeCfg(String sunrise, String sunset, String timezone){
        TimeAndWindCT.systemTimeConfig = new SystemTimeConfig(sunrise, sunset, timezone);
        try(FileWriter writer = getFileWriter("./config/time-and-wind/system-time-data-global.json")) {
            gson.toJson(TimeAndWindCT.systemTimeConfig, writer);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static SystemTimeConfig readGlobalSysTimeCfg(){
        SystemTimeConfig config;
        try {
            config = gson.fromJson(new FileReader("./config/time-and-wind/system-time-data-global.json"), SystemTimeConfig.class);
        } catch (IOException e){
            e.printStackTrace();
            config = new SystemTimeConfig("7:00", "19:00", "local");
        }
        return config;
    }

    public static HashMap<String, SystemTimeConfig> readSysTimeCfg(){
        HashMap<String, SystemTimeConfig> map;
        try {
            map = gson.fromJson(new FileReader("./config/time-and-wind/system-time-data.json"), new TypeToken<HashMap<String, SystemTimeConfig>>(){}.getType());
        } catch (IOException e){
            e.printStackTrace();
            map = new HashMapOf<>("minecraft:overworld", new SystemTimeConfig("5:00", "20:00", "3:00"));
        }
        return map;
    }

    public static void generateModConfig(){
        try(FileWriter writer = getFileWriter("./config/time-and-wind/config.json")){
            gson.toJson(new ModConfig(false, false, false,
                    false, 30, true, 50), writer);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static ModConfig readModConfig(){
        ModConfig config;
        try {
            config = gson.fromJson(new FileReader("./config/time-and-wind/config.json"), ModConfig.class);
            if(config.config_ver < 3){
                config = patchModConfigV1(config);
                try(FileWriter writer = getFileWriter("./config/time-and-wind/config.json")) {
                    gson.toJson(config, writer);
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        } catch (IOException e){
            e.printStackTrace();
            config = new ModConfig(true, false, false,
                    true, 30, true, 50);
        }
        return config;
    }

    public static ModConfig patchModConfigV1(ModConfig config){
        return new ModConfig(config.patchSkyAngle, config.syncWithSystemTime,false, true, 30, true, 50);
    }

    public static void updateTimeData(String id, int dayD, int nightD){
        TimeAndWindCT.timeDataMap.put(id, new TimeDataStorage(dayD, nightD));
        try(FileWriter writer = getFileWriter("./config/time-and-wind/time-data.json")) {
            gson.toJson(TimeAndWindCT.timeDataMap, writer);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void updateTimeData() {
        try(FileWriter writer = getFileWriter("./config/time-and-wind/time-data.json")) {
            gson.toJson(TimeAndWindCT.timeDataMap, writer);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static int readTimeData(){
        HashMap<String, TimeDataStorage> timeDataMap;
        int result;
        try {
            timeDataMap = gson.fromJson(new FileReader("./config/time-and-wind/time-data.json"), new TypeToken<HashMap<String, TimeDataStorage>>(){}.getType());
            result = 1;
        } catch (IOException e){
            e.printStackTrace();
            timeDataMap = new HashMapOf<>("minecraft:overworld", new TimeDataStorage());
            result = 0;
        }
        TimeAndWindCT.timeDataMap = timeDataMap;
        return result;
    }

    public static FileWriter getFileWriter(String path) throws IOException {
        return new FileWriter(path);
    }

}
