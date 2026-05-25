package net.steve.darkfantasy.event;

import java.util.List;
import net.steve.darkfantasy.DarkFantasy;
import net.steve.darkfantasy.entity.custom.GoblinEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Aggros nearby {@link GoblinEntity}s when a player attempts to open a chest within
 * line-of-sight range. Captures the moment of intent (the right-click) rather than the
 * actual container open, so the goblin reaction precedes the inventory screen.
 *
 * <p>Detection rules — deliberately narrow to avoid spurious aggro:
 * <ul>
 *   <li><b>Server-side only.</b> The client mirrors what the server tells goblins to do;
 *       running this on both sides would double-fire.</li>
 *   <li><b>Sneak-clicks skipped.</b> Holding shift + right-click is "place item on
 *       chest" semantics in vanilla, not opening — players shouldn't be punished for
 *       building near a chest.</li>
 *   <li><b>Only vanilla {@code chest} and {@code trapped_chest}.</b> Ender chests are
 *       personal storage; barrels and shulker boxes felt out of scope for "raiding
 *       a stash". Easy to extend if you decide otherwise.</li>
 *   <li><b>Aggro range: 32×16×32 around the chest.</b> Slightly larger horizontally
 *       than vertically because goblin packs roam at ground level and we want sight-
 *       line aggro, not ceiling/cellar surprises.</li>
 *   <li><b>Already-aggro'd goblins are skipped</b> via {@link GoblinEntity#aggroOn}'s
 *       guard — they keep their existing target rather than constantly retargeting.</li>
 * </ul>
 *
 * <p>This is the only aggro trigger besides {@code HurtByTargetGoal}; gold armor on the
 * player is intentionally <em>not</em> a trigger (unlike vanilla piglins), so a
 * well-equipped player can still trade peacefully.
 */
@EventBusSubscriber(modid = DarkFantasy.MOD_ID)
public class GoblinChestAggroHandler {
    /** Half-width of the aggro region around an opened chest, in blocks. */
    private static final double AGGRO_RANGE_HORIZONTAL = 16.0;
    /** Half-height of the aggro region around an opened chest, in blocks. */
    private static final double AGGRO_RANGE_VERTICAL = 8.0;

    @SubscribeEvent
    public static void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide()) return;

        Player player = event.getEntity();
        if (player.isShiftKeyDown()) return;

        BlockState clicked = level.getBlockState(event.getPos());
        if (!isAggroChest(clicked)) return;

        Vec3 chestCenter = Vec3.atCenterOf(event.getPos());
        AABB area = new AABB(
                chestCenter.x - AGGRO_RANGE_HORIZONTAL, chestCenter.y - AGGRO_RANGE_VERTICAL, chestCenter.z - AGGRO_RANGE_HORIZONTAL,
                chestCenter.x + AGGRO_RANGE_HORIZONTAL, chestCenter.y + AGGRO_RANGE_VERTICAL, chestCenter.z + AGGRO_RANGE_HORIZONTAL);

        List<GoblinEntity> nearby = level.getEntitiesOfClass(GoblinEntity.class, area);
        for (GoblinEntity goblin : nearby) {
            goblin.aggroOn(player);
        }
    }

    /** Which container blocks count as "raiding a stash" for goblin aggro purposes. */
    private static boolean isAggroChest(BlockState state) {
        return state.is(Blocks.CHEST) || state.is(Blocks.TRAPPED_CHEST);
    }
}
