package com.fredtargaryen.floocraft.blockentity.renderer;

import com.fredtargaryen.floocraft.DataReference;
import com.fredtargaryen.floocraft.blockentity.FlooSignBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class FlooSignRenderer implements BlockEntityRenderer<FlooSignBlockEntity> {
    public static final int MAX_LINE_WIDTH = 90;
    private static final int LINE_HEIGHT = 10;
    private static final String STICK = "stick";
    private static final int BLACK_TEXT_OUTLINE_COLOR = -988212;
    private static final int OUTLINE_RENDER_DISTANCE = Mth.square(16);
    private final Font font;
    private final SignModel model;

    public FlooSignRenderer(BlockEntityRendererProvider.Context context)
    {
        this.model = new SignModel(context.bakeLayer(new ModelLayerLocation(new ResourceLocation(DataReference.MODID, "sign"), "main")));
        this.font = context.getFont();
    }

    public void render(FlooSignBlockEntity blockEntity, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int combinedLightIn, int combinedOverlayIn) {
        BlockState blockstate = blockEntity.getBlockState();
        poseStack.pushPose();
        float f = 0.6666667F;

        poseStack.translate(0.5D, 0.5D, 0.5D);
        float f4 = -blockstate.getValue(WallSignBlock.FACING).toYRot();
        poseStack.mulPose(Vector3f.YP.rotationDegrees(f4));
        poseStack.translate(0.0D, -0.3125D, -0.4375D);

        poseStack.pushPose();
        poseStack.scale(0.6666667F, -0.6666667F, -0.6666667F);
        Material material = Sheets.getSignMaterial(WoodType.OAK);
        VertexConsumer vertexconsumer = material.buffer(buffer, this.model::renderType);
        this.model.sign.render(poseStack, vertexconsumer, combinedLightIn, combinedOverlayIn);
        poseStack.popPose();
        float f2 = 0.010416667F;
        poseStack.translate(0.0D, (double)0.33333334F, (double)0.046666667F);
        poseStack.scale(0.010416667F, -0.010416667F, 0.010416667F);
        int i = 0; // TODO Check this corresponds to black. Was previously SignRenderer::getDarkColor.
        int j = 20;
        FormattedCharSequence[] aformattedcharsequence = blockEntity.getRenderMessages(Minecraft.getInstance().isTextFilteringEnabled(), (p_173653_) -> {
            List<FormattedCharSequence> list = this.font.split(p_173653_, 90);
            return list.isEmpty() ? FormattedCharSequence.EMPTY : list.get(0);
        });
        int k = i;
        int l = combinedLightIn;

        for(int i1 = 0; i1 < 4; ++i1) {
            FormattedCharSequence formattedcharsequence = aformattedcharsequence[i1];
            float f3 = (float)(-this.font.width(formattedcharsequence) / 2);
            this.font.drawInBatch(formattedcharsequence, f3, (float)(i1 * 10 - 20), k, false, poseStack.last().pose(), buffer, false, 0, l);
        }

        poseStack.popPose();
    }

    private static boolean isOutlineVisible(SignBlockEntity p_173642_, int p_173643_) {
        if (p_173643_ == DyeColor.BLACK.getTextColor()) {
            return true;
        } else {
            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer localplayer = minecraft.player;
            if (localplayer != null && minecraft.options.getCameraType().isFirstPerson() && localplayer.isScoping()) {
                return true;
            } else {
                Entity entity = minecraft.getCameraEntity();
                return entity != null && entity.distanceToSqr(Vec3.atCenterOf(p_173642_.getBlockPos())) < (double)OUTLINE_RENDER_DISTANCE;
            }
        }
    }

    public static SignModel createSignModel(EntityModelSet p_173647_, WoodType p_173648_) {
        return new SignModel(p_173647_.bakeLayer(ModelLayers.createSignModelName(p_173648_)));
    }

    public static LayerDefinition createSignLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("sign", CubeListBuilder.create().texOffs(0, 0).addBox(-12.0F, -14.0F, -1.0F, 24.0F, 12.0F, 2.0F), PartPose.ZERO);
        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @OnlyIn(Dist.CLIENT)
    public static final class SignModel extends Model {
        public final ModelPart sign;

        public SignModel(ModelPart p_173657_) {
            super(RenderType::entityCutoutNoCull);
            this.sign = p_173657_;
        }

        public void renderToBuffer(PoseStack stack, VertexConsumer consumer, int p_112512_, int p_112513_, float p_112514_, float p_112515_, float p_112516_, float p_112517_) {
            this.sign.render(stack, consumer, p_112512_, p_112513_, p_112514_, p_112515_, p_112516_, p_112517_);
        }
    }
}
