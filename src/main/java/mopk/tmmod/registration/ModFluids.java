package mopk.tmmod.registration;

import mopk.tmmod.Tmmod;
import mopk.tmmod.fluids.EffectLiquidBlock;
import mopk.tmmod.fluids.TransformingLiquidBlock;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.joml.Vector3f;

import java.util.function.Supplier;

public class ModFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, Tmmod.MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(BuiltInRegistries.FLUID, Tmmod.MODID);

    private static final ResourceLocation WATER_STILL_RL = ResourceLocation.withDefaultNamespace("block/water_still");
    private static final ResourceLocation WATER_FLOWING_RL = ResourceLocation.withDefaultNamespace("block/water_flow");
    private static final ResourceLocation WATER_OVERLAY_RL = ResourceLocation.withDefaultNamespace("block/water_overlay");

    // 1. Жидкая материя (UU-Matter) - Розовый, Регенерация II
    public static final FluidRegistryObject UU_MATTER = registerFluid("uu_matter", 0xFFFF00FF, 1000, 1000, 
            () -> new MobEffectInstance(MobEffects.REGENERATION, 100, 1), false, null, 0);

    // 2. Строительная пена - Серый, Медлительность II
    public static final FluidRegistryObject CONSTRUCTION_FOAM = registerFluid("construction_foam", 0xFF888888, 2000, 1000, 
            () -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1), false, null, 0);

    // 3. Хладагент - Светло-голубой
    public static final FluidRegistryObject COOLANT = registerFluid("coolant", 0xFF00FFFF, 1000, 1000, 
            null, false, null, 0);

    // 4. Горячий хладагент - Оранжевый, Поджигает
    public static final FluidRegistryObject HOT_COOLANT = registerFluid("hot_coolant", 0xFFFF8800, 1000, 1000, 
            null, true, null, 0);

    // 5. Базальтовая лава - Темно-красный, Поджигает, медленно течет, в базальт
    public static final FluidRegistryObject PAHOEHOE_LAVA = registerFluid("pahoehoe_lava", 0xFFFF4400, 3000, 3000, 
            null, true, () -> Blocks.BASALT, 100);

    // 6. Биомасса - Зеленый
    public static final FluidRegistryObject BIOMASS = registerFluid("biomass", 0xFF228822, 1000, 1000, 
            null, false, null, 0);

    // 7. Пестицид - Темно-желтый, Тошнота
    public static final FluidRegistryObject PESTICIDE = registerFluid("pesticide", 0xFF888800, 1000, 1000, 
            () -> new MobEffectInstance(MobEffects.CONFUSION, 200, 0), false, null, 0);

    // 8. Дистиллированная вода - Прозрачный
    public static final FluidRegistryObject DISTILLED_WATER = registerFluid("distilled_water", 0xFFBBBBFF, 1000, 1000, 
            null, false, null, 0);

    // 9. Теплая вода - Регенерация II, в воду
    public static final FluidRegistryObject HOT_WATER = registerFluid("hot_water", 0xFF88FFFF, 1000, 1000, 
            () -> new MobEffectInstance(MobEffects.REGENERATION, 100, 1), false, () -> Blocks.WATER, 100);

    // 10. Креозот - Черный
    public static final FluidRegistryObject CREOSOTE = registerFluid("creosote", 0xFF222222, 1500, 1000, 
            null, false, null, 0);

    // 11. Молоко - Белый
    public static final FluidRegistryObject MILK = registerFluid("milk", 0xFFFFFFFF, 1000, 1000, 
            null, false, null, 0);


    // Вспомогательный метод регистрации всего сета жидкости
    private static FluidRegistryObject registerFluid(String name, int colorTint, int viscosity, int density, 
                                                     Supplier<MobEffectInstance> effect, boolean setsOnFire, 
                                                     Supplier<Block> transformTarget, int transformChance) {
        
        int tickRate = (viscosity >= 3000) ? 30 : 5;

        Supplier<FluidType> type = FLUID_TYPES.register(name, () -> new FluidType(FluidType.Properties.create()
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

        Supplier<BaseFlowingFluid.Properties> properties = () -> new BaseFlowingFluid.Properties(
                type, null, null // Будет обновлено ниже
        ).bucket(null).block(null).tickRate(tickRate);

        FluidRegistryObject obj = new FluidRegistryObject();

        obj.source = FLUIDS.register(name, () -> new BaseFlowingFluid.Source(obj.properties.get()));
        obj.flowing = FLUIDS.register(name + "_flowing", () -> new BaseFlowingFluid.Flowing(obj.properties.get()));

        if (transformTarget != null) {
            obj.block = (DeferredBlock) ModBlocks.BLOCKS.register(name + "_block", () -> new TransformingLiquidBlock(
                    (net.minecraft.world.level.material.FlowingFluid) obj.source.get(), 
                    BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable(), 
                    effect, setsOnFire, transformTarget, transformChance));
        } else {
            obj.block = (DeferredBlock) ModBlocks.BLOCKS.register(name + "_block", () -> new EffectLiquidBlock(
                    (net.minecraft.world.level.material.FlowingFluid) obj.source.get(), 
                    BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable(), 
                    effect, setsOnFire));
        }

        obj.bucket = ModItems.ITEMS.register(name + "_bucket", () -> new BucketItem(obj.source.get(), 
                new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

        obj.properties = () -> new BaseFlowingFluid.Properties(type, obj.source, obj.flowing)
                .bucket(obj.bucket).block(obj.block);

        return obj;
    }

    public static class FluidRegistryObject {
        public Supplier<Fluid> source;
        public Supplier<Fluid> flowing;
        public DeferredBlock<? extends net.minecraft.world.level.block.LiquidBlock> block;
        public DeferredItem<Item> bucket;
        public Supplier<BaseFlowingFluid.Properties> properties;
    }
}