package net.steve.darkfantasy.item.custom;

import net.steve.darkfantasy.DarkFantasy;
import net.steve.darkfantasy.tags.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

/**
 * Nightfall — the Eclipse King's greatsword and the mod's celestial capstone weapon. Heavy
 * and slow (big single hits, low DPS — deliberately not a stat upgrade over netherite), its
 * value is its <em>eclipse</em>: every strike steals the light.
 *
 * <ul>
 *   <li><b>On hit</b> (player OR the king): the victim is plunged into Darkness and afflicted
 *       with Wither, and the wielder drains a sliver of life back.</li>
 *   <li><b>Right-click — Umbral Sweep</b>: a forward crescent of shadow that damages, withers,
 *       and blinds everything in a cone ahead. On a cooldown; costs durability.</li>
 * </ul>
 */
public class EclipseGreatswordItem extends Item {
    // On-hit "eclipse"
    private static final int DARK_TICKS = 80;          // 4 s of Darkness
    private static final int WITHER_TICKS = 60;        // 3 s of Wither I
    private static final float LIFESTEAL = 1.5F;
    // Umbral Sweep special
    private static final int SWEEP_COOLDOWN = 160;     // 8 s
    private static final double SWEEP_RANGE = 5.0;
    private static final double CONE_DOT = 0.4;        // ~66° half-cone in front
    private static final float SWEEP_DAMAGE = 6.0F;

    private static final Identifier DARK_ID =
            Identifier.fromNamespaceAndPath(DarkFantasy.MOD_ID, "eclipse_greatsword");

    public EclipseGreatswordItem(Properties properties) {
        super(properties);
    }

    /** Eclipsium-grade greatsword stats: heavy hit, slow swing, on-hit durability cost. */
    public static Properties applyProperties(Properties p) {
        return p
                .durability(1400)
                .repairable(ModTags.Items.ECLIPSIUM_REPAIR)
                .enchantable(15)
                .fireResistant()
                .rarity(Rarity.EPIC)
                .component(DataComponents.WEAPON, new Weapon(2))
                .attributes(ItemAttributeModifiers.builder()
                        .add(Attributes.ATTACK_DAMAGE,
                                new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 8.0,
                                        AttributeModifier.Operation.ADD_VALUE),
                                EquipmentSlotGroup.MAINHAND)
                        .add(Attributes.ATTACK_SPEED,
                                new AttributeModifier(BASE_ATTACK_SPEED_ID, -3.0,
                                        AttributeModifier.Operation.ADD_VALUE),
                                EquipmentSlotGroup.MAINHAND)
                        .build());
    }

    /** Eclipse on-hit — fires for ANY wielder, so the Eclipse King's strikes do it too. */
    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.postHurtEnemy(stack, target, attacker);
        if (!(attacker.level() instanceof ServerLevel server)) return;

        target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, DARK_TICKS, 0));
        target.addEffect(new MobEffectInstance(MobEffects.WITHER, WITHER_TICKS, 0));
        if (attacker.getHealth() < attacker.getMaxHealth()) {
            attacker.heal(LIFESTEAL);
        }
        server.sendParticles(ParticleTypes.SQUID_INK,
                target.getX(), target.getY() + target.getBbHeight() * 0.6, target.getZ(),
                10, 0.25, 0.3, 0.25, 0.0);
        server.playSound(null, target.blockPosition(), SoundEvents.SCULK_SHRIEKER_SHRIEK,
                SoundSource.PLAYERS, 0.5F, 1.4F);
    }

    /** Right-click — Umbral Sweep: a shadow crescent through everything in the cone ahead. */
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.PASS;
        }

        level.playSound(player, player.blockPosition(),
                SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.7F, 1.5F);

        if (level instanceof ServerLevel server) {
            Vec3 look = player.getLookAngle();
            double lh = Math.sqrt(look.x * look.x + look.z * look.z);
            double lx = lh < 1.0E-4 ? 0 : look.x / lh;
            double lz = lh < 1.0E-4 ? 1 : look.z / lh;

            for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class,
                    player.getBoundingBox().inflate(SWEEP_RANGE))) {
                if (victim == player || victim instanceof Player || !victim.isAlive()) continue;
                Vec3 to = victim.position().subtract(player.position());
                double th = Math.sqrt(to.x * to.x + to.z * to.z);
                if (th > 1.0E-4 && (to.x / th) * lx + (to.z / th) * lz < CONE_DOT) continue;

                victim.hurt(server.damageSources().playerAttack(player), SWEEP_DAMAGE);
                victim.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0));
                victim.addEffect(new MobEffectInstance(MobEffects.WITHER, WITHER_TICKS, 0));
            }
            // sweeping crescent of shadow in front of the caster
            Vec3 eye = player.getEyePosition();
            for (int i = 0; i <= 16; i++) {
                double a = (i - 8) * 0.16;                 // arc spread
                double dx = lx * Math.cos(a) - lz * Math.sin(a);
                double dz = lz * Math.cos(a) + lx * Math.sin(a);
                for (double r = 1.5; r <= SWEEP_RANGE; r += 1.0) {
                    server.sendParticles(ParticleTypes.SCULK_SOUL,
                            eye.x + dx * r, player.getY() + 1.0, eye.z + dz * r, 1, 0.0, 0.0, 0.0, 0.0);
                }
            }
        }

        player.getCooldowns().addCooldown(stack, SWEEP_COOLDOWN);
        stack.hurtAndBreak(2, player, hand);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.darkfantasy.eclipse_greatsword").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, display, builder, flag);
    }
}
