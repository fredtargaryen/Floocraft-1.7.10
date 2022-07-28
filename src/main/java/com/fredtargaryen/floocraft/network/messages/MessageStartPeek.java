package com.fredtargaryen.floocraft.network.messages;

import com.fredtargaryen.floocraft.FloocraftBase;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class MessageStartPeek {
    public UUID peekerUUID;

    public static void handle(MessageStartPeek message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> FloocraftBase.proxy.onMessage(message));
        ctx.get().setPacketHandled(true);
    }

    public MessageStartPeek(UUID peekerUUID) {
        this.peekerUUID = peekerUUID;
    }

    /**
     * Effectively fromBytes from 1.12.2
     */
    public MessageStartPeek(ByteBuf buf) {
        this.peekerUUID = new UUID(buf.readLong(), buf.readLong());
    }

    public void toBytes(ByteBuf buf) {
        buf.writeLong(this.peekerUUID.getMostSignificantBits());
        buf.writeLong(this.peekerUUID.getLeastSignificantBits());
    }
}
