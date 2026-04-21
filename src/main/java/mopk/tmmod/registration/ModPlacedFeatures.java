package mopk.tmmod.registration;

import mopk.tmmod.Tmmod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class ModPlacedFeatures {
    public static final ResourceKey<PlacedFeature> URANIUM_ORE = registerKey("uranium_ore");
    public static final ResourceKey<PlacedFeature> TIN_ORE = registerKey("tin_ore");
    public static final ResourceKey<PlacedFeature> COPPER_ORE = registerKey("copper_ore");
    public static final ResourceKey<PlacedFeature> LEAD_ORE = registerKey("lead_ore");

    public static ResourceKey<PlacedFeature> registerKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(Tmmod.MODID, name));
    }
}
