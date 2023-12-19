package ru.aiefu.timeandwindct.tickers;

import net.minecraft.world.World;
import ru.aiefu.timeandwindct.ITimeOperations;

public interface Ticker {
    void tick(ITimeOperations world);
    void accelerate(World level, int speed);
}
