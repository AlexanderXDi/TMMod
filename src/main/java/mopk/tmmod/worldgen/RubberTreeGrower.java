package mopk.tmmod.worldgen;

import mopk.tmmod.registration.ModConfiguredFeatures;
import net.minecraft.world.level.block.grower.TreeGrower;
import java.util.Optional;

public class RubberTreeGrower {
    public static final TreeGrower RUBBER_TREE = new TreeGrower(
            "rubber_tree",
            Optional.empty(),
            Optional.of(ModConfiguredFeatures.RUBBER_TREE),
            Optional.empty()
    );
}
