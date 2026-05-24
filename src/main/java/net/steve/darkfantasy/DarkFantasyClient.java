package net.steve.darkfantasy;

import net.steve.darkfantasy.client.renderer.AlchemyStandRenderer;
import net.steve.darkfantasy.client.renderer.ElectroDragonRenderer;
import net.steve.darkfantasy.client.renderer.FairyRenderer;
import net.steve.darkfantasy.client.renderer.GoblinRenderer;
import net.steve.darkfantasy.client.renderer.SkylandsPortalRenderer;
import net.steve.darkfantasy.client.renderer.WizardRenderer;
import net.steve.darkfantasy.client.screen.AlchemyStandScreen;
import net.steve.darkfantasy.client.screen.BrewingKegScreen;
import net.steve.darkfantasy.init.ModBlockEntities;
import net.steve.darkfantasy.init.ModEntities;
import net.steve.darkfantasy.init.ModMenuTypes;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
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
        // Goblin rocks render as a thrown item (vanilla projectile look) — using the
        // built-in ThrownItemRenderer keyed to a "stone" item gives a free pebble visual.
        // ThrownItemRenderer pulls the item to render from the projectile's
        // ItemSupplier.getItem() — GoblinRockProjectile returns a cobblestone.
        event.registerEntityRenderer(ModEntities.GOBLIN_ROCK.get(),
                net.minecraft.client.renderer.entity.ThrownItemRenderer::new);
    }
}
