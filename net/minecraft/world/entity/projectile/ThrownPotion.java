package net.minecraft.world.entity.projectile;

import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class ThrownPotion extends ThrowableItemProjectile implements ItemSupplier {
   public static final double SPLASH_RANGE = 4.0D;
   private static final double SPLASH_RANGE_SQ = 16.0D;
   public static final Predicate<LivingEntity> WATER_SENSITIVE_OR_ON_FIRE = (livingentity) -> livingentity.isSensitiveToWater() || livingentity.isOnFire();

   public ThrownPotion(EntityType<? extends ThrownPotion> entitytype, Level level) {
      super(entitytype, level);
   }

   public ThrownPotion(Level level, LivingEntity livingentity) {
      super(EntityType.POTION, livingentity, level);
   }

   public ThrownPotion(Level level, double d0, double d1, double d2) {
      super(EntityType.POTION, d0, d1, d2, level);
   }

   protected Item getDefaultItem() {
      return Items.SPLASH_POTION;
   }

   protected float getGravity() {
      return 0.05F;
   }

   protected void onHitBlock(BlockHitResult blockhitresult) {
      super.onHitBlock(blockhitresult);
      if (!this.level().isClientSide) {
         ItemStack itemstack = this.getItem();
         Potion potion = PotionUtils.getPotion(itemstack);
         List<MobEffectInstance> list = PotionUtils.getMobEffects(itemstack);
         boolean flag = potion == Potions.WATER && list.isEmpty();
         Direction direction = blockhitresult.getDirection();
         BlockPos blockpos = blockhitresult.getBlockPos();
         BlockPos blockpos1 = blockpos.relative(direction);
         if (flag) {
            this.dowseFire(blockpos1);
            this.dowseFire(blockpos1.relative(direction.getOpposite()));

            for(Direction direction1 : Direction.Plane.HORIZONTAL) {
               this.dowseFire(blockpos1.relative(direction1));
            }
         }

      }
   }

   protected void onHit(HitResult hitresult) {
      super.onHit(hitresult);
      if (!this.level().isClientSide) {
         ItemStack itemstack = this.getItem();
         Potion potion = PotionUtils.getPotion(itemstack);
         List<MobEffectInstance> list = PotionUtils.getMobEffects(itemstack);
         boolean flag = potion == Potions.WATER && list.isEmpty();
         if (flag) {
            this.applyWater();
         } else if (!list.isEmpty()) {
            if (this.isLingering()) {
               this.makeAreaOfEffectCloud(itemstack, potion);
            } else {
               this.applySplash(list, hitresult.getType() == HitResult.Type.ENTITY ? ((EntityHitResult)hitresult).getEntity() : null);
            }
         }

         int i = potion.hasInstantEffects() ? 2007 : 2002;
         this.level().levelEvent(i, this.blockPosition(), PotionUtils.getColor(itemstack));
         this.discard();
      }
   }

   private void applyWater() {
      AABB aabb = this.getBoundingBox().inflate(4.0D, 2.0D, 4.0D);

      for(LivingEntity livingentity : this.level().getEntitiesOfClass(LivingEntity.class, aabb, WATER_SENSITIVE_OR_ON_FIRE)) {
         double d0 = this.distanceToSqr(livingentity);
         if (d0 < 16.0D) {
            if (livingentity.isSensitiveToWater()) {
               livingentity.hurt(this.damageSources().indirectMagic(this, this.getOwner()), 1.0F);
            }

            if (livingentity.isOnFire() && livingentity.isAlive()) {
               livingentity.extinguishFire();
            }
         }
      }

      for(Axolotl axolotl : this.level().getEntitiesOfClass(Axolotl.class, aabb)) {
         axolotl.rehydrate();
      }

   }

   private void applySplash(List<MobEffectInstance> list, @Nullable Entity entity) {
      AABB aabb = this.getBoundingBox().inflate(4.0D, 2.0D, 4.0D);
      List<LivingEntity> list1 = this.level().getEntitiesOfClass(LivingEntity.class, aabb);
      if (!list1.isEmpty()) {
         Entity entity1 = this.getEffectSource();

         for(LivingEntity livingentity : list1) {
            if (livingentity.isAffectedByPotions()) {
               double d0 = this.distanceToSqr(livingentity);
               if (d0 < 16.0D) {
                  double d1;
                  if (livingentity == entity) {
                     d1 = 1.0D;
                  } else {
                     d1 = 1.0D - Math.sqrt(d0) / 4.0D;
                  }

                  for(MobEffectInstance mobeffectinstance : list) {
                     MobEffect mobeffect = mobeffectinstance.getEffect();
                     if (mobeffect.isInstantenous()) {
                        mobeffect.applyInstantenousEffect(this, this.getOwner(), livingentity, mobeffectinstance.getAmplifier(), d1);
                     } else {
                        int i = mobeffectinstance.mapDuration((j) -> (int)(d1 * (double)j + 0.5D));
                        MobEffectInstance mobeffectinstance1 = new MobEffectInstance(mobeffect, i, mobeffectinstance.getAmplifier(), mobeffectinstance.isAmbient(), mobeffectinstance.isVisible());
                        if (!mobeffectinstance1.endsWithin(20)) {
                           livingentity.addEffect(mobeffectinstance1, entity1);
                        }
                     }
                  }
               }
            }
         }
      }

   }

   private void makeAreaOfEffectCloud(ItemStack itemstack, Potion potion) {
      AreaEffectCloud areaeffectcloud = new AreaEffectCloud(this.level(), this.getX(), this.getY(), this.getZ());
      Entity entity = this.getOwner();
      if (entity instanceof LivingEntity) {
         areaeffectcloud.setOwner((LivingEntity)entity);
      }

      areaeffectcloud.setRadius(3.0F);
      areaeffectcloud.setRadiusOnUse(-0.5F);
      areaeffectcloud.setWaitTime(10);
      areaeffectcloud.setRadiusPerTick(-areaeffectcloud.getRadius() / (float)areaeffectcloud.getDuration());
      areaeffectcloud.setPotion(potion);

      for(MobEffectInstance mobeffectinstance : PotionUtils.getCustomEffects(itemstack)) {
         areaeffectcloud.addEffect(new MobEffectInstance(mobeffectinstance));
      }

      CompoundTag compoundtag = itemstack.getTag();
      if (compoundtag != null && compoundtag.contains("CustomPotionColor", 99)) {
         areaeffectcloud.setFixedColor(compoundtag.getInt("CustomPotionColor"));
      }

      this.level().addFreshEntity(areaeffectcloud);
   }

   private boolean isLingering() {
      return this.getItem().is(Items.LINGERING_POTION);
   }

   private void dowseFire(BlockPos blockpos) {
      BlockState blockstate = this.level().getBlockState(blockpos);
      if (blockstate.is(BlockTags.FIRE)) {
         this.level().removeBlock(blockpos, false);
      } else if (AbstractCandleBlock.isLit(blockstate)) {
         AbstractCandleBlock.extinguish((Player)null, blockstate, this.level(), blockpos);
      } else if (CampfireBlock.isLitCampfire(blockstate)) {
         this.level().levelEvent((Player)null, 1009, blockpos, 0);
         CampfireBlock.dowse(this.getOwner(), this.level(), blockpos, blockstate);
         this.level().setBlockAndUpdate(blockpos, blockstate.setValue(CampfireBlock.LIT, Boolean.valueOf(false)));
      }

   }
}
