package aiefu.timeandwind.config;

public class TimeDataStorage {
    public int dayDuration = 12000;
    public int nightDuration = 12000;

    public TimeDataStorage() {
    }

    public TimeDataStorage(int dayD, int nightD) {
        this.dayDuration = dayD;
        this.nightDuration = nightD;
    }
}
