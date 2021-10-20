package ru.aiefu.timeandwindct.tickers;

import ru.aiefu.timeandwindct.ITimeOperations;

public class TimeTicker implements Ticker{

    private final long dayD;
    private final int dayMod;
    private final double dayRoundingError;
    private final long nightD;
    private final int nightMod;
    private final double nightRoundingError;

    private final boolean isDayLesserThanVanilla;
    private final boolean isNightLesserThanVanilla;

    private double leftOver = 0;
    private double leftOverInverted = 0;

    public TimeTicker(long dayD, long nightD){
        double dayVal;
        double nightVal;
        boolean dayInversion = dayD < 12000;
        boolean nightInversion = nightD < 12000;
        dayVal = getFactor(dayD, dayInversion);
        nightVal = getFactor(nightD, nightInversion);
        this.isDayLesserThanVanilla = dayInversion;
        this.isNightLesserThanVanilla = nightInversion;
        int checkDay = (int) dayVal;
        int checkNight = (int) nightVal;
        this.dayMod = checkDay;
        this.nightMod = checkNight;

        this.dayRoundingError = dayVal - checkDay;
        this.nightRoundingError = nightVal - checkNight;

        this.dayD = dayD;
        this.nightD = nightD;
    }

    public void tick(ITimeOperations world, boolean nskip, int acceleration) {
        long time = world.getTimeOfDayTAW();
        if(nskip){
            world.setTimeOfDayTAW(time + acceleration);
            return;
        }
        int currentTime = (int) (time % 24000);
        int mod;
        double leftOverToAdd;
        if(currentTime < 12000){
            mod = dayMod;
            leftOverToAdd = dayRoundingError;
            if(isDayLesserThanVanilla){
                tickTime(world, mod, leftOverToAdd, time, currentTime, 12000);
                return;
            }
        } else {
            mod = nightMod;
            leftOverToAdd = nightRoundingError;
            if(isNightLesserThanVanilla){
                tickTime(world, mod, leftOverToAdd, time, currentTime, 24000);
                return;
            }
        }
        if(world.getTimeTAW() % mod == 0){
            if(leftOver >= 1.0D){
                leftOver -= 1;
                return;
            }
            leftOver += leftOverToAdd;
            world.setTimeOfDayTAW(time + 1L);
        }
    }
    private void tickTime(ITimeOperations world, int mod, double leftOverToAdd, long timeOfDay, int currentTime, int cap){
        if(currentTime + mod > cap){
            world.setTimeOfDayTAW(timeOfDay + (cap - currentTime));
            leftOverInverted = 0.0D;
            return;
        }
        if(leftOverInverted >= 1.0D){
            world.setTimeOfDayTAW(timeOfDay + mod + 1L);
            leftOverInverted -= 1;
            return;
        }
        world.setTimeOfDayTAW(timeOfDay + mod);
        leftOverInverted += leftOverToAdd;
    }

    private double getFactor(long value, boolean shouldInverse){
        return shouldInverse ? 12000.0D / value : value / 12000.0D;
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
        return this.dayRoundingError;
    }
    public double getNightRoundingError(){
        return this.nightRoundingError;
    }
}
