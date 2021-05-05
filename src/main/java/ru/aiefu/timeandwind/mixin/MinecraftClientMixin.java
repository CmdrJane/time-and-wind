package ru.aiefu.timeandwind.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.aiefu.timeandwind.IDimType;
import ru.aiefu.timeandwind.TAWScheduler;
import ru.aiefu.timeandwind.TimeAndWind;
import ru.aiefu.timeandwind.TimeDataStorage;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Shadow @Nullable public ClientWorld world;

    @Inject(method = "joinWorld", at =@At(value = "INVOKE", target = "net/minecraft/client/MinecraftClient.setWorld(Lnet/minecraft/client/world/ClientWorld;)V"))
    private void attachTimeData(ClientWorld world, CallbackInfo ci){
        String worldId = this.world.getRegistryKey().getValue().toString();
        DimensionType dimension = this.world.getDimension();
        if (TimeAndWind.timeDataMap.containsKey(worldId)) {
            TimeDataStorage storage = TimeAndWind.timeDataMap.get(worldId);
            ((IDimType) dimension).setCycleDuration(storage.dayDuration, storage.nightDuration);
        }

        TAWScheduler.createTAWSchedule(dimension, this.world.getRegistryKey().getValue().getPath(), "_villager_taw", false);
        TAWScheduler.createTAWSchedule(dimension, this.world.getRegistryKey().getValue().getPath(), "_villager_baby_taw", true);
    }
}
