package com.fredtargaryen.floocraft.inventory.container;

import com.fredtargaryen.floocraft.FloocraftBase;
import com.fredtargaryen.floocraft.blockentity.FloowerPotBlockEntity;
import com.fredtargaryen.floocraft.item.ItemFlooPowder;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class FloowerPotMenu extends AbstractContainerMenu {
    private BlockEntity potTE;

    @Override
    public boolean stillValid(Player p_38874_) {
        return true;
    }

    private class PowderSlot extends Slot {
        public PowderSlot(Container container) {
            super(container, 0, 80, 35);
        }

        /**
         * Check if the stack is a valid item for this slot. Always true beside for the armor slots.
         */
        @Override
        public boolean mayPlace(ItemStack par1ItemStack) {
            return par1ItemStack.isEmpty() || par1ItemStack.getItem() instanceof ItemFlooPowder ;
        }
    }

    /**
     * CLIENT side constructor for the container
     * @param windowId
     * @param inv
     */
    public FloowerPotMenu(int windowId, Inventory inv, Level level, BlockPos pos) {
        super(FloocraftBase.POT_MENU_TYPE.get(), windowId);
        this.potTE = level.getBlockEntity(pos);
        if(this.potTE != null) {
            this.addSlot(new PowderSlot((FloowerPotBlockEntity) this.potTE));
        }
        this.addPlayerInventorySlots(inv);
    }

    private void addPlayerInventorySlots(Inventory playerInventory) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9,
                        8 + j * 18, 84 + i * 18));
            }
        }
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    @Override
    @MethodsReturnNonnullByDefault
    public ItemStack quickMoveStack(Player player, int slot) {
        ItemStack stack = null;
        Slot slotObject = this.slots.get(slot);

        //null checks and checks if the item can be stacked (maxStackSize > 1)
        if (slotObject != null && slotObject.hasItem()) {
            ItemStack stackInSlot = slotObject.getItem();
            stack = stackInSlot.copy();

            //merges the item into player inventory since its in the blockEntity
            if (slot < 1) {
                if (!this.moveItemStackTo(stackInSlot, 1, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            }
            //places it into the blockEntity is possible since its in the player inventory
            else if (!this.moveItemStackTo(stackInSlot, 0, 1, false)) {
                return ItemStack.EMPTY;
            }

            if (stackInSlot.getCount() == 0) {
                slotObject.set(ItemStack.EMPTY);
            } else {
                slotObject.setChanged();
            }

            if (stackInSlot.getCount() == stack.getCount()) {
                return ItemStack.EMPTY;
            }
            slotObject.onTake(player, stackInSlot);
        }
        return stack == null ? ItemStack.EMPTY : stack;
    }

    public BlockEntity getBlockEntity() {
        return this.potTE;
    }
}