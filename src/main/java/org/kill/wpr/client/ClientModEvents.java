package org.kill.wpr.client;

import org.kill.wpr.Wpr;
import org.kill.wpr.client.renderer.UndyingRenderer;
// import org.kill.wpr.init.EntityInit; // Wherever your EntityType is registered
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.kill.wpr.entity.Undying;

@EventBusSubscriber(modid = Wpr.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Register the entity renderer to your custom EntityType
        event.registerEntityRenderer(EntityInit.MY_ENTITY.get(), UndyingRenderer::new);
    }
}
