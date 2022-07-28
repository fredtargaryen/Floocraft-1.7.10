package com.fredtargaryen.floocraft.client.gui;

import com.fredtargaryen.floocraft.DataReference;
import com.fredtargaryen.floocraft.FloocraftBase;
import com.fredtargaryen.floocraft.config.ClientConfig;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Random;

public class Flash {
    private double ticks;
    private Minecraft minecraft;
    private static final ResourceLocation texLoc = new ResourceLocation(DataReference.MODID, "textures/gui/flash.png");
    private static final ResourceLocation soulTexLoc = new ResourceLocation(DataReference.MODID, "textures/gui/soulflash.png");
    private TextureManager textureManager;
    private long startTime;

    private float yawDirectionStrength;
    private float pitchDirectionStrength;
    private float rollDirectionStrength;

    private boolean soul;
	
    public Flash() {
        this.ticks = -1;
        this.soul = false;
    }

    public void start(boolean soul) {
        if(this.ticks == -1) {
            this.soul = soul;
            this.ticks = 0;
            this.minecraft = Minecraft.getInstance();
            this.textureManager = this.minecraft.getTextureManager();
            MinecraftForge.EVENT_BUS.register(this);
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(FloocraftBase.TP.get(), 1.0F));
            this.startTime = System.currentTimeMillis();

            //Determine roll parameters
            Random rand = this.minecraft.level.random;
            this.yawDirectionStrength = (rand.nextBoolean() ? 1f : -1f) * (45f + rand.nextFloat() * 30f);
            this.pitchDirectionStrength = (rand.nextBoolean() ? 1f : -1f) * (45f + rand.nextFloat() * 30f);
            this.rollDirectionStrength = (rand.nextBoolean() ? 1f : -1f) * (45f + rand.nextFloat() * 30f);
        }
    }

    @SubscribeEvent
    public void flash(TickEvent.RenderTickEvent event) {
        if(event.phase == TickEvent.Phase.END) {
            this.ticks = System.currentTimeMillis() - this.startTime;
            if(ClientConfig.ENABLE_FLASH.get()) {
                GlStateManager._disableBlend();
                GlStateManager._disableDepthTest();
                GlStateManager._depthMask(false);
                GlStateManager._blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA.value, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA.value, GlStateManager.SourceFactor.ONE.value, GlStateManager.DestFactor.ZERO.value);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, (float) Math.cos(Math.toRadians(this.ticks * 90 / 1000.0)));
                RenderSystem.setShaderTexture(0, this.soul ? soulTexLoc : texLoc);
                Tesselator tesselator = Tesselator.getInstance();
                BufferBuilder builder = tesselator.getBuilder();
                builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
                double width = this.minecraft.getWindow().getGuiScaledWidth();
                double height = this.minecraft.getWindow().getGuiScaledHeight();
                builder.vertex(width, height, -90.0).uv(1f, 1f).endVertex();
                builder.vertex(width, 0.0, -90.0).uv(1f, 0f).endVertex();
                builder.vertex(0.0, 0.0, -90.0).uv(0f, 0f).endVertex();
                builder.vertex(0.0, height, -90.0).uv(0f, 1f).endVertex();
                tesselator.end();
                GlStateManager._depthMask(true);
                GlStateManager._enableDepthTest();
                GlStateManager._enableBlend();
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }
        if(this.ticks > 2999) {
            this.ticks = -1;
            MinecraftForge.EVENT_BUS.unregister(this);
        }
    }

    @SubscribeEvent
    public void dizzy(EntityViewRenderEvent.CameraSetup event) {
        if(ClientConfig.ENABLE_DIZZY.get()) {
            float angle = (float) ((this.ticks / 3000.0) * Math.PI * 3);
            this.yawDirectionStrength *= 0.995f;
            this.pitchDirectionStrength *= 0.995f;
            this.rollDirectionStrength *= 0.995f;
            event.setYaw((float) (this.minecraft.player.getYHeadRot() + this.yawDirectionStrength * Math.sin(angle)));
            event.setPitch((float) (this.minecraft.player.getXRot() + this.pitchDirectionStrength * Math.sin(angle)));
            event.setRoll((float) (this.rollDirectionStrength * Math.sin(angle)));
        }
    }
}
