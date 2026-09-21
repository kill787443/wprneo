package org.kill.wpr;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(Wpr.MODID)
public class Wpr {
    public static final String MODID = "wpr";
    private static final Logger LOGGER = LogUtils.getLogger();

    // 1. Core Registries Setup
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // 2. Custom Horror Creative Tab Layout
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> WPR_TAB =
            CREATIVE_MODE_TABS.register("wpr_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.wpr.wpr_tab"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(Items.SKELETON_SKULL::getDefaultInstance) // Temporary icon
                    .displayItems((parameters, output) -> {
                        // Your custom items and blocks will be added here automatically later!
                    }).build());

    // 3. Mod Initialization (The Constructor Engine)
    public Wpr(IEventBus modEventBus) {
        LOGGER.info("wpr is initializing...");

        // Connect our registers directly to the game loader pipeline
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}
