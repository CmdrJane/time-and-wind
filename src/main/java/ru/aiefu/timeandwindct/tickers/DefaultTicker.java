package ru.aiefu.timeandwindct.tickers;

import net.minecraft.world.World;
import ru.aiefu.timeandwindct.ITimeOperations;

public class DefaultTicker implements Ticker{

    @Override
    public void tick(ITimeOperations world) {
        world.time_and_wind_custom_ticker$setTimeOfDayTAW(world.time_and_wind_custom_ticker$getTimeOfDayTAW() + 1L);
    }

    @Override
    public void accelerate(World level, int speed) {
        ((ITimeOperations)level).time_and_wind_custom_ticker$setTimeOfDayTAW(((ITimeOperations) level).time_and_wind_custom_ticker$getTimeOfDayTAW() + speed);
    }
}
