package com.fredtargaryen.floocraft.network.messages;

import com.fredtargaryen.floocraft.entity.PeekerEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class MessageEndPeek {
    public UUID peekerUUID;

    public static void handle(MessageEndPeek message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerLevel sw = ctx.get().getSender().getLevel();
            if(message.peekerUUID != null) {
                PeekerEntity pe = (PeekerEntity) sw.getEntity(message.peekerUUID);
                if (pe != null) pe.remove(Entity.RemovalReason.DISCARDED);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public MessageEndPeek(){}

    /**
     * Effectively fromBytes from 1.12.2
     */
    public MessageEndPeek(ByteBuf buf) {
        this.peekerUUID = new UUID(buf.readLong(), buf.readLong());
    }

    public void toBytes(ByteBuf buf) {
        buf.writeLong(this.peekerUUID.getMostSignificantBits());
        buf.writeLong(this.peekerUUID.getLeastSignificantBits());
    }
}
