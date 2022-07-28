package com.fredtargaryen.floocraft.blockentity.renderer;

import com.fredtargaryen.floocraft.DataReference;
import com.fredtargaryen.floocraft.blockentity.FloowerPotBlockEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemStack;

public class FloowerPotRenderer implements BlockEntityRenderer<FloowerPotBlockEntity>
{
    public FloowerPotRenderer(BlockEntityRendererProvider.Context context)
    {
    }

    /**
     *
     * @param te
     * @param partialTicks
     * @param poseStack the current view transformations
     * @param bufferSource A map from RenderTypes to buffers
     * @param combinedLight The "block light" and "sky light" packed into the space of an int
     * @param combinedOverlay Points to an overlay texture that modifies the bound texture
     */
    @Override
    public void render(FloowerPotBlockEntity te, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        ItemStack stack = te.getItem(0);
        if(stack != null && stack.getCount() > 0) {
            poseStack.pushPose(); // Pushes the current transform and normal matrices. Origin is the (0, 0, 0) corner of the block to be rendered
            VertexConsumer consumer = bufferSource.getBuffer(RenderType.entitySolid(DataReference.TP_BACKGROUND));
            // set the key rendering flags appropriately...
            RenderSystem.disableBlend(); // turn off "alpha" transparency blending
            RenderSystem.depthMask(true); // quad is hidden behind other objects
            Matrix4f pos = poseStack.last().pose();
            Matrix3f norm = poseStack.last().normal();
            float level = (((float)stack.getCount() / 64f) * 0.3125f) + 0.0625f;
            this.doAVertex(consumer, pos, norm, 0.625f, level, 0.625f, 1f, 1f, combinedLight);
            this.doAVertex(consumer, pos, norm, 0.625f, level, 0.375f, 1f, 0f, combinedLight);
            this.doAVertex(consumer, pos, norm, 0.375f, level, 0.375f, 0f, 0f, combinedLight);
            this.doAVertex(consumer, pos, norm, 0.375f, level, 0.625f, 0f, 1f, combinedLight);
            poseStack.popPose();
        }
    }

    private void doAVertex(VertexConsumer consumer, Matrix4f pos, Matrix3f norm, float x, float y, float z, float u, float v, int lightLevel) {
        consumer.vertex(pos, x, y, z)
                .color(1f, 1f, 1f, 1f)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(lightLevel)
                .normal(norm, 0f, 1f, 0f)
                .endVertex();
    }
}
