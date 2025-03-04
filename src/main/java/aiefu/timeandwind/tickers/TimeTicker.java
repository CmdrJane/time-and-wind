package aiefu.timeandwind.tickers;

import aiefu.timeandwind.config.TimeDataStorage;
import net.minecraft.world.level.Level;
import aiefu.timeandwind.ITimeOperations;

public class TimeTicker implements Ticker {

    protected final int dayD;
    protected int dayMod;
    protected float dayFraction;
    protected int dayTicksToAdd;
    protected boolean dayAction;

    protected final int nightD;
    protected int nightMod;
    protected float nightFraction;
    protected int nightTicksToAdd;
    protected boolean nightAction;

    protected float fraction;

    public TimeTicker(TimeDataStorage storage){

        this.dayD = storage.dayDuration;
        this.nightD = storage.nightDuration;

        boolean dayB = dayD < 12000;
        boolean nightB = nightD < 12000;

        float dayF = getFactor(dayD, dayB);
        float nightF = getFactor(nightD, nightB);

        int dayWhole = (int) dayF;
        int nightWhole = (int) nightF;

        float dayFraction = dayF - dayWhole;
        float nightFraction = nightF - nightWhole;

        //Finish Setup

        this.dayFraction = dayFraction;
        this.nightFraction = nightFraction;

        if(dayB){
            this.dayMod = 1;
            this.dayTicksToAdd = dayWhole;
            this.dayAction = false;
        } else {
            this.dayMod = dayWhole;
            this.dayTicksToAdd = 1;
            this.dayAction = true;
        }

        if(nightB){
            this.nightMod = 1;
            this.nightTicksToAdd = nightWhole;
            this.nightAction = false;
        } else {
            this.nightMod = nightWhole;
            this.nightTicksToAdd = 1;
            this.nightAction = true;
        }
    }


    public void tick(ITimeOperations world) {
        if(this.isDay(world)){
            if(world.taw$getTimeTAW() % this.dayMod == 0){
                this.update(world,this.dayTicksToAdd, this.dayFraction, this.dayAction);
            }
        } else if(world.taw$getTimeTAW() % this.nightMod == 0){
            this.update(world, this.nightTicksToAdd, this.nightFraction, this.nightAction);
        }
    }

    @Override
    public void accelerate(Level level, int speed) {

    }

    @Override
    public boolean canAccelerate() {
        return true;
    }

    @Override
    public boolean allowSleeping() {
        return true;
    }

    public boolean isDay(ITimeOperations timeLevel){
        return timeLevel.taw$getTimeOfDayTAW() % 24000 < 12001;
    }

    public void update(ITimeOperations timeLevel, int ticksToAdd, float fraction, boolean skip){
        this.fraction += fraction;
        if(this.fraction > 1.0F){
            this.fraction -= 1.0F;
            if(skip) return;
            ticksToAdd += 1;
        }
        timeLevel.taw$setTimeOfDayTAW(timeLevel.taw$getTimeOfDayTAW() + ticksToAdd);
    }

    protected float getFactor(int duration, boolean bl){
        return bl ? 12000.0F / duration : duration / 12000.0F;
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
