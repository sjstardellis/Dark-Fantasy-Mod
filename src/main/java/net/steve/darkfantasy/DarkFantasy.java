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
import net.steve.darkfantasy.init.ModEntities;
import net.steve.darkfantasy.init.ModFluidTypes;
import net.steve.darkfantasy.init.ModFluids;
import net.steve.darkfantasy.init.ModMenuTypes;
import net.steve.darkfantasy.init.ModPoiTypes;
import net.steve.darkfantasy.init.ModRecipes;
import net.steve.darkfantasy.item.ModItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
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
        ModBlockEntities.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModRecipes.register(modEventBus);
        ModPoiTypes.register(modEventBus);
        // FluidTypes must register BEFORE Fluids — the BaseFlowingFluid constructor
        // resolves its FluidType supplier lazily, but the type holder must exist by
        // the time the Fluid registry freezes. Same-bus same-phase ordering keeps
        // that invariant; the relative call order here documents intent.
        ModFluidTypes.register(modEventBus);
        ModFluids.register(modEventBus);

        // Mod-bus listeners for entity attributes + spawn placement.
        modEventBus.addListener(DarkFantasy::onEntityAttributeCreation);
        modEventBus.addListener(DarkFantasy::onRegisterSpawnPlacements);

        NeoForge.EVENT_BUS.register(this);
        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {

    }

    private static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(ModEntities.FAIRY.get(), FairyEntity.createAttributes().build());
        event.put(ModEntities.WIZARD.get(), WizardEntity.createAttributes().build());
        event.put(ModEntities.ELECTRO_DRAGON.get(), ElectroDragonEntity.createAttributes().build());
        event.put(ModEntities.GOBLIN.get(), GoblinEntity.createAttributes().build());
        event.put(ModEntities.GNOME.get(), GnomeEntity.createAttributes().build());
        event.put(ModEntities.LYTEBUG.get(), LytebugEntity.createAttributes().build());
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
        // Rarity is already heavily controlled by weight=1 + spawn_costs charge=10 in
        // the biome JSON; per-position predicate just needs to confirm there's air to
        // spawn into.
        event.register(ModEntities.ELECTRO_DRAGON.get(),
                SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> level.getBlockState(pos).isAir(),
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

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }
}
