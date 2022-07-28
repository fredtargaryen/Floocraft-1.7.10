package com.fredtargaryen.floocraft.block;

import com.fredtargaryen.floocraft.config.CommonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

public class FlooFlamesTemp extends FlooFlamesBusy {
    public FlooFlamesTemp(int lightLevel) { super(lightLevel); }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, Random rand) {
        level.setBlock(pos, FlooFlamesBase.getFireBlockToPlace(level, pos).defaultBlockState(), 3);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entityIn) { }

    /**
     * When placed, when should the next tick start?
     * @return the number of ticks to wait before the first tick
     */
    @Override
    protected int getTimeToFirstTick() {
        return CommonConfig.FLOO_SAFETY_TIME.get();
    }
}