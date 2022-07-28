package com.fredtargaryen.floocraft.blockentity;

import com.fredtargaryen.floocraft.FloocraftBase;
import com.fredtargaryen.floocraft.block.FlooFlamesBase;
import com.fredtargaryen.floocraft.block.FlooSignBlock;
import com.fredtargaryen.floocraft.network.FloocraftWorldData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Function;

public class FlooSignBlockEntity extends BlockEntity {

    /////////////
    //SIGN INFO//
    /////////////
    public final String[] signText = new String[]{"", "", "", ""};
    private int lineBeingEdited = -1;
	private Player writer;
    private FormattedCharSequence[] renderMessages;
    private boolean renderMessagedFiltered;
    private final Component[] messages = new Component[]{TextComponent.EMPTY, TextComponent.EMPTY, TextComponent.EMPTY, TextComponent.EMPTY};
    private final Component[] filteredMessages = new Component[]{TextComponent.EMPTY, TextComponent.EMPTY, TextComponent.EMPTY, TextComponent.EMPTY};
    private static final String[] RAW_TEXT_FIELD_NAMES = new String[]{"Text1", "Text2", "Text3", "Text4"};
    private static final String[] FILTERED_TEXT_FIELD_NAMES = new String[]{"FilteredText1", "FilteredText2", "FilteredText3", "FilteredText4"};
    private DyeColor color = DyeColor.BLACK;
    private boolean hasGlowingText;
    private boolean isEditable = true;
    @Nullable
    private UUID playerWhoMayEdit;

    public FlooSignBlockEntity(BlockPos pos, BlockState state) {
        super(FloocraftBase.FIREPLACE_TYPE.get(), pos, state);
    }

    //region Copy pasting from SignBlockEntity
    public FormattedCharSequence[] getRenderMessages(boolean p_155718_, Function<Component, FormattedCharSequence> p_155719_) {
        if (this.renderMessages == null || this.renderMessagedFiltered != p_155718_) {
            this.renderMessagedFiltered = p_155718_;
            this.renderMessages = new FormattedCharSequence[4];

            for(int i = 0; i < 4; ++i) {
                this.renderMessages[i] = p_155719_.apply(this.getMessage(i, p_155718_));
            }
        }

        return this.renderMessages;
    }

    public Component getMessage(int p_155707_, boolean p_155708_) {
        return this.getMessages(p_155708_)[p_155707_];
    }

    private Component[] getMessages(boolean p_155725_) {
        return p_155725_ ? this.filteredMessages : this.messages;
    }

    public void setMessage(int index, Component component) {
        this.setMessage(index, component, component);
    }

    public void setMessage(int index, Component component, Component filteredComponent) {
        this.messages[index] = component;
        this.filteredMessages[index] = filteredComponent;
        this.renderMessages = null;
    }

    public boolean isEditable() {
        return this.isEditable;
    }

    public void setEditable(boolean p_59747_) {
        this.isEditable = p_59747_;
        if (!p_59747_) {
            this.playerWhoMayEdit = null;
        }

    }
//endregion
    @Override
    @Nonnull
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("Connected",this.isConnected);
        tag.putInt("Y", this.y);

        for(int i = 0; i < 4; ++i) {
            Component component = this.messages[i];
            String s = Component.Serializer.toJson(component);
            tag.putString(RAW_TEXT_FIELD_NAMES[i], s);
            Component component1 = this.filteredMessages[i];
            if (!component1.equals(component)) {
                tag.putString(FILTERED_TEXT_FIELD_NAMES[i], Component.Serializer.toJson(component1));
            }
        }

        tag.putString("Color", this.color.getName());
        tag.putBoolean("GlowingText", this.hasGlowingText);
    }

    public void load(CompoundTag tag) {
        super.load(tag);
        this.isConnected = tag.getBoolean("Connected");
        this.y = tag.getInt("Y");
        for(int i = 0; i < 4; ++i) this.setString(i, tag.getString(String.valueOf(i)));
    }

    public static String getSignTextAsLine(String[] signText) {
        return signText[0] + " " + signText[1] + " " + signText[2] + " " + signText[3];
    }

    public String getString(int index) {
        return(signText[index]);
    }

    public void setString(int index, String s) {
        this.signText[index] = s;
    }

    @Nonnull
    public Player getPlayer()
    {
        return this.writer;
    }

    public void setPlayer(@Nonnull Player player) {
        if(this.writer == null)
        {
            this.writer = player;
        }
    }

    public int getLineBeingEdited() { return this.lineBeingEdited; }

    public void setLineBeingEdited(int index) {
        this.lineBeingEdited = index;
    }

    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public CompoundTag getUpdateTag() {
        CompoundTag myTag = new CompoundTag();
        this.saveAdditional(myTag);
        return myTag;
    }

    /**
     * Called when you receive a BlockEntityData packet for the location this
     * BlockEntity is currently in. On the client, the NetworkManager will always
     * be the remote server. On the server, it will be whomever is responsible for
     * sending the packet.
     *
     * @param net The NetworkManager the packet originated from
     * @param pkt The data packet
     */
    @Override
    @OnlyIn(Dist.CLIENT)
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.load(pkt.getTag());
    }

    //////////////////
    //FIREPLACE INFO//
    //////////////////
    private boolean isConnected;
    private int y;

    public void addLocation() {
        if(!this.level.isClientSide) {
            // We are on the server side.
            BlockPos locationPos = iterateDownFromSign(this.level, this.worldPosition);
            this.y = locationPos.getY();
            FloocraftWorldData.forLevel(this.level).addLocation(getSignTextAsLine(this.signText), locationPos);
            this.setChanged();
        }
    }

    /**
     * Gets the position of the block which, according to fireplace construction rules, forms the bottom of the fireplace.
     * Fireplaces only permit air, fire and green fire blocks inside them.
     */
    private static BlockPos iterateDownFromSign(Level level, BlockPos pos)
    {
        //The block below the block at the top of the fireplace
        pos = pos.relative(level.getBlockState(pos).getValue(FlooSignBlock.FACING).getOpposite(), 1).relative(Direction.DOWN, 1);
        BlockState currentBlockState = level.getBlockState(pos);
        while((currentBlockState.isAir() || currentBlockState.is(BlockTags.FIRE) || currentBlockState.getBlock() instanceof FlooFlamesBase) && pos.getY() > -1)
        {
            pos = pos.relative(Direction.DOWN, 1);
            currentBlockState = level.getBlockState(pos);
        }
        return pos.relative(Direction.UP, 1);
    }

    public int getY(){return this.y;}

    public void setY(int y){this.y = y;}

    public boolean getConnected() {
        return this.isConnected;
    }

    public void setConnected(boolean b)
    {
        this.isConnected = b;
        this.setChanged();
    }
}
