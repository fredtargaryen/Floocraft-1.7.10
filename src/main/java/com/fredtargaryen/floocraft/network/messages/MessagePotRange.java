package com.fredtargaryen.floocraft.network.messages;

import com.fredtargaryen.floocraft.FloocraftBase;
import com.fredtargaryen.floocraft.blockentity.FloowerPotBlockEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MessagePotRange {
    public char range;
    public int amount;
    public BlockPos pos;

    public static void handle(MessagePotRange message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerLevel sw = ctx.get().getSender().getLevel();
            BlockEntity te = sw.getBlockEntity(message.pos);
            if(te.getType() == FloocraftBase.POT_TYPE.get())
                ((FloowerPotBlockEntity) te).adjustPotRange(message.range, message.amount);
        });
        ctx.get().setPacketHandled(true);
    }

    public MessagePotRange() { }

    /**
     * Effectively fromBytes from 1.12.2
     */
    public MessagePotRange(ByteBuf buf) {
        this.range = buf.readChar();
        this.amount = buf.readInt();
        this.pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
    }

    public void toBytes(ByteBuf buf) {
        buf.writeChar(this.range);
        buf.writeInt(this.amount);
        buf.writeInt(this.pos.getX());
        buf.writeInt(this.pos.getY());
        buf.writeInt(this.pos.getZ());
    }
}
