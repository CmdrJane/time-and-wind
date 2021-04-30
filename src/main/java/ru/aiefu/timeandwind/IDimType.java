package ru.aiefu.timeandwind;

public interface IDimType {
    void setCycleDuration(double day, double night);
    double getDayDuration();
    double getNightDuration();
    double getCycleDuration();
    float getPrevTime();
}
