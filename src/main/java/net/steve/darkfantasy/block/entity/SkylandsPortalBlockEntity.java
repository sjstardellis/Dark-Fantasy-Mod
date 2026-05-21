package net.steve.darkfantasy.block.entity;

import net.steve.darkfantasy.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.TheEndPortalBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Block entity for the Skylands portal. Subclasses vanilla's {@link TheEndPortalBlockEntity}
 * so we plug into the same renderer infrastructure (which produces the end-gateway starfield
 * effect via {@code RenderTypes.endPortal()}).
 *
 * <p>The portal sits in the 1-wide center column of a 4-way symmetric 3D frame, so we render
 * all four horizontal faces (N, S, E, W). Top/bottom faces are skipped — they're internal to
 * the stack of portal blocks or hidden against the soul soil / gold.
 */
public class SkylandsPortalBlockEntity extends TheEndPortalBlockEntity {
    public SkylandsPortalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SKYLANDS_PORTAL_BE.get(), pos, state);
    }

    @Override
    public boolean shouldRenderFace(Direction direction) {
        return direction.getAxis().isHorizontal();
    }
}
