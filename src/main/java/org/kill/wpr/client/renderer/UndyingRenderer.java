package org.kill.wpr.client.renderer;

import org.kill.wpr.client.model.UndyingModel;
import org.kill.wpr.entity.Undying;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class UndyingRenderer extends GeoEntityRenderer<Undying> {
    public UndyingRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new UndyingModel());
        this.shadowRadius = 0.5f; // Adjusts shadow size under the mob
    }
}
