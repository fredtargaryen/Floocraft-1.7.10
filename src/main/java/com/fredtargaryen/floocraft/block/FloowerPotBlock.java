package com.fredtargaryen.floocraft.block;

import com.fredtargaryen.floocraft.FloocraftBase;
import com.fredtargaryen.floocraft.blockentity.FloowerPotBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.TickPriority;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nonnull;
import java.util.Random;

public class FloowerPotBlock extends Block implements EntityBlock {
    private static final VoxelShape POTBOX = Shapes.box(5.0D, 0.0D, 5.0D, 11.0D, 6.0D, 11.0D);

    public FloowerPotBlock()
    {
        super(Block.Properties.of(Material.DECORATION).strength(0F));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return POTBOX;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new FloowerPotBlockEntity(pos, state);
    }

    /**
     * Called upon block activation (right click on the block.)
     */
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null || player.isDiscrete())
        {
            return InteractionResult.FAIL;
        }
        if(!level.isClientSide)
            NetworkHooks.openGui((ServerPlayer) player, (MenuProvider) blockEntity, blockEntity.getBlockPos());
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean isMoving) {
        this.dropPowder(level, pos);
        super.onRemove(state, level, pos, newState, isMoving);
    }

    private void dropPowder(Level level, BlockPos pos){
        Random rand = new Random();

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity != null && blockEntity.getType() == FloocraftBase.POT_TYPE.get()) {
            Container container = (Container) blockEntity;

            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack item = container.getItem(i);

                if (!item.isEmpty() && item.getCount() > 0) {
                    float rx = rand.nextFloat() * 0.8F + 0.1F;
                    float ry = rand.nextFloat() * 0.8F + 0.1F;
                    float rz = rand.nextFloat() * 0.8F + 0.1F;

                    ItemEntity itemEntity = new ItemEntity(level,
                            pos.getX() + rx, pos.getY() + ry, pos.getZ() + rz,
                            new ItemStack(item.getItem(), item.getCount(), item.getTag()));

                    if (item.hasTag()) {
                        itemEntity.getItem().setTag(item.getTag().copy());
                    }

                    float factor = 0.05F;
                    // TODO Check this works ok
                    itemEntity.setDeltaMovement(
                            rand.nextGaussian() * factor,
                            rand.nextGaussian() * factor + 0.2F,
                            rand.nextGaussian() * factor
                    );
                    level.addFreshEntity(itemEntity);
                    item.setCount(0);
                }
            }
        }
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {
        return (!level.isClientSide && type == FloocraftBase.POT_TYPE.get()) ? FloowerPotBlockEntity::tick : null;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean b) {
        level.scheduleTick(pos, this, 40, TickPriority.EXTREMELY_LOW);
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

    /**
     * Called when a neighboring block changes.
     */
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block blockIn, BlockPos fromPos, boolean p_220069_6_) {
        if (!this.canSurvive(state, level, pos)) {
            level.removeBlock(pos, true); // TODO Check if this should be false instead
        }
    }

    @Override
    public boolean isSignalSource(BlockState state) { return true; }

    @Override
    public int getSignal(BlockState state, BlockGetter getter, BlockPos pos, Direction side) {
        BlockEntity be = getter.getBlockEntity(pos);
        if(be instanceof FloowerPotBlockEntity)
        {
            FloowerPotBlockEntity fpbe = (FloowerPotBlockEntity) be;
            int flooCount = fpbe.getItem(0).getCount();
            if(flooCount == 64)
            {
                return 0;
            }
            else
            {
                return (int) ((64 - flooCount) * 14 / 64f + 1);
            }
        }
        return 0;
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter getter, BlockPos pos, Direction side) {
        return 0;
    }
}