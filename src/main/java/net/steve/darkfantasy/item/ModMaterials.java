package net.steve.darkfantasy.item;

import net.steve.darkfantasy.DarkFantasy;
import net.steve.darkfantasy.tags.ModTags;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.Map;

/**
 * Tool + armor materials for the mod's metals. Each metal gets one {@link ToolMaterial}
 * and one {@link ArmorMaterial}; the per-item attack/defense baselines are applied at
 * registration time in {@link ModItems} (mirroring vanilla {@code Items}).
 *
 * <h2>Moonsilver</h2>
 * A lunar metal sitting just above iron: it mines at iron level
 * ({@link BlockTags#INCORRECT_FOR_IRON_TOOL}) but carries more durability, a little extra
 * attack bonus, and notably high enchantability (18) plus a point of armor toughness —
 * the "magical, takes enchants well" identity rather than a raw stat jump. Set-bonus
 * behaviour (stronger at night / vs undead) is layered on separately later.
 */
public final class ModMaterials {
    private ModMaterials() {}

    // ---- Moonsilver ---------------------------------------------------------

    /** Repair/material tag shared by moonsilver tools and armor (contains the ingot). */
    // (defined in ModTags.Items.MOONSILVER_REPAIR)

    public static final ToolMaterial MOONSILVER_TOOL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_IRON_TOOL, // mines the same block tiers as iron
            500,    // durability   (iron 250, diamond 1561)
            6.5F,   // mining speed (iron 6.0, diamond 8.0)
            2.5F,   // attack bonus (iron 2.0, diamond 3.0)
            18,     // enchantability (iron 14) — lunar metal takes enchants well
            ModTags.Items.MOONSILVER_REPAIR);

    /** Equipment-asset key — drives the worn armor layer textures under assets/darkfantasy/equipment/moonsilver.json. */
    public static final ResourceKey<EquipmentAsset> MOONSILVER_ARMOR_ASSET =
            ResourceKey.create(EquipmentAssets.ROOT_ID,
                    Identifier.fromNamespaceAndPath(DarkFantasy.MOD_ID, "moonsilver"));

    public static final ArmorMaterial MOONSILVER_ARMOR = new ArmorMaterial(
            20,     // durability multiplier (iron 15, diamond 33)
            Map.of( // defense per slot (iron: 2/5/6/2)
                    ArmorType.BOOTS, 2,
                    ArmorType.LEGGINGS, 5,
                    ArmorType.CHESTPLATE, 6,
                    ArmorType.HELMET, 2,
                    ArmorType.BODY, 5),
            18,     // enchantability
            SoundEvents.ARMOR_EQUIP_IRON,
            1.0F,   // toughness (iron 0.0, diamond 2.0)
            0.0F,   // knockback resistance
            ModTags.Items.MOONSILVER_REPAIR,
            MOONSILVER_ARMOR_ASSET);

    // ---- Shadowsteel (iron tier; dark/stealth — high attack, lighter armor) ----
    public static final ToolMaterial SHADOWSTEEL_TOOL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_IRON_TOOL,
            450,    // durability
            6.5F,   // speed
            3.0F,   // attack bonus — a wicked dark edge
            15,     // enchantability
            ModTags.Items.SHADOWSTEEL_REPAIR);

    public static final ResourceKey<EquipmentAsset> SHADOWSTEEL_ARMOR_ASSET =
            ResourceKey.create(EquipmentAssets.ROOT_ID,
                    Identifier.fromNamespaceAndPath(DarkFantasy.MOD_ID, "shadowsteel"));

    public static final ArmorMaterial SHADOWSTEEL_ARMOR = new ArmorMaterial(
            17,
            Map.of(ArmorType.BOOTS, 2, ArmorType.LEGGINGS, 5, ArmorType.CHESTPLATE, 6,
                    ArmorType.HELMET, 2, ArmorType.BODY, 5),
            15,
            SoundEvents.ARMOR_EQUIP_IRON,
            0.0F,   // no toughness — light and quick, not a tank
            0.0F,
            ModTags.Items.SHADOWSTEEL_REPAIR,
            SHADOWSTEEL_ARMOR_ASSET);

    // ---- Dawnmetal (diamond tier; solar) -----------------------------------
    public static final ToolMaterial DAWNMETAL_TOOL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
            1600,   // durability (diamond 1561)
            8.0F,   // speed (diamond 8.0)
            3.5F,   // attack bonus (diamond 3.0)
            12,     // enchantability (diamond 10)
            ModTags.Items.DAWNMETAL_REPAIR);

    public static final ResourceKey<EquipmentAsset> DAWNMETAL_ARMOR_ASSET =
            ResourceKey.create(EquipmentAssets.ROOT_ID,
                    Identifier.fromNamespaceAndPath(DarkFantasy.MOD_ID, "dawnmetal"));

    public static final ArmorMaterial DAWNMETAL_ARMOR = new ArmorMaterial(
            34,     // durability multiplier (diamond 33)
            Map.of(ArmorType.BOOTS, 3, ArmorType.LEGGINGS, 6, ArmorType.CHESTPLATE, 8,
                    ArmorType.HELMET, 3, ArmorType.BODY, 11),
            12,
            SoundEvents.ARMOR_EQUIP_DIAMOND,
            2.0F,   // toughness (diamond 2.0)
            0.0F,
            ModTags.Items.DAWNMETAL_REPAIR,
            DAWNMETAL_ARMOR_ASSET);

    // ---- Eclipsium (netherite tier; fused endgame, from alchemy) -----------
    public static final ToolMaterial ECLIPSIUM_TOOL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
            2200,   // durability (netherite 2031)
            9.0F,   // speed (netherite 9.0)
            4.0F,   // attack bonus (netherite 4.0)
            15,     // enchantability (netherite 15)
            ModTags.Items.ECLIPSIUM_REPAIR);

    public static final ResourceKey<EquipmentAsset> ECLIPSIUM_ARMOR_ASSET =
            ResourceKey.create(EquipmentAssets.ROOT_ID,
                    Identifier.fromNamespaceAndPath(DarkFantasy.MOD_ID, "eclipsium"));

    public static final ArmorMaterial ECLIPSIUM_ARMOR = new ArmorMaterial(
            40,     // durability multiplier (netherite 37)
            Map.of(ArmorType.BOOTS, 3, ArmorType.LEGGINGS, 6, ArmorType.CHESTPLATE, 8,
                    ArmorType.HELMET, 3, ArmorType.BODY, 19),
            15,
            SoundEvents.ARMOR_EQUIP_NETHERITE,
            3.5F,   // toughness (netherite 3.0)
            0.1F,   // knockback resistance (netherite 0.1)
            ModTags.Items.ECLIPSIUM_REPAIR,
            ECLIPSIUM_ARMOR_ASSET);
}
