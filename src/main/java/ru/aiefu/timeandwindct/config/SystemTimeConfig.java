package ru.aiefu.timeandwindct.config;

import javax.annotation.Nonnull;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoField;

public class SystemTimeConfig {
    public String sunrise;
    public String sunset;
    public String timeZone;

    public SystemTimeConfig(String sunrise, String sunset, String timeZone){
        this.sunrise = sunrise;
        this.sunset = sunset;
        this.timeZone = timeZone;
    }

    public int getSunriseMs(){
        return parseTime(sunrise);
    }

    public int getSunsetMs(){
        return parseTime(sunset);
    }

    public int getTimeOffset(){
        return parseTimeZone(timeZone);
    }

    private int parseTime(@Nonnull String s){
        long timeMs;
        int i = s.indexOf(":");
        if(i != -1){
            long hour = Long.parseLong(s.substring(0,i)) * 3_600_000;
            timeMs = hour + Long.parseLong(s.substring(i+1)) * 60_000;

        } else timeMs = Long.parseLong(s) * 3_600_000;
        return (int) timeMs;
    }
    private int parseTimeZone(String timeZone){
        if(timeZone.equalsIgnoreCase("local")){
            return OffsetDateTime.now().get(ChronoField.OFFSET_SECONDS) * 1000;
        } else return parseTime(timeZone);
    }
}
