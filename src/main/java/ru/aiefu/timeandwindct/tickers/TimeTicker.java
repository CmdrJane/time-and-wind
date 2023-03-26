package ru.aiefu.timeandwindct.tickers;

import net.minecraft.world.level.Level;
import ru.aiefu.timeandwindct.ITimeOperations;
import ru.aiefu.timeandwindct.time.MinecraftTimeUnit;

public class TimeTicker implements Ticker {

    private final MinecraftTimeUnit timeUnit;

    private final int dayD;
    private int dayMod;
    private float dayFraction;
    private int dayTicksToAdd;
    private boolean dayAction;

    private final int nightD;
    private int nightMod;
    private float nightFraction;
    private int nightTicksToAdd;
    private boolean nightAction;

    public TimeTicker(int dayD, int nightD, Level level){
        timeUnit = new MinecraftTimeUnit(level);

        this.dayD = dayD;
        this.nightD = nightD;

        boolean dayB = dayD < 12000;
        boolean nightB = nightD < 12000;

        float dayF = getFactor(dayD, dayB);
        float nightF = getFactor(nightD, nightB);

        int dayWhole = (int) dayF;
        int nightWhole = (int) nightF;

        float dayFraction = dayF - dayWhole;
        float nightFraction = nightF - nightWhole;

        finishSetup(dayB, nightB, dayWhole, nightWhole, dayFraction, nightFraction);
    }


    public void tick(ITimeOperations world, boolean nskip, int acceleration) {
        if(nskip){
            timeUnit.accelerate(acceleration);
            if(timeUnit.getLevel().isDay()){
                timeUnit.getTimeLevel().wakeUpAllPlayersTAW();
            }
        }
        else if(timeUnit.isDay()){
            if(timeUnit.shouldUpdate(dayMod)){
                timeUnit.update(dayTicksToAdd, dayFraction, dayAction);
            }
        } else if(timeUnit.shouldUpdate(nightMod)){
            timeUnit.update(nightTicksToAdd, nightFraction, nightAction);
        }
    }

    private float getFactor(int duration, boolean bl){
        return bl ? 12000.0F / duration : duration / 12000.0F;
    }

    public void finishSetup(boolean day, boolean night, int dayWhole, int nightWhole, float dayFraction, float nightFraction){
        this.dayFraction = dayFraction;
        this.nightFraction = nightFraction;

        if(day){
            this.dayMod = 1;
            this.dayTicksToAdd = dayWhole;
            this.dayAction = false;
        } else {
            this.dayMod = dayWhole;
            this.dayTicksToAdd = 1;
            this.dayAction = true;
        }

        if(night){
            this.nightMod = 1;
            this.nightTicksToAdd = nightWhole;
            this.nightAction = false;
        } else {
            this.nightMod = nightWhole;
            this.nightTicksToAdd = 1;
            this.nightAction = true;
        }
    }

    public long getDayD(){
        return this.dayD;
    }
    public long getNightD(){
        return this.nightD;
    }
    public int getDayMod(){
        return this.dayMod;
    }
    public int getNightMod(){
        return this.nightMod;
    }
    public double getDayRoundingError(){
        return this.dayFraction;
    }
    public double getNightRoundingError(){
        return this.nightFraction;
    }
}
