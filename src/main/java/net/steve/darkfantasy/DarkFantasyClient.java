package net.steve.darkfantasy;

import net.steve.darkfantasy.client.renderer.AlchemyStandRenderer;
import net.steve.darkfantasy.client.renderer.ElectroDragonRenderer;
import net.steve.darkfantasy.client.renderer.FairyRenderer;
import net.steve.darkfantasy.client.model.GnomeModel;
import net.steve.darkfantasy.client.renderer.GnomeRenderer;
import net.steve.darkfantasy.client.renderer.GoblinRenderer;
import net.steve.darkfantasy.client.renderer.LytebugRenderer;
import net.steve.darkfantasy.client.renderer.SkylandsPortalRenderer;
import net.steve.darkfantasy.client.renderer.WizardRenderer;
import net.steve.darkfantasy.client.screen.AlchemyStandScreen;
import net.steve.darkfantasy.client.screen.BrewingKegScreen;
import net.steve.darkfantasy.init.ModBlockEntities;
import net.steve.darkfantasy.init.ModEntities;
import net.steve.darkfantasy.init.ModFluids;
import net.steve.darkfantasy.init.ModMenuTypes;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = DarkFantasy.MOD_ID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = DarkFantasy.MOD_ID, value = Dist.CLIENT)
public class DarkFantasyClient {
    public DarkFantasyClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
//        DarkFantasy.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    /**
     * Bake custom model-part hierarchies so renderers can look them up. The gnome
     * uses a custom Blockbench-authored model; its layer is registered here before
     * {@code GnomeRenderer}'s constructor bakes it.
     */
    @SubscribeEvent
    static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(GnomeModel.LAYER_LOCATION, GnomeModel::createBodyLayer);
    }

    /**
     * Binds still/flow/overlay sprite materials to the Elixir source + flowing fluids.
     * Replaces the old {@code IClientFluidTypeExtensions.getStillTexture()} API path
     * (removed in 26.1.x): textures are now resolved through the chunk-baked
     * {@link FluidModel} pipeline. Without this registration the fluid would render
     * as the missing-texture purple/black checkerboard.
     *
     * <p>Materials reference textures relative to the block atlas, so
     * {@code "darkfantasy:block/elixir_still"} resolves to
     * {@code assets/darkfantasy/textures/block/elixir_still.png}.
     */
    @SubscribeEvent
    static void onRegisterFluidModels(RegisterFluidModelsEvent event) {
        event.register(new FluidModel.Unbaked(
                        new Material(Identifier.fromNamespaceAndPath("darkfantasy", "block/elixir_still")),
                        new Material(Identifier.fromNamespaceAndPath("darkfantasy", "block/elixir_flow")),
                        new Material(Identifier.fromNamespaceAndPath("darkfantasy", "block/elixir_overlay")),
                        null),
                ModFluids.ELIXIR_SOURCE, ModFluids.ELIXIR_FLOWING);
    }

    @SubscribeEvent
    static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.ALCHEMY_STAND_MENU.get(), AlchemyStandScreen::new);
        event.register(ModMenuTypes.BREWING_KEG_MENU.get(), BrewingKegScreen::new);
    }

    @SubscribeEvent
    static void onRegisterBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.SKYLANDS_PORTAL_BE.get(),
                ctx -> new SkylandsPortalRenderer());
        event.registerBlockEntityRenderer(ModBlockEntities.ALCHEMY_STAND_BE.get(),
                AlchemyStandRenderer::new);

        event.registerEntityRenderer(ModEntities.FAIRY.get(), FairyRenderer::new);
        event.registerEntityRenderer(ModEntities.WIZARD.get(), WizardRenderer::new);
        // Lightning projectile is intentionally invisible — its trail is pure particles
        // and its impact is a real LightningBolt entity. NoopRenderer draws nothing.
        event.registerEntityRenderer(ModEntities.LIGHTNING_PROJECTILE.get(),
                net.minecraft.client.renderer.entity.NoopRenderer::new);
        event.registerEntityRenderer(ModEntities.ELECTRO_DRAGON.get(), ElectroDragonRenderer::new);
        event.registerEntityRenderer(ModEntities.GOBLIN.get(), GoblinRenderer::new);
        event.registerEntityRenderer(ModEntities.GNOME.get(), GnomeRenderer::new);
        event.registerEntityRenderer(ModEntities.LYTEBUG.get(), LytebugRenderer::new);
        // Goblin rocks render as a thrown item (vanilla projectile look) — using the
        // built-in ThrownItemRenderer keyed to a "stone" item gives a free pebble visual.
        // ThrownItemRenderer pulls the item to render from the projectile's
        // ItemSupplier.getItem() — GoblinRockProjectile returns a cobblestone.
        event.registerEntityRenderer(ModEntities.GOBLIN_ROCK.get(),
                net.minecraft.client.renderer.entity.ThrownItemRenderer::new);
        // Frost bolt renders as its grimshard core via the same thrown-item path.
        event.registerEntityRenderer(ModEntities.FROST_BOLT.get(),
                net.minecraft.client.renderer.entity.ThrownItemRenderer::new);
    }
}
