package net.steve.darkfantasy;

import com.mojang.logging.LogUtils;
import net.steve.darkfantasy.block.ModBlocks;
import net.steve.darkfantasy.creativemodetab.ModCreativeModeTabs;
import net.steve.darkfantasy.entity.custom.ElectroDragonEntity;
import net.steve.darkfantasy.entity.custom.FairyEntity;
import net.steve.darkfantasy.entity.custom.GnomeEntity;
import net.steve.darkfantasy.entity.custom.GoblinEntity;
import net.steve.darkfantasy.entity.custom.LytebugEntity;
import net.steve.darkfantasy.entity.custom.WizardEntity;
import net.steve.darkfantasy.init.ModBlockEntities;
import net.steve.darkfantasy.init.ModDataComponents;
import net.steve.darkfantasy.init.ModEntities;
import net.steve.darkfantasy.init.ModFluidTypes;
import net.steve.darkfantasy.init.ModFluids;
import net.steve.darkfantasy.init.ModMenuTypes;
import net.steve.darkfantasy.init.ModPoiTypes;
import net.steve.darkfantasy.init.ModVillagerProfessions;
import net.steve.darkfantasy.init.ModRecipes;
import net.steve.darkfantasy.init.ModStructureTypes;
import net.steve.darkfantasy.item.ModItems;
import net.steve.darkfantasy.worldgen.biome.OverworldBiomeInjector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(DarkFantasy.MOD_ID)
public class DarkFantasy {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "darkfantasy";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();


    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public DarkFantasy(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        ModCreativeModeTabs.register(modEventBus);

        ModEntities.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModDataComponents.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModRecipes.register(modEventBus);
        ModPoiTypes.register(modEventBus);
        ModVillagerProfessions.register(modEventBus);
        ModStructureTypes.register(modEventBus);
        // FluidTypes must register BEFORE Fluids — the BaseFlowingFluid constructor
        // resolves its FluidType supplier lazily, but the type holder must exist by
        // the time the Fluid registry freezes. Same-bus same-phase ordering keeps
        // that invariant; the relative call order here documents intent.
        ModFluidTypes.register(modEventBus);
        ModFluids.register(modEventBus);

        // Mod-bus listeners for entity attributes + spawn placement.
        modEventBus.addListener(DarkFantasy::onEntityAttributeCreation);
        modEventBus.addListener(DarkFantasy::onRegisterSpawnPlacements);

        NeoForge.EVENT_BUS.register(OverworldBiomeInjector.class);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(DarkFantasy::registerFlammability);
    }

    /**
     * Mirrors vanilla {@code FireBlock.bootStrap()} for our overworld-style wood sets
     * (gravewood, ghostwillow). Vanilla flammability values:
     * <ul>
     *   <li>logs/woods/stripped variants — encouragement 5, flammability 5</li>
     *   <li>planks/slab/stairs/fence/fence_gate — 5 / 20</li>
     *   <li>leaves — 30 / 60</li>
     * </ul>
     * Cinderbark is intentionally excluded: it's a nether stem set (mirrors crimson/
     * warped, neither of which is flammable). Doors, trapdoors, buttons, and pressure
     * plates are excluded because vanilla wood doors/trapdoors/etc. are not flammable
     * either — keeping parity avoids surprising players.
     */
    private static void registerFlammability() {
        FireBlock fire = (FireBlock) Blocks.FIRE;
        // logs / woods / stripped variants
        fire.setFlammable(ModBlocks.GHOSTWILLOW_LOG.get(), 5, 5);
        fire.setFlammable(ModBlocks.STRIPPED_GHOSTWILLOW_LOG.get(), 5, 5);
        fire.setFlammable(ModBlocks.GHOSTWILLOW_WOOD.get(), 5, 5);
        fire.setFlammable(ModBlocks.STRIPPED_GHOSTWILLOW_WOOD.get(), 5, 5);
        fire.setFlammable(ModBlocks.GRAVEWOOD_LOG.get(), 5, 5);
        fire.setFlammable(ModBlocks.STRIPPED_GRAVEWOOD_LOG.get(), 5, 5);
        fire.setFlammable(ModBlocks.GRAVEWOOD_WOOD.get(), 5, 5);
        fire.setFlammable(ModBlocks.STRIPPED_GRAVEWOOD_WOOD.get(), 5, 5);
        // planks / slabs / stairs / fence / fence_gate
        fire.setFlammable(ModBlocks.GHOSTWILLOW_PLANKS.get(), 5, 20);
        fire.setFlammable(ModBlocks.GHOSTWILLOW_SLAB.get(), 5, 20);
        fire.setFlammable(ModBlocks.GHOSTWILLOW_STAIRS.get(), 5, 20);
        fire.setFlammable(ModBlocks.GHOSTWILLOW_FENCE.get(), 5, 20);
        fire.setFlammable(ModBlocks.GHOSTWILLOW_FENCE_GATE.get(), 5, 20);
        fire.setFlammable(ModBlocks.GRAVEWOOD_PLANKS.get(), 5, 20);
        fire.setFlammable(ModBlocks.GRAVEWOOD_SLAB.get(), 5, 20);
        fire.setFlammable(ModBlocks.GRAVEWOOD_STAIRS.get(), 5, 20);
        fire.setFlammable(ModBlocks.GRAVEWOOD_FENCE.get(), 5, 20);
        fire.setFlammable(ModBlocks.GRAVEWOOD_FENCE_GATE.get(), 5, 20);
        // leaves — much higher fire spread, matches vanilla leaves
        fire.setFlammable(ModBlocks.GHOSTWILLOW_LEAVES.get(), 30, 60);
        fire.setFlammable(ModBlocks.GRAVEWOOD_LEAVES.get(), 30, 60);
    }

    private static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(ModEntities.FAIRY.get(), FairyEntity.createAttributes().build());
        event.put(ModEntities.WIZARD.get(), WizardEntity.createAttributes().build());
        event.put(ModEntities.ELECTRO_DRAGON.get(), ElectroDragonEntity.createAttributes().build());
        event.put(ModEntities.GOBLIN.get(), GoblinEntity.createAttributes().build());
        event.put(ModEntities.GNOME.get(), GnomeEntity.createAttributes().build());
        event.put(ModEntities.LYTEBUG.get(), LytebugEntity.createAttributes().build());
        event.put(ModEntities.ECLIPSE_KING.get(), net.steve.darkfantasy.entity.custom.EclipseKingEntity.createAttributes().build());
        event.put(ModEntities.CINDER_HOUND.get(), net.steve.darkfantasy.entity.custom.CinderHoundEntity.createAttributes().build());
        event.put(ModEntities.UMBRAL_WRAITH.get(), net.steve.darkfantasy.entity.custom.UmbralWraithEntity.createAttributes().build());
        event.put(ModEntities.BOG_HAG.get(), net.steve.darkfantasy.entity.custom.BogHagEntity.createAttributes().build());
    }

    /**
     * Fairies and wizards both spawn under the standard monster light rules. The Twilight
     * Forest is always at midnight tick so natural light stays below threshold, but using
     * vanilla's predicate means torches still suppress spawns. Wizards need a solid block
     * to stand on; fairies fly so they have no placement restriction.
     */
    private static void onRegisterSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(ModEntities.FAIRY.get(),
                SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkAnyLightMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        // Cinder hounds roam like wolves: on solid ground (enforced by ON_GROUND),
        // no light gate — packs prowl the ashen woods by day.
        event.register(ModEntities.CINDER_HOUND.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> true,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        // Wraiths and hags follow standard darkness-gated monster placement.
        event.register(ModEntities.UMBRAL_WRAITH.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkAnyLightMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.BOG_HAG.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkAnyLightMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.WIZARD.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkAnyLightMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        // Goblins follow standard ground-mob spawning: solid block beneath, dark
        // enough light level. Twilight Forest is permanently night, so light-level
        // gating means torches still suppress goblin packs as expected.
        event.register(ModEntities.GOBLIN.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkAnyLightMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        // Gnomes spawn on ground with the same light gating as other monsters —
        // Twilight Forest is permanent night so torches are the only deterrent.
        event.register(ModEntities.GNOME.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkAnyLightMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        // Electrodragons are big flying bosses — no ground requirement and no light
        // gate (they don't extend Monster, so the standard light predicate doesn't fit).
        // Rarity: weight=1 + spawn_costs charge=10 in the biome JSON, plus a 1-in-3
        // random gate here so attempts succeed a third as often. The entity's
        // finalizeSpawn lifts natural spawns to surface+25..40 (sky encounters).
        event.register(ModEntities.ELECTRO_DRAGON.get(),
                SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) ->
                        level.getBlockState(pos).isAir() && random.nextInt(3) == 0,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        // Lytebugs are passive flyers — no light gating, no ground requirement. The
        // permissive predicate just confirms there's air to spawn into; biome weight
        // + the passive mob cap throttle frequency. They glow themselves so spawning
        // in pitch darkness is exactly the point.
        event.register(ModEntities.LYTEBUG.get(),
                SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> level.getBlockState(pos).isAir(),
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        // Vanilla Allay has no built-in spawn placement (it's normally only released
        // from mansion/outpost cages). We add it to the Twilight Forest's creature
        // spawners so we have to declare a placement ourselves — air check only,
        // since allays fly.
        event.register(EntityType.ALLAY,
                SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> level.getBlockState(pos).isAir(),
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }
}
