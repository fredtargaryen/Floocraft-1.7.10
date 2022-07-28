package com.fredtargaryen.floocraft.network.messages;

import com.fredtargaryen.floocraft.FloocraftBase;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageApproval {
	public boolean answer;

	public static void handle(MessageApproval message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> FloocraftBase.proxy.onMessage(message));
		ctx.get().setPacketHandled(true);
	}

	public MessageApproval(boolean answer) {
		this.answer = answer;
	}

	/**
	 * Effectively fromBytes from 1.12.2
	 */
	public MessageApproval(ByteBuf buf) {
		this.answer = buf.readBoolean();
	}

	public void toBytes(ByteBuf buf)
	{
		buf.writeBoolean(this.answer);
	}
}
