package com.fredtargaryen.floocraft.item;

import com.fredtargaryen.floocraft.FloocraftBase;
import com.fredtargaryen.floocraft.block.FlooSignBlock;
import com.fredtargaryen.floocraft.blockentity.FlooSignBlockEntity;
import com.fredtargaryen.floocraft.client.gui.FlooSignScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class ItemFlooSign extends Item {

	public ItemFlooSign() {
        super(new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS).stacksTo(16));
    }

    @Override
    @Nonnull
	public InteractionResult useOn(UseOnContext context) {
	    Direction side = context.getClickedFace();
		if (side == Direction.DOWN || side == Direction.UP) {
            return InteractionResult.FAIL;
        }
        else {
            Player player = context.getPlayer();
            BlockPos pos = context.getClickedPos();
            ItemStack stack = context.getItemInHand();
            if (!player.mayUseItemAt(pos, side, stack)) {
                return InteractionResult.FAIL;
            }
            else {
                Level level = context.getLevel();
                BlockPos newpos = pos.relative(side);
            	level.setBlock(newpos, FloocraftBase.BLOCK_FLOO_SIGN.get().defaultBlockState().setValue(FlooSignBlock.FACING, side), 3);
            	stack.grow(-1);
            	FlooSignBlockEntity fireplaceTE = (FlooSignBlockEntity)level.getBlockEntity(newpos);
            	if (fireplaceTE != null) {
            		fireplaceTE.setPlayer(player);
            		if(level.isClientSide) {
            			this.dothesigneditguiscreen(fireplaceTE);
            		}
                }
                return InteractionResult.SUCCESS;
            }
        }
    }
	
	@OnlyIn(Dist.CLIENT)
	private void dothesigneditguiscreen(FlooSignBlockEntity t) {
        Minecraft.getInstance().setScreen(new FlooSignScreen(t));
	}
}