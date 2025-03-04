package aiefu.timeandwind.tickers;

import aiefu.timeandwind.TimeAndWind;
import aiefu.timeandwind.config.SystemTimeConfig;
import net.minecraft.world.level.Level;
import aiefu.timeandwind.ITimeOperations;

import java.util.function.LongSupplier;

public class SystemTimeTicker implements Ticker{

    protected long timeZoneOffset;
    protected final LongSupplier systemTime = () -> System.currentTimeMillis() + timeZoneOffset;

    protected int tickMod;
    protected int dayMod;
    protected int nightMod;
    protected int ticks = 0;

    long dayD;
    long nightD;
    int dayF;
    int nightF;
    int sunrise;
    int sunset;

    public SystemTimeTicker(SystemTimeConfig config){
        this.sunrise = config.getSunriseMs();
        this.sunset = config.getSunsetMs();
        this.timeZoneOffset = config.getTimeOffset();
        if(sunrise < sunset){
            dayD = sunset - sunrise;
            nightD = 86_400_000 - dayD;
        } else {
            nightD = sunrise - sunset;
            dayD = 86_400_000 - nightD;
        }
        this.dayF = (int) (dayD / 12000);
        this.nightF = (int) (nightD / 12000);

        this.dayMod = dayF / 50;
        this.nightMod = nightF / 50;
    }

    public void initTime(ITimeOperations world){
        int startingTick = calculateCurrentTick();
        world.taw$setTimeOfDayTAW(unwrapTime(startingTick, world.taw$getTimeOfDayTAW()));
    }

    public void tick(ITimeOperations world){
        ++ticks;
        long time = world.taw$getTimeOfDayTAW();
        tickMod = time % 24000L < 12000 ? dayMod : nightMod;
        if(ticks % tickMod == 0){
            world.taw$setTimeOfDayTAW(time + 1L);
        }
        if(!world.taw$isClient() && ticks % 6000 == 0){
            TimeAndWind.LOGGER.info("Checking if time corrections is needed...");
            int targetTicks = calculateCurrentTick();
            int timeTicks = (int) (time % 24000L);
            if(targetTicks != timeTicks){
                world.taw$setTimeOfDayTAW(unwrapTime(targetTicks, time));
                TimeAndWind.LOGGER.info("Time corrected");
            } else TimeAndWind.LOGGER.info("Skipping correction");
        }
    }

    @Override
    public void accelerate(Level level, int speed) {

    }

    @Override
    public boolean canAccelerate() {
        return false;
    }

    @Override
    public boolean allowSleeping() {
        return false;
    }

    public void updateTime(ITimeOperations world){
        world.taw$setTimeOfDayTAW(unwrapTime(calculateCurrentTick(), world.taw$getTimeOfDayTAW()));
    }

    protected int calculateCurrentTick(){
        long time = calculateElapsedTimeWithShift();
        int returnVal;
        if(time < dayD){
            returnVal = (int) (time / dayF);
        } else returnVal = (int) (12000 + ((time - dayD) / nightF));
        return returnVal;
    }

    protected long calculateElapsedTimeWithShift(){
        long currentTime = systemTime.getAsLong()  % 86_400_000;
        int val = (int) (currentTime - sunrise); /// currentTime - shift
        if(val < 0){
            val = (int) (86_400_000 - (sunrise - currentTime)); //shift - currentTime
        }
        return val;
    }

    protected long unwrapTime(int targetTime, long timeOfDay){
        long zeroPoint = timeOfDay - timeOfDay % 24000L;
        return zeroPoint + targetTime;
    }

    public int getSunrise(){
        return this.sunrise;
    }
    public int getSunset(){
        return this.sunset;
    }
    public int getDayMod(){
        return this.dayMod;
    }
    public int getNightMod(){
        return this.nightMod;
    }
    public long getDayD(){
        return this.dayD;
    }
    public long getNightD(){
        return this.nightD;
    }
}
