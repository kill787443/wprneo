package org.kill.wpr;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.kill.wpr.sound.ModSounds;
import org.slf4j.Logger;
import org.kill.wpr.worldgen.InfestedStructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

@Mod(Wpr.MODID)
public class Wpr {
    public static final String MODID = "wpr";
    private static final Logger LOGGER = LogUtils.getLogger();

    // Core Registries Setup
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, MODID);

    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> INFESTED_STRUCTURE_FEATURE =
            FEATURES.register("infested_structure", () -> new InfestedStructureFeature(NoneFeatureConfiguration.CODEC));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> WPR_TAB =
            CREATIVE_MODE_TABS.register("wpr_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.wpr.wpr_tab"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(Items.SKELETON_SKULL::getDefaultInstance)
                    .displayItems((parameters, output) -> {
                    }).build());

    public Wpr(IEventBus modEventBus) {
        LOGGER.info("wpr is initializing...");

        // Connect our registers directly to the game loader pipeline
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        FEATURES.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);
    }
}
