package net.minecraft.world.entity.monster;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;

public class Stray extends AbstractSkeleton {
   public Stray(EntityType<? extends Stray> entitytype, Level level) {
      super(entitytype, level);
   }

   public static boolean checkStraySpawnRules(EntityType<Stray> entitytype, ServerLevelAccessor serverlevelaccessor, MobSpawnType mobspawntype, BlockPos blockpos, RandomSource randomsource) {
      BlockPos blockpos1 = blockpos;

      do {
         blockpos1 = blockpos1.above();
      } while(serverlevelaccessor.getBlockState(blockpos1).is(Blocks.POWDER_SNOW));

      return checkMonsterSpawnRules(entitytype, serverlevelaccessor, mobspawntype, blockpos, randomsource) && (mobspawntype == MobSpawnType.SPAWNER || serverlevelaccessor.canSeeSky(blockpos1.below()));
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.STRAY_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource damagesource) {
      return SoundEvents.STRAY_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.STRAY_DEATH;
   }

   SoundEvent getStepSound() {
      return SoundEvents.STRAY_STEP;
   }

   protected AbstractArrow getArrow(ItemStack itemstack, float f) {
      AbstractArrow abstractarrow = super.getArrow(itemstack, f);
      if (abstractarrow instanceof Arrow) {
         ((Arrow)abstractarrow).addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 600));
      }

      return abstractarrow;
   }
}
