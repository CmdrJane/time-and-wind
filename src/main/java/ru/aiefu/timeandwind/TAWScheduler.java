package ru.aiefu.timeandwind;

import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.entity.ai.brain.schedule.Schedule;
import net.minecraft.entity.ai.brain.schedule.ScheduleBuilder;
import net.minecraft.world.DimensionType;

import java.util.HashMap;


public class TAWScheduler extends Schedule {

    public static HashMap<String, Schedule> scheduler = new HashMap<>();

    public static void createTAWSchedule(DimensionType dimensionType, String regKey, String type, boolean isBaby){
        IDimType dim = (IDimType) dimensionType;
        int ticksInHour = (int) (dim.getDayDuration() / 12);
        if(!isBaby)
        scheduler.put(regKey + type, new ScheduleBuilder(new Schedule()).changeActivityAt(10, Activity.IDLE).changeActivityAt(ticksInHour, Activity.WORK).changeActivityAt(ticksInHour * 8, Activity.MEET).changeActivityAt(ticksInHour * 10, Activity.IDLE).changeActivityAt((int) dim.getDayDuration(), Activity.REST).build());
        else scheduler.put(regKey + type, new ScheduleBuilder(new Schedule()).changeActivityAt(10, Activity.IDLE).changeActivityAt(ticksInHour * 2, Activity.PLAY).changeActivityAt(ticksInHour * 6, Activity.IDLE).changeActivityAt(ticksInHour * 9, Activity.PLAY).changeActivityAt((int) dim.getDayDuration(), Activity.REST).build());
    }
}
