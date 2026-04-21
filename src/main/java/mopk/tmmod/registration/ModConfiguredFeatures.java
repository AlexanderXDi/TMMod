package mopk.tmmod.registration;

import mopk.tmmod.Tmmod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class ModConfiguredFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> RUBBER_TREE = registerKey("rubber_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> URANIUM_ORE = registerKey("uranium_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TIN_ORE = registerKey("tin_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> COPPER_ORE = registerKey("copper_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> LEAD_ORE = registerKey("lead_ore");

    public static ResourceKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(Tmmod.MODID, name));
    }
}
