package net.steve.darkfantasy.init;

import net.steve.darkfantasy.DarkFantasy;
import net.steve.darkfantasy.menu.AlchemyStandMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, DarkFantasy.MOD_ID);

    public static final Supplier<MenuType<AlchemyStandMenu>> ALCHEMY_STAND_MENU =
            MENU_TYPES.register("alchemy_stand_menu",
                    () -> IMenuTypeExtension.create(AlchemyStandMenu::new));

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }
}
