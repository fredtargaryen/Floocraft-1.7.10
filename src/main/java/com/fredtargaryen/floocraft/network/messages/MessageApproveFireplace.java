package com.fredtargaryen.floocraft.network.messages;

import com.fredtargaryen.floocraft.blockentity.FlooSignBlockEntity;
import com.fredtargaryen.floocraft.network.FloocraftWorldData;
import com.fredtargaryen.floocraft.network.MessageHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.nio.charset.Charset;
import java.util.function.Supplier;

public class MessageApproveFireplace {
    public int x, y, z;
    public boolean attemptingToConnect;
    public String[] name;
    private static final Charset defaultCharset = Charset.defaultCharset();

    public static void handle(MessageApproveFireplace message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer spe = ctx.get().getSender();
            ServerLevel w = spe.getLevel();
            FlooSignBlockEntity fte = (FlooSignBlockEntity) (w.getBlockEntity(new BlockPos(message.x, message.y, message.z)));
            if(fte != null) {
                if (message.attemptingToConnect) {
                    boolean approved = !FloocraftWorldData.forLevel(w).placeList.containsKey(FlooSignBlockEntity.getSignTextAsLine(message.name));
                    MessageApproval ma = new MessageApproval(approved);
                    MessageHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> spe), ma);
                    if (approved) {
                        fte.setString(0, message.name[0]);
                        fte.setString(1, message.name[1]);
                        fte.setString(2, message.name[2]);
                        fte.setString(3, message.name[3]);
                        fte.addLocation();
                        fte.setConnected(true);
                    }
                    else
                    {
                        fte.setConnected(false);
                    }
                } else {
                    MessageApproval ma = new MessageApproval(true);
                    MessageHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> spe), ma);
                    fte.setString(0, message.name[0]);
                    fte.setString(1, message.name[1]);
                    fte.setString(2, message.name[2]);
                    fte.setString(3, message.name[3]);
                    fte.setConnected(false);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public MessageApproveFireplace(){}

    /**
     * Effectively fromBytes from 1.12.2
     */
    public MessageApproveFireplace(ByteBuf buf) {
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
        this.attemptingToConnect = buf.readBoolean();
        this.name = new String[4];
        for(int i = 0; i < 4; ++i) {
            this.name[i] = buf.readBytes(buf.readInt()).toString(defaultCharset);
        }
    }

    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.x);
        buf.writeInt(this.y);
        buf.writeInt(this.z);
        buf.writeBoolean(this.attemptingToConnect);
        for(String s : this.name) {
            buf.writeInt(s.length());
            buf.writeBytes(s.getBytes());
        }
    }
}
