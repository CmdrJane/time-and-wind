package aiefu.timeandwind.tickers;

import net.minecraft.world.level.Level;
import aiefu.timeandwind.ITimeOperations;

public class SkipTicker implements Ticker{
    @Override
    public void tick(ITimeOperations world) {

    }

    @Override
    public void accelerate(Level level, int speed) {

    }

    @Override
    public boolean canAccelerate() {
        return false;
    }

    @Override
    public boolean allowSleeping() {
        return false;
    }
}
