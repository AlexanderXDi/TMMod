package mopk.tmmod.etc;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, "tmmod");

    public static final DeferredHolder<SoundEvent, SoundEvent> GENERATOR_HUM =
            SOUND_EVENTS.register("generator_hum",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("tmmod", "generator_hum")));

    public static final DeferredHolder<SoundEvent, SoundEvent> CRUSHER_HUM =
            SOUND_EVENTS.register("crusher_hum",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("tmmod", "crusher_hum")));

    public static final DeferredHolder<SoundEvent, SoundEvent> BATTERY_BLOCK_HUM =
            SOUND_EVENTS.register("battery_block_hum",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("tmmod", "battery_block_hum")));
}