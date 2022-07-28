package com.fredtargaryen.floocraft.client.gui;

import com.fredtargaryen.floocraft.DataReference;
import com.fredtargaryen.floocraft.blockentity.FlooSignBlockEntity;
import com.fredtargaryen.floocraft.blockentity.renderer.FlooSignRenderer;
import com.fredtargaryen.floocraft.network.MessageHandler;
import com.fredtargaryen.floocraft.network.messages.MessageApproveFireplace;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.stream.IntStream;

@OnlyIn(Dist.CLIENT)
public class FlooSignScreen extends Screen {
    private Component namingStatus = TextComponent.EMPTY;

    private static final TranslatableComponent TITLE = new TranslatableComponent("gui.floosign.title");
    private static final TranslatableComponent DECOR_BUTTON = new TranslatableComponent("gui.floosign.decoration");
    private static final TranslatableComponent CONNECT_BUTTON = new TranslatableComponent("gui.floosign.connect");

    private static final ResourceLocation floosigntexloc = new ResourceLocation(DataReference.MODID, "textures/blocks/floosign.png");

    private final FlooSignBlockEntity fsbe;
    private boolean sendDefaultMessageOnClose;

    //region Copy-pasting practically all of this from SignEditScreen
    private int frame;
    private int line;
    private TextFieldHelper signField;
    private WoodType woodType;
    private FlooSignRenderer.SignModel signModel;
    private String[] messages;

    public FlooSignScreen(FlooSignBlockEntity blockEntity, boolean filtered) {
        super(TITLE);
        this.messages = IntStream.range(0, 4).mapToObj((p_169818_) -> {
            return blockEntity.getMessage(p_169818_, filtered);
        }).map(Component::getString).toArray((p_169814_) -> {
            return new String[p_169814_];
        });
        this.fsbe = blockEntity;
    }

    public FlooSignScreen(FlooSignBlockEntity par1FlooSignBlockEntity) {
        super(TITLE);
        this.fsbe = par1FlooSignBlockEntity;
        this.sendDefaultMessageOnClose = true;
    }

    @Override
    protected void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        // "Use as decoration" button
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 4 + 120, 98, 20, DECOR_BUTTON, button -> FlooSignScreen.this.minecraft.setScreen(null)));
        // "Connect to Network" button
        this.addRenderableWidget(new Button(this.width / 2 + 2, this.height / 4 + 120, 98, 20, CONNECT_BUTTON, button -> FlooSignScreen.this.sendApprovalMessage(true)));
        this.signField = new TextFieldHelper(
                () -> this.messages[this.line],
                lineString -> {
                    this.messages[this.line] = lineString;
                    this.fsbe.setMessage(this.line, new TextComponent(lineString));},
                TextFieldHelper.createClipboardGetter(this.minecraft),
                TextFieldHelper.createClipboardSetter(this.minecraft), lineString -> this.minecraft.font.width(lineString) <= 90);
        this.fsbe.setEditable(false);
        this.woodType = WoodType.OAK;
        this.signModel = FlooSignRenderer.createSignModel(this.minecraft.getEntityModels(), this.woodType);
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
        this.fsbe.setEditable(true);
    }

    @Override
    public void tick() {
        ++this.frame;
        if (!this.fsbe.getType().isValid(this.fsbe.getBlockState())) {
            this.onDone();
        }
    }

    private void onDone() {
        this.fsbe.setChanged();
        this.minecraft.setScreen(null);
    }

    @Override
    public boolean charTyped(char p_99262_, int p_99263_) {
        this.signField.charTyped(p_99262_);
        return true;
    }

    /**
     * Called when the screen is unloaded via the Esc button. Used to disable keyboard repeat events
     */
    @Override
    public void onClose() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
        if(this.sendDefaultMessageOnClose) {
            FlooSignScreen.this.sendApprovalMessage(false);
        }
    }

    @Override
    public boolean keyPressed(int p_99267_, int p_99268_, int p_99269_) {
        if (p_99267_ == 265) {
            this.line = this.line - 1 & 3;
            this.signField.setCursorToEnd();
            return true;
        } else if (p_99267_ != 264 && p_99267_ != 257 && p_99267_ != 335) {
            return this.signField.keyPressed(p_99267_) ? true : super.keyPressed(p_99267_, p_99268_, p_99269_);
        } else {
            this.line = this.line + 1 & 3;
            this.signField.setCursorToEnd();
            return true;
        }
    }

    @Override
    public void render(PoseStack poseStack, int p_99272_, int p_99273_, float p_99274_) {
        Lighting.setupForFlatItems();
        this.renderBackground(poseStack);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 40, 16777215);
        drawCenteredString(
                poseStack,
                this.font,
                TITLE,
                this.width / 2,
                40,
                16777215);
        drawCenteredString(
                poseStack,
                this.font,
                this.namingStatus,
                this.width / 2,
                this.height / 4 + 100,
                16777215);
        poseStack.pushPose();
        poseStack.translate((double)(this.width / 2), 0.0D, 50.0D);
        float f = 93.75F;
        poseStack.scale(93.75F, -93.75F, 93.75F);
        poseStack.translate(0.0D, -1.3125D, 0.0D);
        BlockState blockstate = this.fsbe.getBlockState();
        boolean flag = blockstate.getBlock() instanceof StandingSignBlock;
        if (!flag) {
            poseStack.translate(0.0D, -0.3125D, 0.0D);
        }

        boolean flag1 = this.frame / 6 % 2 == 0;
        float f1 = 0.6666667F;
        poseStack.pushPose();
        poseStack.scale(0.6666667F, -0.6666667F, -0.6666667F);
        MultiBufferSource.BufferSource multibuffersource$buffersource = this.minecraft.renderBuffers().bufferSource();
        Material material = Sheets.getSignMaterial(this.woodType);
        VertexConsumer vertexconsumer = material.buffer(multibuffersource$buffersource, this.signModel::renderType);
        this.signModel.sign.render(poseStack, vertexconsumer, 15728880, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        float f2 = 0.010416667F;
        poseStack.translate(0.0D, (double)0.33333334F, (double)0.046666667F);
        poseStack.scale(0.010416667F, -0.010416667F, 0.010416667F);
        int i = 0;//this.fsbe.getColor().getTextColor();
        int j = this.signField.getCursorPos();
        int k = this.signField.getSelectionPos();
        int l = this.line * 10 - this.messages.length * 5;
        Matrix4f matrix4f = poseStack.last().pose();

        for(int i1 = 0; i1 < this.messages.length; ++i1) {
            String s = this.messages[i1];
            if (s != null) {
                if (this.font.isBidirectional()) {
                    s = this.font.bidirectionalShaping(s);
                }

                float f3 = (float)(-this.minecraft.font.width(s) / 2);
                this.minecraft.font.drawInBatch(s, f3, (float)(i1 * 10 - this.messages.length * 5), i, false, matrix4f, multibuffersource$buffersource, false, 0, 15728880, false);
                if (i1 == this.line && j >= 0 && flag1) {
                    int j1 = this.minecraft.font.width(s.substring(0, Math.max(Math.min(j, s.length()), 0)));
                    int k1 = j1 - this.minecraft.font.width(s) / 2;
                    if (j >= s.length()) {
                        this.minecraft.font.drawInBatch("_", (float)k1, (float)l, i, false, matrix4f, multibuffersource$buffersource, false, 0, 15728880, false);
                    }
                }
            }
        }

        multibuffersource$buffersource.endBatch();

        for(int i3 = 0; i3 < this.messages.length; ++i3) {
            String s1 = this.messages[i3];
            if (s1 != null && i3 == this.line && j >= 0) {
                int j3 = this.minecraft.font.width(s1.substring(0, Math.max(Math.min(j, s1.length()), 0)));
                int k3 = j3 - this.minecraft.font.width(s1) / 2;
                if (flag1 && j < s1.length()) {
                    fill(poseStack, k3, l - 1, k3 + 1, l + 9, -16777216 | i);
                }

                if (k != j) {
                    int l3 = Math.min(j, k);
                    int l1 = Math.max(j, k);
                    int i2 = this.minecraft.font.width(s1.substring(0, l3)) - this.minecraft.font.width(s1) / 2;
                    int j2 = this.minecraft.font.width(s1.substring(0, l1)) - this.minecraft.font.width(s1) / 2;
                    int k2 = Math.min(i2, j2);
                    int l2 = Math.max(i2, j2);
                    Tesselator tesselator = Tesselator.getInstance();
                    BufferBuilder builder = tesselator.getBuilder();
                    RenderSystem.setShader(GameRenderer::getPositionColorShader);
                    RenderSystem.disableTexture();
                    RenderSystem.enableColorLogicOp();
                    RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
                    builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                    builder.vertex(matrix4f, (float)k2, (float)(l + 9), 0.0F).color(0, 0, 255, 255).endVertex();
                    builder.vertex(matrix4f, (float)l2, (float)(l + 9), 0.0F).color(0, 0, 255, 255).endVertex();
                    builder.vertex(matrix4f, (float)l2, (float)l, 0.0F).color(0, 0, 255, 255).endVertex();
                    builder.vertex(matrix4f, (float)k2, (float)l, 0.0F).color(0, 0, 255, 255).endVertex();
                    builder.end();
                    BufferUploader.end(builder);
                    RenderSystem.disableColorLogicOp();
                    RenderSystem.enableTexture();
                }
            }
        }

        poseStack.popPose();
        Lighting.setupFor3DItems();
        super.render(poseStack, p_99272_, p_99273_, p_99274_);
    }
    //endregion

    private void sendApprovalMessage(boolean attemptingToConnect) {
        MessageApproveFireplace maf = new MessageApproveFireplace();
        BlockPos pos = this.fsbe.getBlockPos();
        maf.x = pos.getX();
        maf.y = pos.getY();
        maf.z = pos.getZ();
        maf.attemptingToConnect = attemptingToConnect;
        maf.name = this.fsbe.signText;
        MessageHandler.INSTANCE.sendToServer(maf);
        this.namingStatus = new TranslatableComponent("gui.floosign.approvalwait");
        this.sendDefaultMessageOnClose = false;
    }

    public void dealWithAnswer(boolean answer) {
        if(answer) {
            //Either the sign is for decoration, or it's for connecting and the name is valid
            this.namingStatus = TextComponent.EMPTY;
            this.fsbe.setChanged();
            this.minecraft.setScreen(null);
        }
        else {
            //The sign is for connecting but the name has already been used.
            this.namingStatus = new TranslatableComponent("gui.floosign.nameinuse");
            this.sendDefaultMessageOnClose = true;
        }
    }
}