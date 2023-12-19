package ru.aiefu.timeandwindct;

import ru.aiefu.timeandwindct.tickers.Ticker;

public interface ITimeOperations {
    Ticker time_and_wind_custom_ticker$getTimeTicker();
    void time_and_wind_custom_ticker$setTimeTicker(Ticker timeTicker);
    void time_and_wind_custom_ticker$setTimeOfDayTAW(long time);
    long time_and_wind_custom_ticker$getTimeTAW();
    long time_and_wind_custom_ticker$getTimeOfDayTAW();
    boolean time_and_wind_custom_ticker$isClient();
    void time_and_wind_custom_ticker$setSkipState(boolean bl);
    void time_and_wind_custom_ticker$setSpeed(int speed);
    void time_and_wind_custom_ticker$wakeUpAllPlayersTAW();
}
