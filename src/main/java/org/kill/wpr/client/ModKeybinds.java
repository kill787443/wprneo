package org.kill.wpr.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = "wpr", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class ModKeybinds {
    private static final KeyMapping DEVELOPER_UNLOCK = new KeyMapping(
            "key.wpr.dev_unlock",
            KeyConflictContext.IN_GAME,
            KeyModifier.CONTROL,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            "key.categories.wpr"
    );
    private static final KeyMapping DEBUG_UNDYING_GLOW = new KeyMapping(
            "key.wpr.debug_undying_glow",
            KeyConflictContext.IN_GAME,
            KeyModifier.CONTROL,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            "key.categories.wpr"
    );

    private ModKeybinds() {
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(DEVELOPER_UNLOCK);
        event.register(DEBUG_UNDYING_GLOW);
    }

    public static boolean consumeDeveloperUnlock() {
        return DEVELOPER_UNLOCK.consumeClick();
    }

    public static boolean consumeUndyingGlowToggle() {
        return DEBUG_UNDYING_GLOW.consumeClick();
    }
}
