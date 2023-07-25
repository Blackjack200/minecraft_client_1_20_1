package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class TrackingEmitter extends NoRenderParticle {
   private final Entity entity;
   private int life;
   private final int lifeTime;
   private final ParticleOptions particleType;

   public TrackingEmitter(ClientLevel clientlevel, Entity entity, ParticleOptions particleoptions) {
      this(clientlevel, entity, particleoptions, 3);
   }

   public TrackingEmitter(ClientLevel clientlevel, Entity entity, ParticleOptions particleoptions, int i) {
      this(clientlevel, entity, particleoptions, i, entity.getDeltaMovement());
   }

   private TrackingEmitter(ClientLevel clientlevel, Entity entity, ParticleOptions particleoptions, int i, Vec3 vec3) {
      super(clientlevel, entity.getX(), entity.getY(0.5D), entity.getZ(), vec3.x, vec3.y, vec3.z);
      this.entity = entity;
      this.lifeTime = i;
      this.particleType = particleoptions;
      this.tick();
   }

   public void tick() {
      for(int i = 0; i < 16; ++i) {
         double d0 = (double)(this.random.nextFloat() * 2.0F - 1.0F);
         double d1 = (double)(this.random.nextFloat() * 2.0F - 1.0F);
         double d2 = (double)(this.random.nextFloat() * 2.0F - 1.0F);
         if (!(d0 * d0 + d1 * d1 + d2 * d2 > 1.0D)) {
            double d3 = this.entity.getX(d0 / 4.0D);
            double d4 = this.entity.getY(0.5D + d1 / 4.0D);
            double d5 = this.entity.getZ(d2 / 4.0D);
            this.level.addParticle(this.particleType, false, d3, d4, d5, d0, d1 + 0.2D, d2);
         }
      }

      ++this.life;
      if (this.life >= this.lifeTime) {
         this.remove();
      }

   }
}
