package net.steve.darkfantasy.event;

import net.steve.darkfantasy.DarkFantasy;
import net.steve.darkfantasy.block.ModBlocks;
import net.minecraft.core.BlockPos;
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
 * Validates the Skylands portal frame on flint+steel and extinguishes the portal
 * when any frame block is destroyed.
 *
 * <p>Frame layout (relative to soul soil at origin (0,0,0)):
 * <pre>
 *   y=0 (base — 3x3 nether brick perimeter with soul soil in the center)
 *   y=1, y=2 (only the 4 corner pillars; cardinal sides + center are AIR)
 *   y=3 (top ring — mirror of base but with the gold block in the center)
 *   y=4 (capstone — single nether brick above the gold)
 * </pre>
 *
 * Totals: 25 nether bricks + 1 gold + 1 soul soil = 27 placed frame blocks.
 */
@EventBusSubscriber(modid = DarkFantasy.MOD_ID)
public class PortalIgnitionHandler {

    /** All 25 required nether-brick positions, relative to the soul soil at origin. Public so the block class can reuse it. */
    public static final int[][] REQUIRED_NETHER_BRICKS = {
            // y=0 base perimeter (8 blocks; the center is soul soil)
            {-1, 0, -1}, {0, 0, -1}, {1, 0, -1},
            {-1, 0,  0},               {1, 0,  0},
            {-1, 0,  1}, {0, 0,  1}, {1, 0,  1},

            // y=1 corner pillars (4 blocks; cardinal sides + center are AIR)
            {-1, 1, -1}, {1, 1, -1},
            {-1, 1,  1}, {1, 1,  1},

            // y=2 corner pillars (4 blocks; cardinal sides + center are AIR)
            {-1, 2, -1}, {1, 2, -1},
            {-1, 2,  1}, {1, 2,  1},

            // y=3 top ring (8 blocks; the center is gold)
            {-1, 3, -1}, {0, 3, -1}, {1, 3, -1},
            {-1, 3,  0},               {1, 3,  0},
            {-1, 3,  1}, {0, 3,  1}, {1, 3,  1},

            // y=4 capstone
            {0, 4, 0}
    };

    /** Gold block position relative to the soul soil. */
    public static final int[] GOLD_BLOCK_POS = {0, 3, 0};

    /** Positions that must be air pre-ignition and become portal blocks post-ignition. */
    public static final int[][] PORTAL_POSITIONS = {
            {0, 1, 0},
            {0, 2, 0}
    };

    // ---- Ignition ------------------------------------------------------------

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide()) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        ItemStack stack = event.getItemStack();
        if (!stack.is(Items.FLINT_AND_STEEL)) return;

        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        if (!state.is(Blocks.SOUL_SOIL)) return;

        if (!isFrameStructureIntact(serverLevel, pos)) return;
        if (!arePortalSlotsClear(serverLevel, pos)) return;

        ignitePortal(serverLevel, pos);

        Player player = event.getEntity();
        InteractionHand hand = event.getHand();
        EquipmentSlot slot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
        stack.hurtAndBreak(1, player, slot);

        level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
        event.setCanceled(true);
    }

    /** Returns true iff the 25 nether bricks + gold block are in their required positions. Does NOT check soul soil or portal slots. */
    public static boolean isFrameStructureIntact(LevelReader level, BlockPos soulSoilPos) {
        for (int[] offset : REQUIRED_NETHER_BRICKS) {
            if (!level.getBlockState(soulSoilPos.offset(offset[0], offset[1], offset[2])).is(Blocks.NETHER_BRICKS)) {
                return false;
            }
        }
        BlockPos goldPos = soulSoilPos.offset(GOLD_BLOCK_POS[0], GOLD_BLOCK_POS[1], GOLD_BLOCK_POS[2]);
        return level.getBlockState(goldPos).is(Blocks.GOLD_BLOCK);
    }

    private static boolean arePortalSlotsClear(LevelReader level, BlockPos soulSoilPos) {
        for (int[] offset : PORTAL_POSITIONS) {
            if (!level.getBlockState(soulSoilPos.offset(offset[0], offset[1], offset[2])).isAir()) {
                return false;
            }
        }
        return true;
    }

    private static void ignitePortal(ServerLevel level, BlockPos soulSoilPos) {
        BlockState portalState = ModBlocks.SKYLANDS_PORTAL.get().defaultBlockState();
        for (int[] offset : PORTAL_POSITIONS) {
            BlockPos pos = soulSoilPos.offset(offset[0], offset[1], offset[2]);
            level.setBlock(pos, portalState, 3);
        }
    }

    // ---- Extinguish on frame destruction ------------------------------------

    /**
     * When a frame block (nether brick, gold, or soul soil) is broken, find any active
     * Skylands portal whose frame includes that block and extinguish it.
     */
    @SubscribeEvent
    public static void onBlockBreak(BreakBlockEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        BlockState broken = event.getState();
        BlockPos brokenPos = event.getPos();

        if (broken.is(Blocks.SOUL_SOIL)) {
            // The broken block is the soul soil itself — extinguish at this position.
            extinguishPortalIfActive(level, brokenPos);
        } else if (broken.is(Blocks.GOLD_BLOCK)) {
            // Gold block sits at offset GOLD_BLOCK_POS from the soul soil.
            BlockPos candidate = brokenPos.offset(-GOLD_BLOCK_POS[0], -GOLD_BLOCK_POS[1], -GOLD_BLOCK_POS[2]);
            if (level.getBlockState(candidate).is(Blocks.SOUL_SOIL)) {
                extinguishPortalIfActive(level, candidate);
            }
        } else if (broken.is(Blocks.NETHER_BRICKS)) {
            // The broken block could be at any of 25 frame positions. Try each candidate.
            for (int[] offset : REQUIRED_NETHER_BRICKS) {
                BlockPos candidate = brokenPos.offset(-offset[0], -offset[1], -offset[2]);
                if (level.getBlockState(candidate).is(Blocks.SOUL_SOIL)) {
                    extinguishPortalIfActive(level, candidate);
                }
            }
        }
    }

    /** Sets the 2 portal slots above the given soul-soil position to air, if they're currently portal blocks. */
    public static void extinguishPortalIfActive(ServerLevel level, BlockPos soulSoilPos) {
        boolean anyExtinguished = false;
        for (int[] offset : PORTAL_POSITIONS) {
            BlockPos pos = soulSoilPos.offset(offset[0], offset[1], offset[2]);
            if (level.getBlockState(pos).is(ModBlocks.SKYLANDS_PORTAL.get())) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                anyExtinguished = true;
            }
        }
        if (anyExtinguished) {
            level.playSound(null, soulSoilPos,
                    SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS,
                    0.8F, 1.2F);
        }
    }

    /** Searches 1 or 2 blocks below a portal block for the soul soil anchor. */
    public static @Nullable BlockPos findSoulSoilBelow(LevelReader level, BlockPos portalPos) {
        BlockPos below = portalPos.below();
        if (level.getBlockState(below).is(Blocks.SOUL_SOIL)) return below;
        BlockPos below2 = portalPos.below(2);
        if (level.getBlockState(below2).is(Blocks.SOUL_SOIL)) return below2;
        return null;
    }
}
