package com.example.spdim;

import com.example.spdim.core.artifact.ChaliceOfBlood;
import com.example.spdim.core.artifact.TimekeepersHourglass;
import com.example.spdim.core.network.MyModNetwork;
import com.example.spdim.core.wand.energyWand.WandOfFireblast;
import com.example.spdim.core.wand.energyWand.WandOfRegrowth;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import com.example.spdim.core.projectile.BlastWave;
import com.example.spdim.core.wand.energyWand.WandOfBlastWave;
import com.example.spdim.core.wand.energyWand.WandOfLightning;
// The value here should match an entry in the META-INF/mods.toml file
@Mod(ExampleMod.MODID)
public class ExampleMod
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "spdim";
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPE = DeferredRegister.create(ForgeRegistries.Keys.ENTITY_TYPES, MODID);

    public static final RegistryObject<EntityType<BlastWave>> BLAST_WAVE =
            ENTITY_TYPE.register("blast_wave",
                    () -> EntityType.Builder.<BlastWave>of(BlastWave::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)
                            .build("blast_wave"));

    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "examplemod" namespace
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "examplemod" namespace
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "examplemod" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    // Register all wands
    public static final RegistryObject<Item> WAND_OF_BLAST_WAVE = ITEMS.register("wand_of_blast_wave", () -> new WandOfBlastWave(new Item.Properties().stacksTo(1), 4, 1, 300, Component.translatable("item.spdim.wand_of_blast_wave")));
    public static final RegistryObject<Item> WAND_OF_LIGHTNING = ITEMS.register("wand_of_lightning", () -> new WandOfLightning(new Item.Properties().stacksTo(1), 4, 1, 300, Component.translatable("item.spdim.wand_of_lightning")));
    public static final RegistryObject<Item> WAND_OF_FIREBLAST = ITEMS.register("wand_of_fireblast", () -> new WandOfFireblast(new Item.Properties().stacksTo(1), 4, 1, 300, Component.translatable("item.spdim.wand_of_fireblast")));
    public static final RegistryObject<Item> WAND_OF_REGROWTH = ITEMS.register("wand_of_regrowth", () -> new WandOfRegrowth(new Item.Properties().stacksTo(1), 4, 1, 300, Component.translatable("item.spdim.wand_of_regrowth")));
    // Register all artifacts
    public static final RegistryObject<Item> TIMEKEEPERS_HOURGLASS = ITEMS.register("timekeepers_hourglass", () -> new TimekeepersHourglass(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CHALICE_OF_BLOOD = ITEMS.register("chalice_of_blood", () -> new ChaliceOfBlood(new Item.Properties().stacksTo(1)));

    // Creates a new food item with the id "examplemod:example_id", nutrition 1 and saturation 2
    public static final RegistryObject<Item> EXAMPLE_ITEM = ITEMS.register("example_item", () -> new Item(new Item.Properties().food(new FoodProperties.Builder()
            .alwaysEat().nutrition(1).saturationMod(2f).build())));

    // Creates a creative tab with the id "examplemod:example_tab" for the example item, that is placed after the combat tab
    public static final RegistryObject<CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> WAND_OF_BLAST_WAVE.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(WAND_OF_BLAST_WAVE.get()); // Add the example item to the tab. For your own tabs, this method is preferred over the event
                output.accept(WAND_OF_LIGHTNING.get());
                output.accept(WAND_OF_FIREBLAST.get());
                output.accept(WAND_OF_REGROWTH.get());
                output.accept(TIMEKEEPERS_HOURGLASS.get());
                output.accept(CHALICE_OF_BLOOD.get());
            }).build());
    public static final RegistryObject<Item> BLAST_WAVE_ITEM = ITEMS.register("blast_wave_item",
            () -> new Item(new Item.Properties())
    );
    public ExampleMod(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);
        ENTITY_TYPE.register(modEventBus);


        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.logDirtBlock)
            LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));

        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);

        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));

        MyModNetwork.register();

    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}
