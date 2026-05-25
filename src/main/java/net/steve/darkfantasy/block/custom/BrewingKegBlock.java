package net.steve.darkfantasy.block.custom;

import com.mojang.serialization.MapCodec;
import net.steve.darkfantasy.block.entity.BrewingKegBlockEntity;
import net.steve.darkfantasy.init.ModBlockEntities;
import net.steve.darkfantasy.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * Brewing keg. The visible {@link #STAGE} property is just a four-step visual
 * indicator driven by {@link BrewingKegBlockEntity#serverTick}:
 *
 * <ul>
 *   <li>{@link #STAGE_EMPTY} — no slots filled.</li>
 *   <li>{@link #STAGE_LOADING} — partial ingredients in slots.</li>
 *   <li>{@link #STAGE_READY} — all ingredients loaded + room in tank, waiting for heat.</li>
 *   <li>{@link #STAGE_BREWING} — ingredients + room + heat. Brew  ticks.</li>
 * </ul>
 *
 * <p>The previous "STAGE_DONE" terminal state was removed when the keg gained a
 * multi-batch tank — there's no single "done" moment any more, the tank just
 * accumulates batches. {@link #STAGE_DONE} remains as a constant for backwards
 * compatibility with any code that still references it, but the BE no longer emits it.
 *
 * <p>Player interactions go through one of three paths:
 * <ol>
 *   <li><b>Right-click with a {@link ModItems#STEIN_GLASS Stein Glass}</b> —
 *       drains one quart of beer from the tank and gives the player a filled
 *       {@link ModItems#BEER Beer} (handled in {@link #useItemOn}).</li>
 *   <li><b>Right-click empty-handed</b> — opens the GUI to load/check ingredients
 *       (handled in {@link #useWithoutItem}).</li>
 *   <li><b>Right-click with anything else</b> — same as empty-handed: opens GUI,
 *       so the player isn't blocked just because they have wheat in their hand.</li>
 * </ol>
 */
public class BrewingKegBlock extends BaseEntityBlock {
    public static final MapCodec<BrewingKegBlock> CODEC = simpleCodec(BrewingKegBlock::new);

    public static final int STAGE_EMPTY = 0;
    public static final int STAGE_LOADING = 1;
    public static final int STAGE_READY = 2;
    public static final int STAGE_BREWING = 3;
    /** @deprecated multi-batch tank removed this terminal state. Kept for legacy callers. */
    @Deprecated public static final int STAGE_DONE = 4;
    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, 4);

    /** Direction the keg's tap faces. Set on placement from the player's look direction. */
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    /**
     * Collision/selection hitbox — 14 wide × 15 tall × 14 deep, centred at x/z = 8,
     * raised 1px off the ground. Tighter than a full cube so the model's small gap
     * around the base is reachable, and the player can't get "stuck on air" above
     * the keg.
     */
    private static final VoxelShape SHAPE = Block.box(1.0, 1.0, 1.0, 15.0, 16.0, 15.0);

    public BrewingKegBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(STAGE, STAGE_EMPTY)
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<BrewingKegBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STAGE, FACING);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /**
     * Tap points at the player when placed — same convention as furnaces and most
     * facing blocks (the "front" of the block faces the placer).
     */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BrewingKegBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, ModBlockEntities.BREWING_KEG_BE.get(), BrewingKegBlockEntity::serverTick);
    }

    /**
     * Stein Glass conversion: takes one stein, drains a quart from the tank, hands
     * back a Beer. Any other item falls through to {@link #useWithoutItem} so the
     * player doesn't have to put their stack away to open the GUI.
     */
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof BrewingKegBlockEntity be)) return InteractionResult.PASS;

        if (stack.is(ModItems.STEIN_GLASS.get())) {
            if (!level.isClientSide()) {
                if (!be.drainOneStein()) {
                    // Tank doesn't have a full quart. Soft cue rather than a chat message.
                    level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 0.4F, 0.7F);
                    return InteractionResult.CONSUME;
                }
                if (!player.getAbilities().instabuild) stack.shrink(1);
                ItemStack beer = new ItemStack(ModItems.BEER.get());
                if (!player.getInventory().add(beer)) player.drop(beer, false);
                level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 0.9F, 1.0F);
            }
            return InteractionResult.SUCCESS;
        }

        // Fall through — any non-stein right-click opens the GUI, same as empty hand.
        // Must return TRY_WITH_EMPTY_HAND (the default useItemOn return), not PASS:
        // the framework only calls useWithoutItem when it sees the TRY_WITH_EMPTY_HAND
        // sentinel; PASS terminates the interaction chain and the menu never opens.
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof BrewingKegBlockEntity be) {
            player.openMenu(be, pos);
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * Visible ambient effects mirror the brewing stage:
     * <ul>
     *   <li>{@link #STAGE_BREWING} — frequent bubbles + occasional cosy-smoke puff so
     *       the keg reads as actively cooking.</li>
     *   <li>{@link #STAGE_READY} — single small smoke wisp hinting "needs heat" — the
     *       keg is loaded but the fire underneath is missing or unlit.</li>
     * </ul>
     */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        int stage = state.getValue(STAGE);
        if (stage == STAGE_BREWING) {
            if (random.nextInt(4) == 0) {
                double x = pos.getX() + 0.3 + random.nextDouble() * 0.4;
                double y = pos.getY() + 0.95;
                double z = pos.getZ() + 0.3 + random.nextDouble() * 0.4;
                level.addParticle(ParticleTypes.BUBBLE_POP, x, y, z, 0.0, 0.05, 0.0);
            }
            if (random.nextInt(20) == 0) {
                level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        pos.getX() + 0.5, pos.getY() + 1.05, pos.getZ() + 0.5,
                        0.0, 0.04, 0.0);
            }
        } else if (stage == STAGE_READY) {
            if (random.nextInt(40) == 0) {
                level.addParticle(ParticleTypes.SMOKE,
                        pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                        0.0, 0.02, 0.0);
            }
        }
    }

    /** Drop slot contents (not the tank's beer — that's a fluid, can't drop) on break. */
    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        if (level.getBlockEntity(pos) instanceof BrewingKegBlockEntity be) {
            Containers.dropContents(level, pos, be);
        }
        Containers.updateNeighboursAfterDestroy(state, level, pos);
    }
}
