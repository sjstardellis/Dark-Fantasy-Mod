package net.steve.darkfantasy.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Consumer;

/**
 * Spell book — the evoker's claw line. Erupts a row of {@value #FANG_COUNT}
 * {@link EvokerFangs} marching forward along the caster's look, each on a slightly longer
 * warm-up so the jaws ripple outward and snap in sequence. The fangs follow the ground
 * (same column search the Evoker uses), deal their own damage, and the caster is set as
 * owner so the line never bites them.
 */
public class EvokerClawTomeItem extends Item {
    private static final int COOLDOWN_TICKS = 80;
    /** Number of fangs in the line. */
    private static final int FANG_COUNT = 16;
    /** Spacing between fangs along the look direction. */
    private static final double STEP = 1.25;

    public EvokerClawTomeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        level.playSound(player, player.blockPosition(),
                SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1.0F, 1.0F);

        if (level instanceof ServerLevel server) {
            // Horizontal component of the look vector, normalized; default north if looking straight up/down.
            Vec3 look = player.getLookAngle();
            double dx = look.x;
            double dz = look.z;
            double len = Math.sqrt(dx * dx + dz * dz);
            if (len < 1.0E-4) {
                dx = 0.0; dz = 1.0; len = 1.0;
            }
            dx /= len; dz /= len;

            float angle = (float) Mth.atan2(dz, dx);
            double minY = player.getY() - 3.0;
            double maxY = player.getY() + 2.0;

            for (int i = 0; i < FANG_COUNT; i++) {
                double reach = STEP * (i + 1);
                spawnFang(server, player, player.getX() + dx * reach, player.getZ() + dz * reach,
                        minY, maxY, angle, i);
            }
        }

        player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
        stack.hurtAndBreak(1, player, hand);
        return InteractionResult.SUCCESS;
    }

    /**
     * Drops a fang at column (x, z), seated on the first sturdy surface found scanning down
     * from {@code maxY} to {@code minY}. Mirrors {@code Evoker.createSpellEntity} so the line
     * tracks stairs and small ledges instead of floating.
     */
    private static void spawnFang(ServerLevel level, Player owner, double x, double z,
                                  double minY, double maxY, float angle, int warmup) {
        BlockPos pos = BlockPos.containing(x, maxY, z);
        boolean success = false;
        double topOffset = 0.0;

        do {
            BlockPos below = pos.below();
            BlockState belowState = level.getBlockState(below);
            if (belowState.isFaceSturdy(level, below, Direction.UP)) {
                if (!level.isEmptyBlock(pos)) {
                    VoxelShape shape = level.getBlockState(pos).getCollisionShape(level, pos);
                    if (!shape.isEmpty()) {
                        topOffset = shape.max(Direction.Axis.Y);
                    }
                }
                success = true;
                break;
            }
            pos = pos.below();
        } while (pos.getY() >= Mth.floor(minY) - 1);

        if (success) {
            level.addFreshEntity(new EvokerFangs(level, x, pos.getY() + topOffset, z, angle, warmup, owner));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.darkfantasy.evoker_claw_tome").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, display, builder, flag);
    }
}
