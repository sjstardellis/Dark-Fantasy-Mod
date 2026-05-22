package net.steve.darkfantasy;

import net.steve.darkfantasy.client.renderer.AlchemyStandRenderer;
import net.steve.darkfantasy.client.renderer.FairyRenderer;
import net.steve.darkfantasy.client.renderer.SkylandsPortalRenderer;
import net.steve.darkfantasy.client.renderer.WizardRenderer;
import net.steve.darkfantasy.client.screen.AlchemyStandScreen;
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
    }

    @SubscribeEvent
    static void onRegisterBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.SKYLANDS_PORTAL_BE.get(),
                ctx -> new SkylandsPortalRenderer());
        event.registerBlockEntityRenderer(ModBlockEntities.ALCHEMY_STAND_BE.get(),
                AlchemyStandRenderer::new);

        event.registerEntityRenderer(ModEntities.FAIRY.get(), FairyRenderer::new);
        event.registerEntityRenderer(ModEntities.WIZARD.get(), WizardRenderer::new);
    }
}
