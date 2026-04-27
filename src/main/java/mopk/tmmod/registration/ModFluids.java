package mopk.tmmod.registration;

import mopk.tmmod.Tmmod;
import mopk.tmmod.fluids.EffectLiquidBlock;
import mopk.tmmod.fluids.TransformingLiquidBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ModFluids {

    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, Tmmod.MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(BuiltInRegistries.FLUID, Tmmod.MODID);

    public static final List<FluidRegistryObject> ALL_FLUIDS = new ArrayList<>();

    public static void registerDynamicFluidItems(RegisterEvent event) {
        // Obsolete, left empty or removed entirely, keeping signature if needed or removing.
        // Actually, we can just remove its call from Tmmod.java and remove this method.
    }

    private static final ResourceLocation WATER_STILL_RL = ResourceLocation.withDefaultNamespace("block/water_still");
    private static final ResourceLocation WATER_FLOWING_RL = ResourceLocation.withDefaultNamespace("block/water_flow");
    private static final ResourceLocation WATER_OVERLAY_RL = ResourceLocation.withDefaultNamespace("block/water_overlay");

    public static final FluidRegistryObject UU_MATTER = registerFluid("uu_matter", 0xFFFF00FF, 1000, 1000,      
            () -> new MobEffectInstance(MobEffects.REGENERATION, 100, 1), false, null, 0);

    public static final FluidRegistryObject CONSTRUCTION_FOAM = registerFluid("construction_foam", 0xFF888888, 2000, 1000,
            () -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1), false, null, 0);

    public static final FluidRegistryObject COOLANT = registerFluid("coolant", 0xFF00FFFF, 1000, 1000,
            null, false, null, 0);

    public static final FluidRegistryObject HOT_COOLANT = registerFluid("hot_coolant", 0xFFFF8800, 1000, 1000,  
            null, true, null, 0);

    public static final FluidRegistryObject PAHOEHOE_LAVA = registerFluid("pahoehoe_lava", 0xFFFF4400, 3000, 3000,
            null, true, () -> Blocks.BASALT, 100);

    public static final FluidRegistryObject BIOMASS = registerFluid("biomass", 0xFF228822, 1000, 1000,
            null, false, null, 0);

    public static final FluidRegistryObject PESTICIDE = registerFluid("pesticide", 0xFF888800, 1000, 1000,      
            () -> new MobEffectInstance(MobEffects.CONFUSION, 200, 0), false, null, 0);

    public static final FluidRegistryObject DISTILLED_WATER = registerFluid("distilled_water", 0xFFBBBBFF, 1000, 1000,
            null, false, null, 0);

    public static final FluidRegistryObject HOT_WATER = registerFluid("hot_water", 0xFF88FFFF, 1000, 1000,      
            () -> new MobEffectInstance(MobEffects.REGENERATION, 100, 1), false, () -> Blocks.WATER, 100);      

    public static final FluidRegistryObject CREOSOTE = registerFluid("creosote", 0xFF222222, 1500, 1000,        
            null, false, null, 0);

    public static final FluidRegistryObject MILK = registerFluid("industrial_milk", 0xFFFFFFFF, 1000, 1000,     
            null, false, null, 0);


    private static FluidRegistryObject registerFluid(String name, int colorTint, int viscosity, int density,    
                                                     Supplier<MobEffectInstance> effect, boolean setsOnFire,    
                                                     Supplier<Block> transformTarget, int transformChance) {    

        int tickRate = (viscosity >= 3000) ? 30 : 5;
        FluidRegistryObject obj = new FluidRegistryObject();

        // 1. Fluid Type
        obj.type = FLUID_TYPES.register(name, () -> new FluidType(FluidType.Properties.create()
                .viscosity(viscosity).density(density).canExtinguish(!setsOnFire)) {
            @Override
            public void initializeClient(java.util.function.Consumer<net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions> consumer) {
                consumer.accept(new net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions() {
                    @Override public ResourceLocation getStillTexture() { return WATER_STILL_RL; }
                    @Override public ResourceLocation getFlowingTexture() { return WATER_FLOWING_RL; }
                    @Override public ResourceLocation getOverlayTexture() { return WATER_OVERLAY_RL; }
                    @Override public int getTintColor() { return colorTint; }
                    @Override public Vector3f modifyFogColor(net.minecraft.client.Camera camera, float partialTick, net.minecraft.client.multiplayer.ClientLevel level, int renderDistance, float darkenWorldAmount, Vector3f fluidFogColor) {
                        return new Vector3f((colorTint >> 16 & 0xFF) / 255f, (colorTint >> 8 & 0xFF) / 255f, (colorTint & 0xFF) / 255f);
                    }
                });
            }
        });

        // 2. Block
        BlockBehaviour.Properties blockProps = BlockBehaviour.Properties.of()
                .noCollission()
                .noLootTable()
                .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY);

        if (transformTarget != null) {
            obj.block = (DeferredBlock) ModBlocks.BLOCKS.register(name + "_block", () -> new TransformingLiquidBlock(
                    (net.minecraft.world.level.material.FlowingFluid) obj.source.get(),
                    blockProps,
                    effect, setsOnFire, transformTarget, transformChance));
        } else {
            obj.block = (DeferredBlock) ModBlocks.BLOCKS.register(name + "_block", () -> new EffectLiquidBlock( 
                    (net.minecraft.world.level.material.FlowingFluid) obj.source.get(),
                    blockProps,
                    effect, setsOnFire));
        }

        // 3. Bucket
        obj.bucket = ModItems.ITEMS.register(name + "_bucket", () -> new BucketItem(obj.source.get(),
                new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

        // 4. Source and Flowing
        obj.properties = () -> new BaseFlowingFluid.Properties(obj.type, obj.source, obj.flowing)
                .bucket(obj.bucket).block(obj.block).tickRate(tickRate);

        obj.source = FLUIDS.register(name, () -> new BaseFlowingFluid.Source(obj.properties.get()));
        obj.flowing = FLUIDS.register(name + "_flowing", () -> new BaseFlowingFluid.Flowing(obj.properties.get()));

        ALL_FLUIDS.add(obj);
        return obj;
    }

    public static class FluidRegistryObject {
        public Supplier<FluidType> type;
        public Supplier<Fluid> source;
        public Supplier<Fluid> flowing;
        public DeferredBlock<? extends net.minecraft.world.level.block.LiquidBlock> block;
        public DeferredItem<Item> bucket;
        public Supplier<BaseFlowingFluid.Properties> properties;
    }
}