package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleGroup;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public abstract class Particle {
   private static final AABB INITIAL_AABB = new AABB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
   private static final double MAXIMUM_COLLISION_VELOCITY_SQUARED = Mth.square(100.0D);
   protected final ClientLevel level;
   protected double xo;
   protected double yo;
   protected double zo;
   protected double x;
   protected double y;
   protected double z;
   protected double xd;
   protected double yd;
   protected double zd;
   private AABB bb = INITIAL_AABB;
   protected boolean onGround;
   protected boolean hasPhysics = true;
   private boolean stoppedByCollision;
   protected boolean removed;
   protected float bbWidth = 0.6F;
   protected float bbHeight = 1.8F;
   protected final RandomSource random = RandomSource.create();
   protected int age;
   protected int lifetime;
   protected float gravity;
   protected float rCol = 1.0F;
   protected float gCol = 1.0F;
   protected float bCol = 1.0F;
   protected float alpha = 1.0F;
   protected float roll;
   protected float oRoll;
   protected float friction = 0.98F;
   protected boolean speedUpWhenYMotionIsBlocked = false;

   protected Particle(ClientLevel clientlevel, double d0, double d1, double d2) {
      this.level = clientlevel;
      this.setSize(0.2F, 0.2F);
      this.setPos(d0, d1, d2);
      this.xo = d0;
      this.yo = d1;
      this.zo = d2;
      this.lifetime = (int)(4.0F / (this.random.nextFloat() * 0.9F + 0.1F));
   }

   public Particle(ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
      this(clientlevel, d0, d1, d2);
      this.xd = d3 + (Math.random() * 2.0D - 1.0D) * (double)0.4F;
      this.yd = d4 + (Math.random() * 2.0D - 1.0D) * (double)0.4F;
      this.zd = d5 + (Math.random() * 2.0D - 1.0D) * (double)0.4F;
      double d6 = (Math.random() + Math.random() + 1.0D) * (double)0.15F;
      double d7 = Math.sqrt(this.xd * this.xd + this.yd * this.yd + this.zd * this.zd);
      this.xd = this.xd / d7 * d6 * (double)0.4F;
      this.yd = this.yd / d7 * d6 * (double)0.4F + (double)0.1F;
      this.zd = this.zd / d7 * d6 * (double)0.4F;
   }

   public Particle setPower(float f) {
      this.xd *= (double)f;
      this.yd = (this.yd - (double)0.1F) * (double)f + (double)0.1F;
      this.zd *= (double)f;
      return this;
   }

   public void setParticleSpeed(double d0, double d1, double d2) {
      this.xd = d0;
      this.yd = d1;
      this.zd = d2;
   }

   public Particle scale(float f) {
      this.setSize(0.2F * f, 0.2F * f);
      return this;
   }

   public void setColor(float f, float f1, float f2) {
      this.rCol = f;
      this.gCol = f1;
      this.bCol = f2;
   }

   protected void setAlpha(float f) {
      this.alpha = f;
   }

   public void setLifetime(int i) {
      this.lifetime = i;
   }

   public int getLifetime() {
      return this.lifetime;
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      if (this.age++ >= this.lifetime) {
         this.remove();
      } else {
         this.yd -= 0.04D * (double)this.gravity;
         this.move(this.xd, this.yd, this.zd);
         if (this.speedUpWhenYMotionIsBlocked && this.y == this.yo) {
            this.xd *= 1.1D;
            this.zd *= 1.1D;
         }

         this.xd *= (double)this.friction;
         this.yd *= (double)this.friction;
         this.zd *= (double)this.friction;
         if (this.onGround) {
            this.xd *= (double)0.7F;
            this.zd *= (double)0.7F;
         }

      }
   }

   public abstract void render(VertexConsumer vertexconsumer, Camera camera, float f);

   public abstract ParticleRenderType getRenderType();

   public String toString() {
      return this.getClass().getSimpleName() + ", Pos (" + this.x + "," + this.y + "," + this.z + "), RGBA (" + this.rCol + "," + this.gCol + "," + this.bCol + "," + this.alpha + "), Age " + this.age;
   }

   public void remove() {
      this.removed = true;
   }

   protected void setSize(float f, float f1) {
      if (f != this.bbWidth || f1 != this.bbHeight) {
         this.bbWidth = f;
         this.bbHeight = f1;
         AABB aabb = this.getBoundingBox();
         double d0 = (aabb.minX + aabb.maxX - (double)f) / 2.0D;
         double d1 = (aabb.minZ + aabb.maxZ - (double)f) / 2.0D;
         this.setBoundingBox(new AABB(d0, aabb.minY, d1, d0 + (double)this.bbWidth, aabb.minY + (double)this.bbHeight, d1 + (double)this.bbWidth));
      }

   }

   public void setPos(double d0, double d1, double d2) {
      this.x = d0;
      this.y = d1;
      this.z = d2;
      float f = this.bbWidth / 2.0F;
      float f1 = this.bbHeight;
      this.setBoundingBox(new AABB(d0 - (double)f, d1, d2 - (double)f, d0 + (double)f, d1 + (double)f1, d2 + (double)f));
   }

   public void move(double d0, double d1, double d2) {
      if (!this.stoppedByCollision) {
         double d3 = d0;
         double d4 = d1;
         double d5 = d2;
         if (this.hasPhysics && (d0 != 0.0D || d1 != 0.0D || d2 != 0.0D) && d0 * d0 + d1 * d1 + d2 * d2 < MAXIMUM_COLLISION_VELOCITY_SQUARED) {
            Vec3 vec3 = Entity.collideBoundingBox((Entity)null, new Vec3(d0, d1, d2), this.getBoundingBox(), this.level, List.of());
            d0 = vec3.x;
            d1 = vec3.y;
            d2 = vec3.z;
         }

         if (d0 != 0.0D || d1 != 0.0D || d2 != 0.0D) {
            this.setBoundingBox(this.getBoundingBox().move(d0, d1, d2));
            this.setLocationFromBoundingbox();
         }

         if (Math.abs(d4) >= (double)1.0E-5F && Math.abs(d1) < (double)1.0E-5F) {
            this.stoppedByCollision = true;
         }

         this.onGround = d4 != d1 && d4 < 0.0D;
         if (d3 != d0) {
            this.xd = 0.0D;
         }

         if (d5 != d2) {
            this.zd = 0.0D;
         }

      }
   }

   protected void setLocationFromBoundingbox() {
      AABB aabb = this.getBoundingBox();
      this.x = (aabb.minX + aabb.maxX) / 2.0D;
      this.y = aabb.minY;
      this.z = (aabb.minZ + aabb.maxZ) / 2.0D;
   }

   protected int getLightColor(float f) {
      BlockPos blockpos = BlockPos.containing(this.x, this.y, this.z);
      return this.level.hasChunkAt(blockpos) ? LevelRenderer.getLightColor(this.level, blockpos) : 0;
   }

   public boolean isAlive() {
      return !this.removed;
   }

   public AABB getBoundingBox() {
      return this.bb;
   }

   public void setBoundingBox(AABB aabb) {
      this.bb = aabb;
   }

   public Optional<ParticleGroup> getParticleGroup() {
      return Optional.empty();
   }
}
