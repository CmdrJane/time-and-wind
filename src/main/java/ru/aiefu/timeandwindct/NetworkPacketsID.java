package ru.aiefu.timeandwindct;

import net.minecraft.util.Identifier;

public class NetworkPacketsID {
    public static final Identifier SYNC_CONFIG = new Identifier(TimeAndWindCT.MOD_ID, "sync_config");
    public static final Identifier SETUP_TIME = new Identifier(TimeAndWindCT.MOD_ID, "setup_time");
    public static final Identifier CFG_DEBUG_INFO = new Identifier(TimeAndWindCT.MOD_ID, "cfg_debug_info");
    public static final Identifier WORLD_ID_CLIPBOARD = new Identifier(TimeAndWindCT.MOD_ID, "world_id_clipboard");
    public static final Identifier NIGHT_SKIP_INFO = new Identifier(TimeAndWindCT.MOD_ID, "nskip_info");
}
