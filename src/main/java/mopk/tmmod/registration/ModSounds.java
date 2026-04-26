package mopk.tmmod.registration;

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

    public static final DeferredHolder<SoundEvent, SoundEvent> RECYCLER_HUM =
            SOUND_EVENTS.register("recycler_hum",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("tmmod", "recycler_hum")));

    public static final DeferredHolder<SoundEvent, SoundEvent> METALFORMER_HUM =
            SOUND_EVENTS.register("metalformer_hum",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("tmmod", "metalformer_hum")));

    public static final DeferredHolder<SoundEvent, SoundEvent> ACCUMULATOR_HUM =
            SOUND_EVENTS.register("accumulator_hum",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("tmmod", "accumulator_hum")));

    public static final DeferredHolder<SoundEvent, SoundEvent> ELECTRIC_FURNACE_HUM =
            SOUND_EVENTS.register("electric_furnace_hum",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("tmmod", "electric_furnace_hum")));

    public static final DeferredHolder<SoundEvent, SoundEvent> EXTRACTOR_HUM =
            SOUND_EVENTS.register("extractor_hum",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("tmmod", "extractor_hum")));

    public static final DeferredHolder<SoundEvent, SoundEvent> COMPRESSOR_HUM =
            SOUND_EVENTS.register("compressor_hum",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("tmmod", "compressor_hum")));
}