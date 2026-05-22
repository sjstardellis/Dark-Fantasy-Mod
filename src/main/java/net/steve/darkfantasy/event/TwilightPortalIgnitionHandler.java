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
     * Scan-based extinguish — the old approach checked only the candidate interior center
     * and used setBlock with flag 3, which triggered {@code updateShape} on adjacent portal
     * blocks. Because {@code BreakBlockEvent} fires *before* the bookshelf is removed, those
     * cascade checks saw the perimeter as still intact and resurrected what we just nuked,
     * leaving a stray portal block.
     *
     * <p>New strategy: scan a 9×9×9 region around the broken bookshelf for any twilight
     * portal block, verify the bookshelf actually lies on that portal's frame perimeter,
     * then remove with flag 2 (no neighbor cascade) so nothing can fight us.
     */
    @SubscribeEvent
    public static void onBlockBreak(BreakBlockEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!event.getState().is(Blocks.BOOKSHELF)) return;

        BlockPos brokenPos = event.getPos();
        int reach = FRAME_SIZE - 1; // 4 — covers any portal block reachable from this bookshelf
        boolean anyExtinguished = false;
        for (int dx = -reach; dx <= reach; dx++) {
            for (int dy = -reach; dy <= reach; dy++) {
                for (int dz = -reach; dz <= reach; dz++) {
                    BlockPos candidate = brokenPos.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(candidate);
                    if (!state.is(ModBlocks.TWILIGHT_PORTAL.get())) continue;
                    Direction.Axis axis = state.getValue(TwilightPortalBlock.AXIS);
                    if (!bookshelfBelongsToPortalFrame(candidate, axis, brokenPos)) continue;
                    // Flag 2 = UPDATE_CLIENTS only, no neighbor cascade — prevents the
                    // surviving portal blocks from re-validating with the stale (intact)
                    // perimeter.
                    level.setBlock(candidate, Blocks.AIR.defaultBlockState(), 2);
                    anyExtinguished = true;
                }
            }
        }
        if (anyExtinguished) {
            level.playSound(null, brokenPos,
                    SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS,
                    0.8F, 1.2F);
        }
    }

    /**
     * True iff {@code bookshelfPos} could be a perimeter block of the 5×5 frame containing
     * the portal block at {@code portalPos} with the given {@code axis}. Walks the 9 possible
     * interior positions for the portal, derives each candidate origin, and checks whether
     * the bookshelf falls on that origin's perimeter coordinates.
     */
    private static boolean bookshelfBelongsToPortalFrame(BlockPos portalPos, Direction.Axis axis, BlockPos bookshelfPos) {
        for (int iw = 1; iw <= FRAME_SIZE - 2; iw++) {
            for (int ih = 1; ih <= FRAME_SIZE - 2; ih++) {
                BlockPos origin = axis == Direction.Axis.X
                        ? portalPos.offset(-iw, -ih, 0)
                        : portalPos.offset(0, -ih, -iw);
                int bw, bh, bPerp;
                if (axis == Direction.Axis.X) {
                    bw = bookshelfPos.getX() - origin.getX();
                    bh = bookshelfPos.getY() - origin.getY();
                    bPerp = bookshelfPos.getZ() - origin.getZ();
                } else {
                    bw = bookshelfPos.getZ() - origin.getZ();
                    bh = bookshelfPos.getY() - origin.getY();
                    bPerp = bookshelfPos.getX() - origin.getX();
                }
                if (bPerp != 0) continue; // not in the frame plane
                if (bw < 0 || bw >= FRAME_SIZE || bh < 0 || bh >= FRAME_SIZE) continue;
                if (isPerimeter(bw, bh)) return true;
            }
        }
        return false;
    }

    /** Programmatic extinguish — clears the 9 interior blocks regardless of current state. */
    public static void extinguishPortal(ServerLevel level, BlockPos origin, Direction.Axis axis) {
        for (int w = 1; w <= FRAME_SIZE - 2; w++) {
            for (int h = 1; h <= FRAME_SIZE - 2; h++) {
                BlockPos pos = frameToWorld(origin, axis, w, h);
                if (level.getBlockState(pos).is(ModBlocks.TWILIGHT_PORTAL.get())) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
        level.playSound(null, origin,
                SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS,
                0.8F, 1.2F);
    }
}
