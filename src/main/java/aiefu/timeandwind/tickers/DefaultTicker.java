package aiefu.timeandwind.tickers;

import net.minecraft.world.level.Level;
import aiefu.timeandwind.ITimeOperations;

public class DefaultTicker implements Ticker{

    @Override
    public void tick(ITimeOperations world) {
        world.taw$setTimeOfDayTAW(world.taw$getTimeOfDayTAW() + 1L);
    }

    @Override
    public void accelerate(Level level, int speed) {
        ((ITimeOperations)level).taw$setTimeOfDayTAW(((ITimeOperations) level).taw$getTimeOfDayTAW() + speed);
    }

    @Override
    public boolean canAccelerate() {
        return true;
    }

    @Override
    public boolean allowSleeping() {
        return true;
    }
}
