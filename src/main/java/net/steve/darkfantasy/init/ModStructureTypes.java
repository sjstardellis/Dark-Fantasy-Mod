package net.steve.darkfantasy.init;

import net.steve.darkfantasy.DarkFantasy;
import net.steve.darkfantasy.worldgen.structure.WaterAvoidingJigsawStructure;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModStructureTypes {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, DarkFantasy.MOD_ID);

    public static final Supplier<StructureType<WaterAvoidingJigsawStructure>> WATER_AVOIDING_JIGSAW =
            STRUCTURE_TYPES.register("water_avoiding_jigsaw",
                    () -> () -> WaterAvoidingJigsawStructure.CODEC);

    public static void register(IEventBus eventBus) {
        STRUCTURE_TYPES.register(eventBus);
    }
}
