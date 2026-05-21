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
 * Validates the Twilight Forest portal frame on flint+steel and extinguishes the portal
 * when any frame block is destroyed. Mirrors {@link PortalIgnitionHandler}'s shape (the
 * 25 nether-bricks-or-bookshelves + center anchor + capstone layout) so the portal scales
 * match across dimensions.
 *
 * <p>Frame layout (relative to amethyst block at origin (0,0,0)):
 * <pre>
 *   y=0 (base — 3x3 bookshelf perimeter with amethyst in the center)
 *   y=1, y=2 (only the 4 corner pillars; cardinal sides + center are AIR)
 *   y=3 (top ring — mirror of base but with glowstone in the center)
 *   y=4 (capstone — single bookshelf above the glowstone)
 * </pre>
 *
 * Totals: 25 bookshelves + 1 glowstone + 1 amethyst block = 27 placed frame blocks.
 */
@EventBusSubscriber(modid = DarkFantasy.MOD_ID)
public class TwilightPortalIgnitionHandler {

    /** All 25 required bookshelf positions, relative to the amethyst anchor at origin. */
    public static final int[][] REQUIRED_BOOKSHELVES = {
            {-1, 0, -1}, {0, 0, -1}, {1, 0, -1},
            {-1, 0,  0},               {1, 0,  0},
            {-1, 0,  1}, {0, 0,  1}, {1, 0,  1},

            {-1, 1, -1}, {1, 1, -1},
            {-1, 1,  1}, {1, 1,  1},

            {-1, 2, -1}, {1, 2, -1},
            {-1, 2,  1}, {1, 2,  1},

            {-1, 3, -1}, {0, 3, -1}, {1, 3, -1},
            {-1, 3,  0},               {1, 3,  0},
            {-1, 3,  1}, {0, 3,  1}, {1, 3,  1},

            {0, 4, 0}
    };

    /** Glowstone capstone position relative to the amethyst anchor. */
    public static final int[] GLOWSTONE_POS = {0, 3, 0};

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
        if (!state.is(Blocks.AMETHYST_BLOCK)) return;

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

    /** Returns true iff the 25 bookshelves + glowstone are in their required positions. */
    public static boolean isFrameStructureIntact(LevelReader level, BlockPos amethystPos) {
        for (int[] offset : REQUIRED_BOOKSHELVES) {
            if (!level.getBlockState(amethystPos.offset(offset[0], offset[1], offset[2])).is(Blocks.BOOKSHELF)) {
                return false;
            }
        }
        BlockPos glowstonePos = amethystPos.offset(GLOWSTONE_POS[0], GLOWSTONE_POS[1], GLOWSTONE_POS[2]);
        return level.getBlockState(glowstonePos).is(Blocks.GLOWSTONE);
    }

    private static boolean arePortalSlotsClear(LevelReader level, BlockPos amethystPos) {
        for (int[] offset : PORTAL_POSITIONS) {
            if (!level.getBlockState(amethystPos.offset(offset[0], offset[1], offset[2])).isAir()) {
                return false;
            }
        }
        return true;
    }

    private static void ignitePortal(ServerLevel level, BlockPos amethystPos) {
        BlockState portalState = ModBlocks.TWILIGHT_PORTAL.get().defaultBlockState();
        for (int[] offset : PORTAL_POSITIONS) {
            BlockPos pos = amethystPos.offset(offset[0], offset[1], offset[2]);
            level.setBlock(pos, portalState, 3);
        }
    }

    // ---- Extinguish on frame destruction ------------------------------------

    @SubscribeEvent
    public static void onBlockBreak(BreakBlockEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        BlockState broken = event.getState();
        BlockPos brokenPos = event.getPos();

        if (broken.is(Blocks.AMETHYST_BLOCK)) {
            extinguishPortalIfActive(level, brokenPos);
        } else if (broken.is(Blocks.GLOWSTONE)) {
            BlockPos candidate = brokenPos.offset(-GLOWSTONE_POS[0], -GLOWSTONE_POS[1], -GLOWSTONE_POS[2]);
            if (level.getBlockState(candidate).is(Blocks.AMETHYST_BLOCK)) {
                extinguishPortalIfActive(level, candidate);
            }
        } else if (broken.is(Blocks.BOOKSHELF)) {
            for (int[] offset : REQUIRED_BOOKSHELVES) {
                BlockPos candidate = brokenPos.offset(-offset[0], -offset[1], -offset[2]);
                if (level.getBlockState(candidate).is(Blocks.AMETHYST_BLOCK)) {
                    extinguishPortalIfActive(level, candidate);
                }
            }
        }
    }

    public static void extinguishPortalIfActive(ServerLevel level, BlockPos amethystPos) {
        boolean anyExtinguished = false;
        for (int[] offset : PORTAL_POSITIONS) {
            BlockPos pos = amethystPos.offset(offset[0], offset[1], offset[2]);
            if (level.getBlockState(pos).is(ModBlocks.TWILIGHT_PORTAL.get())) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                anyExtinguished = true;
            }
        }
        if (anyExtinguished) {
            level.playSound(null, amethystPos,
                    SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS,
                    0.8F, 1.2F);
        }
    }

    /** Searches 1 or 2 blocks below a portal block for the amethyst anchor. */
    public static @Nullable BlockPos findAmethystBelow(LevelReader level, BlockPos portalPos) {
        BlockPos below = portalPos.below();
        if (level.getBlockState(below).is(Blocks.AMETHYST_BLOCK)) return below;
        BlockPos below2 = portalPos.below(2);
        if (level.getBlockState(below2).is(Blocks.AMETHYST_BLOCK)) return below2;
        return null;
    }
}
