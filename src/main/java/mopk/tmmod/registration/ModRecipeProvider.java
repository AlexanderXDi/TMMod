package mopk.tmmod.registration;

import mopk.tmmod.Tmmod;
import mopk.tmmod.block_func.Cables.CableTier;
import mopk.tmmod.blocks.CableBlock;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.tags.ItemTags;

public class ModRecipeProvider extends RecipeProvider {

    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        Item rubber = ModItems.RUBBER.get();

        // Перебираем все наши тиры кабелей
        for (CableTier tier : CableTier.values()) {
            List<DeferredBlock<CableBlock>> variants = ModBlocks.ALL_CABLES.get(tier);
            if (variants == null || variants.isEmpty()) continue;

            // Поочередная изоляция: кабель(i) + резина = кабель(i+1)
            for (int i = 0; i < variants.size() - 1; i++) {
                Item currentCable = variants.get(i).asItem();
                Item nextCable = variants.get(i + 1).asItem();

                String recipeName = "insulating_" + tier.name().toLowerCase() + "_cable_to_x" + (i + 1);
                // Сокращенное имя для x1, если это единственный уровень изоляции
                if (tier.getNeedsRubber() == 1 && i == 0) {
                    recipeName = "insulating_" + tier.name().toLowerCase() + "_cable";
                }

                ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, nextCable)
                        .requires(currentCable)
                        .requires(rubber)
                        .unlockedBy("has_rubber", has(rubber))
                        .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(Tmmod.MODID, recipeName));
            }
        }

        // Рецепт теплопровода (условно: медь и резина)
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.HEAT_CONDUCTOR.get())
                .pattern("RRR")
                .pattern("CCC")
                .pattern("RRR")
                .define('R', ModItems.RUBBER.get())
                .define('C', ModItems.COPPER_PLATE.get())
                .unlockedBy("has_copper_plate", has(ModItems.COPPER_PLATE.get()))
                .save(recipeOutput);

        // Рецепт теплообменника (условно: корпус, микросхема, стекло и медь)
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.LIQUID_HEAT_EXCHANGER.get())
                .pattern("GCG")
                .pattern("CMC")
                .pattern("GCG")
                .define('G', Items.GLASS)
                .define('C', ModItems.COPPER_PLATE.get())
                .define('M', ModItems.MACHINE_CASING.get())
                .unlockedBy("has_machine_casing", has(ModItems.MACHINE_CASING.get()))
                .save(recipeOutput);
    }
}
