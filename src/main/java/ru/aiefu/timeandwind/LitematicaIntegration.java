package ru.aiefu.timeandwind;

import fi.dy.masa.litematica.world.WorldSchematic;
import net.minecraft.world.World;

public class LitematicaIntegration {
    public static boolean checkInstance(World instance){
        return instance instanceof WorldSchematic;
    }
}
