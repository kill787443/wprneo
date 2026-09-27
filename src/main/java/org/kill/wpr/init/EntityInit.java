package org.kill.wpr.init; // Make sure this matches your package structure!

import org.kill.wpr.Wpr; // Change to your actual main mod class name
import org.kill.wpr.entity.Undying;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class EntityInit {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, Wpr.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<Undying>> UNDYING =
            ENTITIES.register("undying", () -> EntityType.Builder.of(Undying::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F) // The width and height of your mob's hitbox
                    .build("undying"));

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
        eventBus.addListener(EntityInit::registerAttributes);
    }

    private static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(UNDYING.get(), Undying.createAttributes().build());
    }
}
