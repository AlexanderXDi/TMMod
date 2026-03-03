package mopk.tmmod.blocks.events_and_else;


import mopk.tmmod.blocks.ModBlocks;
import mopk.tmmod.blocks.events_and_else.IronFurnace.IronFurnaceBE;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
    // 1. Создаем список (регистр) для всех сущностей блоков вашего мода
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, "tmmod");

    // 2. Регистрируем конкретно IRON_FURNACE_BE
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<IronFurnaceBE>> IRON_FURNACE_BE =
            BLOCK_ENTITIES.register("iron_furnace_be",
                    () -> BlockEntityType.Builder.of(
                            IronFurnaceBE::new,
                            ModBlocks.IRON_FURNACE.get()
                    ).build(null));
}

