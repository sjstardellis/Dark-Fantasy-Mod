package net.steve.darkfantasy.datagen;

import net.steve.darkfantasy.DarkFantasy;
import net.steve.darkfantasy.block.ModBlocks;
import net.steve.darkfantasy.item.ModItems;
import net.steve.darkfantasy.tags.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.data.ItemTagsProvider;

import java.util.concurrent.CompletableFuture;

public class ModItemTagsProvider extends ItemTagsProvider {
    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, DarkFantasy.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        // ── Metal gear tags (repair material, tool/armor types, enchantability) ──
        gearTags(ModTags.Items.MOONSILVER_REPAIR, ModItems.MOONSILVER.get(),
                ModItems.MOONSILVER_SWORD.get(), ModItems.MOONSILVER_PICKAXE.get(), ModItems.MOONSILVER_AXE.get(),
                ModItems.MOONSILVER_SHOVEL.get(), ModItems.MOONSILVER_HOE.get(), ModItems.MOONSILVER_HELMET.get(),
                ModItems.MOONSILVER_CHESTPLATE.get(), ModItems.MOONSILVER_LEGGINGS.get(), ModItems.MOONSILVER_BOOTS.get());
        gearTags(ModTags.Items.SHADOWSTEEL_REPAIR, ModItems.SHADOWSTEEL.get(),
                ModItems.SHADOWSTEEL_SWORD.get(), ModItems.SHADOWSTEEL_PICKAXE.get(), ModItems.SHADOWSTEEL_AXE.get(),
                ModItems.SHADOWSTEEL_SHOVEL.get(), ModItems.SHADOWSTEEL_HOE.get(), ModItems.SHADOWSTEEL_HELMET.get(),
                ModItems.SHADOWSTEEL_CHESTPLATE.get(), ModItems.SHADOWSTEEL_LEGGINGS.get(), ModItems.SHADOWSTEEL_BOOTS.get());
        gearTags(ModTags.Items.DAWNMETAL_REPAIR, ModItems.DAWNMETAL.get(),
                ModItems.DAWNMETAL_SWORD.get(), ModItems.DAWNMETAL_PICKAXE.get(), ModItems.DAWNMETAL_AXE.get(),
                ModItems.DAWNMETAL_SHOVEL.get(), ModItems.DAWNMETAL_HOE.get(), ModItems.DAWNMETAL_HELMET.get(),
                ModItems.DAWNMETAL_CHESTPLATE.get(), ModItems.DAWNMETAL_LEGGINGS.get(), ModItems.DAWNMETAL_BOOTS.get());
        gearTags(ModTags.Items.ECLIPSIUM_REPAIR, ModItems.ECLIPSIUM.get(),
                ModItems.ECLIPSIUM_SWORD.get(), ModItems.ECLIPSIUM_PICKAXE.get(), ModItems.ECLIPSIUM_AXE.get(),
                ModItems.ECLIPSIUM_SHOVEL.get(), ModItems.ECLIPSIUM_HOE.get(), ModItems.ECLIPSIUM_HELMET.get(),
                ModItems.ECLIPSIUM_CHESTPLATE.get(), ModItems.ECLIPSIUM_LEGGINGS.get(), ModItems.ECLIPSIUM_BOOTS.get());

        // Signature weapons — melee-enchantable (sharpness, fire aspect, unbreaking, …).
        Item scythe = ModItems.MOONSILVER_SCYTHE.get();
        Item daggers = ModItems.SHADOWSTEEL_DAGGERS.get();
        Item sunlance = ModItems.DAWNMETAL_SUNLANCE.get();
        tag(ItemTags.WEAPON_ENCHANTABLE).add(scythe).add(daggers).add(sunlance);
        tag(ItemTags.SHARP_WEAPON_ENCHANTABLE).add(scythe).add(daggers).add(sunlance);
        tag(ItemTags.MELEE_WEAPON_ENCHANTABLE).add(scythe).add(daggers).add(sunlance);
        tag(ItemTags.FIRE_ASPECT_ENCHANTABLE).add(scythe).add(daggers).add(sunlance);
        tag(ItemTags.DURABILITY_ENCHANTABLE).add(scythe).add(daggers).add(sunlance);
        tag(ItemTags.VANISHING_ENCHANTABLE).add(scythe).add(daggers).add(sunlance);

        // Cinderbark is fire-immune nether wood. Tagging the items as
        // NON_FLAMMABLE_WOOD prevents the item form from burning when dropped
        // into fire/lava, matching crimson/warped item behavior.
        tag(ItemTags.NON_FLAMMABLE_WOOD)
                .add(ModBlocks.CINDERBARK_STEM.get().asItem())
                .add(ModBlocks.STRIPPED_CINDERBARK_STEM.get().asItem())
                .add(ModBlocks.CINDERBARK_HYPHAE.get().asItem())
                .add(ModBlocks.STRIPPED_CINDERBARK_HYPHAE.get().asItem())
                .add(ModBlocks.CINDERBARK_PLANKS.get().asItem())
                .add(ModBlocks.CINDERBARK_STAIRS.get().asItem())
                .add(ModBlocks.CINDERBARK_SLAB.get().asItem())
                .add(ModBlocks.CINDERBARK_FENCE.get().asItem())
                .add(ModBlocks.CINDERBARK_FENCE_GATE.get().asItem())
                .add(ModBlocks.CINDERBARK_DOOR.get().asItem())
                .add(ModBlocks.CINDERBARK_TRAPDOOR.get().asItem())
                .add(ModBlocks.CINDERBARK_BUTTON.get().asItem())
                .add(ModBlocks.CINDERBARK_PRESSURE_PLATE.get().asItem());
    }

    /**
     * Adds the full tag set for one metal's gear: repair material, tool/armor type
     * tags, and the enchantable/* tags that drive what the enchanting table and anvil
     * offer (mining enchants on tools, weapon enchants on sword/axe, armor enchants on
     * the four pieces, durability/vanishing on everything).
     */
    private void gearTags(TagKey<Item> repair, Item ingot, Item sword, Item pickaxe, Item axe,
                          Item shovel, Item hoe, Item helmet, Item chestplate, Item leggings, Item boots) {
        tag(repair).add(ingot);

        tag(ItemTags.SWORDS).add(sword);
        tag(ItemTags.PICKAXES).add(pickaxe);
        tag(ItemTags.AXES).add(axe);
        tag(ItemTags.SHOVELS).add(shovel);
        tag(ItemTags.HOES).add(hoe);

        tag(ItemTags.HEAD_ARMOR).add(helmet);
        tag(ItemTags.CHEST_ARMOR).add(chestplate);
        tag(ItemTags.LEG_ARMOR).add(leggings);
        tag(ItemTags.FOOT_ARMOR).add(boots);
        tag(ItemTags.TRIMMABLE_ARMOR).add(helmet).add(chestplate).add(leggings).add(boots);

        tag(ItemTags.DURABILITY_ENCHANTABLE)
                .add(sword).add(pickaxe).add(axe).add(shovel).add(hoe)
                .add(helmet).add(chestplate).add(leggings).add(boots);
        tag(ItemTags.VANISHING_ENCHANTABLE)
                .add(sword).add(pickaxe).add(axe).add(shovel).add(hoe)
                .add(helmet).add(chestplate).add(leggings).add(boots);

        tag(ItemTags.WEAPON_ENCHANTABLE).add(sword).add(axe);
        tag(ItemTags.SHARP_WEAPON_ENCHANTABLE).add(sword).add(axe);
        tag(ItemTags.MELEE_WEAPON_ENCHANTABLE).add(sword).add(axe);
        tag(ItemTags.FIRE_ASPECT_ENCHANTABLE).add(sword);
        tag(ItemTags.SWEEPING_ENCHANTABLE).add(sword);

        tag(ItemTags.MINING_ENCHANTABLE).add(pickaxe).add(axe).add(shovel).add(hoe);
        tag(ItemTags.MINING_LOOT_ENCHANTABLE).add(pickaxe).add(axe).add(shovel).add(hoe);

        tag(ItemTags.ARMOR_ENCHANTABLE).add(helmet).add(chestplate).add(leggings).add(boots);
        tag(ItemTags.EQUIPPABLE_ENCHANTABLE).add(helmet).add(chestplate).add(leggings).add(boots);
        tag(ItemTags.HEAD_ARMOR_ENCHANTABLE).add(helmet);
        tag(ItemTags.CHEST_ARMOR_ENCHANTABLE).add(chestplate);
        tag(ItemTags.LEG_ARMOR_ENCHANTABLE).add(leggings);
        tag(ItemTags.FOOT_ARMOR_ENCHANTABLE).add(boots);
    }
}
