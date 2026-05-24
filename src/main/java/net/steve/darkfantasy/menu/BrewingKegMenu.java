package net.steve.darkfantasy.menu;

import net.steve.darkfantasy.block.entity.BrewingKegBlockEntity;
import net.steve.darkfantasy.init.ModMenuTypes;
import net.steve.darkfantasy.item.ModItems;
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

/**
 * Container menu for the brewing keg. Three input slots in a horizontal row:
 *
 * <ul>
 *   <li><b>Hops</b> (slot 0) — accepts {@link ModItems#HOPS} only.</li>
 *   <li><b>Wheat</b> (slot 1) — accepts {@link Items#WHEAT} only.</li>
 *   <li><b>Water bucket</b> (slot 2) — accepts {@link Items#WATER_BUCKET} or
 *       {@link Items#BUCKET} (the empty bucket left after a brew completes,
 *       so the player can still pull it out via shift-click).</li>
 * </ul>
 *
 * <p>There's no output slot — beer accumulates in the BE's internal tank and is
 * extracted by right-clicking the keg with a Stein Glass.
 */
public class BrewingKegMenu extends AbstractContainerMenu {
    public static final int BE_SLOT_COUNT = 3;
    public static final int SLOT_HOPS = 0;
    public static final int SLOT_WHEAT = 1;
    public static final int SLOT_BUCKET = 2;

    private static final int PLAYER_INV_START = BE_SLOT_COUNT;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 36;

    private final Container container;
    private final ContainerData data;

    /** Client-side constructor (called via {@code IMenuTypeExtension.create}). */
    public BrewingKegMenu(int containerId, Inventory inv, RegistryFriendlyByteBuf buf) {
        this(containerId, inv,
                getBlockEntity(inv, buf.readBlockPos()),
                new SimpleContainerData(BrewingKegBlockEntity.DATA_COUNT));
    }

    /** Server-side constructor (called from {@code BrewingKegBlockEntity.createMenu}). */
    public BrewingKegMenu(int containerId, Inventory inv, Container container, ContainerData data) {
        super(ModMenuTypes.BREWING_KEG_MENU.get(), containerId);
        checkContainerSize(container, BE_SLOT_COUNT);
        checkContainerDataCount(data, BrewingKegBlockEntity.DATA_COUNT);
        this.container = container;
        this.data = data;

        // Three input slots — horizontal row at the top of the GUI.
        // Layout is intentionally identical to the alchemy stand's input row so
        // it looks "the same kind of machine" at a glance.
        this.addSlot(new Slot(container, SLOT_HOPS, 52, 17) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModItems.HOPS.get());
            }
        });
        this.addSlot(new Slot(container, SLOT_WHEAT, 80, 17) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.WHEAT);
            }
        });
        this.addSlot(new Slot(container, SLOT_BUCKET, 108, 17) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                // Both full and empty buckets are valid — the BE swaps them in place.
                return stack.is(Items.WATER_BUCKET) || stack.is(Items.BUCKET);
            }
        });

        this.addStandardInventorySlots(inv, 8, 84);
        this.addDataSlots(data);
    }

    private static Container getBlockEntity(Inventory inv, BlockPos pos) {
        BlockEntity be = inv.player.level().getBlockEntity(pos);
        if (be instanceof BrewingKegBlockEntity bkbe) return bkbe;
        return new SimpleContainer(BE_SLOT_COUNT);
    }

    public int getProgress() {
        return data.get(BrewingKegBlockEntity.DATA_PROGRESS);
    }

    public int getMaxProgress() {
        return data.get(BrewingKegBlockEntity.DATA_MAX_PROGRESS);
    }

    public int getBeerAmount() {
        return data.get(BrewingKegBlockEntity.DATA_BEER);
    }

    public int getTankCapacity() {
        return BrewingKegBlockEntity.TANK_CAPACITY;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    /**
     * Shift-click routing:
     * <ul>
     *   <li>From a BE slot → player inventory.</li>
     *   <li>From player inventory: route to the matching input slot if the item is
     *       a valid ingredient; otherwise the move fails.</li>
     * </ul>
     */
    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            copy = stack.copy();

            if (slotIndex < BE_SLOT_COUNT) {
                if (!this.moveItemStackTo(stack, PLAYER_INV_START, PLAYER_INV_END, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // From player inv: send each ingredient to its dedicated slot.
                // Buckets (both full and empty) target the bucket slot.
                int targetSlot;
                if (stack.is(ModItems.HOPS.get())) targetSlot = SLOT_HOPS;
                else if (stack.is(Items.WHEAT)) targetSlot = SLOT_WHEAT;
                else if (stack.is(Items.WATER_BUCKET) || stack.is(Items.BUCKET)) targetSlot = SLOT_BUCKET;
                else return ItemStack.EMPTY;

                if (!this.moveItemStackTo(stack, targetSlot, targetSlot + 1, false)) {
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
