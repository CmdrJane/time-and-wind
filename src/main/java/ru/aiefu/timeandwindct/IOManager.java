package ru.aiefu.timeandwindct;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

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

    public static void generateModConfig(){
        String gson = new GsonBuilder().setPrettyPrinting().create().toJson(new ModConfig(true, false));
        File file = new File("./config/time-and-wind/config.json");
        fileWriter(file, gson);
    }

    public static ModConfig readModConfig(){
        ModConfig config;
        try {
            config = new Gson().fromJson(new FileReader("./config/time-and-wind/config.json"), ModConfig.class);
        } catch (IOException e){
            e.printStackTrace();
            config = new ModConfig(true, false);
        }
        return config;
    }

    public static void updateTimeData(String id, long dayD, long nightD){
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
