package ru.aiefu.timeandwind;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class TimeAndWind implements ModInitializer {
	public static final String MOD_ID = "timeandwind";
	public static HashMap<String, TimeDataStorage> timeDataMap;

	@Override
	public void onInitialize() {
		craftPaths();
		ServerLifecycleEvents.SERVER_STARTING.register(server -> IOManager.readTimeData());
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> TAWCommands.registerCommands(dispatcher));
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
		}
		catch (IOException e){
			e.printStackTrace();
		}
	}

	public static void sendConfigSyncPacket(ServerPlayerEntity player){
		if(!player.getServer().isHost(player.getGameProfile())) {
			ListTag listTag = new ListTag();
			int i = 0;
			for (Map.Entry<String, TimeDataStorage> e : timeDataMap.entrySet()) {
				CompoundTag tag = new CompoundTag();
				tag.putString("id", e.getKey());
				TimeDataStorage storage = e.getValue();
				tag.putLong("dayD", storage.dayDuration);
				tag.putLong("nightD", storage.nightDuration);
				listTag.add(i, tag);
				++i;
			}
			CompoundTag tag = new CompoundTag();
			tag.put("tawConfig", listTag);
			ServerPlayNetworking.send(player, new Identifier(MOD_ID, "sync_config"), new PacketByteBuf(Unpooled.buffer()).writeCompoundTag(tag));
		}
	}
	public static String get24TimeFormat(World world){
		if(world != null){
			double duration = ((IDimType) world.getDimension()).getCycleDuration();
			double currentTime =  world.getTimeOfDay() % duration;
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

	public static int [] get24TimeFormatRaw(World world){
		if(world != null){
			double duration = ((IDimType) world.getDimension()).getCycleDuration();
			double currentTime =  world.getTimeOfDay() % duration;
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
