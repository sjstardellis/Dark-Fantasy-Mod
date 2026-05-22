package net.steve.darkfantasy.event;

import net.steve.darkfantasy.DarkFantasy;
import net.steve.darkfantasy.block.ModBlocks;
import net.steve.darkfantasy.block.custom.TwilightPortalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import org.jspecify.annotations.Nullable;

/**
 * Validates a flat, nether-portal-style 5×5 bookshelf frame and lights it with flint+steel.
 * Frame orientations: vertical, on either the X or Z horizontal axis.
 *
 * <p>Layout (frame coordinate {@code (w, h)}, with {@code w} along the chosen axis and
 * {@code h} vertical):
 * <pre>
 *   B B B B B   ← h=4
 *   B P P P B
 *   B P P P B
 *   B P P P B
 *   B B B B B   ← h=0
 *   w=0 ..... w=4
 * </pre>
 * 16 bookshelves form the perimeter, 9 portal blocks fill the inner 3×3.
 */
@EventBusSubscriber(modid = DarkFantasy.MOD_ID)
public class TwilightPortalIgnitionHandler {

    public static final int FRAME_SIZE = 5;

    // ---- Frame geometry helpers --------------------------------------------

    private static boolean isPerimeter(int w, int h) {
        return w == 0 || w == FRAME_SIZE - 1 || h == 0 || h == FRAME_SIZE - 1;
    }

    private static boolean isInterior(int w, int h) {
        return w >= 1 && w <= FRAME_SIZE - 2 && h >= 1 && h <= FRAME_SIZE - 2;
    }

    /** World position of frame coordinate {@code (w, h)} given the bottom-left origin and axis. */
    private static BlockPos frameToWorld(BlockPos origin, Direction.Axis axis, int w, int h) {
        return axis == Direction.Axis.X
                ? origin.offset(w, h, 0)
                : origin.offset(0, h, w);
    }

    // ---- Validators --------------------------------------------------------

    /** Both perimeter (bookshelves) and interior (air) constraints must be satisfied. */
    private static boolean isValidEmptyFrame(LevelReader level, BlockPos origin, Direction.Axis axis) {
        for (int w = 0; w < FRAME_SIZE; w++) {
            for (int h = 0; h < FRAME_SIZE; h++) {
                BlockState state = level.getBlockState(frameToWorld(origin, axis, w, h));
                if (isPerimeter(w, h)) {
                    if (!state.is(Blocks.BOOKSHELF)) return false;
                } else if (isInterior(w, h)) {
                    if (!state.isAir()) return false;
                }
            }
        }
        return true;
    }

    /** Perimeter-only check, used by an active portal block to confirm its frame is still intact. */
    private static boolean isPerimeterIntact(LevelReader level, BlockPos origin, Direction.Axis axis) {
        for (int w = 0; w < FRAME_SIZE; w++) {
            for (int h = 0; h < FRAME_SIZE; h++) {
                if (isPerimeter(w, h)
                        && !level.getBlockState(frameToWorld(origin, axis, w, h)).is(Blocks.BOOKSHELF)) {
                    return false;
                }
            }
        }
        return true;
    }

    // ---- Frame search ------------------------------------------------------

    /** Given a bookshelf at {@code clickedPos}, find a valid empty frame it could be part of. */
    private static @Nullable BlockPos findEmptyFrameOrigin(LevelReader level, BlockPos clickedPos, Direction.Axis axis) {
        for (int w = 0; w < FRAME_SIZE; w++) {
            for (int h = 0; h < FRAME_SIZE; h++) {
                if (!isPerimeter(w, h)) continue;
                BlockPos origin = axis == Direction.Axis.X
                        ? clickedPos.offset(-w, -h, 0)
                        : clickedPos.offset(0, -h, -w);
                if (isValidEmptyFrame(level, origin, axis)) {
                    return origin;
                }
            }
        }
        return null;
    }

    // ---- Ignition ----------------------------------------------------------

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide()) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        ItemStack stack = event.getItemStack();
        if (!stack.is(Items.FLINT_AND_STEEL)) return;

        BlockPos clickedPos = event.getPos();
        if (!level.getBlockState(clickedPos).is(Blocks.BOOKSHELF)) return;

        for (Direction.Axis axis : new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Z}) {
            BlockPos origin = findEmptyFrameOrigin(serverLevel, clickedPos, axis);
            if (origin != null) {
                ignitePortal(serverLevel, origin, axis);
                Player player = event.getEntity();
                InteractionHand hand = event.getHand();
                EquipmentSlot slot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                stack.hurtAndBreak(1, player, slot);
                level.playSound(null, clickedPos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                event.setCanceled(true);
                return;
            }
        }
    }

    /** Fills the 3×3 interior with portal blocks oriented along {@code axis}. */
    public static void ignitePortal(ServerLevel level, BlockPos origin, Direction.Axis axis) {
        BlockState portalState = ModBlocks.TWILIGHT_PORTAL.get().defaultBlockState()
                .setValue(TwilightPortalBlock.AXIS, axis);
        for (int w = 0; w < FRAME_SIZE; w++) {
            for (int h = 0; h < FRAME_SIZE; h++) {
                if (isInterior(w, h)) {
                    level.setBlock(frameToWorld(origin, axis, w, h), portalState, 3);
                }
            }
        }
    }

    // ---- Frame-still-valid (called from the portal block's updateShape) ----

    /**
     * Given an active portal block at {@code pos} with {@code axis}, look for a valid
     * surrounding frame perimeter — the portal block could be at any of 9 interior positions.
     */
    public static boolean isFrameStillValid(LevelReader level, BlockPos pos, Direction.Axis axis) {
        for (int w = 1; w <= FRAME_SIZE - 2; w++) {
            for (int h = 1; h <= FRAME_SIZE - 2; h++) {
                BlockPos origin = axis == Direction.Axis.X
                        ? pos.offset(-w, -h, 0)
                        : pos.offset(0, -h, -w);
                if (isPerimeterIntact(level, origin, axis)) {
                    return true;
                }
            }
        }
        return false;
    }

    // ---- Extinguish on frame destruction -----------------------------------

    /**
     * Corner bookshelf breaks don't propagate via {@code updateShape} (no portal block is
     * adjacent), so we listen for bookshelf breaks explicitly. For each candidate frame
     * the broken position could belong to, if there's an active portal in the interior,
     * extinguish it.
     */
    @SubscribeEvent
    public static void onBlockBreak(BreakBlockEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!event.getState().is(Blocks.BOOKSHELF)) return;

        BlockPos brokenPos = event.getPos();
        for (Direction.Axis axis : new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Z}) {
            for (int w = 0; w < FRAME_SIZE; w++) {
                for (int h = 0; h < FRAME_SIZE; h++) {
                    if (!isPerimeter(w, h)) continue;
                    BlockPos origin = axis == Direction.Axis.X
                            ? brokenPos.offset(-w, -h, 0)
                            : brokenPos.offset(0, -h, -w);
                    BlockPos interiorCenter = frameToWorld(origin, axis, 2, 2);
                    if (level.getBlockState(interiorCenter).is(ModBlocks.TWILIGHT_PORTAL.get())) {
                        extinguishPortal(level, origin, axis);
                    }
                }
            }
        }
    }

    public static void extinguishPortal(ServerLevel level, BlockPos origin, Direction.Axis axis) {
        boolean anyExtinguished = false;
        for (int w = 0; w < FRAME_SIZE; w++) {
            for (int h = 0; h < FRAME_SIZE; h++) {
                if (isInterior(w, h)) {
                    BlockPos pos = frameToWorld(origin, axis, w, h);
                    if (level.getBlockState(pos).is(ModBlocks.TWILIGHT_PORTAL.get())) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                        anyExtinguished = true;
                    }
                }
            }
        }
        if (anyExtinguished) {
            level.playSound(null, origin,
                    SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS,
                    0.8F, 1.2F);
        }
    }
}
