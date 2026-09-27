package org.kill.wpr.client.model;

import org.kill.wpr.Wpr;
import org.kill.wpr.entity.Undying;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class UndyingModel extends GeoModel<Undying> {
    private static final ResourceLocation TEXTURE_FRAME_0 =
            ResourceLocation.fromNamespaceAndPath(Wpr.MODID, "textures/entity/undying_0.png");
    private static final ResourceLocation TEXTURE_FRAME_1 =
            ResourceLocation.fromNamespaceAndPath(Wpr.MODID, "textures/entity/undying_1.png");

    @Override
    public ResourceLocation getModelResource(Undying animatable) {
        // Points to: assets/modid/geo/my_entity.geo.json
        return ResourceLocation.fromNamespaceAndPath(Wpr.MODID, "geo/undying.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Undying animatable) {
        return (animatable.tickCount / 2) % 2 == 0 ? TEXTURE_FRAME_0 : TEXTURE_FRAME_1;
    }

    @Override
    public ResourceLocation getAnimationResource(Undying animatable) {
        // Points to: assets/modid/animations/my_entity.animation.json
        return ResourceLocation.fromNamespaceAndPath(Wpr.MODID, "animations/undying.animation.json");
    }
}
