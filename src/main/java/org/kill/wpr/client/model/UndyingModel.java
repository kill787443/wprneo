package org.kill.wpr.client.model;

import org.kill.wpr.Wpr;
import org.kill.wpr.entity.Undying;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class UndyingModel extends GeoModel<Undying> {
    @Override
    public ResourceLocation getModelResource(Undying animatable) {
        // Points to: assets/modid/geo/my_entity.geo.json
        return ResourceLocation.fromNamespaceAndPath(Wpr.MODID, "geo/my_entity.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Undying animatable) {
        // Points to: assets/modid/textures/entity/my_entity.png
        return ResourceLocation.fromNamespaceAndPath(Wpr.MODID, "textures/entity/my_entity.png");
    }

    @Override
    public ResourceLocation getAnimationResource(Undying animatable) {
        // Points to: assets/modid/animations/my_entity.animation.json
        return ResourceLocation.fromNamespaceAndPath(Wpr.MODID, "animations/my_entity.animation.json");
    }
}
