package net.steve.darkfantasy.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Consumer;

/**
 * Spell book — a dowsing rod, not a weapon. Scans a {@value #RADIUS}-block cube around the
 * caster for ore blocks, pings each with a glint, and reports the tally plus the nearest
 * vein in chat. The pings are depth-tested like any particle (buried ore shows only once
 * exposed), so the chat readout is the real payload — it tells you a vein is worth digging
 * toward before you've uncovered it.
 */
public class ProspectorTomeItem extends Item {
    private static final int COOLDOWN_TICKS = 200;
    private static final int RADIUS = 8;

    public ProspectorTomeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        level.playSound(player, player.blockPosition(),
                SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.8F, 1.4F);

        if (level instanceof ServerLevel server) {
            BlockPos origin = player.blockPosition();
            int total = 0;
            double nearestD2 = Double.MAX_VALUE;
            BlockState nearestState = null;

            for (BlockPos pos : BlockPos.betweenClosed(
                    origin.offset(-RADIUS, -RADIUS, -RADIUS),
                    origin.offset(RADIUS, RADIUS, RADIUS))) {
                BlockState state = server.getBlockState(pos);
                if (!isOre(state)) continue;

                total++;
                server.sendParticles(ParticleTypes.GLOW,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1, 0.1, 0.1, 0.1, 0.0);

                double d2 = origin.distSqr(pos);
                if (d2 < nearestD2) {
                    nearestD2 = d2;
                    nearestState = state;
                }
            }

            if (total == 0) {
                player.sendSystemMessage(Component.translatable(
                        "message.darkfantasy.prospector_tome.none").withStyle(ChatFormatting.GRAY));
            } else {
                player.sendSystemMessage(Component.translatable(
                        "message.darkfantasy.prospector_tome.found", total).withStyle(ChatFormatting.GOLD));
                player.sendSystemMessage(Component.translatable(
                        "message.darkfantasy.prospector_tome.nearest",
                        nearestState.getBlock().getName(),
                        (int) Math.round(Math.sqrt(nearestD2))).withStyle(ChatFormatting.YELLOW));
            }
        }

        player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
        stack.hurtAndBreak(1, player, hand);
        return InteractionResult.SUCCESS;
    }

    /** Any block whose id ends in {@code _ore}, plus ancient debris (the nether's ore-in-all-but-name). */
    private static boolean isOre(BlockState state) {
        Identifier id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (id == null) return false;
        String path = id.getPath();
        return path.endsWith("_ore") || path.equals("ancient_debris");
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.darkfantasy.prospector_tome").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, display, builder, flag);
    }
}
