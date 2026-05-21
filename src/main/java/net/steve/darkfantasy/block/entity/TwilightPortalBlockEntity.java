package net.steve.darkfantasy.block.entity;

import net.steve.darkfantasy.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.TheEndPortalBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Block entity for the Twilight Forest portal. Same starfield-rendering plumbing as the
 * Skylands portal — both subclass {@link TheEndPortalBlockEntity} so we can reuse the
 * end-portal render type. The two are kept as separate types so the dimension dispatch
 * (which key the portal teleports to) can be wired through the block class cleanly.
 */
public class TwilightPortalBlockEntity extends TheEndPortalBlockEntity {
    public TwilightPortalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TWILIGHT_PORTAL_BE.get(), pos, state);
    }

    @Override
    public boolean shouldRenderFace(Direction direction) {
        return direction.getAxis().isHorizontal();
    }
}
