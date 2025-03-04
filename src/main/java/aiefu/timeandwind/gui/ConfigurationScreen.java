package aiefu.timeandwind.gui;

import aiefu.timeandwind.TimeAndWind;
import aiefu.timeandwind.config.ModConfig;
import aiefu.timeandwind.config.SystemTimeConfig;
import aiefu.timeandwind.config.TimeDataStorage;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.List;

public class ConfigurationScreen extends Screen {

    private int startY;
    private int startX;
    List<Component> textToRender;
    private int textInterval = 20;

    protected ModConfig config;

    protected HashMap<String, TimeDataStorage> timeConfig;
    protected HashMap<String, SystemTimeConfig> sysTimeConfig;

    public ConfigurationScreen(Component component) {
        super(component);
        this.config = TimeAndWind.config;
        this.timeConfig = TimeAndWind.timeConfig;
        this.sysTimeConfig = TimeAndWind.sysTimeConfig;
    }

    @Override
    protected void init() {
        this.startY = this.height / 2 - 200;
        this.startX = this.width / 2 - 200;
    }

    public void loadModConfig(){

    }
}
