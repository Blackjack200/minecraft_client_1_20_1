package net.minecraft.client.particle;

import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

public class FallingDustParticle extends TextureSheetParticle {
   private final float rotSpeed;
   private final SpriteSet sprites;

   FallingDustParticle(ClientLevel clientlevel, double d0, double d1, double d2, float f, float f1, float f2, SpriteSet spriteset) {
      super(clientlevel, d0, d1, d2);
      this.sprites = spriteset;
      this.rCol = f;
      this.gCol = f1;
      this.bCol = f2;
      float f3 = 0.9F;
      this.quadSize *= 0.67499995F;
      int i = (int)(32.0D / (Math.random() * 0.8D + 0.2D));
      this.lifetime = (int)Math.max((float)i * 0.9F, 1.0F);
      this.setSpriteFromAge(spriteset);
      this.rotSpeed = ((float)Math.random() - 0.5F) * 0.1F;
      this.roll = (float)Math.random() * ((float)Math.PI * 2F);
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
   }

   public float getQuadSize(float f) {
      return this.quadSize * Mth.clamp(((float)this.age + f) / (float)this.lifetime * 32.0F, 0.0F, 1.0F);
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      if (this.age++ >= this.lifetime) {
         this.remove();
      } else {
         this.setSpriteFromAge(this.sprites);
         this.oRoll = this.roll;
         this.roll += (float)Math.PI * this.rotSpeed * 2.0F;
         if (this.onGround) {
            this.oRoll = this.roll = 0.0F;
         }

         this.move(this.xd, this.yd, this.zd);
         this.yd -= (double)0.003F;
         this.yd = Math.max(this.yd, (double)-0.14F);
      }
   }

   public static class Provider implements ParticleProvider<BlockParticleOption> {
      private final SpriteSet sprite;

      public Provider(SpriteSet spriteset) {
         this.sprite = spriteset;
      }

      @Nullable
      public Particle createParticle(BlockParticleOption blockparticleoption, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         BlockState blockstate = blockparticleoption.getState();
         if (!blockstate.isAir() && blockstate.getRenderShape() == RenderShape.INVISIBLE) {
            return null;
         } else {
            BlockPos blockpos = BlockPos.containing(d0, d1, d2);
            int i = Minecraft.getInstance().getBlockColors().getColor(blockstate, clientlevel, blockpos);
            if (blockstate.getBlock() instanceof FallingBlock) {
               i = ((FallingBlock)blockstate.getBlock()).getDustColor(blockstate, clientlevel, blockpos);
            }

            float f = (float)(i >> 16 & 255) / 255.0F;
            float f1 = (float)(i >> 8 & 255) / 255.0F;
            float f2 = (float)(i & 255) / 255.0F;
            return new FallingDustParticle(clientlevel, d0, d1, d2, f, f1, f2, this.sprite);
         }
      }
   }
}
