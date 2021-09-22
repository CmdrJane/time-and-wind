package ru.aiefu.timeandwindct;

public interface ITimeOperations {
    TimeTicker getTimeTicker();
    void setTimeTicker(TimeTicker timeTicker);
    void setTimeOfDayTAW(long time);
    long getTimeTAW();
    long getTimeOfDayTAW();
}
