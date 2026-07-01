package net.steve.darkfantasy.menu;

import net.steve.darkfantasy.block.entity.BrewingKegBlockEntity;
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

/**
 * Container menu for the brewing keg. Three input slots in a horizontal row:
 *
 * <ul>
 *   <li><b>Ingredient</b> (slots 0 &amp; 1) — accept any non-bucket item; the brewing
 *       recipe decides which pairs actually ferment (hops + wheat → beer, etc.).</li>
 *   <li><b>Water bucket</b> (slot 2) — accepts {@link Items#WATER_BUCKET} or
 *       {@link Items#BUCKET} (the empty bucket left after a brew completes,
 *       so the player can still pull it out via shift-click).</li>
 * </ul>
 *
 * <p>There's no output slot — the brew accumulates in the BE's internal tank and is
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
        // The two solid slots accept any non-bucket item; the brewing recipe decides
        // what actually ferments, so every brew's ingredients (hops, nether wart, glow
        // berries, mushrooms, wither rose, …) can be loaded here.
        // Slot positions match the vanilla brewing stand's three-bottle arc exactly
        // (56,51)/(79,51)/(102,51). There is no output slot — the brew goes to the tank.
        this.addSlot(new Slot(container, SLOT_HOPS, 56, 51) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return isSolidIngredient(stack);
            }
        });
        this.addSlot(new Slot(container, SLOT_WHEAT, 79, 51) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return isSolidIngredient(stack);
            }
        });
        this.addSlot(new Slot(container, SLOT_BUCKET, 102, 51) {
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

    /** Both solid slots accept any non-empty, non-bucket item; the recipe gates brewing. */
    private static boolean isSolidIngredient(ItemStack stack) {
        return !stack.isEmpty() && !stack.is(Items.WATER_BUCKET) && !stack.is(Items.BUCKET);
    }

    /** The drink the keg's tank currently holds (client-synced), or empty. Drives the tank tint. */
    public ItemStack currentBrew() {
        return this.container instanceof BrewingKegBlockEntity be ? be.getCurrentBrew() : ItemStack.EMPTY;
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
                // From player inv: buckets go to the bucket slot; any other item is a
                // candidate brewing ingredient and fills the two solid slots (0..1).
                if (stack.is(Items.WATER_BUCKET) || stack.is(Items.BUCKET)) {
                    if (!this.moveItemStackTo(stack, SLOT_BUCKET, SLOT_BUCKET + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(stack, SLOT_HOPS, SLOT_BUCKET, false)) {
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
