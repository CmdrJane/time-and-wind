package ru.aiefu.timeandwindct.time;

import net.minecraft.world.level.Level;
import ru.aiefu.timeandwindct.ITimeOperations;

public class MinecraftTimeUnit {
    private Level level;
    private ITimeOperations timeLevel;
    private float fraction;

    public MinecraftTimeUnit(Level level){
        this.level = level;
        this.timeLevel = (ITimeOperations) level;
    }

    public boolean isDay(){
        return timeLevel.getTimeOfDayTAW() % 24000 < 12001;
    }

    public boolean shouldUpdate(int mod){
        return timeLevel.getTimeTAW() % mod == 0;
    }

    public void update(int ticksToAdd, float fraction, boolean skip){
        this.fraction += fraction;
        if(this.fraction > 1.0F){
            this.fraction -= 1.0F;
            if(skip) return;
            ticksToAdd += 1;
        }
        this.timeLevel.setTimeOfDayTAW(timeLevel.getTimeOfDayTAW() + ticksToAdd);
    }

    public ITimeOperations getTimeLevel(){
        return timeLevel;
    }

    public Level getLevel() {
        return level;
    }

    public void accelerate(int val){
        this.timeLevel.setTimeOfDayTAW(this.timeLevel.getTimeOfDayTAW() + val);
    }
}
