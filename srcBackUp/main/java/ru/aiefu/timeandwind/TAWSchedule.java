package ru.aiefu.timeandwind;

import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Schedule;
import net.minecraft.entity.ai.brain.ScheduleBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionType;

public class TAWSchedule extends Schedule {
    public static Schedule VILLAGER_TAW;

    public static Schedule createTAWSchedule(DimensionType dimensionType, String id){
        IDimType dim = (IDimType) dimensionType;
        Identifier id1 = new Identifier(id);
        Schedule reg = Registry.SCHEDULE.containsId(id1) ? Registry.SCHEDULE.get(id1) : Registry.register(Registry.SCHEDULE, id, new Schedule());
        int ticksInHour = (int) (dim.getDayDuration() / 12);
        VILLAGER_TAW = new ScheduleBuilder(reg).withActivity(10, Activity.IDLE).withActivity(ticksInHour * 2, Activity.WORK).withActivity(ticksInHour * 8, Activity.MEET).withActivity(ticksInHour * 10, Activity.IDLE).withActivity((int) dim.getDayDuration(), Activity.REST).build();
        return VILLAGER_TAW;
    }
}
