package com.fredtargaryen.floocraft.client.renderer;

import com.fredtargaryen.floocraft.entity.PeekerEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class PeekerRenderer extends EntityRenderer<PeekerEntity> {
    private static ResourceLocation NULL = new ResourceLocation("textures/entity/steve.png");

    private static final float minx = -0.25F;
    private static final float maxx = 0.25F;
    private static final float miny = 0.0F;
    private static final float maxy = 0.5F;
    private static final float minz = -0.001F;
    private static final float maxz = 0.0F;
    //UV of player head on skin texture in texels is from (8, 8) to (16, 16). Divided by 64 to normalise coords
    private static final float minu = 0.125F;
    private static final float maxu = 0.25F;
    private static final float minv = 0.125F;
    private static final float maxv = 0.25F;

    public PeekerRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(PeekerEntity par1PeekerEntity, float entityYaw, float partialTicks, PoseStack stack, MultiBufferSource bufferSource, int packedLightIn) {
        stack.pushPose();
        stack.mulPose(new Quaternion(new Vector3f(0f, 1f, 0f), 180f - par1PeekerEntity.getYRot(), true));
        //stack.mulPose(new Quaternion(new Vector3f(1f, 0f, 0f), 15f, true));
        Matrix4f pos = stack.last().pose();
        Matrix3f norm = stack.last().normal();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(this.getTextureLocation(par1PeekerEntity), false)); // TODO False correct value?
        consumer.vertex(pos, minx, miny, minz)
                .color(1f, 1f, 1f, 1f)
                .uv(maxu, maxv)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLightIn)
                .normal(norm, 0f, 1f, 0f)
                .endVertex();
        consumer.vertex(pos, minx, maxy, maxz)
                .color(1f, 1f, 1f, 1f)
                .uv(maxu, minv)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLightIn)
                .normal(norm, 0f, 1f, 0f)
                .endVertex();
        consumer.vertex(pos, maxx, maxy, maxz)
                .color(1f, 1f, 1f, 1f)
                .uv(minu, minv)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLightIn)
                .normal(norm, 0f, 1f, 0f)
                .endVertex();
        consumer.vertex(pos, maxx, miny, minz)
                .color(1f, 1f, 1f, 1f)
                .uv(minu, maxv)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLightIn)
                .normal(norm, 0f, 1f, 0f)
                .endVertex();
        stack.popPose();
        super.render(par1PeekerEntity, entityYaw, partialTicks, stack, bufferSource, packedLightIn);
    }

    @Override
    @Nonnull
    public ResourceLocation getTextureLocation(PeekerEntity entity) {
        ResourceLocation rl = entity.getTexture();
        return rl == null ? NULL : rl;
    }
}