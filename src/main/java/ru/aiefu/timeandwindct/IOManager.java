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
    private HashMap<String, TimeDataStorage> timeData = new HashMapOf<>("minecraft:overworld", new TimeDataStorage());

    public void genTimeData(){
        String gson = new GsonBuilder().setPrettyPrinting().create().toJson(this.timeData);
        File file = new File("./config/time-and-wind/time-data.json");
        fileWriter(file, gson);
    }

    public static void readTimeData(){
        HashMap<String, TimeDataStorage> timeDataMap;
        try {
            timeDataMap = new Gson().fromJson(new FileReader("./config/time-and-wind/time-data.json"), new TypeToken<HashMap<String, TimeDataStorage>>(){}.getType());
        } catch (Exception e){
            e.printStackTrace();
            timeDataMap = new HashMapOf<>("minecraft:overworld", new TimeDataStorage());
        }
        TimeAndWindCT.timeDataMap = timeDataMap;
    }

    public void fileWriter(File file, String gson){
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
