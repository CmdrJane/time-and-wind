package aiefu.timeandwind.mixin;

import aiefu.timeandwind.TimeAndWind;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixins {


	@Shadow public abstract Iterable<ServerLevel> getAllLevels();

	@Inject(at = @At("TAIL"), method = "loadLevel")
	private void afterLevelsLoadedAndPrepared(CallbackInfo info) {
		TimeAndWind.assignTickers(this.getAllLevels());
	}
}