package net.steve.darkfantasy.event;

import net.steve.darkfantasy.DarkFantasy;
import net.steve.darkfantasy.worldgen.dimension.ModDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.animal.cow.MushroomCow;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

/**
 * Randomizes mooshroom variants in the Twilight Forest. Vanilla biome spawners always produce
 * red mooshrooms — brown ones only naturally exist via lightning conversion. Since we want
 * both variants to appear occasionally as part of the biome's mob mix, we flip a coin for
 * each newly-spawned mooshroom in the Twilight Forest dimension.
 *
 * <p>Skips lightning-conversion and breeding spawns so those keep their vanilla behavior
 * (lightning toggles RED↔BROWN; breeding picks a variant based on the parents).
 */
@EventBusSubscriber(modid = DarkFantasy.MOD_ID)
public final class TwilightMooshroomSpawnHandler {
    private TwilightMooshroomSpawnHandler() {}

    @SubscribeEvent
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        if (!(event.getEntity() instanceof MushroomCow cow)) return;
        if (cow.level().dimension() != ModDimensions.TWILIGHT_FOREST) return;

        EntitySpawnReason reason = event.getSpawnType();
        if (reason == EntitySpawnReason.CONVERSION || reason == EntitySpawnReason.BREEDING) return;

        MushroomCow.Variant pick = cow.getRandom().nextBoolean()
                ? MushroomCow.Variant.RED
                : MushroomCow.Variant.BROWN;
        cow.setVariant(pick);
    }
}
