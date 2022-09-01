package com.fredtargaryen.floocraft.client.gui;

import com.fredtargaryen.floocraft.DataReference;
import com.fredtargaryen.floocraft.blockentity.FloowerPotBlockEntity;
import com.fredtargaryen.floocraft.inventory.container.FloowerPotMenu;
import com.fredtargaryen.floocraft.network.MessageHandler;
import com.fredtargaryen.floocraft.network.messages.MessagePotRange;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FloowerPotScreen extends AbstractContainerScreen<FloowerPotMenu> implements MenuAccess<FloowerPotMenu> {
    private Button hLess;
    private Button hMore;
    private Button vLess;
    private Button vMore;
    private FloowerPotBlockEntity fpbe;
    private int hRangeCache;
    private int vRangeCache;

    private static final TextComponent MINUS = new TextComponent("-");
    private static final TextComponent PLUS = new TextComponent("+");
    private ResourceLocation MENU_LOCATION = new ResourceLocation(DataReference.MODID, "textures/gui/guifloowerpot.png");

    public FloowerPotScreen(FloowerPotMenu menu, Inventory inv, Component comp) {
        super(menu, inv, comp);
        this.fpbe = (FloowerPotBlockEntity) menu.getBlockEntity();
        this.hRangeCache = DataReference.POT_MIN_H_RANGE;
        this.vRangeCache = DataReference.POT_MIN_V_RANGE;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @Override
    public void init() {
        super.init();
        this.hLess = new Button(this.leftPos + 8, this.topPos + this.height - 96 - 30, 10, 10, MINUS, button -> {
            this.hRangeCache -= 1;
            FloowerPotScreen.this.sendPotRangeMessage('h', -1);
        });
        this.addRenderableWidget(this.hLess);
        this.hMore = new Button(this.leftPos + 28, this.topPos + this.height - 96 - 30, 10, 10, PLUS, button -> {
            this.hRangeCache += 1;
            FloowerPotScreen.this.sendPotRangeMessage('h', 1);
        });
        this.addRenderableWidget(this.hMore);
        this.vLess = new Button(this.leftPos + 8, this.topPos + this.height - 96 - 10, 10, 10, MINUS, button -> {
            this.vRangeCache -= 1;
            FloowerPotScreen.this.sendPotRangeMessage('v', -1);
        });
        this.addRenderableWidget(this.vLess);
        this.vMore = new Button(this.leftPos + 28, this.topPos + this.height - 96 - 10, 10, 10, PLUS, button -> {
            this.vRangeCache += 1;
            FloowerPotScreen.this.sendPotRangeMessage('v', 1);
        });
        this.addRenderableWidget(this.vMore);
    }

    @Override
    public void render(PoseStack stack, int x, int y, float partialTicks) {
        this.renderBackground(stack);
        super.render(stack, x, y, partialTicks);
        this.renderTooltip(stack, x, y);
    }

    @Override
    protected void renderLabels(PoseStack stack, int param1, int param2) {
        if(this.fpbe.justUpdated) {
            this.hRangeCache = this.fpbe.getHRange();
            this.vRangeCache = this.fpbe.getVRange();
            this.fpbe.justUpdated = false;
        }
        this.hLess.active = this.hRangeCache != DataReference.POT_MIN_H_RANGE;
        this.hMore.active = this.hRangeCache != DataReference.POT_MAX_H_RANGE;
        this.vLess.active = this.vRangeCache != DataReference.POT_MIN_V_RANGE;
        this.vMore.active = this.vRangeCache != DataReference.POT_MAX_V_RANGE;
        //the parameters for drawString are: string, x, y, color
        drawCenteredString(stack, this.font, new TranslatableComponent("block.floocraftft.floowerpot"), 8, 6, 4210752);
        //draws "Inventory" or your regional equivalent
        drawCenteredString(stack, this.font, new TranslatableComponent("container.inventory"), 8, this.height - 96 + 2, 4210752);
        drawCenteredString(stack, this.font, new TranslatableComponent("gui.pot.range"), 8, this.height - 96 - 48, 4210752);
        drawCenteredString(stack, this.font, new TranslatableComponent("gui.pot.horizontal"), 8, this.height - 96 - 38, 4210752);
        drawCenteredString(stack, this.font, new TextComponent("" + this.hRangeCache), 20, this.height - 96 - 28, 4210752);
        drawCenteredString(stack, this.font, new TranslatableComponent("gui.pot.vertical"), 8, this.height - 96 - 18, 4210752);
        drawCenteredString(stack, this.font, new TextComponent("" + this.vRangeCache), 20, this.height - 96 - 8, 4210752);
    }

    @Override
    protected void renderBg(PoseStack stack, float par1, int par2, int par3) {
        //draw your Gui here, only thing you need to change is the path
        //May need to do getTexture first if this method causes trouble
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, this.MENU_LOCATION);
        int x = (width - this.imageWidth) / 2;
        int y = (height - this.imageHeight) / 2;
        this.blit(stack, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    private void sendPotRangeMessage(char range, int amount) {
        MessagePotRange mpr = new MessagePotRange();
        mpr.range = range;
        mpr.amount = amount;
        BlockPos blockPos = this.fpbe.getBlockPos();
        mpr.pos = blockPos;
        MessageHandler.INSTANCE.sendToServer(mpr);
    }
}
