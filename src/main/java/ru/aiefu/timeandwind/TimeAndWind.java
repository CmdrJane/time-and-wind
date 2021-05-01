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

	public static void sendTimeSyncPacket(ServerPlayerEntity player, double dayDuration, double nightDuration){
		CompoundTag tag = new CompoundTag();
		tag.putDouble("dayD", dayDuration);
		tag.putDouble("nightD", nightDuration);
		ServerPlayNetworking.send(player, new Identifier(MOD_ID, "sync_cycle"), new PacketByteBuf(Unpooled.buffer()).writeCompoundTag(tag));
	}

}
