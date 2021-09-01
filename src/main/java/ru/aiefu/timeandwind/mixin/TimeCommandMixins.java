package ru.aiefu.timeandwind.mixin;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.impl.TimeCommand;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import ru.aiefu.timeandwind.IDimType;

@Mixin(TimeCommand.class)
public class TimeCommandMixins {
    @ModifyConstant(method = "lambda$register$2(Lcom/mojang/brigadier/context/CommandContext;)I", constant = @Constant(intValue = 6000))
    private static int modifyNoonValue(int value, CommandContext<CommandSource> context){
        return (int) (((IDimType) context.getSource().getLevel().dimensionType()).getDayDuration() / 2);
    }
    @ModifyConstant(method = "lambda$register$1(Lcom/mojang/brigadier/context/CommandContext;)I", constant = @Constant(intValue = 1000))
    private static int modifyDayValue(int value, CommandContext<CommandSource> context){
        return (int) (((IDimType) context.getSource().getLevel().dimensionType()).getDayDuration() / 100 * 5);
    }
    @ModifyConstant(method = "lambda$register$3(Lcom/mojang/brigadier/context/CommandContext;)I", constant = @Constant(intValue = 13000))
    private static int modifyNightValue(int value, CommandContext<CommandSource> context){
        IDimType dim = ((IDimType) context.getSource().getLevel().dimensionType());
        return (int) (dim.getDayDuration() + (dim.getNightDuration() / 100 * 5));
    }
    @ModifyConstant(method = "lambda$register$4(Lcom/mojang/brigadier/context/CommandContext;)I", constant = @Constant(intValue = 18000))
    private static int modifyMidnightValue(int value, CommandContext<CommandSource> context){
        IDimType dim = ((IDimType) context.getSource().getLevel().dimensionType());
        return (int) (dim.getDayDuration() + dim.getNightDuration() / 2);
    }
    @ModifyConstant(method = "getDayTime(Lnet/minecraft/world/server/ServerWorld;)I", constant = @Constant(longValue = 24000L))
    private static long modifyGetDayTimeValue(long value, ServerWorld world){
        return ((IDimType)world.dimensionType()).getCycleDuration();
    }
    @ModifyConstant(method = "lambda$register$9(Lcom/mojang/brigadier/context/CommandContext;)I", constant = @Constant(longValue = 24000L))
    private static long modifyTotalDaysValue(long value, CommandContext<CommandSource> context){
        return ((IDimType)context.getSource().getLevel().dimensionType()).getCycleDuration();
    }
}
