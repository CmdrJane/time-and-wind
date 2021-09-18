package ru.aiefu.timeandwindct;

public class TimeDataStorage {
    public long dayDuration = 12000;
    public long nightDuration = 12000;

    public TimeDataStorage() {
    }

    public TimeDataStorage(long dayD, long nightD) {
        this.dayDuration = dayD;
        this.nightDuration = nightD;
    }
}
