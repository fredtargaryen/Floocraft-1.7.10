package com.fredtargaryen.floocraft.network.messages;

import com.fredtargaryen.floocraft.FloocraftBase;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.nio.charset.Charset;
import java.util.function.Supplier;

public class MessageFireplaceList {
	public Object[] places;
	public boolean[] enabledList;
	private static final Charset defaultCharset = Charset.defaultCharset();

	public static void handle(MessageFireplaceList message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> FloocraftBase.proxy.onMessage(message));
		ctx.get().setPacketHandled(true);
	}

	public MessageFireplaceList() {}

	/**
	 * Effectively fromBytes from 1.12.2
	 */
	public MessageFireplaceList(ByteBuf buf) {
		this.places = new Object[]{};
		this.enabledList = new boolean[]{};
		int y = buf.readInt();
		if(y > 0) {
			this.places = new Object[y];
			this.enabledList = new boolean[y];
			for(int x = 0; x < y; ++x) {
				this.places[x] = buf.readBytes(buf.readInt()).toString(defaultCharset);
				this.enabledList[x] = buf.readBoolean();
			}
		}
	}

	public void toBytes(ByteBuf buf) {
		int y = this.places.length;
		buf.writeInt(y);
		int keyCount = 0;
		for(Object o : this.places) {
			String s = (String) o;
			buf.writeInt(s.length());
	        buf.writeBytes(s.getBytes());
			buf.writeBoolean(this.enabledList[keyCount]);
			++keyCount;
		}
	}
}
