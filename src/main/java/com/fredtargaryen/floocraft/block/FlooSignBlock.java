package com.fredtargaryen.floocraft.block;

import com.fredtargaryen.floocraft.FloocraftBase;
import com.fredtargaryen.floocraft.blockentity.FlooSignBlockEntity;
import com.fredtargaryen.floocraft.network.FloocraftWorldData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.Material;

import javax.annotation.Nonnull;

public class FlooSignBlock extends WallSignBlock implements EntityBlock {
	public FlooSignBlock() {
		super(BlockBehaviour.Properties.of(Material.WOOD)
                .noCollission()
                .strength(1f)
                .sound(SoundType.WOOD), WoodType.OAK);
		this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, false));
	}

    @Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        try {
            return new FlooSignBlockEntity();
        }
        catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    @Nonnull
    public Item asItem() { return FloocraftBase.ITEM_FLOO_SIGN.get(); }

    @Override
    @Nonnull
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if(!level.isClientSide) {
            FlooSignBlockEntity tef = (FlooSignBlockEntity) level.getBlockEntity(pos);
            if (tef != null && tef.getConnected()) {
                //Finds the fireplace position from the sign position and rotation
                //The block below the block at the top of the fireplace
                BlockPos locationPos = pos.relative(state.getValue(FACING).getOpposite());
                FloocraftWorldData.forLevel(level).removeLocation(locationPos.getX(), tef.getY(), locationPos.getZ());
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    /**
     * The type of render function called. MODEL for mixed BlockEntityRenderer and static model, MODELBLOCK_ANIMATED for BER-only,
     * LIQUID for vanilla liquids, INVISIBLE to skip all rendering
     * @deprecated call via IBlockState#getRenderShape() whenever possible. Implementing/overriding is fine.
     */
    @Override
    public RenderShape getRenderShape(BlockState state)
    {
        return RenderShape.MODEL;
    }
}