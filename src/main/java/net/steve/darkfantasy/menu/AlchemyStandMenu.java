package net.steve.darkfantasy.menu;

import net.steve.darkfantasy.block.entity.AlchemyStandBlockEntity;
import net.steve.darkfantasy.init.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;

public class AlchemyStandMenu extends AbstractContainerMenu {
    // BE slots:
    //   0..2  = inputs
    //   3     = output
    //   4     = bucket slot (lava only)
    public static final int BE_SLOT_COUNT = 5;
    public static final int OUTPUT_SLOT = 3;
    public static final int BUCKET_SLOT = 4;

    private static final int PLAYER_INV_START = BE_SLOT_COUNT;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 36; // 27 main + 9 hotbar

    private final Container container;
    private final ContainerData data;

    /** Client-side constructor (from IMenuTypeExtension.create). */
    public AlchemyStandMenu(int containerId, Inventory inv, RegistryFriendlyByteBuf buf) {
        this(containerId, inv,
                getBlockEntity(inv, buf.readBlockPos()),
                new SimpleContainerData(AlchemyStandBlockEntity.DATA_COUNT));
    }

    /** Server-side constructor (called from AlchemyStandBlockEntity#createMenu). */
    public AlchemyStandMenu(int containerId, Inventory inv, Container container, ContainerData data) {
        super(ModMenuTypes.ALCHEMY_STAND_MENU.get(), containerId);
        checkContainerSize(container, BE_SLOT_COUNT);
        checkContainerDataCount(data, AlchemyStandBlockEntity.DATA_COUNT);
        this.container = container;
        this.data = data;

        // 3 input slots — horizontal row at the top.
        this.addSlot(new Slot(container, 0, 52, 17));
        this.addSlot(new Slot(container, 1, 80, 17));
        this.addSlot(new Slot(container, 2, 108, 17));

        // Output slot — centered below the inputs, beneath the progress arrow.
        this.addSlot(new Slot(container, OUTPUT_SLOT, 80, 60) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        // Bucket slot — right side, mid-height (lava buckets only).
        this.addSlot(new Slot(container, BUCKET_SLOT, 140, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.LAVA_BUCKET) || stack.is(Items.BUCKET);
            }
        });

        this.addStandardInventorySlots(inv, 8, 84);
        this.addDataSlots(data);
    }

    private static Container getBlockEntity(Inventory inv, BlockPos pos) {
        BlockEntity be = inv.player.level().getBlockEntity(pos);
        if (be instanceof AlchemyStandBlockEntity asbe) return asbe;
        return new SimpleContainer(BE_SLOT_COUNT);
    }

    public int getProgress() {
        return data.get(AlchemyStandBlockEntity.DATA_PROGRESS);
    }

    public int getMaxProgress() {
        return data.get(AlchemyStandBlockEntity.DATA_MAX_PROGRESS);
    }

    public int getLavaAmount() {
        return data.get(AlchemyStandBlockEntity.DATA_LAVA);
    }

    public int getTankCapacity() {
        return AlchemyStandBlockEntity.TANK_CAPACITY;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            copy = stack.copy();

            if (slotIndex < BE_SLOT_COUNT) {
                // BE slot -> player inventory.
                if (!this.moveItemStackTo(stack, PLAYER_INV_START, PLAYER_INV_END, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (stack.is(Items.LAVA_BUCKET)) {
                // Lava bucket -> bucket slot.
                if (!this.moveItemStackTo(stack, BUCKET_SLOT, BUCKET_SLOT + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Anything else -> input slots (0..2 only, never output or bucket).
                if (!this.moveItemStackTo(stack, 0, OUTPUT_SLOT, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return copy;
    }
}
