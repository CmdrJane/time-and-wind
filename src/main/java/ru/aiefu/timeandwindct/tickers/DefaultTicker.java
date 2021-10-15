package ru.aiefu.timeandwindct.tickers;

import ru.aiefu.timeandwindct.ITimeOperations;

public class DefaultTicker implements Ticker{

    @Override
    public void tick(ITimeOperations world) {
        world.setTimeOfDayTAW(world.getTimeOfDayTAW() + 1L);
    }
}
