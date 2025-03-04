package aiefu.timeandwind.config;

public class ModConfig {
    public boolean patchSkyAngle;
    public boolean enableGlobalSystemTime;
    public boolean enableNightSkipAcceleration;
    public int accelerationSpeed;

    public ModConfig(boolean patchSkyAngle, boolean enableGlobalSystemTime, boolean enableNightSkipAcceleration,
                     int accelerationSpeed){
        this.patchSkyAngle = patchSkyAngle;
        this.enableGlobalSystemTime = enableGlobalSystemTime;
        this.enableNightSkipAcceleration = enableNightSkipAcceleration;
        this.accelerationSpeed = accelerationSpeed;
    }

    public static ModConfig getDefault(){
        return new ModConfig(false,false, false, 40);
    }
}
