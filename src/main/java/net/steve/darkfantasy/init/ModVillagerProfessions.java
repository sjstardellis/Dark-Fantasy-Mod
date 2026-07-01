package net.steve.darkfantasy.init;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.steve.darkfantasy.DarkFantasy;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.trading.TradeSet;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Predicate;

/**
 * The <b>Brewer</b> villager profession — the trade hub for the mod's beer-brewing line.
 *
 * <p>It claims a {@link net.steve.darkfantasy.block.ModBlocks#BREWING_KEG} as its job site
 * (see {@link ModPoiTypes#BREWING_KEG}, which is also added to the {@code minecraft:acquirable_job_site}
 * POI tag so jobless villagers will take up brewing). Its trades give players a renewable source of
 * hops and beer.
 *
 * <p>Trades are data-driven in 26.1: each villager level maps to a {@code trade_set} resource
 * (<code>data/darkfantasy/trade_set/brewer/level_N.json</code>), which in turn lists the individual
 * <code>villager_trade</code> entries. See {@code tools/gen_brewer_trades.py}.
 */
public class ModVillagerProfessions {
    public static final DeferredRegister<VillagerProfession> PROFESSIONS =
            DeferredRegister.create(Registries.VILLAGER_PROFESSION, DarkFantasy.MOD_ID);

    /** Job-site POI key the profession claims — must match {@link ModPoiTypes#BREWING_KEG}. */
    private static final ResourceKey<PoiType> BREWING_KEG_POI =
            ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE,
                    Identifier.fromNamespaceAndPath(DarkFantasy.MOD_ID, "brewing_keg"));

    private static final Predicate<Holder<PoiType>> AT_KEG = holder -> holder.is(BREWING_KEG_POI);

    private static ResourceKey<TradeSet> tradeSet(int level) {
        return ResourceKey.create(Registries.TRADE_SET,
                Identifier.fromNamespaceAndPath(DarkFantasy.MOD_ID, "brewer/level_" + level));
    }

    public static final DeferredHolder<VillagerProfession, VillagerProfession> BREWER = PROFESSIONS.register(
            "brewer",
            () -> new VillagerProfession(
                    Component.translatable("entity.darkfantasy.villager.brewer"),
                    AT_KEG,                       // heldJobSite
                    AT_KEG,                       // acquirableJobSite
                    ImmutableSet.of(),            // requestedItems (picked up off the ground) — none
                    ImmutableSet.of(),            // secondaryPoi (e.g. farmer's farmland) — none
                    SoundEvents.VILLAGER_WORK_CLERIC,   // closest themed work sound (potion-brewing vibe)
                    Int2ObjectMap.ofEntries(
                            Int2ObjectMap.entry(1, tradeSet(1)),
                            Int2ObjectMap.entry(2, tradeSet(2)),
                            Int2ObjectMap.entry(3, tradeSet(3)),
                            Int2ObjectMap.entry(4, tradeSet(4)),
                            Int2ObjectMap.entry(5, tradeSet(5)))));

    public static void register(IEventBus eventBus) {
        PROFESSIONS.register(eventBus);
    }
}
