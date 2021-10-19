package ru.aiefu.timeandwindct.tickers;

import ru.aiefu.timeandwindct.ITimeOperations;

public interface Ticker {
    void tick(ITimeOperations world, boolean nskip, int acceleration);
}
