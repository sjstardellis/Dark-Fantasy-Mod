package net.steve.darkfantasy.tags;

import net.steve.darkfantasy.DarkFantasy;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModTags {
    public static class Blocks {
        public static final TagKey<Block> SOUL_DETECTABLES = createTag("soul_detectables");
        /** Blocks accepted as the 16-block perimeter of the Twilight Forest portal frame. */
        public static final TagKey<Block> TWILIGHT_PORTAL_FRAME = createTag("twilight_portal_frame");

        private static TagKey<Block> createTag(String name) {
            return BlockTags.create(Identifier.fromNamespaceAndPath(DarkFantasy.MOD_ID, name));
        }
    }

    public static class Items {
        /** Repair + tool/armor material tags for each metal's gear (each contains the matching ingot). */
        public static final TagKey<Item> MOONSILVER_REPAIR = createTag("repairs_moonsilver_equipment");
        public static final TagKey<Item> SHADOWSTEEL_REPAIR = createTag("repairs_shadowsteel_equipment");
        public static final TagKey<Item> DAWNMETAL_REPAIR = createTag("repairs_dawnmetal_equipment");
        public static final TagKey<Item> ECLIPSIUM_REPAIR = createTag("repairs_eclipsium_equipment");

        private static TagKey<Item> createTag(String name) {
            return ItemTags.create(Identifier.fromNamespaceAndPath(DarkFantasy.MOD_ID, name));
        }
    }
}
