package com.fredtargaryen.floocraft.block;

import com.fredtargaryen.floocraft.FloocraftBase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.AGE_15;

public abstract class FlooFlamesBase extends FlooTeleporterBase {
    private static final Direction[] HORIZONTALS = new Direction[] { Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST };
    private static final VoxelShape TALLBOX = Shapes.box(0.0D, 0.0D, 0.0D, 16.0D, 32.0D, 16.0D);

    FlooFlamesBase(int lightLevel) { super(Properties.of(Material.FIRE)
            .lightLevel(state -> lightLevel)
            .isViewBlocking((state, getter, pos) -> false)); }

    public static Block getFireBlockToPlace(Level l, BlockPos placeHere)
    {
        return FlooTeleporterBase.shouldPlaceSoulBlockHere(l, placeHere) ? Blocks.SOUL_FIRE : Blocks.FIRE;
    }

    public static Block getFlooFireBlockToPlace(Level l, BlockPos placeHere)
    {
        return FlooTeleporterBase.shouldPlaceSoulBlockHere(l, placeHere) ? FloocraftBase.MAGENTA_FLAMES_BUSY.get() : FloocraftBase.GREEN_FLAMES_BUSY.get();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) { return TALLBOX; }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean b) {
        if (isInFireplace(level, pos) != null) {
            level.scheduleTick(pos, this, this.getTimeToFirstTick());
        } else {
            level.setBlock(pos, getFireBlockToPlace(level, pos).defaultBlockState(), 3);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, Random rand) {
        if (isInFireplace(level, pos) == null || level.getBlockState(pos).getValue(AGE_15).equals(0)) {
            level.setBlock(pos, getFireBlockToPlace(level, pos).defaultBlockState(), 3);
        } else {
            level.scheduleTick(pos, this, 30 + rand.nextInt(10));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState stateIn, Level level, BlockPos pos, Random rand) {
        if (rand.nextInt(24) == 0) {
            level.playSound(null, pos, SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS, 1.0F + rand.nextFloat(), rand.nextFloat() * 0.7F + 0.3F);
        }
        this.doSmokeParticles(stateIn, level, pos, rand);
    }

    @OnlyIn(Dist.CLIENT)
    protected void doSmokeParticles(BlockState stateIn, Level level, BlockPos pos, Random rand) {
        if(rand.nextInt(8) == 0) {
            double d0 = (double)pos.getX() + rand.nextDouble();
            double d1 = (double)pos.getY() + rand.nextDouble() * 0.5D + 0.5D;
            double d2 = (double)pos.getZ() + rand.nextDouble();
            level.addParticle(ParticleTypes.LARGE_SMOKE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
        }
    }

    /**
     * When placed, when should the next tick start?
     * @return the number of ticks to wait before the first tick
     */
    protected int getTimeToFirstTick() {
        return 30;
    }

    //FIREPLACE VALIDATION CODE STARTS HERE
    private int getTopBlockY(Level l, BlockPos pos) {
        BlockPos newPos = pos.relative(Direction.UP, 1);
        int y = newPos.getY();
        BlockState bs = l.getBlockState(newPos);
        while (bs.isAir() && y < 256) {
            newPos = newPos.relative(Direction.UP, 1);
            y = newPos.getY();
            bs = l.getBlockState(newPos);
        }
        //When y >= 256 you get an air block, so if b is a solid cube y is implicitly < 256
        if (bs.getMaterial().isSolid()) return y;
        return 0;
    }

    private boolean isWallColumn(Level l, BlockPos bottomPos, int topY) {
        boolean valid = true;
        BlockPos newBottomPos = bottomPos;
        while (valid && newBottomPos.getY() < topY) {
            BlockState bs = l.getBlockState(newBottomPos);
            if (bs.getMaterial().isSolid()) {
                newBottomPos = newBottomPos.relative(Direction.UP, 1);
            } else {
                valid = false;
            }
        }
        return valid;
    }

    private List<Direction> getWalls(Level l, BlockPos bottomPos, int topY) {
        List<Direction> walls = new ArrayList<>();
        if (this.isWallColumn(l, bottomPos.relative(Direction.NORTH), topY)) {
            walls.add(Direction.NORTH);
        }
        if (this.isWallColumn(l, bottomPos.relative(Direction.WEST), topY)) {
            walls.add(Direction.WEST);
        }
        if (this.isWallColumn(l, bottomPos.relative(Direction.EAST), topY)) {
            walls.add(Direction.EAST);
        }
        if (this.isWallColumn(l, bottomPos.relative(Direction.SOUTH), topY)) {
            walls.add(Direction.SOUTH);
        }
        return walls;
    }

    private boolean canLoopToCorner(Level l, int x, int y, int z, Direction backWall, Direction oldSideWall, int top) {
        int oldX = x;
        int oldZ = z;
        Direction sideWall = oldSideWall.getOpposite();
        boolean stop = false;
        while (!stop) {
            if (backWall == Direction.NORTH || backWall == Direction.SOUTH) {
                if (sideWall == Direction.WEST) {
                    x--;
                } else {
                    x++;
                }
            } else {
                if (sideWall == Direction.NORTH) {
                    z--;
                } else {
                    z++;
                }
            }
            BlockPos newBottomPos = new BlockPos(x, y, z);
            int newTop = this.getTopBlockY(l, newBottomPos);
            List<Direction> walls = this.getWalls(l, newBottomPos, newTop);
            switch (walls.size()) {
                case 1:
                    if (!walls.contains(backWall)) {
                        return false;
                    } else {
                        if (newTop > top++) {
                            if (!this.isWallColumn(l, new BlockPos(oldX, top, oldZ), newTop)) {
                                return false;
                            }
                        } else if (newTop < top--) {
                            if (!this.isWallColumn(l, new BlockPos(x, newTop, z), top)) {
                                return false;
                            }
                        }
                        oldX = x;
                        top = newTop;
                        oldZ = z;
                    }
                    break;
                case 2:
                    if (walls.contains(backWall) && walls.contains(sideWall)) {
                        stop = true;
                    } else {
                        return false;
                    }
                    break;
                default:
                    return false;
            }
        }
        return true;
    }

    /**
     * Returns the Direction that points out of the fireplace, if the fireplace is valid.
     * Not necessarily the direction of the sign.
     * If the fireplace is invalid, returns null.
     * @return Direction.UP if the fire block is in a corner of a valid fireplace; NORTH, SOUTH, EAST, WEST if the fire
     * is in a valid fireplace but not a corner; null if the fireplace is invalid
     */
    public Direction isInFireplace(Level l, BlockPos pos) {
        if (pos.getY() < 254) {
            int t = this.getTopBlockY(l, pos);
            if (t > 0) {
                List<Direction> walls = this.getWalls(l, pos, t);
                int x = pos.getX();
                int y = pos.getY();
                int z = pos.getZ();
                switch (walls.size()) {
                    case 3:
                        //One-block-long fireplace
                        for(Direction ef : HORIZONTALS) {
                            if(!walls.contains(ef)) return ef;
                        }
                        break;
                    case 2:
                        if ((walls.contains(Direction.NORTH) && (walls.contains(Direction.WEST) || walls.contains(Direction.EAST))
                                || (walls.contains(Direction.SOUTH) && (walls.contains(Direction.WEST) || walls.contains(Direction.EAST))))) {
                            boolean zeroToOne = this.canLoopToCorner(l, x, y, z, walls.get(0), walls.get(1), t);
                            boolean oneToZero = this.canLoopToCorner(l, x, y, z, walls.get(1), walls.get(0), t);
                            if(zeroToOne && oneToZero) {
                                //Fire is in corner of fireplace. Valid fireplace, but can't put a sign on a corner, so
                                //return UP
                                return Direction.UP;
                            }
                            else if(zeroToOne) {
                                //End of a long fireplace
                                //Wall 0 is the back of the fireplace
                                return walls.get(0).getOpposite();
                            }
                            else if(oneToZero) {
                                //End of a long fireplace
                                //Wall 1 is the back of the fireplace
                                return walls.get(1).getOpposite();
                            }
                        }
                        break;
                    case 1:
                        switch (walls.get(0)) {
                            //This will be the back wall. If valid, this is the middle of a long fireplace
                            case NORTH:
                                if      (this.canLoopToCorner(l, x, y, z, Direction.NORTH, Direction.WEST, t)
                                        &&  this.canLoopToCorner(l, x, y, z, Direction.NORTH, Direction.EAST, t))
                                    return Direction.SOUTH;
                            case WEST:
                                if      (this.canLoopToCorner(l, x, y, z, Direction.WEST, Direction.SOUTH, t)
                                        &&  this.canLoopToCorner(l, x, y, z, Direction.WEST, Direction.NORTH, t))
                                    return Direction.EAST;
                            case EAST:
                                if      (this.canLoopToCorner(l, x, y, z, Direction.EAST, Direction.SOUTH, t)
                                        &&  this.canLoopToCorner(l, x, y, z, Direction.EAST, Direction.NORTH, t))
                                    return Direction.WEST;
                            case SOUTH:
                                if      (this.canLoopToCorner(l, x, y, z, Direction.SOUTH, Direction.WEST, t)
                                        &&  this.canLoopToCorner(l, x, y, z, Direction.SOUTH, Direction.EAST, t))
                                    return Direction.NORTH;
                                break;
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        return null;
    }
}