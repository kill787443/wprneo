package org.kill.wpr.sound;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, "wpr");

    public static final DeferredHolder<SoundEvent, SoundEvent> INFESTED_AMBIENCE =
            SOUNDS.register("infested_ambience", () ->
                    SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath("wpr", "infested_ambience")
                    ));

    public static final DeferredHolder<SoundEvent, SoundEvent> INFESTED_COUNTDOWN =
            SOUNDS.register("infested_countdown", () ->
                    SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath("wpr", "infested_countdown")
                    ));
}
