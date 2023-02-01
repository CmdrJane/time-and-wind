package ru.aiefu.timeandwindct.time;

import net.minecraft.world.level.Level;
import ru.aiefu.timeandwindct.ITimeOperations;

public class MinecraftTimeUnit {
    private ITimeOperations level;
    private float fraction;

    public MinecraftTimeUnit(Level level){
        this.level = (ITimeOperations) level;
    }

    public boolean isDay(){
        return level.getTimeOfDayTAW() % 24000 < 12001;
    }

    public boolean shouldUpdate(int mod){
        return level.getTimeOfDayTAW() % mod == 0;
    }

    public void update(int ticksToAdd, float fraction, boolean skip){
        this.fraction += fraction;
        if(this.fraction > 1.0F){
            this.fraction -= 1.0F;
            if(skip) return;
            ticksToAdd += 1;
        }
        level.setTimeOfDayTAW(level.getTimeOfDayTAW() + ticksToAdd);
    }

    public ITimeOperations getLevel(){
        return level;
    }

    public void accelerate(int val){
        level.setTimeOfDayTAW(level.getTimeOfDayTAW() + val);
    }
}
