package net.minecraft.world.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class LlamaSpit extends Projectile {
   public LlamaSpit(EntityType<? extends LlamaSpit> entitytype, Level level) {
      super(entitytype, level);
   }

   public LlamaSpit(Level level, Llama llama) {
      this(EntityType.LLAMA_SPIT, level);
      this.setOwner(llama);
      this.setPos(llama.getX() - (double)(llama.getBbWidth() + 1.0F) * 0.5D * (double)Mth.sin(llama.yBodyRot * ((float)Math.PI / 180F)), llama.getEyeY() - (double)0.1F, llama.getZ() + (double)(llama.getBbWidth() + 1.0F) * 0.5D * (double)Mth.cos(llama.yBodyRot * ((float)Math.PI / 180F)));
   }

   public void tick() {
      super.tick();
      Vec3 vec3 = this.getDeltaMovement();
      HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
      this.onHit(hitresult);
      double d0 = this.getX() + vec3.x;
      double d1 = this.getY() + vec3.y;
      double d2 = this.getZ() + vec3.z;
      this.updateRotation();
      float f = 0.99F;
      float f1 = 0.06F;
      if (this.level().getBlockStates(this.getBoundingBox()).noneMatch(BlockBehaviour.BlockStateBase::isAir)) {
         this.discard();
      } else if (this.isInWaterOrBubble()) {
         this.discard();
      } else {
         this.setDeltaMovement(vec3.scale((double)0.99F));
         if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, (double)-0.06F, 0.0D));
         }

         this.setPos(d0, d1, d2);
      }
   }

   protected void onHitEntity(EntityHitResult entityhitresult) {
      super.onHitEntity(entityhitresult);
      Entity var3 = this.getOwner();
      if (var3 instanceof LivingEntity livingentity) {
         entityhitresult.getEntity().hurt(this.damageSources().mobProjectile(this, livingentity), 1.0F);
      }

   }

   protected void onHitBlock(BlockHitResult blockhitresult) {
      super.onHitBlock(blockhitresult);
      if (!this.level().isClientSide) {
         this.discard();
      }

   }

   protected void defineSynchedData() {
   }

   public void recreateFromPacket(ClientboundAddEntityPacket clientboundaddentitypacket) {
      super.recreateFromPacket(clientboundaddentitypacket);
      double d0 = clientboundaddentitypacket.getXa();
      double d1 = clientboundaddentitypacket.getYa();
      double d2 = clientboundaddentitypacket.getZa();

      for(int i = 0; i < 7; ++i) {
         double d3 = 0.4D + 0.1D * (double)i;
         this.level().addParticle(ParticleTypes.SPIT, this.getX(), this.getY(), this.getZ(), d0 * d3, d1, d2 * d3);
      }

      this.setDeltaMovement(d0, d1, d2);
   }
}
