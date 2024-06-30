package ru.aiefu.timeandwindct;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import ru.aiefu.timeandwindct.config.TimeDataStorage;
import ru.aiefu.timeandwindct.payloads.*;
import ru.aiefu.timeandwindct.tickers.DefaultTicker;
import ru.aiefu.timeandwindct.tickers.SystemTimeTicker;
import ru.aiefu.timeandwindct.tickers.TimeTicker;

public class TimeAndWindCTClient implements ClientModInitializer {
    /**
     * Runs the mod initializer on the client environment.
     */
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(SyncConfigPayload.ID, (payload, context) -> {
            TimeAndWindCT.modConfig = payload.modConfig();
            TimeAndWindCT.systemTimeConfig = payload.systemTimeConfig();
            TimeAndWindCT.timeDataMap = payload.timeDataMap();
            TimeAndWindCT.sysTimeMap = payload.sysTimeMap();

            TimeAndWindCT.LOGGER.info("[Time & Wind] Configuration synchronized");

            Minecraft client = context.client();
            ClientLevel clientWorld = client.level;

            if (clientWorld == null) {
                return;
            }

            String worldId = clientWorld.dimension().location().toString();
            ITimeOperations timeOps = (ITimeOperations) clientWorld;

            if (TimeAndWindCT.modConfig.syncWithSystemTime) {
                if (TimeAndWindCT.modConfig.systemTimePerDimensions
                    && TimeAndWindCT.sysTimeMap.containsKey(worldId)
                ) {
                    timeOps.time_and_wind_custom_ticker$setTimeTicker(new SystemTimeTicker(
                        (ITimeOperations) clientWorld,
                        TimeAndWindCT.sysTimeMap.get(worldId)
                    ));
                } else {
                    timeOps.time_and_wind_custom_ticker$setTimeTicker(new SystemTimeTicker(
                        (ITimeOperations) clientWorld,
                        TimeAndWindCT.systemTimeConfig
                    ));
                }

                TimeAndWindCT.LOGGER.info("[Time & Wind] System time ticker synchronized");
            } else {
                if (TimeAndWindCT.timeDataMap.containsKey(worldId)) {
                    TimeDataStorage storage = TimeAndWindCT.timeDataMap.get(worldId);

                    timeOps.time_and_wind_custom_ticker$setTimeTicker(new TimeTicker(
                        storage.dayDuration,
                        storage.nightDuration,
                        clientWorld
                    ));

                    TimeAndWindCT.LOGGER.info(
                        "[Time & Wind] Custom time ticker for world " + worldId + " synchronized"
                    );
                } else {
                    timeOps.time_and_wind_custom_ticker$setTimeTicker(new DefaultTicker());
                }
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(CfgDebugInfoPayload.ID, (payload, context) -> {
            Minecraft client = context.client();

            if (client.level == null || client.player == null) {
                return;
            }

            String worldId = client.level.dimension().location().toString();

            if (((ITimeOperations) client.level).time_and_wind_custom_ticker$getTimeTicker() instanceof SystemTimeTicker) {
                return;
            }

            if (TimeAndWindCT.timeDataMap == null) {
                client.player.sendSystemMessage(Component.literal(
                    "[Client Side] TimeDataMap is NULL, this is a bug"
                ));
            } else if (TimeAndWindCT.timeDataMap.containsKey(worldId)) {
                TimeDataStorage storage = TimeAndWindCT.timeDataMap.get(worldId);

                client.player.sendSystemMessage(Component.literal(
                    "Client config for current world: Day Duration: "
                        + storage.dayDuration
                        + " Night Duration: "
                        + storage.nightDuration
                ));
            } else {
                client.player.sendSystemMessage(Component.literal(
                    "No Data found for current world on client side"
                ));
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(WorldIdClipboardPayload.ID, (payload, context) -> {
            Minecraft client = context.client();
            LocalPlayer player = client.player;

            if (player == null) {
                return;
            }

            client.keyboardHandler.setClipboard(payload.worldId());

            client.player.displayClientMessage(
                Component.literal("Also copied this to clipboard"),
                false
            );
        });

        ClientPlayNetworking.registerGlobalReceiver(SetupTimeLocalPayload.ID, (payload, context) -> {
            Minecraft client = context.client();
            ClientLevel clientWorld = client.level;

            if (clientWorld == null) {
                return;
            }

            String worldId = clientWorld.dimension().location().toString();
            ITimeOperations timeOps = (ITimeOperations) clientWorld;

            if (TimeAndWindCT.modConfig.syncWithSystemTime) {
                if (TimeAndWindCT.modConfig.systemTimePerDimensions
                    && TimeAndWindCT.sysTimeMap.containsKey(worldId)
                ) {
                    timeOps.time_and_wind_custom_ticker$setTimeTicker(new SystemTimeTicker(
                        (ITimeOperations) clientWorld,
                        TimeAndWindCT.sysTimeMap.get(worldId)
                    ));
                } else {
                    timeOps.time_and_wind_custom_ticker$setTimeTicker(new SystemTimeTicker(
                        (ITimeOperations) clientWorld,
                        TimeAndWindCT.systemTimeConfig
                    ));
                }
            } else if (TimeAndWindCT.timeDataMap != null
                && TimeAndWindCT.timeDataMap.containsKey(worldId)
            ) {
                TimeDataStorage storage = TimeAndWindCT.timeDataMap.get(worldId);

                timeOps.time_and_wind_custom_ticker$setTimeTicker(new TimeTicker(
                    storage.dayDuration,
                    storage.nightDuration,
                    clientWorld
                ));
            } else {
                timeOps.time_and_wind_custom_ticker$setTimeTicker(new DefaultTicker());
            }

            TimeAndWindCT.LOGGER.info("[Time & Wind] Timedata reloaded on client");
        });

        ClientPlayNetworking.registerGlobalReceiver(NightSkipInfoPayload.ID, (payload, context) -> {
            ClientLevel world = Minecraft.getInstance().level;

            if (world != null) {
                ITimeOperations ops = (ITimeOperations) world;
                ops.time_and_wind_custom_ticker$setSkipState(payload.skipState());
                ops.time_and_wind_custom_ticker$setSpeed(payload.accelerationSpeed());
            }
        });
    }
}
