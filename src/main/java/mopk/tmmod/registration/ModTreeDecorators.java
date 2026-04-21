package mopk.tmmod.registration;

import mopk.tmmod.Tmmod;
import mopk.tmmod.worldgen.RubberTreeDecorator;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModTreeDecorators {
    public static final DeferredRegister<TreeDecoratorType<?>> TREE_DECORATORS =
            DeferredRegister.create(Registries.TREE_DECORATOR_TYPE, Tmmod.MODID);

    public static final DeferredHolder<TreeDecoratorType<?>, TreeDecoratorType<RubberTreeDecorator>> RUBBER_TREE_DECORATOR =
            TREE_DECORATORS.register("rubber_tree_decorator", () -> new TreeDecoratorType<>(RubberTreeDecorator.CODEC));
}
