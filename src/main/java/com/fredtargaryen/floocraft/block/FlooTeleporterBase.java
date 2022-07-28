package com.fredtargaryen.floocraft.block;

import com.fredtargaryen.floocraft.FloocraftBase;
import com.fredtargaryen.floocraft.client.gui.TeleportScreen;
import com.fredtargaryen.floocraft.config.CommonConfig;
import com.fredtargaryen.floocraft.network.FloocraftWorldData;
import com.fredtargaryen.floocraft.network.messages.MessageFireplaceList;
import com.fredtargaryen.floocraft.proxy.ClientProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;

public abstract class FlooTeleporterBase extends Block {

    public FlooTeleporterBase(Block.Properties properties)
    {
        super(properties.noCollission());
    }

    public static boolean shouldPlaceSoulBlockHere(Level l, BlockPos here)
    {
        return l.getBlockState(here.below()).is(BlockTags.SOUL_FIRE_BASE_BLOCKS);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(BlockStateProperties.AGE_15);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entityIn) {
        if (level.isClientSide) {
            if (entityIn == Minecraft.getInstance().player) {
                this.openTeleportGui(pos);
            }
        }
        else {
            //Server side. Players are dealt with on the client because of the GUI
            if(!(entityIn instanceof Player)) {
                boolean teleport = false;
                //Set teleport destination one block outside the fire, instead of in the fire
                boolean landOutside = false;

                if(CommonConfig.ITEMS_TELEPORT.get() && entityIn instanceof ItemEntity) {
                    teleport = true;
                    landOutside = true;
                }
                else if(    (CommonConfig.VILLAGERS_TELEPORT.get() && entityIn instanceof Villager)
                          ||(CommonConfig.MISC_MOBS_TELEPORT.get() && entityIn instanceof LivingEntity)) {
                    teleport = level.random.nextFloat() < 0.2;
                }
                if(teleport) {
                    //Get list of locations and whether they are available
                    FloocraftWorldData fwd = FloocraftWorldData.forLevel(level);
                    MessageFireplaceList mfl = fwd.assembleNewFireplaceList(level);
                    ArrayList<String> possibleLocations = new ArrayList<>();
                    //Add the enabled locations to possibleLocations
                    for (int i = 0; i < mfl.places.length; ++i) {
                        if (mfl.enabledList[i]) possibleLocations.add((String) mfl.places[i]);
                    }
                    if(!possibleLocations.isEmpty()) {
                        //Pick a random location from possibleLocations
                        int destNo = level.random.nextInt(possibleLocations.size());
                        //Teleport to that location
                        String destName = possibleLocations.get(destNo);
                        //Get location coords
                        int[] coords = fwd.placeList.get(destName);
                        BlockPos dest = new BlockPos(coords[0], coords[1], coords[2]);
                        //Set a temporary Floo fire here
                        BlockState blockOnTop = level.getBlockState(dest);
                        if(blockOnTop.is(BlockTags.FIRE))
                        {
                            if(level.getBlockState(dest.below()).is(BlockTags.SOUL_FIRE_BASE_BLOCKS))
                            {
                                level.setBlock(dest, FloocraftBase.MAGENTA_FLAMES_TEMP.get().defaultBlockState(), 3);
                            }
                            else
                            {
                                level.setBlock(dest, FloocraftBase.GREEN_FLAMES_TEMP.get().defaultBlockState(), 3);
                            }
                        }
                        // TODO try absMoveTo
                        if (landOutside) {
                            dest = dest.relative(((FlooFlamesBase) FloocraftBase.GREEN_FLAMES_TEMP.get()).isInFireplace(level, dest));
                            entityIn.moveTo(
                                    dest.getX(), coords[1], dest.getZ(), entityIn.getYRot(), entityIn.getXRot());
                        } else {
                            entityIn.moveTo(
                                    coords[0], coords[1], coords[2], entityIn.getYRot(), entityIn.getXRot());
                        }
                    }
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void openTeleportGui(BlockPos pos) {
        ClientProxy proxy = (ClientProxy) FloocraftBase.proxy;
        if (Minecraft.getInstance().screen == null && !proxy.overrideTicker.isOverriding()) {
            Minecraft.getInstance().setScreen(new TeleportScreen(pos.getX(), pos.getY(), pos.getZ()));
            proxy.overrideTicker.start();
        }
    }

    //FOR ALLOWING COLLISIONS TO HAPPEN
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }
}