package ru.aiefu.timeandwindct;

public class TimeTicker{

    private boolean enableCustomTicker = true;
    private boolean disableRoundChecker = false;
    private long dayD;
    private int dayMod;
    private double dayRoundingError;
    private long nightD;
    private int nightMod;
    private double nightRoundingError;

    private double leftOver = 0;

    public void tickTime(ITimeOperations world, long timeOfDay){
        if(!enableCustomTicker){
            world.setTimeOfDayTAW(world.getTimeOfDayTAW() + 1L);
            return;
        }
        int currentTime = (int) (timeOfDay % 24000);
        boolean isDay = currentTime < 12000;
        double mod = isDay ? dayMod : nightMod;
        if(world.getTimeTAW() % mod == 0){
            if(!disableRoundChecker && leftOver >= 1.0D){
                leftOver -= 1;
                return;
            }
            leftOver += isDay ? dayRoundingError : nightRoundingError;
            world.setTimeOfDayTAW(world.getTimeOfDayTAW() + 1);
        }
    }

    public void setupCustomTime(long dayD, long nightD){
        if(dayD <= 12000 && nightD <= 12000){
            this.enableCustomTicker = false;
            return;
        }
        this.enableCustomTicker = true;
        double dayVal = dayD / 12000.0D;
        double nightVal = nightD / 12000.0D;
        int checkDay = (int) dayVal;
        int checkNight = (int) nightVal;
        if(checkDay == dayVal && checkNight == nightVal){
            this.dayMod = checkDay;
            this.nightMod = checkNight;
            this.dayD = dayD;
            this.nightD = nightD;
            this.disableRoundChecker = true;
            return;
        } else disableRoundChecker = false;

        this.dayMod = checkDay;
        this.nightMod = checkNight;

        this.dayRoundingError = dayVal - checkDay;
        this.nightRoundingError = nightVal - checkNight;

        this.dayD = dayD;
        this.nightD = nightD;

    }
    public void setCustomTicker(boolean bl){
        this.enableCustomTicker = bl;
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
