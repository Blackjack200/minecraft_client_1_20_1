package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class DripParticle extends TextureSheetParticle {
   private final Fluid type;
   protected boolean isGlowing;

   DripParticle(ClientLevel clientlevel, double d0, double d1, double d2, Fluid fluid) {
      super(clientlevel, d0, d1, d2);
      this.setSize(0.01F, 0.01F);
      this.gravity = 0.06F;
      this.type = fluid;
   }

   protected Fluid getType() {
      return this.type;
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
   }

   public int getLightColor(float f) {
      return this.isGlowing ? 240 : super.getLightColor(f);
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      this.preMoveUpdate();
      if (!this.removed) {
         this.yd -= (double)this.gravity;
         this.move(this.xd, this.yd, this.zd);
         this.postMoveUpdate();
         if (!this.removed) {
            this.xd *= (double)0.98F;
            this.yd *= (double)0.98F;
            this.zd *= (double)0.98F;
            if (this.type != Fluids.EMPTY) {
               BlockPos blockpos = BlockPos.containing(this.x, this.y, this.z);
               FluidState fluidstate = this.level.getFluidState(blockpos);
               if (fluidstate.getType() == this.type && this.y < (double)((float)blockpos.getY() + fluidstate.getHeight(this.level, blockpos))) {
                  this.remove();
               }

            }
         }
      }
   }

   protected void preMoveUpdate() {
      if (this.lifetime-- <= 0) {
         this.remove();
      }

   }

   protected void postMoveUpdate() {
   }

   public static TextureSheetParticle createWaterHangParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
      DripParticle dripparticle = new DripParticle.DripHangParticle(clientlevel, d0, d1, d2, Fluids.WATER, ParticleTypes.FALLING_WATER);
      dripparticle.setColor(0.2F, 0.3F, 1.0F);
      return dripparticle;
   }

   public static TextureSheetParticle createWaterFallParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
      DripParticle dripparticle = new DripParticle.FallAndLandParticle(clientlevel, d0, d1, d2, Fluids.WATER, ParticleTypes.SPLASH);
      dripparticle.setColor(0.2F, 0.3F, 1.0F);
      return dripparticle;
   }

   public static TextureSheetParticle createLavaHangParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
      return new DripParticle.CoolingDripHangParticle(clientlevel, d0, d1, d2, Fluids.LAVA, ParticleTypes.FALLING_LAVA);
   }

   public static TextureSheetParticle createLavaFallParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
      DripParticle dripparticle = new DripParticle.FallAndLandParticle(clientlevel, d0, d1, d2, Fluids.LAVA, ParticleTypes.LANDING_LAVA);
      dripparticle.setColor(1.0F, 0.2857143F, 0.083333336F);
      return dripparticle;
   }

   public static TextureSheetParticle createLavaLandParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
      DripParticle dripparticle = new DripParticle.DripLandParticle(clientlevel, d0, d1, d2, Fluids.LAVA);
      dripparticle.setColor(1.0F, 0.2857143F, 0.083333336F);
      return dripparticle;
   }

   public static TextureSheetParticle createHoneyHangParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
      DripParticle.DripHangParticle dripparticle_driphangparticle = new DripParticle.DripHangParticle(clientlevel, d0, d1, d2, Fluids.EMPTY, ParticleTypes.FALLING_HONEY);
      dripparticle_driphangparticle.gravity *= 0.01F;
      dripparticle_driphangparticle.lifetime = 100;
      dripparticle_driphangparticle.setColor(0.622F, 0.508F, 0.082F);
      return dripparticle_driphangparticle;
   }

   public static TextureSheetParticle createHoneyFallParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
      DripParticle dripparticle = new DripParticle.HoneyFallAndLandParticle(clientlevel, d0, d1, d2, Fluids.EMPTY, ParticleTypes.LANDING_HONEY);
      dripparticle.gravity = 0.01F;
      dripparticle.setColor(0.582F, 0.448F, 0.082F);
      return dripparticle;
   }

   public static TextureSheetParticle createHoneyLandParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
      DripParticle dripparticle = new DripParticle.DripLandParticle(clientlevel, d0, d1, d2, Fluids.EMPTY);
      dripparticle.lifetime = (int)(128.0D / (Math.random() * 0.8D + 0.2D));
      dripparticle.setColor(0.522F, 0.408F, 0.082F);
      return dripparticle;
   }

   public static TextureSheetParticle createDripstoneWaterHangParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
      DripParticle dripparticle = new DripParticle.DripHangParticle(clientlevel, d0, d1, d2, Fluids.WATER, ParticleTypes.FALLING_DRIPSTONE_WATER);
      dripparticle.setColor(0.2F, 0.3F, 1.0F);
      return dripparticle;
   }

   public static TextureSheetParticle createDripstoneWaterFallParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
      DripParticle dripparticle = new DripParticle.DripstoneFallAndLandParticle(clientlevel, d0, d1, d2, Fluids.WATER, ParticleTypes.SPLASH);
      dripparticle.setColor(0.2F, 0.3F, 1.0F);
      return dripparticle;
   }

   public static TextureSheetParticle createDripstoneLavaHangParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
      return new DripParticle.CoolingDripHangParticle(clientlevel, d0, d1, d2, Fluids.LAVA, ParticleTypes.FALLING_DRIPSTONE_LAVA);
   }

   public static TextureSheetParticle createDripstoneLavaFallParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
      DripParticle dripparticle = new DripParticle.DripstoneFallAndLandParticle(clientlevel, d0, d1, d2, Fluids.LAVA, ParticleTypes.LANDING_LAVA);
      dripparticle.setColor(1.0F, 0.2857143F, 0.083333336F);
      return dripparticle;
   }

   public static TextureSheetParticle createNectarFallParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
      DripParticle dripparticle = new DripParticle.FallingParticle(clientlevel, d0, d1, d2, Fluids.EMPTY);
      dripparticle.lifetime = (int)(16.0D / (Math.random() * 0.8D + 0.2D));
      dripparticle.gravity = 0.007F;
      dripparticle.setColor(0.92F, 0.782F, 0.72F);
      return dripparticle;
   }

   public static TextureSheetParticle createSporeBlossomFallParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
      int i = (int)(64.0F / Mth.randomBetween(clientlevel.getRandom(), 0.1F, 0.9F));
      DripParticle dripparticle = new DripParticle.FallingParticle(clientlevel, d0, d1, d2, Fluids.EMPTY, i);
      dripparticle.gravity = 0.005F;
      dripparticle.setColor(0.32F, 0.5F, 0.22F);
      return dripparticle;
   }

   public static TextureSheetParticle createObsidianTearHangParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
      DripParticle.DripHangParticle dripparticle_driphangparticle = new DripParticle.DripHangParticle(clientlevel, d0, d1, d2, Fluids.EMPTY, ParticleTypes.FALLING_OBSIDIAN_TEAR);
      dripparticle_driphangparticle.isGlowing = true;
      dripparticle_driphangparticle.gravity *= 0.01F;
      dripparticle_driphangparticle.lifetime = 100;
      dripparticle_driphangparticle.setColor(0.51171875F, 0.03125F, 0.890625F);
      return dripparticle_driphangparticle;
   }

   public static TextureSheetParticle createObsidianTearFallParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
      DripParticle dripparticle = new DripParticle.FallAndLandParticle(clientlevel, d0, d1, d2, Fluids.EMPTY, ParticleTypes.LANDING_OBSIDIAN_TEAR);
      dripparticle.isGlowing = true;
      dripparticle.gravity = 0.01F;
      dripparticle.setColor(0.51171875F, 0.03125F, 0.890625F);
      return dripparticle;
   }

   public static TextureSheetParticle createObsidianTearLandParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
      DripParticle dripparticle = new DripParticle.DripLandParticle(clientlevel, d0, d1, d2, Fluids.EMPTY);
      dripparticle.isGlowing = true;
      dripparticle.lifetime = (int)(28.0D / (Math.random() * 0.8D + 0.2D));
      dripparticle.setColor(0.51171875F, 0.03125F, 0.890625F);
      return dripparticle;
   }

   static class CoolingDripHangParticle extends DripParticle.DripHangParticle {
      CoolingDripHangParticle(ClientLevel clientlevel, double d0, double d1, double d2, Fluid fluid, ParticleOptions particleoptions) {
         super(clientlevel, d0, d1, d2, fluid, particleoptions);
      }

      protected void preMoveUpdate() {
         this.rCol = 1.0F;
         this.gCol = 16.0F / (float)(40 - this.lifetime + 16);
         this.bCol = 4.0F / (float)(40 - this.lifetime + 8);
         super.preMoveUpdate();
      }
   }

   static class DripHangParticle extends DripParticle {
      private final ParticleOptions fallingParticle;

      DripHangParticle(ClientLevel clientlevel, double d0, double d1, double d2, Fluid fluid, ParticleOptions particleoptions) {
         super(clientlevel, d0, d1, d2, fluid);
         this.fallingParticle = particleoptions;
         this.gravity *= 0.02F;
         this.lifetime = 40;
      }

      protected void preMoveUpdate() {
         if (this.lifetime-- <= 0) {
            this.remove();
            this.level.addParticle(this.fallingParticle, this.x, this.y, this.z, this.xd, this.yd, this.zd);
         }

      }

      protected void postMoveUpdate() {
         this.xd *= 0.02D;
         this.yd *= 0.02D;
         this.zd *= 0.02D;
      }
   }

   static class DripLandParticle extends DripParticle {
      DripLandParticle(ClientLevel clientlevel, double d0, double d1, double d2, Fluid fluid) {
         super(clientlevel, d0, d1, d2, fluid);
         this.lifetime = (int)(16.0D / (Math.random() * 0.8D + 0.2D));
      }
   }

   static class DripstoneFallAndLandParticle extends DripParticle.FallAndLandParticle {
      DripstoneFallAndLandParticle(ClientLevel clientlevel, double d0, double d1, double d2, Fluid fluid, ParticleOptions particleoptions) {
         super(clientlevel, d0, d1, d2, fluid, particleoptions);
      }

      protected void postMoveUpdate() {
         if (this.onGround) {
            this.remove();
            this.level.addParticle(this.landParticle, this.x, this.y, this.z, 0.0D, 0.0D, 0.0D);
            SoundEvent soundevent = this.getType() == Fluids.LAVA ? SoundEvents.POINTED_DRIPSTONE_DRIP_LAVA : SoundEvents.POINTED_DRIPSTONE_DRIP_WATER;
            float f = Mth.randomBetween(this.random, 0.3F, 1.0F);
            this.level.playLocalSound(this.x, this.y, this.z, soundevent, SoundSource.BLOCKS, f, 1.0F, false);
         }

      }
   }

   static class FallAndLandParticle extends DripParticle.FallingParticle {
      protected final ParticleOptions landParticle;

      FallAndLandParticle(ClientLevel clientlevel, double d0, double d1, double d2, Fluid fluid, ParticleOptions particleoptions) {
         super(clientlevel, d0, d1, d2, fluid);
         this.landParticle = particleoptions;
      }

      protected void postMoveUpdate() {
         if (this.onGround) {
            this.remove();
            this.level.addParticle(this.landParticle, this.x, this.y, this.z, 0.0D, 0.0D, 0.0D);
         }

      }
   }

   static class FallingParticle extends DripParticle {
      FallingParticle(ClientLevel clientlevel, double d0, double d1, double d2, Fluid fluid) {
         this(clientlevel, d0, d1, d2, fluid, (int)(64.0D / (Math.random() * 0.8D + 0.2D)));
      }

      FallingParticle(ClientLevel clientlevel, double d0, double d1, double d2, Fluid fluid, int i) {
         super(clientlevel, d0, d1, d2, fluid);
         this.lifetime = i;
      }

      protected void postMoveUpdate() {
         if (this.onGround) {
            this.remove();
         }

      }
   }

   static class HoneyFallAndLandParticle extends DripParticle.FallAndLandParticle {
      HoneyFallAndLandParticle(ClientLevel clientlevel, double d0, double d1, double d2, Fluid fluid, ParticleOptions particleoptions) {
         super(clientlevel, d0, d1, d2, fluid, particleoptions);
      }

      protected void postMoveUpdate() {
         if (this.onGround) {
            this.remove();
            this.level.addParticle(this.landParticle, this.x, this.y, this.z, 0.0D, 0.0D, 0.0D);
            float f = Mth.randomBetween(this.random, 0.3F, 1.0F);
            this.level.playLocalSound(this.x, this.y, this.z, SoundEvents.BEEHIVE_DRIP, SoundSource.BLOCKS, f, 1.0F, false);
         }

      }
   }
}
