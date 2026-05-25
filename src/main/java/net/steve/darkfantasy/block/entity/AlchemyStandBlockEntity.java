package net.steve.darkfantasy.block.entity;

import net.steve.darkfantasy.init.ModBlockEntities;
import net.steve.darkfantasy.init.ModFluids;
import net.steve.darkfantasy.init.ModRecipes;
import net.steve.darkfantasy.item.ModItems;
import net.steve.darkfantasy.menu.AlchemyStandMenu;
import net.steve.darkfantasy.recipe.AlchemyRecipe;
import net.steve.darkfantasy.recipe.AlchemyRecipeInput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.Optional;

public class AlchemyStandBlockEntity extends BaseContainerBlockEntity {
    public static final int SLOT_COUNT = 5;
    public static final int INPUT_SLOT_0 = 0;
    public static final int INPUT_SLOT_1 = 1;
    public static final int INPUT_SLOT_2 = 2;
    public static final int OUTPUT_SLOT = 3;
    public static final int BUCKET_SLOT = 4;

    public static final int TANK_CAPACITY = 4000;
    public static final int ELIXIR_PER_BUCKET = 1000;
    private static final int TANK_INDEX = 0; // single-tank handler

    // ContainerData indices for client-server sync of progress + fluid amount.
    public static final int DATA_PROGRESS = 0;
    public static final int DATA_MAX_PROGRESS = 1;
    public static final int DATA_ELIXIR = 2;
    public static final int DATA_COUNT = 3;

    private static final Component DEFAULT_NAME = Component.translatable("container.darkfantasy.alchemy_stand");

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);

    private int progress = 0;
    private int maxProgress = 200;

    /**
     * Elixir-only fluid tank (single index). Uses NeoForge's modern transfer API
     * ({@link FluidStacksResourceHandler}) instead of the deprecated {@code FluidTank}.
     * Validity is gated to {@link ModFluids#ELIXIR_SOURCE}; any other fluid attempting
     * to insert is rejected by the resource handler.
     */
    private final FluidStacksResourceHandler tank = new FluidStacksResourceHandler(1, TANK_CAPACITY) {
        @Override
        public boolean isValid(int index, FluidResource resource) {
            return resource.getFluid() == ModFluids.ELIXIR_SOURCE.get();
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
                case DATA_ELIXIR -> tank.getAmountAsInt(TANK_INDEX);
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case DATA_PROGRESS -> progress = value;
                case DATA_MAX_PROGRESS -> maxProgress = value;
                case DATA_ELIXIR -> {
                    // Client-side write — reconstruct as elixir with the synced amount.
                    if (value > 0) {
                        tank.set(TANK_INDEX, FluidResource.of(ModFluids.ELIXIR_SOURCE.get()), Math.min(value, TANK_CAPACITY));
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

    private final RecipeManager.CachedCheck<AlchemyRecipeInput, AlchemyRecipe> quickCheck =
            RecipeManager.createCheck(ModRecipes.ALCHEMY_TYPE.get());

    public AlchemyStandBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ALCHEMY_STAND_BE.get(), pos, state);
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

    public int getProgress() {
        return this.progress;
    }

    public int getMaxProgress() {
        return this.maxProgress;
    }

    public int getElixirAmount() {
        return this.tank.getAmountAsInt(TANK_INDEX);
    }

    // Send the full save tag (items + elixir + progress) when this BE is sent to the client.
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }

    /**
     * Server-side broadcast so the in-world BER (items on top, elixir level in the cauldron)
     * stays in sync — slot moves and tank fills happen via menu/right-click and would
     * otherwise only reach the client on chunk reload.
     */
    @Override
    public void setChanged() {
        super.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(),
                    this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);
        this.progress = input.getIntOr("Progress", 0);
        this.maxProgress = input.getIntOr("MaxProgress", 200);
        int elixirAmount = input.getIntOr("ElixirAmount", 0);
        if (elixirAmount > 0) {
            tank.set(TANK_INDEX, FluidResource.of(ModFluids.ELIXIR_SOURCE.get()), Math.min(elixirAmount, TANK_CAPACITY));
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
        output.putInt("ElixirAmount", tank.getAmountAsInt(TANK_INDEX));
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new AlchemyStandMenu(containerId, inventory, this, this.dataAccess);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == OUTPUT_SLOT) return false;
        if (slot == BUCKET_SLOT) return stack.is(ModItems.ELIXIR_BUCKET.get());
        return true;
    }

    /**
     * Attempt to drain an elixir bucket into the tank. Uses a transaction so we don't
     * commit the change unless we can fit the whole 1000 mB.
     * @return true if a bucket was consumed.
     */
    public boolean tryFillFromBucket() {
        try (Transaction txn = Transaction.openRoot()) {
            int filled = tank.insert(TANK_INDEX, FluidResource.of(ModFluids.ELIXIR_SOURCE.get()), ELIXIR_PER_BUCKET, txn);
            if (filled < ELIXIR_PER_BUCKET) {
                return false; // transaction auto-rolls back on close without commit
            }
            txn.commit();
            return true;
        }
    }

    // ---- Cook loop -----------------------------------------------------------

    public static void serverTick(Level level, BlockPos pos, BlockState state, AlchemyStandBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        be.tickBucketSlot();

        AlchemyRecipeInput input = new AlchemyRecipeInput(
                be.items.get(INPUT_SLOT_0),
                be.items.get(INPUT_SLOT_1),
                be.items.get(INPUT_SLOT_2));

        Optional<RecipeHolder<AlchemyRecipe>> recipeOpt = be.quickCheck.getRecipeFor(input, serverLevel);
        if (recipeOpt.isEmpty()) {
            be.resetProgressIfNeeded(level, pos, state);
            return;
        }

        AlchemyRecipe recipe = recipeOpt.get().value();
        ItemStack result = recipe.assemble(input);

        if (!be.canFitOutput(result)) {
            be.resetProgressIfNeeded(level, pos, state);
            return;
        }

        if (be.tank.getAmountAsInt(TANK_INDEX) < recipe.elixir()) {
            be.resetProgressIfNeeded(level, pos, state);
            return;
        }

        be.maxProgress = recipe.cookTime();
        be.progress++;
        if (be.progress >= be.maxProgress) {
            be.craft(result);
            be.drainElixir(recipe.elixir());
            be.progress = 0;
        }
        setChanged(level, pos, state);
    }

    private void tickBucketSlot() {
        ItemStack inBucketSlot = items.get(BUCKET_SLOT);
        if (!inBucketSlot.is(ModItems.ELIXIR_BUCKET.get())) return;
        if (!tryFillFromBucket()) return;

        if (inBucketSlot.getCount() == 1) {
            items.set(BUCKET_SLOT, new ItemStack(Items.BUCKET));
        } else {
            inBucketSlot.shrink(1);
        }
    }

    private boolean canFitOutput(ItemStack result) {
        ItemStack current = items.get(OUTPUT_SLOT);
        if (current.isEmpty()) return true;
        if (!ItemStack.isSameItemSameComponents(current, result)) return false;
        return current.getCount() + result.getCount() <= current.getMaxStackSize();
    }

    private void craft(ItemStack result) {
        ItemStack current = items.get(OUTPUT_SLOT);
        if (current.isEmpty()) {
            items.set(OUTPUT_SLOT, result.copy());
        } else {
            current.grow(result.getCount());
        }
        for (int i = INPUT_SLOT_0; i <= INPUT_SLOT_2; i++) {
            items.get(i).shrink(1);
        }
    }

    private void drainElixir(int amount) {
        try (Transaction txn = Transaction.openRoot()) {
            tank.extract(TANK_INDEX, FluidResource.of(ModFluids.ELIXIR_SOURCE.get()), amount, txn);
            txn.commit();
        }
    }

    private void resetProgressIfNeeded(Level level, BlockPos pos, BlockState state) {
        if (this.progress > 0) {
            this.progress = 0;
            setChanged(level, pos, state);
        }
    }
}
