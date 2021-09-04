package ru.aiefu.timeandwind;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class TimeAndWind implements ModInitializer {
	public static final String MOD_ID = "timeandwind";
	public static final Logger LOGGER = LogManager.getLogger();
	public static HashMap<String, TimeDataStorage> timeDataMap;

	@Override
	public void onInitialize() {
		LOGGER.info("[Time & Wind] Initializing...");
		craftPaths();
		registerReceivers();
		ServerLifecycleEvents.SERVER_STARTING.register(server -> IOManager.readTimeData());
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> TAWCommands.registerCommands(dispatcher));
		LOGGER.info("[Time & Wind] I'm in time control now!");
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

	public static void checkConfig(MinecraftServer server){

	}

	public static void sendConfigSyncPacket(ServerPlayerEntity player){
		if(!player.getServer().isHost(player.getGameProfile())) {
			NbtList listTag = new NbtList();
			int i = 0;
			for (Map.Entry<String, TimeDataStorage> e : timeDataMap.entrySet()) {
				NbtCompound tag = new NbtCompound();
				tag.putString("id", e.getKey());
				TimeDataStorage storage = e.getValue();
				tag.putLong("dayD", storage.dayDuration);
				tag.putLong("nightD", storage.nightDuration);
				listTag.add(i, tag);
				++i;
			}
			NbtCompound tag = new NbtCompound();
			tag.put("tawConfig", listTag);
			ServerPlayNetworking.send(player, NetworkPacketsID.SYNC_CONFIG, new PacketByteBuf(Unpooled.buffer()).writeNbt(tag));
			LOGGER.info("[Time & Wind] Sending config to player");
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
	private void registerReceivers(){
		ServerPlayNetworking.registerGlobalReceiver(new Identifier(TimeAndWind.MOD_ID, "request_resync"), (server, player, handler, buf, responseSender) -> {
			sendConfigSyncPacket(player);
			LOGGER.warn("[Time & Wind] Player requested config resync, this shouldn't happen");
			LOGGER.info("[Time & Wind] Sending resync packet");
		});
	}
}
