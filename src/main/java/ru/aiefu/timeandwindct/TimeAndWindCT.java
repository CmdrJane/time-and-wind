package ru.aiefu.timeandwindct;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import org.apache.logging.log4j.Logger;
import ru.aiefu.timeandwindct.commands.TAWCommands;
import ru.aiefu.timeandwindct.packets.SyncConfig;
import ru.aiefu.timeandwindct.packets.SyncModConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

@Mod(modid = TimeAndWindCT.MODID, name = TimeAndWindCT.NAME, version = TimeAndWindCT.VERSION)
public class TimeAndWindCT {

    public static final String MODID = "tawct";
    public static final String NAME = "Time & Wind CT Forge";
    public static final String VERSION = "1.0";

    public static HashMap<String, TimeDataStorage> timeDataMap;

    public static ModConfig CONFIG;
    public static boolean debugMode = false;

    private static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        craftPaths();
        MinecraftForge.EVENT_BUS.register(this);
        TAWNetworkHandler.registerMessages(MODID);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {

    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent e){
        EntityPlayer player = e.player;
        if(!player.world.isRemote){
            TAWNetworkHandler.INSTANCE.sendTo(new SyncConfig(), (EntityPlayerMP) player);
            TAWNetworkHandler.INSTANCE.sendTo(new SyncModConfig(), (EntityPlayerMP) player);
            logger.info("Sending config to client...");
        }
    }

    @EventHandler
    public void onServerStartup(FMLServerStartingEvent e){
        e.registerServerCommand(new TAWCommands());
    }

    @EventHandler
    public void onServerStartupEarly(FMLServerAboutToStartEvent e){
        IOManager.readTimeData();
    }

    public static Logger getLogger(){
        return logger;
    }

    public void craftPaths(){
        try{
            if(!Files.isDirectory(Paths.get("./config"))){
                Files.createDirectory(Paths.get("./config"));
            }
            if(!Files.isDirectory(Paths.get("./config/time-and-wind"))){
                Files.createDirectory(Paths.get("./config/time-and-wind"));
            }
            if(!Files.exists(Paths.get("./config/time-and-wind/time-data.json"))){
                IOManager.genTimeData();
            }
            if(!Files.exists(Paths.get("./config/time-and-wind/config.json"))){
                IOManager.generateModConfig();
            }
            CONFIG = IOManager.readModConfig();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
