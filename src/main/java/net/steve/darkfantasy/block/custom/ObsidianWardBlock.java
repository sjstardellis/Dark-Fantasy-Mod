package net.steve.darkfantasy.block.custom;

import net.steve.darkfantasy.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Eclipse Citadel's warded Obsidian Brick. Indestructible to everything <em>except</em>
 * a netherite or eclipsium pickaxe, and even then it mines at a flat, brutal rate:
 * {@value #BREAK_TICKS} ticks ≈ 3 minutes per block, regardless of Efficiency/Haste.
 *
 * <p>Vanilla tool-gating can't truly forbid weaker tools (they'd just grind through slowly),
 * so {@link #getDestroyProgress} is overridden to return zero progress for any other tool —
 * the block simply never breaks. TNT-proof via its blast resistance. This lets a fully
 * geared endgame player carve a way in if they're determined, without letting anyone cheat
 * past the adventure casually.
 */
public class ObsidianWardBlock extends Block {
    /** Ticks to break one block with an allowed pickaxe (20 tps → 3 minutes). */
    public static final float BREAK_TICKS = 3600.0F;

    public ObsidianWardBlock(Properties properties) {
        super(properties);
    }

    /** A netherite or eclipsium pickaxe — the only tools that bite this stone. */
    public static boolean canBreak(ItemStack tool) {
        return tool.is(Items.NETHERITE_PICKAXE) || tool.is(ModItems.ECLIPSIUM_PICKAXE.get());
    }

    /** Per-tick break progress: fixed slow rate for allowed picks, zero (unbreakable) otherwise. */
    public static float wardedProgress(Player player) {
        return canBreak(player.getMainHandItem()) ? 1.0F / BREAK_TICKS : 0.0F;
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        return wardedProgress(player);
    }
}
