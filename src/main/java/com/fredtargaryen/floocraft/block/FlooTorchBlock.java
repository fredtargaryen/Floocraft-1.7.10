package com.fredtargaryen.floocraft.block;

import com.fredtargaryen.floocraft.FloocraftBase;
import com.fredtargaryen.floocraft.network.MessageHandler;
import com.fredtargaryen.floocraft.network.messages.MessageFlooTorchTeleport;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.Random;

public class FlooTorchBlock extends Block {
    private static final VoxelShape NORTH_SHAPE = Shapes.box(6.0D, 3.0D, 12.0D, 10.0D, 13.0D, 16.0D);
    private static final VoxelShape SOUTH_SHAPE = Shapes.box(6.0D, 3.0D, 0.0D, 10.0D, 13.0D, 4.0D);
    private static final VoxelShape WEST_SHAPE = Shapes.box(12.0D, 3.0D, 6.0D, 16.0D, 13.0D, 10.0D);
    private static final VoxelShape EAST_SHAPE = Shapes.box(0.0D, 3.0D, 6.0D, 4.0D, 13.0D, 10.0D);
    private static final VoxelShape STANDING_SHAPE = Shapes.box(6.0D, 0.0D, 6.0D, 10.0D, 10.0D, 10.0D);
    public static final DirectionProperty FACING_EXCEPT_DOWN = DirectionProperty.create("facing", direction -> direction != Direction.DOWN);

	public FlooTorchBlock() {
		super(Block.Properties.of(Material.DECORATION)
                .noCollission()
                .strength(0f)
                .lightLevel(state -> 14)
                .sound(SoundType.WOOD));
	}

	@Override
    @Nonnull
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        switch(state.getValue(FACING_EXCEPT_DOWN)) {
            case NORTH:
                return NORTH_SHAPE;
            case SOUTH:
                return SOUTH_SHAPE;
            case WEST:
                return WEST_SHAPE;
            case EAST:
                return EAST_SHAPE;
            default:
                return STANDING_SHAPE;
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING_EXCEPT_DOWN);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, Level level, BlockPos pos, Random rand) {
        double d0 = (double)pos.getX() + 0.5D;
        double d1 = (double)pos.getY() + 0.7D;
        double d2 = (double)pos.getZ() + 0.5D;
        switch(state.getValue(FACING_EXCEPT_DOWN)) {
            case NORTH:
                d1 += 0.145D;
                d2 += 0.25D;
                break;
            case SOUTH:
                d1 += 0.145D;
                d2 -= 0.25D;
                break;
            case WEST:
                d1 += 0.145D;
                d0 += 0.25D;
                break;
            case EAST:
                d1 += 0.145D;
                d0 -= 0.25D;
                break;
        }
        level.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
        Minecraft.getInstance().particleEngine.createParticle(FloocraftBase.GREEN_FLAME.get(), d0, d1, d2, 0.0D, 0.0D, 0.0D);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide) {
            if (entity instanceof Player) {
                //Triggered by a player on the client side.
                MessageFlooTorchTeleport mftt = new MessageFlooTorchTeleport();
                mftt.torchX = pos.getX();
                mftt.torchY = pos.getY();
                mftt.torchZ = pos.getZ();
                MessageHandler.INSTANCE.sendToServer(mftt);
            }
        }
    }

    @Override
    @Nonnull
    public Item asItem() { return FloocraftBase.ITEM_FLOO_TORCH.get(); }
}