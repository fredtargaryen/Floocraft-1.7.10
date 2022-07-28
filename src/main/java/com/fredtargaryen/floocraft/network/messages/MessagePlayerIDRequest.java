package com.fredtargaryen.floocraft.network.messages;

import com.fredtargaryen.floocraft.entity.PeekerEntity;
import com.fredtargaryen.floocraft.network.MessageHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.UUID;
import java.util.function.Supplier;

public class MessagePlayerIDRequest {
    public UUID peekerUUID;

    public static void handle(MessagePlayerIDRequest message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer spe = ctx.get().getSender();
            PeekerEntity pe = (PeekerEntity) spe.getLevel().getEntity(message.peekerUUID);
            MessagePlayerID mpID = new MessagePlayerID(message.peekerUUID, pe.getPlayerUUID());
            MessageHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> spe), mpID);
        });
        ctx.get().setPacketHandled(true);
    }

    public MessagePlayerIDRequest() {}

    /**
     * Effectively fromBytes from 1.12.2
     */
    public MessagePlayerIDRequest(ByteBuf buf) {
        this.peekerUUID = new UUID(buf.readLong(), buf.readLong());
    }

    public void toBytes(ByteBuf buf) {
        buf.writeLong(this.peekerUUID.getMostSignificantBits());
        buf.writeLong(this.peekerUUID.getLeastSignificantBits());
    }
}
