package aiefu.timeandwind;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;

public class TawJsonUtils {
    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    public static <T> T loadJson(String path, Type type) throws FileNotFoundException {
        return gson.fromJson(new FileReader(path), type);
    }

    public static <T> T loadJson(String path, Class<T> type) throws FileNotFoundException {
        return gson.fromJson(new FileReader(path), type);
    }

    public static JsonObject loadJsonObject(String path) throws FileNotFoundException {
        return JsonParser.parseReader(new FileReader(path)).getAsJsonObject();
    }

    public static <T> void writeToJsonFile(T target, String path) throws IOException {
        try(FileWriter writer = new FileWriter(path)){
            gson.toJson(target, writer);
        }
    }

    public static <T> T readYaml(String path, Class<T> type) throws IOException {
        return mapper.readValue(new FileReader(path), type);
    }

    public static <T> T readYaml(String path, Class<?> c, Class<?>... params) throws IOException {
        return mapper.readValue(new FileReader(path), mapper.getTypeFactory().constructParametricType(c, params));
    }


    public static <T> void writeYaml(T target, String path) throws IOException {
        try(FileWriter writer = new FileWriter(path)){
            mapper.writeValue(writer, target);
        }
    }

    public static <T> JsonObject convertToJsonObject(T target){
        return gson.toJsonTree(target).getAsJsonObject();
    }


}
