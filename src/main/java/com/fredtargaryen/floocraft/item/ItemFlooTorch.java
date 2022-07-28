package com.fredtargaryen.floocraft.item;

import com.fredtargaryen.floocraft.FloocraftBase;
import com.fredtargaryen.floocraft.block.FlooTorchBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

public class ItemFlooTorch extends Item {
    public ItemFlooTorch() {
        super(new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS));
    }

    @Override
    @Nonnull
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if(!level.isClientSide) {
            Direction side = context.getClickedFace();
            if (side == Direction.DOWN) {
                return InteractionResult.PASS;
            } else {
                BlockPos pos = context.getClickedPos();
                BlockState blockPlacedOn = level.getBlockState(pos);
                Player player = context.getPlayer();
                ItemStack stack = context.getItemInHand();
                if (player != null && player.mayUseItemAt(pos, side, stack)) {
                    if (blockPlacedOn.isFaceSturdy(level, pos, side)) {
                        level.setBlock(pos.relative(side),
                                FloocraftBase.BLOCK_FLOO_TORCH.get().defaultBlockState().setValue(FlooTorchBlock.FACING_EXCEPT_DOWN, side),
                                3);
                        stack.grow(-1);
                        return InteractionResult.CONSUME;
                    }
                }
            }
        }
        return InteractionResult.PASS;
    }
}
