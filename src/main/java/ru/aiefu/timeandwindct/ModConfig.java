package ru.aiefu.timeandwindct;

public class ModConfig {
    public boolean patchSkyAngle;
    public boolean syncWithSystemTime;

    public ModConfig(boolean patchSkyAngle, boolean syncWithSystemTime){
        this.patchSkyAngle = patchSkyAngle;
        this.syncWithSystemTime = syncWithSystemTime;
    }

    public int config_ver = 1;
}
