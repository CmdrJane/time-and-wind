package ru.aiefu.timeandwindct.tickers;

import ru.aiefu.timeandwindct.ITimeOperations;

public class DefaultTicker implements Ticker{

    @Override
    public void tick(ITimeOperations world, boolean nskip, int acceleration) {
        if(nskip) world.time_and_wind_custom_ticker$setTimeOfDayTAW(world.time_and_wind_custom_ticker$getTimeOfDayTAW() + acceleration);
        else world.time_and_wind_custom_ticker$setTimeOfDayTAW(world.time_and_wind_custom_ticker$getTimeOfDayTAW() + 1L);
    }
}
