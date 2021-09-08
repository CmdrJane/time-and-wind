package ru.aiefu.timeandwind;

public interface IDimType {
    void setCycleDuration(long day, long night);
    long getDayDuration();
    long getNightDuration();
    long getCycleDuration();
    float getPrevAngle();
    float untweakedAngle();
    int calculateIrisWorldDayTime(long time);
}
