package com.fredtargaryen.floocraft.network.messages;

import com.fredtargaryen.floocraft.DataReference;
import com.fredtargaryen.floocraft.FloocraftBase;
import com.fredtargaryen.floocraft.block.FlooFlamesBase;
import com.fredtargaryen.floocraft.config.CommonConfig;
import com.fredtargaryen.floocraft.network.FloocraftWorldData;
import com.fredtargaryen.floocraft.network.MessageHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.nio.charset.Charset;
import java.util.function.Supplier;

public class MessageTeleportEntity {
	public int initX, initY, initZ;
    public String dest;
    private static final Charset defaultCharset = Charset.defaultCharset();

	public static void handle(MessageTeleportEntity message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
		    //Whether it is permitted for the player to travel to the named destination
            boolean validDest = false;
            ServerPlayer player = ctx.get().getSender();
            Level level = player.level;
            //The coordinates of the destination: [x, y, z]
            int[] destCoords = FloocraftWorldData.forLevel(level).placeList.get(message.dest);
            //Stop everything if the destination has the same coordinates as where the player is
            if(!(destCoords[0] == message.initX && destCoords[1] == message.initY && destCoords[2] == message.initZ))
            {
                BlockPos destBlockPos = new BlockPos(destCoords[0], destCoords[1], destCoords[2]);
                BlockState destState = level.getBlockState(destBlockPos);
                Block destBlock = destState.getBlock();
                //Checks whether the destination has a block that can be arrived in, and is in a valid fireplace
                FlooFlamesBase greenTemp = (FlooFlamesBase) FloocraftBase.GREEN_FLAMES_TEMP.get();
                if (destState.is(DataReference.VALID_ARRIVAL_BLOCKS_TAG)) {
                    validDest = greenTemp.isInFireplace(level, destBlockPos) != null;
                }

                BlockPos initBlockPos = new BlockPos(message.initX, message.initY, message.initZ);
                BlockState initBlockState = level.getBlockState(initBlockPos);
                Block initBlock = initBlockState.getBlock();
                //If destination is valid, checks whether the player is currently in a valid departure block
                if (validDest && (initBlockState.is(DataReference.VALID_DEPARTURE_BLOCKS_TAG))) {
                    //Is the player teleporting from a soul block?
                    boolean initSoul;
                    if(initBlock == FloocraftBase.FLOO_CAMPFIRE.get())
                    {
                        initSoul = false; //No
                    }
                    else if(initBlock == FloocraftBase.FLOO_SOUL_CAMPFIRE.get())
                    {
                        initSoul = true; //Yes
                    }
                    else {
                        //Assume a fire block
                        initSoul = level.getBlockState(initBlockPos.below()).is(BlockTags.SOUL_FIRE_BASE_BLOCKS);
                    }
                    boolean destSoul = level.getBlockState(destBlockPos.below()).is(BlockTags.SOUL_FIRE_BASE_BLOCKS);
                    //Get the fire ready. If it's already a busy or idle Floo fire, leave it as is. Otherwise, set a temp. Floo fire
                    if(!destState.is(DataReference.VALID_DEPARTURE_BLOCKS_TAG)) {
                        level.setBlock(destBlockPos, destSoul ? FloocraftBase.MAGENTA_FLAMES_TEMP.get().defaultBlockState() : greenTemp.defaultBlockState(), 3);
                    }
                    //...then do the teleport...
                    MessageHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new MessageDoGreenFlash(initSoul));
                    if (player.getVehicle() != null) {
                        player.stopRiding();
                    }
                    player.connection.teleport(destCoords[0] + 0.5D, destCoords[1], destCoords[2] + 0.5D, player.getRandom().nextFloat() * 360, player.getXRot());
                    player.fallDistance = 0.0F;
                    //...then update the age of the fire, if configured that way.
                    if(CommonConfig.DEPLETE_FLOO.get()) {
                        int m = initBlockState.getValue(BlockStateProperties.AGE_15);
                        if (m < 2) {
                            if(initBlock == FloocraftBase.FLOO_CAMPFIRE.get()) {
                                level.setBlock(initBlockPos, Blocks.CAMPFIRE.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, initBlockState.getValue(BlockStateProperties.HORIZONTAL_FACING)), 3);
                            }
                            else if(initBlock == FloocraftBase.FLOO_SOUL_CAMPFIRE.get()) {
                                level.setBlock(initBlockPos, Blocks.SOUL_CAMPFIRE.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, initBlockState.getValue(BlockStateProperties.HORIZONTAL_FACING)), 3);
                            }
                            else {
                                level.setBlock(initBlockPos, initSoul ? Blocks.SOUL_FIRE.defaultBlockState() : Blocks.FIRE.defaultBlockState(), 3);
                            }
                        } else {
                            level.setBlock(initBlockPos, initBlockState.setValue(BlockStateProperties.AGE_15, m == 9 ? 9 : m - 1), 3);
                        }
                    }
                }
            }
        });

		ctx.get().setPacketHandled(true);
	}

	public MessageTeleportEntity() {}

    /**
     * Effectively fromBytes from 1.12.2
     */
	public MessageTeleportEntity(ByteBuf buf) {
        this.initX = buf.readInt();
        this.initY = buf.readInt();
        this.initZ = buf.readInt();
        this.dest = buf.readBytes(buf.readInt()).toString(defaultCharset);
	}

	public void toBytes(ByteBuf buf) {
        buf.writeInt(initX);
        buf.writeInt(initY);
        buf.writeInt(initZ);
        buf.writeInt(dest.length());
        buf.writeBytes(dest.getBytes());
	}
}