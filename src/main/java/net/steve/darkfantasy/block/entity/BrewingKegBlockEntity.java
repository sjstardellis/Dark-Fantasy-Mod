package net.steve.darkfantasy.block.entity;

import net.steve.darkfantasy.block.custom.BrewingKegBlock;
import net.steve.darkfantasy.init.ModBlockEntities;
import net.steve.darkfantasy.item.ModItems;
import net.steve.darkfantasy.menu.BrewingKegMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/**
 * Brewing keg block entity. Holds three input slots (hops / wheat / water bucket),
 * a {@link #TANK_CAPACITY 4-quart} internal beer tank, and a fermentation timer that
 * only ticks when:
 * <ol>
 *   <li>All three ingredient slots are populated.</li>
 *   <li>The tank has room for another {@link #BEER_PER_BATCH quart}.</li>
 *   <li>A heat source ({@link #isHeatSource}) sits directly below the keg.</li>
 * </ol>
 *
 * <p>When a batch completes the keg consumes one of each ingredient (returning an
 * empty bucket to the water slot) and adds {@link #BEER_PER_BATCH} mB to the tank.
 *
 * <p>A separate path — {@link #drainOneStein} — is called by
 * {@link BrewingKegBlock#useItemOn} when the player right-clicks the block with a
 * Stein Glass; it drains one quart from the tank and is the only way fluid leaves.
 *
 * <p>The tank's underlying {@link FluidResource} is {@link Fluids#WATER} purely as a
 * texture proxy — the GUI uses the vanilla water-still texture to draw the level.
 * There is no real "beer fluid" registered; the tank is gameplay-only.
 */
public class BrewingKegBlockEntity extends BaseContainerBlockEntity {
    public static final int SLOT_COUNT = 3;
    public static final int SLOT_HOPS = 0;
    public static final int SLOT_WHEAT = 1;
    public static final int SLOT_BUCKET = 2;

    /** 4 quarts. Each completed brew adds 1 quart, so 4 brews fills the tank. */
    public static final int TANK_CAPACITY = 4000;
    /** Beer added to the tank by one completed brew batch. */
    public static final int BEER_PER_BATCH = 1000;
    /** Beer drained per stein fill — half a batch, so 1 brew = 2 steins, full tank = 8 steins. */
    public static final int BEER_PER_STEIN = 500;
    /** ~5 minutes at 20 tps. Effective time only — pauses without heat. */
    public static final int BREW_DURATION_TICKS = 6_000;

    private static final int TANK_INDEX = 0;

    public static final int DATA_PROGRESS = 0;
    public static final int DATA_MAX_PROGRESS = 1;
    public static final int DATA_BEER = 2;
    public static final int DATA_COUNT = 3;

    private static final Component DEFAULT_NAME = Component.translatable("container.darkfantasy.brewing_keg");

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private int progress = 0;
    private int maxProgress = BREW_DURATION_TICKS;

    /**
     * Beer tank, expressed as a water-fluid resource so the GUI can reuse the
     * vanilla water-still texture without registering a custom fluid. Capacity is
     * exactly 4 batches; insert is rejected if it would overflow.
     */
    private final FluidStacksResourceHandler tank = new FluidStacksResourceHandler(1, TANK_CAPACITY) {
        @Override
        public boolean isValid(int index, FluidResource resource) {
            return resource.getFluid() == Fluids.WATER;
        }

        @Override
        protected void onContentsChanged(int index, FluidStack previousContents) {
            setChanged();
        }
    };

    public final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_PROGRESS -> progress;
                case DATA_MAX_PROGRESS -> maxProgress;
                case DATA_BEER -> tank.getAmountAsInt(TANK_INDEX);
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case DATA_PROGRESS -> progress = value;
                case DATA_MAX_PROGRESS -> maxProgress = value;
                case DATA_BEER -> {
                    // Client-side reconstruction so the screen can read tank.getAmountAsInt
                    // without a separate sync path.
                    if (value > 0) {
                        tank.set(TANK_INDEX, FluidResource.of(Fluids.WATER), Math.min(value, TANK_CAPACITY));
                    } else {
                        tank.set(TANK_INDEX, FluidResource.EMPTY, 0);
                    }
                }
            }
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public BrewingKegBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BREWING_KEG_BE.get(), pos, state);
    }

    @Override
    protected Component getDefaultName() {
        return DEFAULT_NAME;
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    public int getBeerAmount() {
        return this.tank.getAmountAsInt(TANK_INDEX);
    }

    public int getProgress() {
        return this.progress;
    }

    public int getMaxProgress() {
        return this.maxProgress;
    }

    /**
     * Called from {@link BrewingKegBlock#useItemOn} when the player right-clicks with a
     * Stein Glass. Drains one stein's worth ({@link #BEER_PER_STEIN} mB) from the tank.
     * Returns true if the drain succeeded.
     */
    public boolean drainOneStein() {
        if (this.tank.getAmountAsInt(TANK_INDEX) < BEER_PER_STEIN) return false;
        try (Transaction txn = Transaction.openRoot()) {
            int drained = tank.extract(TANK_INDEX, FluidResource.of(Fluids.WATER), BEER_PER_STEIN, txn);
            if (drained < BEER_PER_STEIN) return false; // shouldn't happen given the check above
            txn.commit();
        }
        return true;
    }

    // ---- BE sync ------------------------------------------------------------

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(),
                    this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    // ---- Persistence --------------------------------------------------------

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);
        this.progress = input.getIntOr("Progress", 0);
        this.maxProgress = input.getIntOr("MaxProgress", BREW_DURATION_TICKS);
        int beerAmount = input.getIntOr("BeerAmount", 0);
        if (beerAmount > 0) {
            tank.set(TANK_INDEX, FluidResource.of(Fluids.WATER), Math.min(beerAmount, TANK_CAPACITY));
        } else {
            tank.set(TANK_INDEX, FluidResource.EMPTY, 0);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
        output.putInt("Progress", this.progress);
        output.putInt("MaxProgress", this.maxProgress);
        output.putInt("BeerAmount", tank.getAmountAsInt(TANK_INDEX));
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new BrewingKegMenu(containerId, inventory, this, this.dataAccess);
    }

    // ---- Slot validation ----------------------------------------------------

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return switch (slot) {
            case SLOT_HOPS -> stack.is(ModItems.HOPS.get());
            case SLOT_WHEAT -> stack.is(Items.WHEAT);
            case SLOT_BUCKET -> stack.is(Items.WATER_BUCKET) || stack.is(Items.BUCKET);
            default -> false;
        };
    }

    // ---- Brew loop ----------------------------------------------------------

    public static void serverTick(Level level, BlockPos pos, BlockState state, BrewingKegBlockEntity be) {
        boolean canBrew = be.hasAllIngredients() && be.hasRoomInTank();
        boolean heated = isHeatSource(level, pos.below());

        // Update visual stage. Five-state machine driven by ingredient presence + heat:
        //   STAGE_BREWING when actively cooking (heated + can brew)
        //   STAGE_READY   when ingredients loaded but no heat
        //   STAGE_LOADING when at least one slot has something
        //   STAGE_EMPTY   otherwise (regardless of tank fill — the tank is a separate axis)
        // STAGE_DONE was used in the previous design; we no longer emit it because the
        // tank can hold multiple batches and "done" isn't a discrete state any more.
        int desiredStage;
        if (canBrew && heated) desiredStage = BrewingKegBlock.STAGE_BREWING;
        else if (canBrew) desiredStage = BrewingKegBlock.STAGE_READY;
        else if (be.hasAnyIngredient()) desiredStage = BrewingKegBlock.STAGE_LOADING;
        else desiredStage = BrewingKegBlock.STAGE_EMPTY;
        if (state.getValue(BrewingKegBlock.STAGE) != desiredStage) {
            level.setBlock(pos, state.setValue(BrewingKegBlock.STAGE, desiredStage), 3);
        }

        if (!canBrew || !heated) {
            // Don't accumulate progress if we can't actually brew this tick.
            if (be.progress > 0) {
                be.progress = 0;
                setChanged(level, pos, state);
            }
            return;
        }

        be.progress++;
        if (be.progress >= be.maxProgress) {
            be.completeBatch();
            be.progress = 0;
        }
        setChanged(level, pos, state);
    }

    private boolean hasAllIngredients() {
        return items.get(SLOT_HOPS).is(ModItems.HOPS.get())
                && items.get(SLOT_WHEAT).is(Items.WHEAT)
                && items.get(SLOT_BUCKET).is(Items.WATER_BUCKET);
    }

    private boolean hasAnyIngredient() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (!items.get(i).isEmpty()) return true;
        }
        return false;
    }

    /** True iff the tank can fit another full batch ({@link #BEER_PER_BATCH} mB). */
    private boolean hasRoomInTank() {
        return tank.getAmountAsInt(TANK_INDEX) + BEER_PER_BATCH <= TANK_CAPACITY;
    }

    /**
     * Consume one of each ingredient, return an empty bucket to the water slot, and
     * add one batch of beer to the tank. Called only when {@link #hasAllIngredients}
     * and {@link #hasRoomInTank} are both true.
     */
    private void completeBatch() {
        items.get(SLOT_HOPS).shrink(1);
        items.get(SLOT_WHEAT).shrink(1);
        // Water bucket → empty bucket, in-place. The slot validator accepts both.
        items.set(SLOT_BUCKET, new ItemStack(Items.BUCKET));
        try (Transaction txn = Transaction.openRoot()) {
            tank.insert(TANK_INDEX, FluidResource.of(Fluids.WATER), BEER_PER_BATCH, txn);
            txn.commit();
        }
    }

    /**
     * Heat sources accepted directly underneath the keg. Same set as the previous
     * non-GUI design: anything that produces a visible flame in vanilla, with
     * campfires only counting when lit.
     */
    private static boolean isHeatSource(Level level, BlockPos below) {
        BlockState state = level.getBlockState(below);
        if (state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE)) return true;
        if (state.is(Blocks.LAVA) || state.is(Blocks.MAGMA_BLOCK)) return true;
        if ((state.is(Blocks.CAMPFIRE) || state.is(Blocks.SOUL_CAMPFIRE))
                && state.getValue(CampfireBlock.LIT)) return true;
        return false;
    }
}
