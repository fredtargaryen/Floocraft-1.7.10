package com.fredtargaryen.floocraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.NotNull;

public class GreenFlameParticle extends TextureSheetParticle {
    private GreenFlameParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet spriteSet) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.setSprite(spriteSet.get(0, 50));
        this.lifetime = (int)(8.0D / (Math.random() * 0.8D + 0.2D)) + 4;
    }

    @Override
    public float getQuadSize(float p_217561_1_) {
        float f = ((float)this.age + p_217561_1_) / (float)this.lifetime;
        return this.quadSize * (1.0F - f * f * 0.5F);
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public int getLightColor(float partialTick) {
        //Full brightness
        return 0xf000f0;
    }

    /**
     * call once per tick to update the EntityFX position, calculate collisions, remove when max lifetime is reached, etc
     */
    @Override
    public void tick() {
        if (this.age++ >= this.lifetime) {
            this.remove();
        }
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new GreenFlameParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprite);
        }
    }
}