package com.fredtargaryen.floocraft.network.messages;

import com.fredtargaryen.floocraft.FloocraftBase;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class MessagePlayerID {
    public UUID peekerUUID;
    public UUID playerUUID;

    public static void handle(MessagePlayerID message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> FloocraftBase.proxy.setUUIDs(message));
        ctx.get().setPacketHandled(true);
    }

    public MessagePlayerID(UUID peekerUUID, UUID playerUUID) {
        this.peekerUUID = peekerUUID;
        this.playerUUID = playerUUID;
    }

    /**
     * Effectively fromBytes from 1.12.2
     */
	public MessagePlayerID(ByteBuf buf) {
        this.peekerUUID = new UUID(buf.readLong(), buf.readLong());
        this.playerUUID = new UUID(buf.readLong(), buf.readLong());
    }

	public void toBytes(ByteBuf buf) {
        buf.writeLong(this.peekerUUID.getMostSignificantBits());
        buf.writeLong(this.peekerUUID.getLeastSignificantBits());
        buf.writeLong(this.playerUUID.getMostSignificantBits());
        buf.writeLong(this.playerUUID.getLeastSignificantBits());
    }
}