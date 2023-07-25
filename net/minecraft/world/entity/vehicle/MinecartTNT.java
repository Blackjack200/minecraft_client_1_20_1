package net.minecraft.world.entity.vehicle;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class MinecartTNT extends AbstractMinecart {
   private static final byte EVENT_PRIME = 10;
   private int fuse = -1;

   public MinecartTNT(EntityType<? extends MinecartTNT> entitytype, Level level) {
      super(entitytype, level);
   }

   public MinecartTNT(Level level, double d0, double d1, double d2) {
      super(EntityType.TNT_MINECART, level, d0, d1, d2);
   }

   public AbstractMinecart.Type getMinecartType() {
      return AbstractMinecart.Type.TNT;
   }

   public BlockState getDefaultDisplayBlockState() {
      return Blocks.TNT.defaultBlockState();
   }

   public void tick() {
      super.tick();
      if (this.fuse > 0) {
         --this.fuse;
         this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5D, this.getZ(), 0.0D, 0.0D, 0.0D);
      } else if (this.fuse == 0) {
         this.explode(this.getDeltaMovement().horizontalDistanceSqr());
      }

      if (this.horizontalCollision) {
         double d0 = this.getDeltaMovement().horizontalDistanceSqr();
         if (d0 >= (double)0.01F) {
            this.explode(d0);
         }
      }

   }

   public boolean hurt(DamageSource damagesource, float f) {
      Entity entity = damagesource.getDirectEntity();
      if (entity instanceof AbstractArrow abstractarrow) {
         if (abstractarrow.isOnFire()) {
            DamageSource damagesource1 = this.damageSources().explosion(this, damagesource.getEntity());
            this.explode(damagesource1, abstractarrow.getDeltaMovement().lengthSqr());
         }
      }

      return super.hurt(damagesource, f);
   }

   public void destroy(DamageSource damagesource) {
      double d0 = this.getDeltaMovement().horizontalDistanceSqr();
      if (!damagesource.is(DamageTypeTags.IS_FIRE) && !damagesource.is(DamageTypeTags.IS_EXPLOSION) && !(d0 >= (double)0.01F)) {
         super.destroy(damagesource);
      } else {
         if (this.fuse < 0) {
            this.primeFuse();
            this.fuse = this.random.nextInt(20) + this.random.nextInt(20);
         }

      }
   }

   protected Item getDropItem() {
      return Items.TNT_MINECART;
   }

   protected void explode(double d0) {
      this.explode((DamageSource)null, d0);
   }

   protected void explode(@Nullable DamageSource damagesource, double d0) {
      if (!this.level().isClientSide) {
         double d1 = Math.sqrt(d0);
         if (d1 > 5.0D) {
            d1 = 5.0D;
         }

         this.level().explode(this, damagesource, (ExplosionDamageCalculator)null, this.getX(), this.getY(), this.getZ(), (float)(4.0D + this.random.nextDouble() * 1.5D * d1), false, Level.ExplosionInteraction.TNT);
         this.discard();
      }

   }

   public boolean causeFallDamage(float f, float f1, DamageSource damagesource) {
      if (f >= 3.0F) {
         float f2 = f / 10.0F;
         this.explode((double)(f2 * f2));
      }

      return super.causeFallDamage(f, f1, damagesource);
   }

   public void activateMinecart(int i, int j, int k, boolean flag) {
      if (flag && this.fuse < 0) {
         this.primeFuse();
      }

   }

   public void handleEntityEvent(byte b0) {
      if (b0 == 10) {
         this.primeFuse();
      } else {
         super.handleEntityEvent(b0);
      }

   }

   public void primeFuse() {
      this.fuse = 80;
      if (!this.level().isClientSide) {
         this.level().broadcastEntityEvent(this, (byte)10);
         if (!this.isSilent()) {
            this.level().playSound((Player)null, this.getX(), this.getY(), this.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
         }
      }

   }

   public int getFuse() {
      return this.fuse;
   }

   public boolean isPrimed() {
      return this.fuse > -1;
   }

   public float getBlockExplosionResistance(Explosion explosion, BlockGetter blockgetter, BlockPos blockpos, BlockState blockstate, FluidState fluidstate, float f) {
      return !this.isPrimed() || !blockstate.is(BlockTags.RAILS) && !blockgetter.getBlockState(blockpos.above()).is(BlockTags.RAILS) ? super.getBlockExplosionResistance(explosion, blockgetter, blockpos, blockstate, fluidstate, f) : 0.0F;
   }

   public boolean shouldBlockExplode(Explosion explosion, BlockGetter blockgetter, BlockPos blockpos, BlockState blockstate, float f) {
      return !this.isPrimed() || !blockstate.is(BlockTags.RAILS) && !blockgetter.getBlockState(blockpos.above()).is(BlockTags.RAILS) ? super.shouldBlockExplode(explosion, blockgetter, blockpos, blockstate, f) : false;
   }

   protected void readAdditionalSaveData(CompoundTag compoundtag) {
      super.readAdditionalSaveData(compoundtag);
      if (compoundtag.contains("TNTFuse", 99)) {
         this.fuse = compoundtag.getInt("TNTFuse");
      }

   }

   protected void addAdditionalSaveData(CompoundTag compoundtag) {
      super.addAdditionalSaveData(compoundtag);
      compoundtag.putInt("TNTFuse", this.fuse);
   }
}
