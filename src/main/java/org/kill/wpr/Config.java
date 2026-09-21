package org.kill.wpr;

import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLE_TEMP = BUILDER
            .comment("temp")
            .define("enableTemp", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean enableTemp;

    // This method will now be called safely by your main Wpr class instead of an automated system
    public static void onLoad(final ModConfigEvent event) {
        enableTemp = ENABLE_TEMP.get();
    }
}
