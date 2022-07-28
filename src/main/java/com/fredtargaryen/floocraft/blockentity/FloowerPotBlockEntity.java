package com.fredtargaryen.floocraft.blockentity;

import com.fredtargaryen.floocraft.DataReference;
import com.fredtargaryen.floocraft.FloocraftBase;
import com.fredtargaryen.floocraft.block.FlooFlamesBase;
import com.fredtargaryen.floocraft.inventory.container.FloowerPotMenu;
import com.fredtargaryen.floocraft.item.ItemFlooPowder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.ticks.TickPriority;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

public class FloowerPotBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider, Nameable {
    public boolean justUpdated;
    private final ItemStack[] inv;
    private int hRange;
    private int vRange;

    private net.minecraftforge.common.util.LazyOptional<net.minecraftforge.items.IItemHandlerModifiable> itemHandler;

    public FloowerPotBlockEntity(BlockPos pos, BlockState state) {
        super(FloocraftBase.POT_TYPE.get(), pos, state);
        this.inv = new ItemStack[1];
        this.inv[0] = ItemStack.EMPTY;
        this.hRange = DataReference.POT_MAX_H_RANGE;
        this.vRange = 5;
    }

    //region WorldlyContainer overrides
    @Override
    public void clearContent() {}

    @Override
    public int getContainerSize()
    {
        return this.inv.length;
    }

    @Override
    public boolean isEmpty()
    {
        return this.inv[0].isEmpty();
    }

    @Override
    public ItemStack getItem(int slot)
    {
        return this.inv[slot];
    }

    @Override
    public ItemStack removeItem(int slot, int amt)
    {
        ItemStack stack = getItem(slot);
        if (!stack.isEmpty())
        {
            if (stack.getCount() <= amt)
            {
                setItem(slot, ItemStack.EMPTY);
            }
            else
            {
                stack = stack.split(amt);
                if (stack.getCount() == 0)
                {
                    setItem(slot, ItemStack.EMPTY);
                }
            }
        }
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        return ItemStack.EMPTY;
    }

    @Override
    @ParametersAreNonnullByDefault
    public void setItem(int slot, ItemStack stack)
    {
        ItemStack nonnullstack = stack == null ? ItemStack.EMPTY : stack;
        this.inv[slot] = nonnullstack;

        if (!nonnullstack.isEmpty() && nonnullstack.getCount() > this.getMaxStackSize())
        {
            nonnullstack.setCount(this.getMaxStackSize());
        }
        this.setChanged();
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean stillValid(Player player)
    {
        return this.level.getBlockEntity(this.worldPosition) == this &&
                player.distanceToSqr(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5) < 64;
    }

    @Override
    @ParametersAreNonnullByDefault
    public void startOpen(Player player) { }

    @Override
    @ParametersAreNonnullByDefault
    public void stopOpen(Player player) { }

    @Override
    @ParametersAreNonnullByDefault
    public boolean canPlaceItem(int slot, ItemStack stack)
    {
        return stack.getItem() instanceof ItemFlooPowder;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return side == Direction.DOWN ? new int[] { 0 } : new int[0];
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack itemStackIn, Direction direction) {
        if(index == 0 && direction == Direction.DOWN)
        {
            return this.canPlaceItem(index, itemStackIn);
        }
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return index == 0 && direction == Direction.DOWN;
    }
//endregion

    //region MenuProvider overrides
    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, @Nonnull Inventory inventory, @Nonnull Player player) {
        return new FloowerPotMenu(windowId, inventory, player.level, this.worldPosition);
    }

    @Override
    public Component getDisplayName() {
        return new TranslatableComponent("block.floocraftft.floowerpot");
    }
    //endregion



    @Override
    public void load(CompoundTag tagCompound) {
        super.load(tagCompound);
        ListTag tagList = tagCompound.getList("Inventory", 10);
        if(tagList.size() == 0) {
            this.inv[0] = ItemStack.EMPTY;
        }
        else {
            CompoundTag tag = tagList.getCompound(0);
            byte slot = tag.getByte("Slot");
            if (slot == 0) {
                this.inv[slot] = ItemStack.of(tag);
            }
        }
        //Clamp ranges between 2 and 5 inclusive
        this.hRange = Math.max(DataReference.POT_MIN_H_RANGE, Math.min(DataReference.POT_MAX_H_RANGE, tagCompound.getInt("hRange")));
        this.vRange = Math.max(DataReference.POT_MIN_V_RANGE, Math.min(DataReference.POT_MAX_V_RANGE, tagCompound.getInt("vRange")));
    }

    @Override
    protected void saveAdditional(CompoundTag tagCompound) {
        super.saveAdditional(tagCompound);
        ListTag itemList = new ListTag();
        ItemStack stack = inv[0];
        if (stack != null && !stack.isEmpty()) {
            CompoundTag tag = new CompoundTag();
            tag.putByte("Slot", (byte) 0);
            stack.save(tag);
            itemList.add(tag);
        }
        tagCompound.put("Inventory", itemList);
        tagCompound.putInt("hRange", this.hRange);
        tagCompound.putInt("vRange", this.vRange);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    /**
     * Called when you receive a BlockEntityData packet for the location this
     * BlockEntity is currently in. On the client, the NetworkManager will always
     * be the remote server. On the server, it will be whomever is responsible for
     * sending the packet.
     *
     * @param net The NetworkManager the packet originated from
     * @param pkt The data packet
     */
    @Override
    @OnlyIn(Dist.CLIENT)
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        super.onDataPacket(net, pkt);
        this.justUpdated = true;
    }

    @Override
    public Component getName() {
        return this.getDisplayName();
    }

    public void adjustPotRange(char range, int amount) {
        if(range == 'h') {
            //Adjust horizontal range
            if(amount == 1 && this.hRange < DataReference.POT_MAX_H_RANGE) ++this.hRange;
            else if(amount == -1 && this.hRange > DataReference.POT_MIN_H_RANGE) --this.hRange;
            this.setChanged();
        }
        else if(range == 'v') {
            //Adjust horizontal range
            if(amount == 1 && this.vRange < DataReference.POT_MAX_V_RANGE) ++this.vRange;
            else if(amount == -1 && this.vRange > DataReference.POT_MIN_V_RANGE) --this.vRange;
            this.setChanged();
        }
    }

    public int getHRange() { return this.hRange; }

    public int getVRange() { return this.vRange; }

    @Override
    @Nonnull
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
    {
        if (!this.remove && facing == Direction.DOWN && capability == net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (this.itemHandler == null)
                this.itemHandler = net.minecraftforge.items.wrapper.SidedInvWrapper.create(this, Direction.DOWN)[0];
            return this.itemHandler.cast();
        }
        return super.getCapability(capability, facing);
    }

    /**
     * invalidates a tile entity
     */
    @Override
    public void setRemoved() {
        super.setRemoved();
        this.itemHandler.invalidate();
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        // getTicker guard ensures we know pot is a FloowerPotBlockEntity and level is on logical server
        FloowerPotBlockEntity pot = (FloowerPotBlockEntity) blockEntity;
        int hRange = pot.getHRange();
        int vRange = pot.getVRange();
        ItemStack stack = pot.getItem(0);
        if (stack != null && stack.getCount() > 0) {
            int par2 = pos.getX();
            int par3 = pos.getY();
            int par4 = pos.getZ();
            BlockPos currentPos;
            BlockState currentState;
            Block currentBlock;
            Block greenBusy = FloocraftBase.GREEN_FLAMES_BUSY.get();
            SoundEvent greened = FloocraftBase.GREENED.get();
            for (int x = par2 - hRange; x <= par2 + hRange; x++) {
                for (int y = par3 - vRange; y <= par3 + vRange; y++) {
                    for (int z = par4 - hRange; z <= par4 + hRange; z++) {
                        if(stack != null && stack.getCount() > 0) {
                            currentPos = new BlockPos(x, y, z);
                            currentState = level.getBlockState(currentPos);
                            currentBlock = currentState.getBlock();
                            if(currentBlock == Blocks.CAMPFIRE && currentState.getValue(BlockStateProperties.LIT)) {
                                Item i = stack.getItem();
                                level.setBlock(currentPos, FloocraftBase.FLOO_CAMPFIRE.get().defaultBlockState()
                                        .setValue(BlockStateProperties.AGE_15, (int) ((ItemFlooPowder) i).getConcentration())
                                        .setValue(BlockStateProperties.HORIZONTAL_FACING, currentState.getValue(BlockStateProperties.HORIZONTAL_FACING)), 3);
                                level.playSound(null, currentPos, greened, SoundSource.BLOCKS, 1.0F, 1.0F);
                                stack = stack.getCount() == 1 ? ItemStack.EMPTY : stack.split(stack.getCount() - 1);
                            }
                            else if(currentBlock == Blocks.SOUL_CAMPFIRE && currentState.getValue(BlockStateProperties.LIT)) {
                                Item i = stack.getItem();
                                level.setBlock(currentPos, FloocraftBase.FLOO_SOUL_CAMPFIRE.get().defaultBlockState()
                                        .setValue(BlockStateProperties.AGE_15, (int) ((ItemFlooPowder) i).getConcentration())
                                        .setValue(BlockStateProperties.HORIZONTAL_FACING, currentState.getValue(BlockStateProperties.HORIZONTAL_FACING)), 3);
                                level.playSound(null, currentPos, greened, SoundSource.BLOCKS, 1.0F, 1.0F);
                                stack = stack.getCount() == 1 ? ItemStack.EMPTY : stack.split(stack.getCount() - 1);
                            }
                            else if (currentState.is(BlockTags.FIRE)) {
                                if (((FlooFlamesBase) greenBusy).isInFireplace(level, currentPos) != null) {
                                    Item i = stack.getItem();
                                    BlockState stateToSet = FlooFlamesBase.getFireBlockToPlace(level, currentPos).defaultBlockState();
                                    level.setBlock(currentPos, stateToSet.setValue(BlockStateProperties.AGE_15, (int) ((ItemFlooPowder) i).getConcentration()), 3);
                                    level.playSound(null, currentPos, greened, SoundSource.BLOCKS, 1.0F, 1.0F);
                                    stack = stack.getCount() == 1 ? ItemStack.EMPTY : stack.split(stack.getCount() - 1);
                                }
                            }
                        }
                    }
                }
            }
        }
        pot.setItem(0, stack);
        level.onBlockStateChange(pos, state, state);
        level.updateNeighborsAt(pos, FloocraftBase.BLOCK_FLOOWER_POT.get());
        level.scheduleTick(pos, state.getBlock(), 40, TickPriority.EXTREMELY_LOW);
    }
}
