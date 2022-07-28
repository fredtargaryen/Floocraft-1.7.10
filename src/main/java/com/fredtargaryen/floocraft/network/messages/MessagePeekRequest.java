package com.fredtargaryen.floocraft.network.messages;

import com.fredtargaryen.floocraft.DataReference;
import com.fredtargaryen.floocraft.FloocraftBase;
import com.fredtargaryen.floocraft.block.FlooFlamesBase;
import com.fredtargaryen.floocraft.entity.PeekerEntity;
import com.fredtargaryen.floocraft.network.FloocraftWorldData;
import com.fredtargaryen.floocraft.network.MessageHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.nio.charset.Charset;
import java.util.function.Supplier;

public class MessagePeekRequest {
    public int initX, initY, initZ;
    public String dest;
    private static final Charset defaultCharset = Charset.defaultCharset();

	public static void handle(MessagePeekRequest message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            int initX = message.initX;
            int initY = message.initY;
            int initZ = message.initZ;
            ServerPlayer player = ctx.get().getSender();
            Level level = player.getLevel();
            BlockState initState = level.getBlockState(new BlockPos(initX, initY, initZ));
            Block initBlock = level.getBlockState(new BlockPos(initX, initY, initZ)).getBlock();
            int[] destCoords = FloocraftWorldData.forLevel(level).placeList.get(message.dest);
            //Stop everything if the destination has the same coordinates as where the player is
            if(!(destCoords[0] == message.initX && destCoords[1] == message.initY && destCoords[2] == message.initZ)) {
                Block greenBusy = FloocraftBase.GREEN_FLAMES_BUSY.get();
                int destX = destCoords[0];
                int destY = destCoords[1];
                int destZ = destCoords[2];
                //Checks whether the player is currently in busy or idle green flames
                if (initState.is(DataReference.VALID_DEPARTURE_BLOCKS_TAG)) {
                    BlockPos dest = new BlockPos(destX, destY, destZ);
                    BlockState destState = level.getBlockState(dest);
                    Block destBlock = level.getBlockState(dest).getBlock();
                    //Checks whether the destination is fire
                    if (destState.is(DataReference.VALID_ARRIVAL_BLOCKS_TAG)) {
                        Direction direction = ((FlooFlamesBase) FloocraftBase.GREEN_FLAMES_TEMP.get()).isInFireplace(level, dest);
                        if (direction != null) {
                            Direction.Axis axis = direction.getAxis();
                            if (axis == Direction.Axis.X || axis == Direction.Axis.Z) {
                                //Create peeker
                                PeekerEntity peeker = new PeekerEntity(level);
                                peeker.setPeekerData(player, dest, direction);
                                level.addFreshEntity(peeker);
                                //Create message
                                MessageStartPeek msp = new MessageStartPeek(peeker.getUUID());
                                MessageHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), msp);
                            }
                        }
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public MessagePeekRequest() {}

    /**
     * Effectively fromBytes from 1.12.2
     */
	public MessagePeekRequest(ByteBuf buf) {
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