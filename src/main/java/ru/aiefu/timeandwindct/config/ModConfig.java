package ru.aiefu.timeandwindct.config;

public class ModConfig {
    public boolean patchSkyAngle;
    public boolean syncWithSystemTime;
    public boolean systemTimePerDimensions;
    public boolean enableNightSkipAcceleration;
    public int accelerationSpeed;

    public int thresholdPercentage;

    public int config_ver = 3;

    public ModConfig(boolean patchSkyAngle, boolean syncWithSystemTime, boolean systemTimePerDimensions, boolean enableNightSkipAcceleration,
                     int accelerationSpeed, int thresholdPercentage){
        this.patchSkyAngle = patchSkyAngle;
        this.syncWithSystemTime = syncWithSystemTime;
        this.systemTimePerDimensions = systemTimePerDimensions;
        this.enableNightSkipAcceleration = enableNightSkipAcceleration;
        this.accelerationSpeed = accelerationSpeed;
        this.thresholdPercentage = thresholdPercentage;
    }

    public ModConfig copy(){
        return new ModConfig(patchSkyAngle, syncWithSystemTime, systemTimePerDimensions, enableNightSkipAcceleration, accelerationSpeed, thresholdPercentage);
    }
}
