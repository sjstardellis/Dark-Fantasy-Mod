package net.steve.darkfantasy.item;

import net.minecraft.world.item.Rarity;
import net.steve.darkfantasy.DarkFantasy;
import net.steve.darkfantasy.food.ModFoods;
import net.steve.darkfantasy.init.ModEntities;
import net.steve.darkfantasy.init.ModFluids;
import net.steve.darkfantasy.item.custom.BlinkStaffItem;
import net.steve.darkfantasy.item.custom.CinderStaffItem;
import net.steve.darkfantasy.item.custom.CrescentBowItem;
import net.steve.darkfantasy.item.custom.DaggerItem;
import net.steve.darkfantasy.item.custom.DawnmetalArbalestItem;
import net.steve.darkfantasy.item.custom.ThrowingDaggerItem;
import net.steve.darkfantasy.item.custom.FireballStaffItem;
import net.steve.darkfantasy.item.custom.FrostStaffItem;
import net.steve.darkfantasy.item.custom.EclipseGreatswordItem;
import net.steve.darkfantasy.item.custom.EclipseTomeItem;
import net.steve.darkfantasy.item.custom.EvokerClawTomeItem;
import net.steve.darkfantasy.item.custom.LightningStaffItem;
import net.steve.darkfantasy.item.custom.MaelstromTomeItem;
import net.steve.darkfantasy.item.custom.ProspectorTomeItem;
import net.steve.darkfantasy.item.custom.ScytheItem;
import net.steve.darkfantasy.item.custom.StasisTomeItem;
import net.steve.darkfantasy.item.custom.SunlanceItem;
import net.steve.darkfantasy.item.custom.WardingTomeItem;
import net.steve.darkfantasy.item.custom.WayfarerTomeItem;
import net.steve.darkfantasy.item.custom.WitherSkullTomeItem;
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

    // Eclipse Crown — the Eclipse King's headgear and guaranteed boss drop. A helmet-slot
    // relic: Epic, fireproof, strong head defense, and (via ArmorSetBonusHandler) grants
    // Night Vision while worn.
    public static final DeferredItem<Item> ECLIPSE_CROWN = ITEMS.registerItem("eclipse_crown",
            p -> new Item(p.humanoidArmor(ModMaterials.ECLIPSE_CROWN_ARMOR, ArmorType.HELMET)
                    .fireResistant().rarity(Rarity.EPIC)));

    // Nightfall — the Eclipse King's greatsword (he wields it; it's his guaranteed drop).
    public static final DeferredItem<Item> ECLIPSE_GREATSWORD = ITEMS.registerItem("eclipse_greatsword",
            p -> new EclipseGreatswordItem(EclipseGreatswordItem.applyProperties(p)));

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

    // ---- Ranged line: one per metal identity (lunar bow / stealth thrown / solar crossbow) ----
    public static final DeferredItem<CrescentBowItem> CRESCENT_BOW = ITEMS.registerItem("crescent_bow",
            p -> new CrescentBowItem(p.durability(420).enchantable(1)));
    public static final DeferredItem<ThrowingDaggerItem> THROWING_DAGGER = ITEMS.registerItem("throwing_dagger",
            p -> new ThrowingDaggerItem(p.stacksTo(16)));
    public static final DeferredItem<DawnmetalArbalestItem> DAWNMETAL_ARBALEST = ITEMS.registerItem("dawnmetal_arbalest",
            p -> new DawnmetalArbalestItem(p.stacksTo(1).durability(520)
                    .component(net.minecraft.core.component.DataComponents.CHARGED_PROJECTILES,
                            net.minecraft.world.item.component.ChargedProjectiles.EMPTY)
                    .enchantable(1)));

    // ---- Biome gems (each mined only in its rare biome; staff cores + alchemy reagents) ----
    public static final DeferredItem<Item> MERCURYGLASS = ITEMS.registerSimpleItem("mercuryglass");
    public static final DeferredItem<Item> EMBERSTONE = ITEMS.registerSimpleItem("emberstone");
    public static final DeferredItem<Item> LARIMAR_PEARL = ITEMS.registerSimpleItem("larimar_pearl");

    // ---- Alchemy reagents (mob drops) ----
    public static final DeferredItem<Item> ARCANE_ASH = ITEMS.registerSimpleItem("arcane_ash");
    public static final DeferredItem<Item> STORM_SCALE = ITEMS.registerSimpleItem("storm_scale");
    /** Falls from the night sky during a starfall (see StarfallHandler). */
    public static final DeferredItem<Item> FALLEN_STAR = ITEMS.registerItem("fallen_star",
            p -> new Item(p.rarity(Rarity.UNCOMMON)));
    /** Dropped by umbral wraiths. */
    public static final DeferredItem<Item> UMBRA_ESSENCE = ITEMS.registerSimpleItem("umbra_essence");
    /** Dropped by cinder hounds. */
    public static final DeferredItem<Item> CINDER_FANG = ITEMS.registerSimpleItem("cinder_fang");
    /** Dropped by bog hags. */
    public static final DeferredItem<Item> HAG_ICHOR = ITEMS.registerSimpleItem("hag_ichor");

    // ---- Drinkable elixirs (brewed at the alchemy stand) ----
    public static final DeferredItem<Item> MOONLIGHT_ELIXIR = ITEMS.registerItem("moonlight_elixir",
            p -> new Item(p.stacksTo(8).food(ModFoods.ELIXIR, ModFoods.MOONLIGHT_ELIXIR)));
    public static final DeferredItem<Item> STONESKIN_ELIXIR = ITEMS.registerItem("stoneskin_elixir",
            p -> new Item(p.stacksTo(8).food(ModFoods.ELIXIR, ModFoods.STONESKIN_ELIXIR)));
    public static final DeferredItem<Item> EMBERBLOOD_ELIXIR = ITEMS.registerItem("emberblood_elixir",
            p -> new Item(p.stacksTo(8).food(ModFoods.ELIXIR, ModFoods.EMBERBLOOD_ELIXIR)));
    public static final DeferredItem<Item> WISPSTEP_ELIXIR = ITEMS.registerItem("wispstep_elixir",
            p -> new Item(p.stacksTo(8).food(ModFoods.ELIXIR, ModFoods.WISPSTEP_ELIXIR)));
    public static final DeferredItem<Item> STARLIGHT_ELIXIR = ITEMS.registerItem("starlight_elixir",
            p -> new Item(p.stacksTo(8).food(ModFoods.ELIXIR, ModFoods.STARLIGHT_ELIXIR)));
    public static final DeferredItem<Item> UMBRAL_ELIXIR = ITEMS.registerItem("umbral_elixir",
            p -> new Item(p.stacksTo(8).food(ModFoods.ELIXIR, ModFoods.UMBRAL_ELIXIR)));
    public static final DeferredItem<Item> HOUNDSBLOOD_ELIXIR = ITEMS.registerItem("houndsblood_elixir",
            p -> new Item(p.stacksTo(8).food(ModFoods.ELIXIR, ModFoods.HOUNDSBLOOD_ELIXIR)));
    public static final DeferredItem<Item> WITCHBANE_ELIXIR = ITEMS.registerItem("witchbane_elixir",
            p -> new Item(p.stacksTo(8).food(ModFoods.ELIXIR, ModFoods.WITCHBANE_ELIXIR)));

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

    // Brewing-keg drinks. Each is poured from the keg into a stein like beer; the keg
    // remembers which brew it holds and hands back the matching drink (see BrewingKegBlock).
    public static final DeferredItem<Item> DARK_ALE = ITEMS.registerItem("dark_ale",
            properties -> new Item(properties.stacksTo(16).food(ModFoods.DRINK, ModFoods.DARK_ALE_CONSUMABLE)));
    public static final DeferredItem<Item> HONEY_MEAD = ITEMS.registerItem("honey_mead",
            properties -> new Item(properties.stacksTo(16).food(ModFoods.DRINK, ModFoods.HONEY_MEAD_CONSUMABLE)));
    public static final DeferredItem<Item> GLOWBREW = ITEMS.registerItem("glowbrew",
            properties -> new Item(properties.stacksTo(16).food(ModFoods.DRINK, ModFoods.GLOWBREW_CONSUMABLE)));
    public static final DeferredItem<Item> MUSHROOM_STOUT = ITEMS.registerItem("mushroom_stout",
            properties -> new Item(properties.stacksTo(16).food(ModFoods.DRINK, ModFoods.MUSHROOM_STOUT_CONSUMABLE)));
    public static final DeferredItem<Item> WITHER_STOUT = ITEMS.registerItem("wither_stout",
            properties -> new Item(properties.stacksTo(16).food(ModFoods.DRINK, ModFoods.WITHER_STOUT_CONSUMABLE)));
    public static final DeferredItem<Item> BATTLE_BREW = ITEMS.registerItem("battle_brew",
            properties -> new Item(properties.stacksTo(16).food(ModFoods.DRINK, ModFoods.BATTLE_BREW_CONSUMABLE)));

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

    // ---- Spell books (cast at the alchemy stand; ritual/utility magic vs the staffs' projectiles) ----
    public static final DeferredItem<Item> WARDING_TOME = ITEMS.registerItem("warding_tome",
            properties -> new WardingTomeItem(properties.durability(40).stacksTo(1)));
    public static final DeferredItem<Item> MAELSTROM_TOME = ITEMS.registerItem("maelstrom_tome",
            properties -> new MaelstromTomeItem(properties.durability(40).stacksTo(1)));
    public static final DeferredItem<Item> WAYFARER_TOME = ITEMS.registerItem("wayfarer_tome",
            properties -> new WayfarerTomeItem(properties.durability(24).stacksTo(1)));
    public static final DeferredItem<Item> WITHER_SKULL_TOME = ITEMS.registerItem("wither_skull_tome",
            properties -> new WitherSkullTomeItem(properties.durability(24).stacksTo(1)));
    public static final DeferredItem<Item> PROSPECTOR_TOME = ITEMS.registerItem("prospector_tome",
            properties -> new ProspectorTomeItem(properties.durability(32).stacksTo(1)));
    public static final DeferredItem<Item> STASIS_TOME = ITEMS.registerItem("stasis_tome",
            properties -> new StasisTomeItem(properties.durability(30).stacksTo(1)));
    public static final DeferredItem<Item> EVOKER_CLAW_TOME = ITEMS.registerItem("evoker_claw_tome",
            properties -> new EvokerClawTomeItem(properties.durability(30).stacksTo(1)));
    /** Boss loot only — dropped by the Eclipse King, no recipe. */
    public static final DeferredItem<Item> ECLIPSE_TOME = ITEMS.registerItem("eclipse_tome",
            properties -> new EclipseTomeItem(properties.durability(40).stacksTo(1)
                    .fireResistant().rarity(Rarity.EPIC)));

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

    public static final DeferredItem<SpawnEggItem> ECLIPSE_KING_SPAWN_EGG = ITEMS.registerItem(
            "eclipse_king_spawn_egg",
            properties -> new SpawnEggItem(properties.spawnEgg(ModEntities.ECLIPSE_KING.get())));

    public static final DeferredItem<SpawnEggItem> CINDER_HOUND_SPAWN_EGG = ITEMS.registerItem(
            "cinder_hound_spawn_egg",
            properties -> new SpawnEggItem(properties.spawnEgg(ModEntities.CINDER_HOUND.get())));

    public static final DeferredItem<SpawnEggItem> UMBRAL_WRAITH_SPAWN_EGG = ITEMS.registerItem(
            "umbral_wraith_spawn_egg",
            properties -> new SpawnEggItem(properties.spawnEgg(ModEntities.UMBRAL_WRAITH.get())));

    public static final DeferredItem<SpawnEggItem> BOG_HAG_SPAWN_EGG = ITEMS.registerItem(
            "bog_hag_spawn_egg",
            properties -> new SpawnEggItem(properties.spawnEgg(ModEntities.BOG_HAG.get())));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
