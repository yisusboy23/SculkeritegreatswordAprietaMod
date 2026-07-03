package net.tucas.sculkeritegreatsword.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class LapisControlParticle extends TextureSheetParticle {

    private final SpriteSet spriteSet;

    protected LapisControlParticle(ClientLevel level, double x, double y, double z,
                                   SpriteSet spriteSet, double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);

        this.spriteSet = spriteSet;
        this.friction = 0.8F;
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
        this.quadSize *= 0.75F;
        this.lifetime = 40;
        this.setSpriteFromAge(spriteSet);

        // Color azul brillante
        this.rCol = 0.3F;
        this.gCol = 0.6F;
        this.bCol = 1.0F;
        this.alpha = 1.0F;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.spriteSet);

        // Fade out al final
        if (this.age > this.lifetime / 2) {
            this.alpha = 1.0F - ((float) this.age - (float) (this.lifetime / 2)) / (float) this.lifetime;
        }
    }

    @Override
    @Nonnull
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void setSpriteFromAge(@Nonnull SpriteSet spriteSet) {
        if (!this.removed) {
            this.setSprite(spriteSet.get(this.age, this.lifetime));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(@Nonnull SimpleParticleType typeIn, @Nonnull ClientLevel worldIn,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return new LapisControlParticle(worldIn, x, y, z, this.sprites, xSpeed, ySpeed, zSpeed);
        }
    }

    public static ParticleProvider<SimpleParticleType> provider(SpriteSet spriteSet) {
        return new LapisControlParticle.Provider(spriteSet);
    }
}