package net.minecraft.world.entity.projectile;

import com.google.common.base.MoreObjects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public abstract class Projectile extends Entity implements TraceableEntity {
   @Nullable
   private UUID ownerUUID;
   @Nullable
   private Entity cachedOwner;
   private boolean leftOwner;
   private boolean hasBeenShot;

   Projectile(EntityType<? extends Projectile> entitytype, Level level) {
      super(entitytype, level);
   }

   public void setOwner(@Nullable Entity entity) {
      if (entity != null) {
         this.ownerUUID = entity.getUUID();
         this.cachedOwner = entity;
      }

   }

   @Nullable
   public Entity getOwner() {
      if (this.cachedOwner != null && !this.cachedOwner.isRemoved()) {
         return this.cachedOwner;
      } else if (this.ownerUUID != null && this.level() instanceof ServerLevel) {
         this.cachedOwner = ((ServerLevel)this.level()).getEntity(this.ownerUUID);
         return this.cachedOwner;
      } else {
         return null;
      }
   }

   public Entity getEffectSource() {
      return MoreObjects.firstNonNull(this.getOwner(), this);
   }

   protected void addAdditionalSaveData(CompoundTag compoundtag) {
      if (this.ownerUUID != null) {
         compoundtag.putUUID("Owner", this.ownerUUID);
      }

      if (this.leftOwner) {
         compoundtag.putBoolean("LeftOwner", true);
      }

      compoundtag.putBoolean("HasBeenShot", this.hasBeenShot);
   }

   protected boolean ownedBy(Entity entity) {
      return entity.getUUID().equals(this.ownerUUID);
   }

   protected void readAdditionalSaveData(CompoundTag compoundtag) {
      if (compoundtag.hasUUID("Owner")) {
         this.ownerUUID = compoundtag.getUUID("Owner");
         this.cachedOwner = null;
      }

      this.leftOwner = compoundtag.getBoolean("LeftOwner");
      this.hasBeenShot = compoundtag.getBoolean("HasBeenShot");
   }

   public void tick() {
      if (!this.hasBeenShot) {
         this.gameEvent(GameEvent.PROJECTILE_SHOOT, this.getOwner());
         this.hasBeenShot = true;
      }

      if (!this.leftOwner) {
         this.leftOwner = this.checkLeftOwner();
      }

      super.tick();
   }

   private boolean checkLeftOwner() {
      Entity entity = this.getOwner();
      if (entity != null) {
         for(Entity entity1 : this.level().getEntities(this, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0D), (entity2) -> !entity2.isSpectator() && entity2.isPickable())) {
            if (entity1.getRootVehicle() == entity.getRootVehicle()) {
               return false;
            }
         }
      }

      return true;
   }

   public void shoot(double d0, double d1, double d2, float f, float f1) {
      Vec3 vec3 = (new Vec3(d0, d1, d2)).normalize().add(this.random.triangle(0.0D, 0.0172275D * (double)f1), this.random.triangle(0.0D, 0.0172275D * (double)f1), this.random.triangle(0.0D, 0.0172275D * (double)f1)).scale((double)f);
      this.setDeltaMovement(vec3);
      double d3 = vec3.horizontalDistance();
      this.setYRot((float)(Mth.atan2(vec3.x, vec3.z) * (double)(180F / (float)Math.PI)));
      this.setXRot((float)(Mth.atan2(vec3.y, d3) * (double)(180F / (float)Math.PI)));
      this.yRotO = this.getYRot();
      this.xRotO = this.getXRot();
   }

   public void shootFromRotation(Entity entity, float f, float f1, float f2, float f3, float f4) {
      float f5 = -Mth.sin(f1 * ((float)Math.PI / 180F)) * Mth.cos(f * ((float)Math.PI / 180F));
      float f6 = -Mth.sin((f + f2) * ((float)Math.PI / 180F));
      float f7 = Mth.cos(f1 * ((float)Math.PI / 180F)) * Mth.cos(f * ((float)Math.PI / 180F));
      this.shoot((double)f5, (double)f6, (double)f7, f3, f4);
      Vec3 vec3 = entity.getDeltaMovement();
      this.setDeltaMovement(this.getDeltaMovement().add(vec3.x, entity.onGround() ? 0.0D : vec3.y, vec3.z));
   }

   protected void onHit(HitResult hitresult) {
      HitResult.Type hitresult_type = hitresult.getType();
      if (hitresult_type == HitResult.Type.ENTITY) {
         this.onHitEntity((EntityHitResult)hitresult);
         this.level().gameEvent(GameEvent.PROJECTILE_LAND, hitresult.getLocation(), GameEvent.Context.of(this, (BlockState)null));
      } else if (hitresult_type == HitResult.Type.BLOCK) {
         BlockHitResult blockhitresult = (BlockHitResult)hitresult;
         this.onHitBlock(blockhitresult);
         BlockPos blockpos = blockhitresult.getBlockPos();
         this.level().gameEvent(GameEvent.PROJECTILE_LAND, blockpos, GameEvent.Context.of(this, this.level().getBlockState(blockpos)));
      }

   }

   protected void onHitEntity(EntityHitResult entityhitresult) {
   }

   protected void onHitBlock(BlockHitResult blockhitresult) {
      BlockState blockstate = this.level().getBlockState(blockhitresult.getBlockPos());
      blockstate.onProjectileHit(this.level(), blockstate, blockhitresult, this);
   }

   public void lerpMotion(double d0, double d1, double d2) {
      this.setDeltaMovement(d0, d1, d2);
      if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
         double d3 = Math.sqrt(d0 * d0 + d2 * d2);
         this.setXRot((float)(Mth.atan2(d1, d3) * (double)(180F / (float)Math.PI)));
         this.setYRot((float)(Mth.atan2(d0, d2) * (double)(180F / (float)Math.PI)));
         this.xRotO = this.getXRot();
         this.yRotO = this.getYRot();
         this.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
      }

   }

   protected boolean canHitEntity(Entity entity) {
      if (!entity.canBeHitByProjectile()) {
         return false;
      } else {
         Entity entity1 = this.getOwner();
         return entity1 == null || this.leftOwner || !entity1.isPassengerOfSameVehicle(entity);
      }
   }

   protected void updateRotation() {
      Vec3 vec3 = this.getDeltaMovement();
      double d0 = vec3.horizontalDistance();
      this.setXRot(lerpRotation(this.xRotO, (float)(Mth.atan2(vec3.y, d0) * (double)(180F / (float)Math.PI))));
      this.setYRot(lerpRotation(this.yRotO, (float)(Mth.atan2(vec3.x, vec3.z) * (double)(180F / (float)Math.PI))));
   }

   protected static float lerpRotation(float f, float f1) {
      while(f1 - f < -180.0F) {
         f -= 360.0F;
      }

      while(f1 - f >= 180.0F) {
         f += 360.0F;
      }

      return Mth.lerp(0.2F, f, f1);
   }

   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      Entity entity = this.getOwner();
      return new ClientboundAddEntityPacket(this, entity == null ? 0 : entity.getId());
   }

   public void recreateFromPacket(ClientboundAddEntityPacket clientboundaddentitypacket) {
      super.recreateFromPacket(clientboundaddentitypacket);
      Entity entity = this.level().getEntity(clientboundaddentitypacket.getData());
      if (entity != null) {
         this.setOwner(entity);
      }

   }

   public boolean mayInteract(Level level, BlockPos blockpos) {
      Entity entity = this.getOwner();
      if (entity instanceof Player) {
         return entity.mayInteract(level, blockpos);
      } else {
         return entity == null || level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
      }
   }
}
