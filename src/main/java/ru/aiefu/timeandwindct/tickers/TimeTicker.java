package ru.aiefu.timeandwindct.tickers;

import net.minecraft.world.level.Level;
import ru.aiefu.timeandwindct.ITimeOperations;

public class TimeTicker implements Ticker {

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

    private final Level level;
    private final ITimeOperations timeLevel;
    private float fraction;

    public TimeTicker(int dayD, int nightD, Level level){

        this.level = level;
        this.timeLevel = (ITimeOperations) level;

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

        this.finishSetup(dayB, nightB, dayWhole, nightWhole, dayFraction, nightFraction);
    }


    public void tick(ITimeOperations world, boolean nskip, int acceleration) {
        if(nskip){
            this.accelerate(acceleration);
            if(this.level.isDay()){
                this.timeLevel.time_and_wind_custom_ticker$wakeUpAllPlayersTAW();
            }
        }
        else if(this.isDay()){
            if(this.timeLevel.time_and_wind_custom_ticker$getTimeTAW() % this.dayMod == 0){
                this.update(this.dayTicksToAdd, this.dayFraction, this.dayAction);
            }
        } else if(this.timeLevel.time_and_wind_custom_ticker$getTimeTAW() % this.nightMod == 0){
            this.update(this.nightTicksToAdd, this.nightFraction, this.nightAction);
        }
    }

    public boolean isDay(){
        return this.timeLevel.time_and_wind_custom_ticker$getTimeOfDayTAW() % 24000 < 12001;
    }

    public void update(int ticksToAdd, float fraction, boolean skip){
        this.fraction += fraction;
        if(this.fraction > 1.0F){
            this.fraction -= 1.0F;
            if(skip) return;
            ticksToAdd += 1;
        }
        this.timeLevel.time_and_wind_custom_ticker$setTimeOfDayTAW(this.timeLevel.time_and_wind_custom_ticker$getTimeOfDayTAW() + ticksToAdd);
    }

    public void accelerate(int val){
        this.timeLevel.time_and_wind_custom_ticker$setTimeOfDayTAW(this.timeLevel.time_and_wind_custom_ticker$getTimeOfDayTAW() + val);
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
