package mopk.tmmod.fluids;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

import java.util.function.Supplier;

public class EffectLiquidBlock extends LiquidBlock {
    private final Supplier<MobEffectInstance> effect;
    private final boolean setsOnFire;

    public EffectLiquidBlock(FlowingFluid fluid, Properties properties, Supplier<MobEffectInstance> effect, boolean setsOnFire) {
        super(fluid, properties);
        this.effect = effect;
        this.setsOnFire = setsOnFire;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (this.setsOnFire && !entity.fireImmune()) {
            entity.igniteForSeconds(5);
        }
        if (this.effect != null && entity instanceof LivingEntity living) {
            MobEffectInstance eff = this.effect.get();
            if (eff != null) {
                living.addEffect(new MobEffectInstance(eff.getEffect(), eff.getDuration(), eff.getAmplifier(), eff.isAmbient(), eff.isVisible()));
            }
        }
        super.entityInside(state, level, pos, entity);
    }
}
