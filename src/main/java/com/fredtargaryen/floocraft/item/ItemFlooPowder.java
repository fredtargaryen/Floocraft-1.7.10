package com.fredtargaryen.floocraft.item;

import com.fredtargaryen.floocraft.FloocraftBase;
import com.fredtargaryen.floocraft.block.FlooFlamesBase;
import com.fredtargaryen.floocraft.config.CommonConfig;
import com.fredtargaryen.floocraft.entity.DroppedFlooPowderEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class ItemFlooPowder extends Item {
    private final byte concentration;

    public byte getConcentration()
    {
        return this.concentration;
    }

	public ItemFlooPowder(byte conc) {
		super(new Item.Properties().tab(CreativeModeTab.TAB_MISC).stacksTo(64));
        this.concentration = conc;
	}

    @Override
	public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
	    if(!level.isClientSide) {
	        BlockState state = level.getBlockState(pos);
	        Block b = state.getBlock();
	        if(b == Blocks.CAMPFIRE && state.getValue(BlockStateProperties.LIT))
            {
                level.setBlock(pos, FloocraftBase.FLOO_CAMPFIRE.get().defaultBlockState()
                        .setValue(BlockStateProperties.HORIZONTAL_FACING, state.getValue(BlockStateProperties.HORIZONTAL_FACING))
                        .setValue(BlockStateProperties.AGE_15, (int) this.concentration), 3);
                level.playSound(null, pos, FloocraftBase.GREENED.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                context.getItemInHand().grow(-1);
                return InteractionResult.SUCCESS;
            }
            else if(b == Blocks.SOUL_CAMPFIRE && state.getValue(BlockStateProperties.LIT))
            {
                level.setBlock(pos, FloocraftBase.FLOO_SOUL_CAMPFIRE.get().defaultBlockState()
                        .setValue(BlockStateProperties.HORIZONTAL_FACING, state.getValue(BlockStateProperties.HORIZONTAL_FACING))
                        .setValue(BlockStateProperties.AGE_15, (int) this.concentration), 3);
                level.playSound(null, pos, FloocraftBase.GREENED.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                context.getItemInHand().grow(-1);
                return InteractionResult.SUCCESS;
            }
            else if (state.is(BlockTags.FIRE)) {
                if (((FlooFlamesBase) FloocraftBase.GREEN_FLAMES_TEMP.get()).isInFireplace(level, pos) != null) {
                    level.setBlock(pos, FlooFlamesBase.getFireBlockToPlace(level, pos).defaultBlockState().setValue(BlockStateProperties.AGE_15, (int) this.concentration), 3);
                    level.playSound(null, pos, FloocraftBase.GREENED.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                }
                context.getItemInHand().grow(-1);
                return InteractionResult.SUCCESS;
            }
        }
		return InteractionResult.FAIL;
	}
	
	/**
     * This function should return a new entity to replace the dropped item.
     * Returning null here will not kill the ItemEntity and will leave it to function normally.
     * Called when the item it placed in a world.
     *
     * @param level The level object
     * @param location The ItemEntity object, useful for getting the position of the entity
     * @param itemstack The current item stack
     * @return A new Entity object to spawn or null
     */
	@Override
    public Entity createEntity(Level level, Entity location, ItemStack itemstack) {
        if(!level.isClientSide) {
            Vec3 pos = location.position();
            DroppedFlooPowderEntity flp = new DroppedFlooPowderEntity(level, pos.x, pos.y, pos.z, itemstack, this.concentration);
            //Set immune to fire in type;
            flp.setPickUpDelay(40);
            flp.setDeltaMovement(location.getDeltaMovement());
            return flp;
        }
        return null;
    }

    /**
     * TODO Temporarily disabled. Entering a world with DroppedFlooPowderEntities in it causes the server to freeze.
     * This is a known Forge issue.
     */
    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flagIn) {
		if(this.concentration == 9) {
			tooltip.add(new TranslatableComponent("item.floocraftft.concentration", '\u221E').withStyle(ChatFormatting.GREEN));
            tooltip.add(new TranslatableComponent("item.floocraftft.creativeonly"));
		}
		else {
		    if(CommonConfig.DEPLETE_FLOO.get()) {
                tooltip.add(new TranslatableComponent("item.floocraftft.concentration", this.concentration).withStyle(ChatFormatting.GREEN));
            }
		    else
            {
                tooltip.add(new TranslatableComponent("item.floocraftft.concentration", '\u221E').withStyle(ChatFormatting.GREEN));
            }
        	if(this.concentration == 1) {
                tooltip.add(new TranslatableComponent("item.floocraftft.craftable"));
			}
        }
    }
}