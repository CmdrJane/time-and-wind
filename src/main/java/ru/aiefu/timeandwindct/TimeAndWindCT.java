package ru.aiefu.timeandwindct;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class TimeAndWindCT implements ModInitializer {
	public static final String MOD_ID = "tawct";
	public static final Logger LOGGER = LogManager.getLogger();
	public static HashMap<String, TimeDataStorage> timeDataMap;
	public static ModConfig CONFIG;
	public static boolean debugMode = false;

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
		} else ServerPlayNetworking.send(player, NetworkPacketsID.SETUP_TIME, new PacketByteBuf(Unpooled.buffer()));
	}
}
