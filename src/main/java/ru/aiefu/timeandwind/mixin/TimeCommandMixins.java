package ru.aiefu.timeandwind.mixin;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.TimeCommand;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import ru.aiefu.timeandwind.IDimType;

@Mixin(TimeCommand.class)
public class TimeCommandMixins {
    @ModifyConstant(method = "method_13794(Lcom/mojang/brigadier/context/CommandContext;)I", constant = @Constant(intValue = 6000))
    private static int modifyNoonValue(int value, CommandContext<ServerCommandSource> context){
        return (int) (((IDimType) context.getSource().getWorld().getDimension()).getDayDuration() / 2);
    }
    @ModifyConstant(method = "method_13792(Lcom/mojang/brigadier/context/CommandContext;)I", constant = @Constant(intValue = 1000))
    private static int modifyDayValue(int value, CommandContext<ServerCommandSource> context){
        return (int) (((IDimType) context.getSource().getWorld().getDimension()).getDayDuration() / 100 * 5);
    }
    @ModifyConstant(method = "method_13797(Lcom/mojang/brigadier/context/CommandContext;)I", constant = @Constant(intValue = 13000))
    private static int modifyNightValue(int value, CommandContext<ServerCommandSource> context){
        IDimType dim = ((IDimType) context.getSource().getWorld().getDimension());
        return (int) (dim.getDayDuration() + (dim.getNightDuration() / 100 * 5));
    }
    @ModifyConstant(method = "method_13785(Lcom/mojang/brigadier/context/CommandContext;)I", constant = @Constant(intValue = 18000))
    private static int modifyMidnightValue(int value, CommandContext<ServerCommandSource> context){
        IDimType dim = ((IDimType) context.getSource().getWorld().getDimension());
        return (int) (dim.getDayDuration() + dim.getNightDuration() / 2);
    }
    @ModifyConstant(method = "getDayTime(Lnet/minecraft/server/world/ServerWorld;)I", constant = @Constant(longValue = 24000L))
    private static long modifyGetDayTimeValue(long value, ServerWorld world){
        return ((IDimType)world.getDimension()).getCycleDuration();
    }
    @ModifyConstant(method = "method_13795(Lcom/mojang/brigadier/context/CommandContext;)I", constant = @Constant(longValue = 24000L))
    private static long modifyTotalDaysValue(long value, CommandContext<ServerCommandSource> context){
        return ((IDimType)context.getSource().getWorld().getDimension()).getCycleDuration();
    }
}
