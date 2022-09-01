package com.fredtargaryen.floocraft.entity;

import com.fredtargaryen.floocraft.FloocraftBase;
import com.fredtargaryen.floocraft.network.MessageHandler;
import com.fredtargaryen.floocraft.network.messages.MessageEndPeek;
import com.fredtargaryen.floocraft.network.messages.MessagePlayerIDRequest;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;

public class PeekerEntity extends Entity {
    private UUID playerUUID;
    //For the client side
    private ResourceLocation texture;
    private boolean sentRequest;

    public PeekerEntity(Level level) {
        super(FloocraftBase.PEEKER_TYPE.get(), level);
        MinecraftForge.EVENT_BUS.register(this);
        this.texture = null;
        this.sentRequest = false;
    }

    public UUID getPlayerUUID() {
        return this.playerUUID;
    }

    public void setPeekerData(Player player, BlockPos spawnPos, Direction direction) {
        BlockPos landPos = spawnPos.relative(direction);
        float x = landPos.getX() + 0.5F;
        float y = landPos.getY();
        float z = landPos.getZ() + 0.5F;
        switch (direction) {
            case NORTH:
                z += 0.5F;
                break;
            case SOUTH:
                z -= 0.5F;
                break;
            case EAST:
                x -= 0.5F;
                break;
            default:
                x += 0.5;
                break;
        }
        this.playerUUID = player.getUUID();
        this.absMoveTo(x, y, z, this.getYawFromDirection(direction), 0.0F);
    }

    public void setPlayerUUID(UUID uuid) {
        this.playerUUID = uuid;
    }

    private float getYawFromDirection(Direction ef) {
        return switch (ef) {
            case NORTH -> 180.0F;
            case EAST -> -90.0F;
            case SOUTH -> 0.0F;
            default -> 90.0F;
        };
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @Override
    protected void defineSynchedData() {
    }

    /**
     * Gets called every tick from main Entity class
     * Kills itself if the player it is tied to is dead or not connected
     */
    @Override
    public void tick() {
        if (!this.level.isClientSide) {
            Player player = this.level.getPlayerByUUID(this.playerUUID);
            if (player == null || !player.isAlive()) {
                this.remove(RemovalReason.DISCARDED);
            }
        }
    }

    public ResourceLocation getTexture() {
        if (this.playerUUID == null) {
            if (this.level.isClientSide) {
                //Client; needs to send one request message. PlayerUUID will be set by MessagePlayerID
                if (!this.sentRequest) {
                    MessagePlayerIDRequest mpidr = new MessagePlayerIDRequest();
                    mpidr.peekerUUID = this.getUUID();
                    MessageHandler.INSTANCE.sendToServer(mpidr);
                }
            }
            return null;
        }
        if (this.texture == null && this.level.isClientSide) {
            AbstractClientPlayer acp = (AbstractClientPlayer) this.level.getPlayerByUUID(this.playerUUID);
            // If we can't find the player with this UUID they probably don't exist now, so we shouldn't render their peeker
            this.texture = acp == null ? null : acp.getSkinTextureLocation();
        }
        return this.texture;
    }

    @SubscribeEvent
    public void onHurt(LivingHurtEvent lhe) {
        if (this.level.isClientSide && this.playerUUID != null) {
            UUID hurtEntityUUID = lhe.getEntity().getUUID();
            if(hurtEntityUUID.equals(this.playerUUID)) {
                MessageEndPeek mep = new MessageEndPeek();
                mep.peekerUUID = this.getUUID();
                MessageHandler.INSTANCE.sendToServer(mep);
            }
        }
    }

    @SubscribeEvent
    public void onDeath(LivingDeathEvent lde) {
        if (this.level.isClientSide && this.playerUUID != null && lde.getEntity().getUUID().equals(this.playerUUID)) {
            MessageEndPeek mep = new MessageEndPeek();
            mep.peekerUUID = this.getUUID();
            MessageHandler.INSTANCE.sendToServer(mep);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        this.playerUUID = new UUID(compound.getLong("msb"), compound.getLong("lsb"));
        this.setRot(compound.getFloat("yaw"), 0.0F);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putLong("msb", this.playerUUID.getMostSignificantBits());
        compound.putLong("lsb", this.playerUUID.getLeastSignificantBits());
        compound.putFloat("yaw", this.getYRot());
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}