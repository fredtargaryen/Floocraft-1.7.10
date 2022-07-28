package com.fredtargaryen.floocraft.block;

import com.fredtargaryen.floocraft.DataReference;
import com.fredtargaryen.floocraft.FloocraftBase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.AGE_15;

public class FlooFlamesIdle extends FlooFlamesBase {
    private static final VoxelShape SMALLBOX = Shapes.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);

	public FlooFlamesIdle(int lightLevel) { super(lightLevel); }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) { return SMALLBOX; }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, Random rand) {
        if(level.getNearestPlayer((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, (double) DataReference.FLOO_FIRE_DETECTION_RANGE, false) != null)
        {
            Block fireBlock = level.getBlockState(pos.below()).is(BlockTags.SOUL_FIRE_BASE_BLOCKS) ? FloocraftBase.MAGENTA_FLAMES_BUSY.get() : FloocraftBase.GREEN_FLAMES_BUSY.get();
            level.setBlock(pos, fireBlock.defaultBlockState().setValue(AGE_15, state.getValue(AGE_15)), 3);
        }
        super.tick(state, level, pos, rand);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    protected void doSmokeParticles(BlockState stateIn, Level level, BlockPos pos, Random rand) {
        for (int i = 0; i < 3; ++i) {
            double d0 = (double)pos.getX() + rand.nextDouble();
            double d1 = (double)pos.getY() + rand.nextDouble() * 0.5D + 0.5D;
            double d2 = (double)pos.getZ() + rand.nextDouble();
            level.addParticle(ParticleTypes.LARGE_SMOKE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
        }
    }
}
