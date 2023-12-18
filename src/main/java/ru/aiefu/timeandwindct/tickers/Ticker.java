package ru.aiefu.timeandwindct.tickers;

import net.minecraft.world.level.Level;
import ru.aiefu.timeandwindct.ITimeOperations;

public interface Ticker {
    void tick(ITimeOperations world);
    void accelerate(Level level, int speed);
}
