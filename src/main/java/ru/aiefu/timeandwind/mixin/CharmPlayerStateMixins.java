package ru.aiefu.timeandwind.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import ru.aiefu.timeandwind.IDimType;
import svenhjol.charm.module.player_state.PlayerState;

@Pseudo
@Mixin(PlayerState.class)
public class CharmPlayerStateMixins {
    @ModifyConstant(method = "serverCallback", constant = @Constant(longValue = 24000L), remap = false)
    private static long patchDayDuration(long l, ServerPlayerEntity player){
        return ((IDimType)player.world.getDimension()).getCycleDuration();
    }
    @ModifyConstant(method = "serverCallback", constant = @Constant(longValue = 12700L), remap = false)
    private static long patchHalfDayDuration(long l, ServerPlayerEntity player){
        return ((IDimType)player.world.getDimension()).getCycleDuration() / 2;
    }
}
