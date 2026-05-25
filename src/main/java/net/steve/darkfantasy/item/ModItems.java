package net.steve.darkfantasy.item;

import net.minecraft.world.item.Rarity;
import net.steve.darkfantasy.DarkFantasy;
import net.steve.darkfantasy.food.ModFoods;
import net.steve.darkfantasy.init.ModEntities;
import net.steve.darkfantasy.init.ModFluids;
import net.steve.darkfantasy.item.custom.FireballStaffItem;
import net.steve.darkfantasy.item.custom.LightningStaffItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.steve.darkfantasy.block.ModBlocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(DarkFantasy.MOD_ID);

    public static final DeferredItem<Item> SHADOWSTEEL = ITEMS.registerSimpleItem("shadowsteel");
    public static final DeferredItem<Item> RAW_SHADOWSTEEL = ITEMS.registerSimpleItem("raw_shadowsteel");


    public static final DeferredItem<Item> ECLIPSIUM = ITEMS.registerItem("eclipsium",
            properties -> new Item(properties.rarity(Rarity.EPIC)));

    public static final DeferredItem<Item> MOONSILVER = ITEMS.registerSimpleItem("moonsilver");
    public static final DeferredItem<Item> RAW_MOONSILVER = ITEMS.registerSimpleItem("raw_moonsilver");

    public static final DeferredItem<Item> DAWNMETAL = ITEMS.registerSimpleItem("dawnmetal");
    public static final DeferredItem<Item> RAW_DAWNMETAL = ITEMS.registerSimpleItem("raw_dawnmetal");

    public static final DeferredItem<Item> FAIRY_DUST = ITEMS.registerSimpleItem("fairy_dust");

    public static final DeferredItem<Item> LYTEBUG_DUST = ITEMS.registerSimpleItem("lytebug_dust");

    // Elixir bucket — stacks to 1 (matches vanilla water/lava buckets). The
    // {@code craftRemainder(Items.BUCKET)} hook means if elixir is ever used in
    // a recipe, the empty bucket is returned (vanilla water-bucket pattern).
    // Rarity RARE is cosmetic only (cyan name in tooltips) — matches the
    // FluidType's rarity setting.
    public static final DeferredItem<BucketItem> ELIXIR_BUCKET = ITEMS.registerItem("elixir_bucket",
            properties -> new BucketItem(ModFluids.ELIXIR_SOURCE.get(), properties
                    .stacksTo(1)
                    .craftRemainder(Items.BUCKET)
                    .rarity(Rarity.RARE)));

    // Beer-brewing ingredients + drinkable. Hops doubles as the planting item for the
    // hops crop (BlockItem wrapping HOPS_CROP) — right-clicking tilled dirt with hops
    // places a stage-0 plant. Beer applies a fixed Resistance + Nausea combo via the
    // Consumable system (no custom Item subclass needed).
    public static final DeferredItem<Item> HOPS = ITEMS.registerItem("hops",
            properties -> new BlockItem(ModBlocks.HOPS_CROP.get(), properties));

    public static final DeferredItem<Item> BEER = ITEMS.registerItem("beer",
            properties -> new Item(properties
                    .stacksTo(16)
                    .food(ModFoods.BEER, ModFoods.BEER_CONSUMABLE)));

    // Empty container — players right-click the brewing keg with this to draw off a
    // quart of beer (one stein, one quart drained, one Beer item handed back).
    // Interaction logic lives in BrewingKegBlock#useItemOn.
    public static final DeferredItem<Item> STEIN_GLASS = ITEMS.registerItem("stein_glass",
            properties -> new Item(properties.stacksTo(16)));

    public static final DeferredItem<Item> LIGHTNING_STAFF = ITEMS.registerItem("lightning_staff",
            properties -> new LightningStaffItem(properties.durability(30).stacksTo(1)));

    public static final DeferredItem<Item> FIREBALL_STAFF = ITEMS.registerItem("fireball_staff",
            properties -> new FireballStaffItem(properties.durability(30).stacksTo(1)));

    // Spawn egg is tinted automatically via Item.Properties.spawnEgg(EntityType).
    public static final DeferredItem<SpawnEggItem> FAIRY_SPAWN_EGG = ITEMS.registerItem(
            "fairy_spawn_egg",
            properties -> new SpawnEggItem(properties.spawnEgg(ModEntities.FAIRY.get())));

    public static final DeferredItem<SpawnEggItem> WIZARD_SPAWN_EGG = ITEMS.registerItem(
            "wizard_spawn_egg",
            properties -> new SpawnEggItem(properties.spawnEgg(ModEntities.WIZARD.get())));

    public static final DeferredItem<SpawnEggItem> ELECTRO_DRAGON_SPAWN_EGG = ITEMS.registerItem(
            "electro_dragon_spawn_egg",
            properties -> new SpawnEggItem(properties.spawnEgg(ModEntities.ELECTRO_DRAGON.get())));

    public static final DeferredItem<SpawnEggItem> GOBLIN_SPAWN_EGG = ITEMS.registerItem(
            "goblin_spawn_egg",
            properties -> new SpawnEggItem(properties.spawnEgg(ModEntities.GOBLIN.get())));

    public static final DeferredItem<SpawnEggItem> GNOME_SPAWN_EGG = ITEMS.registerItem(
            "gnome_spawn_egg",
            properties -> new SpawnEggItem(properties.spawnEgg(ModEntities.GNOME.get())));

    public static final DeferredItem<SpawnEggItem> LYTEBUG_SPAWN_EGG = ITEMS.registerItem(
            "lytebug_spawn_egg",
            properties -> new SpawnEggItem(properties.spawnEgg(ModEntities.LYTEBUG.get())));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
