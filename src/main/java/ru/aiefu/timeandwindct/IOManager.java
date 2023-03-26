package ru.aiefu.timeandwindct;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import ru.aiefu.timeandwindct.config.ModConfig;
import ru.aiefu.timeandwindct.config.SystemTimeConfig;
import ru.aiefu.timeandwindct.config.TimeDataStorage;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class IOManager {
    public static void genTimeData(){
        String gson = new GsonBuilder().setPrettyPrinting().create().toJson(new HashMapOf<>("minecraft:overworld", new TimeDataStorage()));
        File file = new File("./config/time-and-wind/time-data.json");
        fileWriter(file, gson);
    }

    public static void generateSysTimeCfg(){
        String gson = new GsonBuilder().setPrettyPrinting().create().toJson(new SystemTimeConfig("5:00", "20:00", "3:00"));
        File file = new File("./config/time-and-wind/system-time-data-global.json");
        fileWriter(file, gson);
    }

    public static void generateMapSysTime(){
        String gson2 = new GsonBuilder().setPrettyPrinting().create().toJson(new HashMapOf<>("minecraft:overworld", new SystemTimeConfig("5:00", "20:00", "3:00")));
        File file2 = new File("./config/time-and-wind/system-time-data.json");
        fileWriter(file2, gson2);
    }



    public static void updateModConfig(ModConfig config){
        String gson = new GsonBuilder().setPrettyPrinting().create().toJson(config);
        File file = new File("./config/time-and-wind/config.json");
        fileWriter(file, gson);
    }

    public static void updateMapSysTime(String worldId, String sunrise, String sunset, String timeZone){
        TimeAndWindCT.sysTimeMap.put(worldId, new SystemTimeConfig(sunrise, sunset, timeZone));
        String gson = new GsonBuilder().setPrettyPrinting().create().toJson(TimeAndWindCT.sysTimeMap);
        File file = new File("./config/time-and-wind/system-time-data.json");
        fileWriter(file, gson);
    }

    public static void updateGlobalSysTimeCfg(String sunrise, String sunset, String timezone){
        TimeAndWindCT.systemTimeConfig = new SystemTimeConfig(sunrise, sunset, timezone);
        String gson = new GsonBuilder().setPrettyPrinting().create().toJson(TimeAndWindCT.systemTimeConfig);
        File file = new File("./config/time-and-wind/system-time-data-global.json");
        fileWriter(file, gson);
    }

    public static SystemTimeConfig readGlobalSysTimeCfg(){
        SystemTimeConfig config;
        try {
            config = new Gson().fromJson(new FileReader("./config/time-and-wind/system-time-data-global.json"), SystemTimeConfig.class);
        } catch (IOException e){
            e.printStackTrace();
            config = new SystemTimeConfig("7:00", "19:00", "local");
        }
        return config;
    }

    public static HashMap<String, SystemTimeConfig> readSysTimeCfg(){
        HashMap<String, SystemTimeConfig> map;
        try {
            map = new Gson().fromJson(new FileReader("./config/time-and-wind/system-time-data.json"), new TypeToken<HashMap<String, SystemTimeConfig>>(){}.getType());
        } catch (IOException e){
            e.printStackTrace();
            map = new HashMapOf<>("minecraft:overworld", new SystemTimeConfig("5:00", "20:00", "3:00"));
        }
        return map;
    }

    public static void generateModConfig(){
        String gson = new GsonBuilder().setPrettyPrinting().create().toJson(new ModConfig(true, false, false,
                true, 30, true, 50));
        File file = new File("./config/time-and-wind/config.json");
        fileWriter(file, gson);
    }

    public static ModConfig readModConfig(){
        ModConfig config;
        try {
            config = new Gson().fromJson(new FileReader("./config/time-and-wind/config.json"), ModConfig.class);
            if(config.config_ver < 3){
                config = patchModConfigV1(config);
                String gson = new GsonBuilder().setPrettyPrinting().create().toJson(config);
                File file = new File("./config/time-and-wind/config.json");
                fileWriter(file, gson);
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
        String gson = new GsonBuilder().setPrettyPrinting().create().toJson(TimeAndWindCT.timeDataMap);
        File file = new File("./config/time-and-wind/time-data.json");
        fileWriter(file, gson);
    }

    public static void updateTimeData(){
        String gson = new GsonBuilder().setPrettyPrinting().create().toJson(TimeAndWindCT.timeDataMap);
        File file = new File("./config/time-and-wind/time-data.json");
        fileWriter(file, gson);
    }

    public static int readTimeData(){
        HashMap<String, TimeDataStorage> timeDataMap;
        int result;
        try {
            timeDataMap = new Gson().fromJson(new FileReader("./config/time-and-wind/time-data.json"), new TypeToken<HashMap<String, TimeDataStorage>>(){}.getType());
            result = 1;
        } catch (IOException e){
            e.printStackTrace();
            timeDataMap = new HashMapOf<>("minecraft:overworld", new TimeDataStorage());
            result = 0;
        }
        TimeAndWindCT.timeDataMap = timeDataMap;
        return result;
    }

    public static void fileWriter(File file, String gson){
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try(FileWriter writer = new FileWriter(file)) {
            writer.write(gson);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
