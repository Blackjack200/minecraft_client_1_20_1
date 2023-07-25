package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.FireworkRocketItem;

public class FireworkParticles {
   public static class FlashProvider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public FlashProvider(SpriteSet spriteset) {
         this.sprite = spriteset;
      }

      public Particle createParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         FireworkParticles.OverlayParticle fireworkparticles_overlayparticle = new FireworkParticles.OverlayParticle(clientlevel, d0, d1, d2);
         fireworkparticles_overlayparticle.pickSprite(this.sprite);
         return fireworkparticles_overlayparticle;
      }
   }

   public static class OverlayParticle extends TextureSheetParticle {
      OverlayParticle(ClientLevel clientlevel, double d0, double d1, double d2) {
         super(clientlevel, d0, d1, d2);
         this.lifetime = 4;
      }

      public ParticleRenderType getRenderType() {
         return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
      }

      public void render(VertexConsumer vertexconsumer, Camera camera, float f) {
         this.setAlpha(0.6F - ((float)this.age + f - 1.0F) * 0.25F * 0.5F);
         super.render(vertexconsumer, camera, f);
      }

      public float getQuadSize(float f) {
         return 7.1F * Mth.sin(((float)this.age + f - 1.0F) * 0.25F * (float)Math.PI);
      }
   }

   static class SparkParticle extends SimpleAnimatedParticle {
      private boolean trail;
      private boolean flicker;
      private final ParticleEngine engine;
      private float fadeR;
      private float fadeG;
      private float fadeB;
      private boolean hasFade;

      SparkParticle(ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5, ParticleEngine particleengine, SpriteSet spriteset) {
         super(clientlevel, d0, d1, d2, spriteset, 0.1F);
         this.xd = d3;
         this.yd = d4;
         this.zd = d5;
         this.engine = particleengine;
         this.quadSize *= 0.75F;
         this.lifetime = 48 + this.random.nextInt(12);
         this.setSpriteFromAge(spriteset);
      }

      public void setTrail(boolean flag) {
         this.trail = flag;
      }

      public void setFlicker(boolean flag) {
         this.flicker = flag;
      }

      public void render(VertexConsumer vertexconsumer, Camera camera, float f) {
         if (!this.flicker || this.age < this.lifetime / 3 || (this.age + this.lifetime) / 3 % 2 == 0) {
            super.render(vertexconsumer, camera, f);
         }

      }

      public void tick() {
         super.tick();
         if (this.trail && this.age < this.lifetime / 2 && (this.age + this.lifetime) % 2 == 0) {
            FireworkParticles.SparkParticle fireworkparticles_sparkparticle = new FireworkParticles.SparkParticle(this.level, this.x, this.y, this.z, 0.0D, 0.0D, 0.0D, this.engine, this.sprites);
            fireworkparticles_sparkparticle.setAlpha(0.99F);
            fireworkparticles_sparkparticle.setColor(this.rCol, this.gCol, this.bCol);
            fireworkparticles_sparkparticle.age = fireworkparticles_sparkparticle.lifetime / 2;
            if (this.hasFade) {
               fireworkparticles_sparkparticle.hasFade = true;
               fireworkparticles_sparkparticle.fadeR = this.fadeR;
               fireworkparticles_sparkparticle.fadeG = this.fadeG;
               fireworkparticles_sparkparticle.fadeB = this.fadeB;
            }

            fireworkparticles_sparkparticle.flicker = this.flicker;
            this.engine.add(fireworkparticles_sparkparticle);
         }

      }
   }

   public static class SparkProvider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprites;

      public SparkProvider(SpriteSet spriteset) {
         this.sprites = spriteset;
      }

      public Particle createParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         FireworkParticles.SparkParticle fireworkparticles_sparkparticle = new FireworkParticles.SparkParticle(clientlevel, d0, d1, d2, d3, d4, d5, Minecraft.getInstance().particleEngine, this.sprites);
         fireworkparticles_sparkparticle.setAlpha(0.99F);
         return fireworkparticles_sparkparticle;
      }
   }

   public static class Starter extends NoRenderParticle {
      private int life;
      private final ParticleEngine engine;
      private ListTag explosions;
      private boolean twinkleDelay;

      public Starter(ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5, ParticleEngine particleengine, @Nullable CompoundTag compoundtag) {
         super(clientlevel, d0, d1, d2);
         this.xd = d3;
         this.yd = d4;
         this.zd = d5;
         this.engine = particleengine;
         this.lifetime = 8;
         if (compoundtag != null) {
            this.explosions = compoundtag.getList("Explosions", 10);
            if (this.explosions.isEmpty()) {
               this.explosions = null;
            } else {
               this.lifetime = this.explosions.size() * 2 - 1;

               for(int i = 0; i < this.explosions.size(); ++i) {
                  CompoundTag compoundtag1 = this.explosions.getCompound(i);
                  if (compoundtag1.getBoolean("Flicker")) {
                     this.twinkleDelay = true;
                     this.lifetime += 15;
                     break;
                  }
               }
            }
         }

      }

      public void tick() {
         if (this.life == 0 && this.explosions != null) {
            boolean flag = this.isFarAwayFromCamera();
            boolean flag1 = false;
            if (this.explosions.size() >= 3) {
               flag1 = true;
            } else {
               for(int i = 0; i < this.explosions.size(); ++i) {
                  CompoundTag compoundtag = this.explosions.getCompound(i);
                  if (FireworkRocketItem.Shape.byId(compoundtag.getByte("Type")) == FireworkRocketItem.Shape.LARGE_BALL) {
                     flag1 = true;
                     break;
                  }
               }
            }

            SoundEvent soundevent;
            if (flag1) {
               soundevent = flag ? SoundEvents.FIREWORK_ROCKET_LARGE_BLAST_FAR : SoundEvents.FIREWORK_ROCKET_LARGE_BLAST;
            } else {
               soundevent = flag ? SoundEvents.FIREWORK_ROCKET_BLAST_FAR : SoundEvents.FIREWORK_ROCKET_BLAST;
            }

            this.level.playLocalSound(this.x, this.y, this.z, soundevent, SoundSource.AMBIENT, 20.0F, 0.95F + this.random.nextFloat() * 0.1F, true);
         }

         if (this.life % 2 == 0 && this.explosions != null && this.life / 2 < this.explosions.size()) {
            int j = this.life / 2;
            CompoundTag compoundtag1 = this.explosions.getCompound(j);
            FireworkRocketItem.Shape fireworkrocketitem_shape = FireworkRocketItem.Shape.byId(compoundtag1.getByte("Type"));
            boolean flag2 = compoundtag1.getBoolean("Trail");
            boolean flag3 = compoundtag1.getBoolean("Flicker");
            int[] aint = compoundtag1.getIntArray("Colors");
            int[] aint1 = compoundtag1.getIntArray("FadeColors");
            if (aint.length == 0) {
               aint = new int[]{DyeColor.BLACK.getFireworkColor()};
            }

            switch (fireworkrocketitem_shape) {
               case SMALL_BALL:
               default:
                  this.createParticleBall(0.25D, 2, aint, aint1, flag2, flag3);
                  break;
               case LARGE_BALL:
                  this.createParticleBall(0.5D, 4, aint, aint1, flag2, flag3);
                  break;
               case STAR:
                  this.createParticleShape(0.5D, new double[][]{{0.0D, 1.0D}, {0.3455D, 0.309D}, {0.9511D, 0.309D}, {0.3795918367346939D, -0.12653061224489795D}, {0.6122448979591837D, -0.8040816326530612D}, {0.0D, -0.35918367346938773D}}, aint, aint1, flag2, flag3, false);
                  break;
               case CREEPER:
                  this.createParticleShape(0.5D, new double[][]{{0.0D, 0.2D}, {0.2D, 0.2D}, {0.2D, 0.6D}, {0.6D, 0.6D}, {0.6D, 0.2D}, {0.2D, 0.2D}, {0.2D, 0.0D}, {0.4D, 0.0D}, {0.4D, -0.6D}, {0.2D, -0.6D}, {0.2D, -0.4D}, {0.0D, -0.4D}}, aint, aint1, flag2, flag3, true);
                  break;
               case BURST:
                  this.createParticleBurst(aint, aint1, flag2, flag3);
            }

            int k = aint[0];
            float f = (float)((k & 16711680) >> 16) / 255.0F;
            float f1 = (float)((k & '\uff00') >> 8) / 255.0F;
            float f2 = (float)((k & 255) >> 0) / 255.0F;
            Particle particle = this.engine.createParticle(ParticleTypes.FLASH, this.x, this.y, this.z, 0.0D, 0.0D, 0.0D);
            particle.setColor(f, f1, f2);
         }

         ++this.life;
         if (this.life > this.lifetime) {
            if (this.twinkleDelay) {
               boolean flag4 = this.isFarAwayFromCamera();
               SoundEvent soundevent2 = flag4 ? SoundEvents.FIREWORK_ROCKET_TWINKLE_FAR : SoundEvents.FIREWORK_ROCKET_TWINKLE;
               this.level.playLocalSound(this.x, this.y, this.z, soundevent2, SoundSource.AMBIENT, 20.0F, 0.9F + this.random.nextFloat() * 0.15F, true);
            }

            this.remove();
         }

      }

      private boolean isFarAwayFromCamera() {
         Minecraft minecraft = Minecraft.getInstance();
         return minecraft.gameRenderer.getMainCamera().getPosition().distanceToSqr(this.x, this.y, this.z) >= 256.0D;
      }

      private void createParticle(double d0, double d1, double d2, double d3, double d4, double d5, int[] aint, int[] aint1, boolean flag, boolean flag1) {
         FireworkParticles.SparkParticle fireworkparticles_sparkparticle = (FireworkParticles.SparkParticle)this.engine.createParticle(ParticleTypes.FIREWORK, d0, d1, d2, d3, d4, d5);
         fireworkparticles_sparkparticle.setTrail(flag);
         fireworkparticles_sparkparticle.setFlicker(flag1);
         fireworkparticles_sparkparticle.setAlpha(0.99F);
         int i = this.random.nextInt(aint.length);
         fireworkparticles_sparkparticle.setColor(aint[i]);
         if (aint1.length > 0) {
            fireworkparticles_sparkparticle.setFadeColor(Util.getRandom(aint1, this.random));
         }

      }

      private void createParticleBall(double d0, int i, int[] aint, int[] aint1, boolean flag, boolean flag1) {
         double d1 = this.x;
         double d2 = this.y;
         double d3 = this.z;

         for(int j = -i; j <= i; ++j) {
            for(int k = -i; k <= i; ++k) {
               for(int l = -i; l <= i; ++l) {
                  double d4 = (double)k + (this.random.nextDouble() - this.random.nextDouble()) * 0.5D;
                  double d5 = (double)j + (this.random.nextDouble() - this.random.nextDouble()) * 0.5D;
                  double d6 = (double)l + (this.random.nextDouble() - this.random.nextDouble()) * 0.5D;
                  double d7 = Math.sqrt(d4 * d4 + d5 * d5 + d6 * d6) / d0 + this.random.nextGaussian() * 0.05D;
                  this.createParticle(d1, d2, d3, d4 / d7, d5 / d7, d6 / d7, aint, aint1, flag, flag1);
                  if (j != -i && j != i && k != -i && k != i) {
                     l += i * 2 - 1;
                  }
               }
            }
         }

      }

      private void createParticleShape(double d0, double[][] adouble, int[] aint, int[] aint1, boolean flag, boolean flag1, boolean flag2) {
         double d1 = adouble[0][0];
         double d2 = adouble[0][1];
         this.createParticle(this.x, this.y, this.z, d1 * d0, d2 * d0, 0.0D, aint, aint1, flag, flag1);
         float f = this.random.nextFloat() * (float)Math.PI;
         double d3 = flag2 ? 0.034D : 0.34D;

         for(int i = 0; i < 3; ++i) {
            double d4 = (double)f + (double)((float)i * (float)Math.PI) * d3;
            double d5 = d1;
            double d6 = d2;

            for(int j = 1; j < adouble.length; ++j) {
               double d7 = adouble[j][0];
               double d8 = adouble[j][1];

               for(double d9 = 0.25D; d9 <= 1.0D; d9 += 0.25D) {
                  double d10 = Mth.lerp(d9, d5, d7) * d0;
                  double d11 = Mth.lerp(d9, d6, d8) * d0;
                  double d12 = d10 * Math.sin(d4);
                  d10 *= Math.cos(d4);

                  for(double d13 = -1.0D; d13 <= 1.0D; d13 += 2.0D) {
                     this.createParticle(this.x, this.y, this.z, d10 * d13, d11, d12 * d13, aint, aint1, flag, flag1);
                  }
               }

               d5 = d7;
               d6 = d8;
            }
         }

      }

      private void createParticleBurst(int[] aint, int[] aint1, boolean flag, boolean flag1) {
         double d0 = this.random.nextGaussian() * 0.05D;
         double d1 = this.random.nextGaussian() * 0.05D;

         for(int i = 0; i < 70; ++i) {
            double d2 = this.xd * 0.5D + this.random.nextGaussian() * 0.15D + d0;
            double d3 = this.zd * 0.5D + this.random.nextGaussian() * 0.15D + d1;
            double d4 = this.yd * 0.5D + this.random.nextDouble() * 0.5D;
            this.createParticle(this.x, this.y, this.z, d2, d4, d3, aint, aint1, flag, flag1);
         }

      }
   }
}
