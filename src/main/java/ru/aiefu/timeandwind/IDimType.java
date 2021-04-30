package ru.aiefu.timeandwind;

import net.minecraft.util.Identifier;

public interface IDimType {
    void setCycleDuration(double day, double night);
    double getDayDuration();
    double getNightDuration();
    double getCycleDuration();
}
