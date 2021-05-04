package ru.aiefu.timeandwind;

import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Schedule;
import net.minecraft.entity.ai.brain.ScheduleBuilder;
import net.minecraft.world.dimension.DimensionType;

import java.util.HashMap;

public class TAWScheduler extends Schedule {

    public static HashMap<String, Schedule> scheduler = new HashMap<>();

    public static void createTAWSchedule(DimensionType dimensionType, String regKey, String type, boolean isBaby){
        IDimType dim = (IDimType) dimensionType;
        int ticksInHour = (int) (dim.getDayDuration() / 12);
        if(!isBaby)
        scheduler.put(regKey + type, new ScheduleBuilder(new Schedule()).withActivity(10, Activity.IDLE).withActivity(ticksInHour, Activity.WORK).withActivity(ticksInHour * 8, Activity.MEET).withActivity(ticksInHour * 10, Activity.IDLE).withActivity((int) dim.getDayDuration(), Activity.REST).build());
        else scheduler.put(regKey + type, new ScheduleBuilder(new Schedule()).withActivity(10, Activity.IDLE).withActivity(ticksInHour * 2, Activity.PLAY).withActivity(ticksInHour * 6, Activity.IDLE).withActivity(ticksInHour * 9, Activity.PLAY).withActivity((int) dim.getDayDuration(), Activity.REST).build());
    }
}
