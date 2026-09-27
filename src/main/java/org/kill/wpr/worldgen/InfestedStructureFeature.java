package org.kill.wpr.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class InfestedStructureFeature extends Feature<NoneFeatureConfiguration> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation TEMPLATE =
            ResourceLocation.fromNamespaceAndPath("wpr", "infestedopen");

    public InfestedStructureFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        BlockPos origin = context.origin();
        BlockPos placement = new BlockPos(origin.getX() & -16, 40, origin.getZ() & -16);
        StructureTemplate template = loadTemplate(context);

        if (template == null) {
            return false;
        }

        boolean placed = template.placeInWorld(
                context.level(),
                placement,
                placement,
                new StructurePlaceSettings().setKnownShape(true).setRandom(context.random()),
                context.random(),
                2
            );
        if (!placed) {
            LOGGER.warn("Structure template {} placed no blocks at {}", TEMPLATE, placement);
        }
        return placed;
    }

    private static StructureTemplate loadTemplate(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        ResourceLocation resourceId = ResourceLocation.fromNamespaceAndPath("wpr", "structures/infestedopen.nbt");
        Optional<Resource> resource = context.level().getLevel().getServer().getResourceManager().getResource(resourceId);
        if (resource.isEmpty()) {
            LOGGER.error("Could not find structure resource {}", resourceId);
            return null;
        }

        try (InputStream input = resource.get().open()) {
            StructureTemplate template = new StructureTemplate();
            template.load(
                    context.level().registryAccess().lookupOrThrow(Registries.BLOCK),
                    NbtIo.readCompressed(input, NbtAccounter.unlimitedHeap())
            );
            return template;
        } catch (IOException | RuntimeException exception) {
            LOGGER.error("Could not load structure resource {}", resourceId, exception);
            return null;
        }
    }
}
