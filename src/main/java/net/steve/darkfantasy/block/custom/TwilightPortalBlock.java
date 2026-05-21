package net.steve.darkfantasy.block.custom;

import com.mojang.serialization.MapCodec;
import net.steve.darkfantasy.block.entity.TwilightPortalBlockEntity;
import net.steve.darkfantasy.event.TwilightPortalIgnitionHandler;
import net.steve.darkfantasy.worldgen.dimension.ModDimensions;
import net.steve.darkfantasy.worldgen.dimension.TwilightTeleporter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * Twilight Forest portal. Sits in the center column of a 3D bookshelf+amethyst+glowstone
 * frame, visible from all four horizontal sides. Mirrors {@link SkylandsPortalBlock}'s
 * structure — only the frame composition and destination dimension differ.
 */
public class TwilightPortalBlock extends BaseEntityBlock implements Portal {
    public static final MapCodec<TwilightPortalBlock> CODEC = simpleCodec(TwilightPortalBlock::new);
    private static final VoxelShape SHAPE = Shapes.block();

    public TwilightPortalBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<TwilightPortalBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TwilightPortalBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                InsideBlockEffectApplier applier, boolean isPrecise) {
        if (entity.canUsePortal(false)) {
            entity.setAsInsidePortal(this, pos);
        }
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,
                                     BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos,
                                     BlockState neighbourState, RandomSource random) {
        BlockPos anchorPos = TwilightPortalIgnitionHandler.findAmethystBelow(level, pos);
        if (anchorPos == null || !TwilightPortalIgnitionHandler.isFrameStructureIntact(level, anchorPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
    }

    @Override
    public @Nullable TeleportTransition getPortalDestination(ServerLevel currentLevel, Entity entity, BlockPos portalEntryPos) {
        ResourceKey<Level> targetDim = currentLevel.dimension() == ModDimensions.TWILIGHT_FOREST
                ? Level.OVERWORLD
                : ModDimensions.TWILIGHT_FOREST;
        ServerLevel destination = currentLevel.getServer().getLevel(targetDim);
        if (destination == null) return null;
        return TwilightTeleporter.createTransition(entity, destination, portalEntryPos);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(100) == 0) {
            level.playLocalSound(
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    net.minecraft.sounds.SoundEvents.PORTAL_AMBIENT,
                    net.minecraft.sounds.SoundSource.BLOCKS,
                    0.5F, random.nextFloat() * 0.4F + 0.8F, false);
        }
        // Twilight portal: enchant-style swirls drifting upward for the magical feel.
        for (int i = 0; i < 4; ++i) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble();
            double z = pos.getZ() + random.nextDouble();
            double dx = (random.nextDouble() - 0.5) * 0.5;
            double dy = random.nextDouble() * 0.5;
            double dz = (random.nextDouble() - 0.5) * 0.5;
            level.addParticle(net.minecraft.core.particles.ParticleTypes.ENCHANT, x, y, z, dx, dy, dz);
        }
    }
}
