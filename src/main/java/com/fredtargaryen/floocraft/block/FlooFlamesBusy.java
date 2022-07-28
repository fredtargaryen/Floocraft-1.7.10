package com.fredtargaryen.floocraft.block;

import com.fredtargaryen.floocraft.DataReference;
import com.fredtargaryen.floocraft.FloocraftBase;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.AGE_15;

public class FlooFlamesBusy extends FlooFlamesBase {
    public FlooFlamesBusy(int lightLevel) { super(lightLevel); }

	@Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, Random rand) {
        if(level.getNearestPlayer((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, (double) DataReference.FLOO_FIRE_DETECTION_RANGE, false) == null) {
            Block fireBlock = level.getBlockState(pos.below()).is(BlockTags.SOUL_FIRE_BASE_BLOCKS) ? FloocraftBase.MAGENTA_FLAMES_IDLE.get() : FloocraftBase.GREEN_FLAMES_IDLE.get();
            level.setBlock(pos, fireBlock.defaultBlockState().setValue(AGE_15, state.getValue(AGE_15)), 3);
        }
        super.tick(state, level, pos, rand);
    }
}