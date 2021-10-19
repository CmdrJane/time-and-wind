package ru.aiefu.timeandwindct.config;

public class ModConfig {
    public boolean patchSkyAngle;
    public boolean syncWithSystemTime;
    public boolean enableNightSkipAcceleration;
    public int accelerationSpeed;
    public boolean enableThreshold;
    public int thresholdPercentage;
    public boolean flatAcceleration;

    public int config_ver = 2;

    public ModConfig(boolean patchSkyAngle, boolean syncWithSystemTime, boolean enableNightSkipAcceleration,
                     int accelerationSpeed, boolean enableThreshold, int thresholdPercentage, boolean flatAcceleration){
        this.patchSkyAngle = patchSkyAngle;
        this.syncWithSystemTime = syncWithSystemTime;
        this.enableNightSkipAcceleration = enableNightSkipAcceleration;
        this.accelerationSpeed = accelerationSpeed;
        this.enableThreshold = enableThreshold;
        this.thresholdPercentage = thresholdPercentage;
        this.flatAcceleration = flatAcceleration;
    }
}
