package net.steve.darkfantasy.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Warded Obsidian Glass — the see-through counterpart to {@link ObsidianWardBlock}. Same
 * rule: only a netherite or eclipsium pickaxe can break it, at the same flat ~3-minute rate.
 */
public class ObsidianWardGlassBlock extends TransparentBlock {
    public ObsidianWardGlassBlock(Properties properties) {
        super(properties);
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        return ObsidianWardBlock.wardedProgress(player);
    }
}
