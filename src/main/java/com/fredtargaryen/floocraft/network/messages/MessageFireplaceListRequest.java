package com.fredtargaryen.floocraft.network.messages;

import com.fredtargaryen.floocraft.network.FloocraftWorldData;
import com.fredtargaryen.floocraft.network.MessageHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class MessageFireplaceListRequest {
	public static void handle(MessageFireplaceListRequest message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Level level = ctx.get().getSender().level;
			MessageFireplaceList mfl = FloocraftWorldData.forLevel(level).assembleNewFireplaceList(level);
			MessageHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> ctx.get().getSender()), mfl);
		});
		ctx.get().setPacketHandled(true);
	}

	public MessageFireplaceListRequest () {}

	/**
	 * Effectively fromBytes from 1.12.2
	 */
	public MessageFireplaceListRequest(ByteBuf buf) {}

	public void toBytes(ByteBuf buf) {}
}
