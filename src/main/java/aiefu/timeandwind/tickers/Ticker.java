package aiefu.timeandwind.tickers;

import net.minecraft.world.level.Level;
import aiefu.timeandwind.ITimeOperations;

public interface Ticker {
    void tick(ITimeOperations world);
    void accelerate(Level level, int speed);
    boolean canAccelerate();
    boolean allowSleeping();
}
