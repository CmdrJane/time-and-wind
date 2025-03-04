package aiefu.timeandwind;

import aiefu.timeandwind.tickers.Ticker;

public interface ITimeOperations {
    Ticker taw$getTimeTicker();
    void taw$setTimeTicker(Ticker timeTicker);
    void taw$setTimeOfDayTAW(long time);
    long taw$getTimeTAW();
    long taw$getTimeOfDayTAW();
    boolean taw$isClient();
}
