package net.steve.darkfantasy.item;

import net.minecraft.world.item.Rarity;
import net.steve.darkfantasy.DarkFantasy;
import net.steve.darkfantasy.food.ModFoods;
import net.steve.darkfantasy.init.ModEntities;
import net.steve.darkfantasy.init.ModFluids;
import net.steve.darkfantasy.item.custom.BlinkStaffItem;
import net.steve.darkfantasy.item.custom.CinderStaffItem;
import net.steve.darkfantasy.item.custom.DaggerItem;
import net.steve.darkfantasy.item.custom.FireballStaffItem;
import net.steve.darkfantasy.item.custom.FrostStaffItem;
import net.steve.darkfantasy.item.custom.LightningStaffItem;
import net.steve.darkfantasy.item.custom.ScytheItem;
import net.steve.darkfantasy.item.custom.SunlanceItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.equipment.ArmorType;
import net.steve.darkfantasy.block.ModBlocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(DarkFantasy.MOD_ID);

    public static final DeferredItem<Item> SHADOWSTEEL = ITEMS.registerSimpleItem("shadowsteel");
    public static final DeferredItem<Item> RAW_SHADOWSTEEL = ITEMS.registerSimpleItem("raw_shadowsteel");

    // ---- Shadowsteel tools + armor (dark/stealth tier) ----
    public static final DeferredItem<Item> SHADOWSTEEL_SWORD = ITEMS.registerItem("shadowsteel_sword",
            p -> new Item(p.sword(ModMaterials.SHADOWSTEEL_TOOL, 3.0F, -2.4F)));
    public static final DeferredItem<Item> SHADOWSTEEL_PICKAXE = ITEMS.registerItem("shadowsteel_pickaxe",
            p -> new Item(p.pickaxe(ModMaterials.SHADOWSTEEL_TOOL, 1.0F, -2.8F)));
    public static final DeferredItem<AxeItem> SHADOWSTEEL_AXE = ITEMS.registerItem("shadowsteel_axe",
            p -> new AxeItem(ModMaterials.SHADOWSTEEL_TOOL, 6.0F, -3.1F, p));
    public static final DeferredItem<ShovelItem> SHADOWSTEEL_SHOVEL = ITEMS.registerItem("shadowsteel_shovel",
            p -> new ShovelItem(ModMaterials.SHADOWSTEEL_TOOL, 1.5F, -3.0F, p));
    public static final DeferredItem<HoeItem> SHADOWSTEEL_HOE = ITEMS.registerItem("shadowsteel_hoe",
            p -> new HoeItem(ModMaterials.SHADOWSTEEL_TOOL, -2.0F, -1.0F, p));
    public static final DeferredItem<Item> SHADOWSTEEL_HELMET = ITEMS.registerItem("shadowsteel_helmet",
            p -> new Item(p.humanoidArmor(ModMaterials.SHADOWSTEEL_ARMOR, ArmorType.HELMET)));
    public static final DeferredItem<Item> SHADOWSTEEL_CHESTPLATE = ITEMS.registerItem("shadowsteel_chestplate",
            p -> new Item(p.humanoidArmor(ModMaterials.SHADOWSTEEL_ARMOR, ArmorType.CHESTPLATE)));
    public static final DeferredItem<Item> SHADOWSTEEL_LEGGINGS = ITEMS.registerItem("shadowsteel_leggings",
            p -> new Item(p.humanoidArmor(ModMaterials.SHADOWSTEEL_ARMOR, ArmorType.LEGGINGS)));
    public static final DeferredItem<Item> SHADOWSTEEL_BOOTS = ITEMS.registerItem("shadowsteel_boots",
            p -> new Item(p.humanoidArmor(ModMaterials.SHADOWSTEEL_ARMOR, ArmorType.BOOTS)));

    // Signature: fast paired daggers — bonus damage from behind or while sneaking.
    public static final DeferredItem<DaggerItem> SHADOWSTEEL_DAGGERS = ITEMS.registerItem("shadowsteel_daggers",
            p -> new DaggerItem(p.sword(ModMaterials.SHADOWSTEEL_TOOL, 2.0F, -1.6F)));

    public static final DeferredItem<Item> ECLIPSIUM = ITEMS.registerItem("eclipsium",
            properties -> new Item(properties.rarity(Rarity.EPIC)));

    // ---- Eclipsium tools + armor (fused endgame; fireproof + Epic) ----
    public static final DeferredItem<Item> ECLIPSIUM_SWORD = ITEMS.registerItem("eclipsium_sword",
            p -> new Item(p.sword(ModMaterials.ECLIPSIUM_TOOL, 3.0F, -2.4F).fireResistant().rarity(Rarity.EPIC)));
    public static final DeferredItem<Item> ECLIPSIUM_PICKAXE = ITEMS.registerItem("eclipsium_pickaxe",
            p -> new Item(p.pickaxe(ModMaterials.ECLIPSIUM_TOOL, 1.0F, -2.8F).fireResistant().rarity(Rarity.EPIC)));
    public static final DeferredItem<AxeItem> ECLIPSIUM_AXE = ITEMS.registerItem("eclipsium_axe",
            p -> new AxeItem(ModMaterials.ECLIPSIUM_TOOL, 6.0F, -3.1F, p.fireResistant().rarity(Rarity.EPIC)));
    public static final DeferredItem<ShovelItem> ECLIPSIUM_SHOVEL = ITEMS.registerItem("eclipsium_shovel",
            p -> new ShovelItem(ModMaterials.ECLIPSIUM_TOOL, 1.5F, -3.0F, p.fireResistant().rarity(Rarity.EPIC)));
    public static final DeferredItem<HoeItem> ECLIPSIUM_HOE = ITEMS.registerItem("eclipsium_hoe",
            p -> new HoeItem(ModMaterials.ECLIPSIUM_TOOL, -2.0F, -1.0F, p.fireResistant().rarity(Rarity.EPIC)));
    public static final DeferredItem<Item> ECLIPSIUM_HELMET = ITEMS.registerItem("eclipsium_helmet",
            p -> new Item(p.humanoidArmor(ModMaterials.ECLIPSIUM_ARMOR, ArmorType.HELMET).fireResistant().rarity(Rarity.EPIC)));
    public static final DeferredItem<Item> ECLIPSIUM_CHESTPLATE = ITEMS.registerItem("eclipsium_chestplate",
            p -> new Item(p.humanoidArmor(ModMaterials.ECLIPSIUM_ARMOR, ArmorType.CHESTPLATE).fireResistant().rarity(Rarity.EPIC)));
    public static final DeferredItem<Item> ECLIPSIUM_LEGGINGS = ITEMS.registerItem("eclipsium_leggings",
            p -> new Item(p.humanoidArmor(ModMaterials.ECLIPSIUM_ARMOR, ArmorType.LEGGINGS).fireResistant().rarity(Rarity.EPIC)));
    public static final DeferredItem<Item> ECLIPSIUM_BOOTS = ITEMS.registerItem("eclipsium_boots",
            p -> new Item(p.humanoidArmor(ModMaterials.ECLIPSIUM_ARMOR, ArmorType.BOOTS).fireResistant().rarity(Rarity.EPIC)));

    public static final DeferredItem<Item> MOONSILVER = ITEMS.registerSimpleItem("moonsilver");
    public static final DeferredItem<Item> RAW_MOONSILVER = ITEMS.registerSimpleItem("raw_moonsilver");

    // ---- Moonsilver tools + armor (lunar tier; materials in ModMaterials) ----
    // Per-item attack/defense baselines mirror vanilla iron (see net.minecraft Items).
    public static final DeferredItem<Item> MOONSILVER_SWORD = ITEMS.registerItem("moonsilver_sword",
            p -> new Item(p.sword(ModMaterials.MOONSILVER_TOOL, 3.0F, -2.4F)));
    public static final DeferredItem<Item> MOONSILVER_PICKAXE = ITEMS.registerItem("moonsilver_pickaxe",
            p -> new Item(p.pickaxe(ModMaterials.MOONSILVER_TOOL, 1.0F, -2.8F)));
    public static final DeferredItem<AxeItem> MOONSILVER_AXE = ITEMS.registerItem("moonsilver_axe",
            p -> new AxeItem(ModMaterials.MOONSILVER_TOOL, 6.0F, -3.1F, p));
    public static final DeferredItem<ShovelItem> MOONSILVER_SHOVEL = ITEMS.registerItem("moonsilver_shovel",
            p -> new ShovelItem(ModMaterials.MOONSILVER_TOOL, 1.5F, -3.0F, p));
    public static final DeferredItem<HoeItem> MOONSILVER_HOE = ITEMS.registerItem("moonsilver_hoe",
            p -> new HoeItem(ModMaterials.MOONSILVER_TOOL, -2.0F, -1.0F, p));

    public static final DeferredItem<Item> MOONSILVER_HELMET = ITEMS.registerItem("moonsilver_helmet",
            p -> new Item(p.humanoidArmor(ModMaterials.MOONSILVER_ARMOR, ArmorType.HELMET)));
    public static final DeferredItem<Item> MOONSILVER_CHESTPLATE = ITEMS.registerItem("moonsilver_chestplate",
            p -> new Item(p.humanoidArmor(ModMaterials.MOONSILVER_ARMOR, ArmorType.CHESTPLATE)));
    public static final DeferredItem<Item> MOONSILVER_LEGGINGS = ITEMS.registerItem("moonsilver_leggings",
            p -> new Item(p.humanoidArmor(ModMaterials.MOONSILVER_ARMOR, ArmorType.LEGGINGS)));
    public static final DeferredItem<Item> MOONSILVER_BOOTS = ITEMS.registerItem("moonsilver_boots",
            p -> new Item(p.humanoidArmor(ModMaterials.MOONSILVER_ARMOR, ArmorType.BOOTS)));

    // Signature: heavy scythe — normal hit + a wide sweep to nearby foes.
    public static final DeferredItem<ScytheItem> MOONSILVER_SCYTHE = ITEMS.registerItem("moonsilver_scythe",
            p -> new ScytheItem(p.sword(ModMaterials.MOONSILVER_TOOL, 4.0F, -3.0F)));

    public static final DeferredItem<Item> DAWNMETAL = ITEMS.registerSimpleItem("dawnmetal");
    public static final DeferredItem<Item> RAW_DAWNMETAL = ITEMS.registerSimpleItem("raw_dawnmetal");

    // ---- Dawnmetal tools + armor (solar / diamond tier) ----
    public static final DeferredItem<Item> DAWNMETAL_SWORD = ITEMS.registerItem("dawnmetal_sword",
            p -> new Item(p.sword(ModMaterials.DAWNMETAL_TOOL, 3.0F, -2.4F)));
    public static final DeferredItem<Item> DAWNMETAL_PICKAXE = ITEMS.registerItem("dawnmetal_pickaxe",
            p -> new Item(p.pickaxe(ModMaterials.DAWNMETAL_TOOL, 1.0F, -2.8F)));
    public static final DeferredItem<AxeItem> DAWNMETAL_AXE = ITEMS.registerItem("dawnmetal_axe",
            p -> new AxeItem(ModMaterials.DAWNMETAL_TOOL, 6.0F, -3.1F, p));
    public static final DeferredItem<ShovelItem> DAWNMETAL_SHOVEL = ITEMS.registerItem("dawnmetal_shovel",
            p -> new ShovelItem(ModMaterials.DAWNMETAL_TOOL, 1.5F, -3.0F, p));
    public static final DeferredItem<HoeItem> DAWNMETAL_HOE = ITEMS.registerItem("dawnmetal_hoe",
            p -> new HoeItem(ModMaterials.DAWNMETAL_TOOL, -2.0F, -1.0F, p));
    public static final DeferredItem<Item> DAWNMETAL_HELMET = ITEMS.registerItem("dawnmetal_helmet",
            p -> new Item(p.humanoidArmor(ModMaterials.DAWNMETAL_ARMOR, ArmorType.HELMET)));
    public static final DeferredItem<Item> DAWNMETAL_CHESTPLATE = ITEMS.registerItem("dawnmetal_chestplate",
            p -> new Item(p.humanoidArmor(ModMaterials.DAWNMETAL_ARMOR, ArmorType.CHESTPLATE)));
    public static final DeferredItem<Item> DAWNMETAL_LEGGINGS = ITEMS.registerItem("dawnmetal_leggings",
            p -> new Item(p.humanoidArmor(ModMaterials.DAWNMETAL_ARMOR, ArmorType.LEGGINGS)));
    public static final DeferredItem<Item> DAWNMETAL_BOOTS = ITEMS.registerItem("dawnmetal_boots",
            p -> new Item(p.humanoidArmor(ModMaterials.DAWNMETAL_ARMOR, ArmorType.BOOTS)));

    // Signature: solar reach weapon — longer reach, ignites foes, smites undead.
    public static final DeferredItem<SunlanceItem> DAWNMETAL_SUNLANCE = ITEMS.registerItem("dawnmetal_sunlance",
            p -> new SunlanceItem(SunlanceItem.applyProperties(p)));

    // ---- Biome gems (each mined only in its rare biome; staff cores + alchemy reagents) ----
    public static final DeferredItem<Item> GRIMSHARD = ITEMS.registerSimpleItem("grimshard");
    public static final DeferredItem<Item> EMBERSTONE = ITEMS.registerSimpleItem("emberstone");
    public static final DeferredItem<Item> SOUL_PEARL = ITEMS.registerSimpleItem("soul_pearl");

    // ---- Alchemy reagents (mob drops) ----
    public static final DeferredItem<Item> ARCANE_ASH = ITEMS.registerSimpleItem("arcane_ash");
    public static final DeferredItem<Item> STORM_SCALE = ITEMS.registerSimpleItem("storm_scale");

    // ---- Drinkable elixirs (brewed at the alchemy stand) ----
    public static final DeferredItem<Item> MOONLIGHT_ELIXIR = ITEMS.registerItem("moonlight_elixir",
            p -> new Item(p.stacksTo(8).food(ModFoods.ELIXIR, ModFoods.MOONLIGHT_ELIXIR)));
    public static final DeferredItem<Item> STONESKIN_ELIXIR = ITEMS.registerItem("stoneskin_elixir",
            p -> new Item(p.stacksTo(8).food(ModFoods.ELIXIR, ModFoods.STONESKIN_ELIXIR)));
    public static final DeferredItem<Item> EMBERBLOOD_ELIXIR = ITEMS.registerItem("emberblood_elixir",
            p -> new Item(p.stacksTo(8).food(ModFoods.ELIXIR, ModFoods.EMBERBLOOD_ELIXIR)));
    public static final DeferredItem<Item> WISPSTEP_ELIXIR = ITEMS.registerItem("wispstep_elixir",
            p -> new Item(p.stacksTo(8).food(ModFoods.ELIXIR, ModFoods.WISPSTEP_ELIXIR)));

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

    // ---- Gem-core staves (crafted at the alchemy stand from biome gems) ----
    public static final DeferredItem<Item> FROST_STAFF = ITEMS.registerItem("frost_staff",
            properties -> new FrostStaffItem(properties.durability(50).stacksTo(1)));
    public static final DeferredItem<Item> BLINK_STAFF = ITEMS.registerItem("blink_staff",
            properties -> new BlinkStaffItem(properties.durability(50).stacksTo(1)));
    public static final DeferredItem<Item> CINDER_STAFF = ITEMS.registerItem("cinder_staff",
            properties -> new CinderStaffItem(properties.durability(40).stacksTo(1)));

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
