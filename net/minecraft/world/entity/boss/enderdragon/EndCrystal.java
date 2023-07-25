package net.minecraft.world.entity.boss.enderdragon;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.dimension.end.EndDragonFight;

public class EndCrystal extends Entity {
   private static final EntityDataAccessor<Optional<BlockPos>> DATA_BEAM_TARGET = SynchedEntityData.defineId(EndCrystal.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
   private static final EntityDataAccessor<Boolean> DATA_SHOW_BOTTOM = SynchedEntityData.defineId(EndCrystal.class, EntityDataSerializers.BOOLEAN);
   public int time;

   public EndCrystal(EntityType<? extends EndCrystal> entitytype, Level level) {
      super(entitytype, level);
      this.blocksBuilding = true;
      this.time = this.random.nextInt(100000);
   }

   public EndCrystal(Level level, double d0, double d1, double d2) {
      this(EntityType.END_CRYSTAL, level);
      this.setPos(d0, d1, d2);
   }

   protected Entity.MovementEmission getMovementEmission() {
      return Entity.MovementEmission.NONE;
   }

   protected void defineSynchedData() {
      this.getEntityData().define(DATA_BEAM_TARGET, Optional.empty());
      this.getEntityData().define(DATA_SHOW_BOTTOM, true);
   }

   public void tick() {
      ++this.time;
      if (this.level() instanceof ServerLevel) {
         BlockPos blockpos = this.blockPosition();
         if (((ServerLevel)this.level()).getDragonFight() != null && this.level().getBlockState(blockpos).isAir()) {
            this.level().setBlockAndUpdate(blockpos, BaseFireBlock.getState(this.level(), blockpos));
         }
      }

   }

   protected void addAdditionalSaveData(CompoundTag compoundtag) {
      if (this.getBeamTarget() != null) {
         compoundtag.put("BeamTarget", NbtUtils.writeBlockPos(this.getBeamTarget()));
      }

      compoundtag.putBoolean("ShowBottom", this.showsBottom());
   }

   protected void readAdditionalSaveData(CompoundTag compoundtag) {
      if (compoundtag.contains("BeamTarget", 10)) {
         this.setBeamTarget(NbtUtils.readBlockPos(compoundtag.getCompound("BeamTarget")));
      }

      if (compoundtag.contains("ShowBottom", 1)) {
         this.setShowBottom(compoundtag.getBoolean("ShowBottom"));
      }

   }

   public boolean isPickable() {
      return true;
   }

   public boolean hurt(DamageSource damagesource, float f) {
      if (this.isInvulnerableTo(damagesource)) {
         return false;
      } else if (damagesource.getEntity() instanceof EnderDragon) {
         return false;
      } else {
         if (!this.isRemoved() && !this.level().isClientSide) {
            this.remove(Entity.RemovalReason.KILLED);
            if (!damagesource.is(DamageTypeTags.IS_EXPLOSION)) {
               DamageSource damagesource1 = damagesource.getEntity() != null ? this.damageSources().explosion(this, damagesource.getEntity()) : null;
               this.level().explode(this, damagesource1, (ExplosionDamageCalculator)null, this.getX(), this.getY(), this.getZ(), 6.0F, false, Level.ExplosionInteraction.BLOCK);
            }

            this.onDestroyedBy(damagesource);
         }

         return true;
      }
   }

   public void kill() {
      this.onDestroyedBy(this.damageSources().generic());
      super.kill();
   }

   private void onDestroyedBy(DamageSource damagesource) {
      if (this.level() instanceof ServerLevel) {
         EndDragonFight enddragonfight = ((ServerLevel)this.level()).getDragonFight();
         if (enddragonfight != null) {
            enddragonfight.onCrystalDestroyed(this, damagesource);
         }
      }

   }

   public void setBeamTarget(@Nullable BlockPos blockpos) {
      this.getEntityData().set(DATA_BEAM_TARGET, Optional.ofNullable(blockpos));
   }

   @Nullable
   public BlockPos getBeamTarget() {
      return this.getEntityData().get(DATA_BEAM_TARGET).orElse((BlockPos)null);
   }

   public void setShowBottom(boolean flag) {
      this.getEntityData().set(DATA_SHOW_BOTTOM, flag);
   }

   public boolean showsBottom() {
      return this.getEntityData().get(DATA_SHOW_BOTTOM);
   }

   public boolean shouldRenderAtSqrDistance(double d0) {
      return super.shouldRenderAtSqrDistance(d0) || this.getBeamTarget() != null;
   }

   public ItemStack getPickResult() {
      return new ItemStack(Items.END_CRYSTAL);
   }
}
