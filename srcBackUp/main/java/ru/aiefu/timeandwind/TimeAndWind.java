package ru.aiefu.timeandwind;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

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
		ServerLifecycleEvents.SERVER_STARTED.register(server -> server.getWorlds().forEach(serverWorld -> {
			String id = serverWorld.getRegistryKey().getValue().toString();
			if(timeDataMap.containsKey(id)){
				((IDimType)serverWorld.getDimension()).setCycleDuration(timeDataMap.get(id).dayDuration, timeDataMap.get(id).nightDuration);
			}
		}));
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
			TAWCommands.reloadCfgReg(dispatcher);
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
			IOManager.readTimeData();

		}
		catch (IOException e){
			e.printStackTrace();
		}
	}

	public static void sendTimeSyncPacket(ServerPlayerEntity player, long dayDuration, long nightDuration){
		CompoundTag tag = new CompoundTag();
		tag.putLong("dayD", dayDuration);
		tag.putLong("nightD", nightDuration);
		ServerPlayNetworking.send(player, new Identifier(MOD_ID, "sync_cycle"), new PacketByteBuf(Unpooled.buffer()).writeCompoundTag(tag));
	}
	public static void sendConfigSyncPacket(ServerPlayerEntity player){
		
	}

	public static String get24TimeFormat(long time, World world){
		if(world != null){
			double duration = ((IDimType) world.getDimension()).getCycleDuration();
			double currentTime = time % duration;
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

	public static int [] get24TimeFormatRaw(long time, World world){
		if(world != null){
			double duration = ((IDimType) world.getDimension()).getCycleDuration();
			double currentTime = time % duration;
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

}
