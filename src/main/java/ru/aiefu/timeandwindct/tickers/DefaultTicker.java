package ru.aiefu.timeandwindct.tickers;

import ru.aiefu.timeandwindct.ITimeOperations;

public class DefaultTicker implements Ticker{

    @Override
    public void tick(ITimeOperations world, boolean nskip, int acceleration) {
        if(nskip) world.setTimeOfDayTAW(world.getTimeOfDayTAW() + acceleration);
        else world.setTimeOfDayTAW(world.getTimeOfDayTAW() + 1L);
    }
}
