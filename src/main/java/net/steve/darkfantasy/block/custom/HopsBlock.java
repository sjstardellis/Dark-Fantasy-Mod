package net.steve.darkfantasy.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.steve.darkfantasy.item.ModItems;

/**
 * Hops crop. 4-stage farmland plant (same pattern as beetroots — fewer stages than
 * wheat for a faster grow cycle). Used as a brewing ingredient for beer, not as
 * food on its own.
 *
 * <p>{@link #getBaseSeedId()} returns the Hops item, so breaking a young plant
 * gives back a single hops to replant — consistent with how beetroots / wheat
 * recover their seed item.
 */
public class HopsBlock extends CropBlock {
    public static final MapCodec<HopsBlock> CODEC = simpleCodec(HopsBlock::new);
    public static final int MAX_AGE = 3;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;

    public HopsBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<HopsBlock> codec() {
        return CODEC;
    }

    @Override
    public int getMaxAge() {
        return MAX_AGE;
    }

    @Override
    protected IntegerProperty getAgeProperty() {
        return AGE;
    }

    /** Smaller bonemeal increment than wheat — matches the 4-stage crop convention. */
    @Override
    protected int getBonemealAgeIncrease(net.minecraft.world.level.Level level) {
        return net.minecraft.util.Mth.nextInt(level.getRandom(), 1, 2);
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ModItems.HOPS.get();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }
}
