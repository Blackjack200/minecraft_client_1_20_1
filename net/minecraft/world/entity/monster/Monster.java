package net.minecraft.world.entity.monster;

import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.dimension.DimensionType;

public abstract class Monster extends PathfinderMob implements Enemy {
   protected Monster(EntityType<? extends Monster> entitytype, Level level) {
      super(entitytype, level);
      this.xpReward = 5;
   }

   public SoundSource getSoundSource() {
      return SoundSource.HOSTILE;
   }

   public void aiStep() {
      this.updateSwingTime();
      this.updateNoActionTime();
      super.aiStep();
   }

   protected void updateNoActionTime() {
      float f = this.getLightLevelDependentMagicValue();
      if (f > 0.5F) {
         this.noActionTime += 2;
      }

   }

   protected boolean shouldDespawnInPeaceful() {
      return true;
   }

   protected SoundEvent getSwimSound() {
      return SoundEvents.HOSTILE_SWIM;
   }

   protected SoundEvent getSwimSplashSound() {
      return SoundEvents.HOSTILE_SPLASH;
   }

   protected SoundEvent getHurtSound(DamageSource damagesource) {
      return SoundEvents.HOSTILE_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.HOSTILE_DEATH;
   }

   public LivingEntity.Fallsounds getFallSounds() {
      return new LivingEntity.Fallsounds(SoundEvents.HOSTILE_SMALL_FALL, SoundEvents.HOSTILE_BIG_FALL);
   }

   public float getWalkTargetValue(BlockPos blockpos, LevelReader levelreader) {
      return -levelreader.getPathfindingCostFromLightLevels(blockpos);
   }

   public static boolean isDarkEnoughToSpawn(ServerLevelAccessor serverlevelaccessor, BlockPos blockpos, RandomSource randomsource) {
      if (serverlevelaccessor.getBrightness(LightLayer.SKY, blockpos) > randomsource.nextInt(32)) {
         return false;
      } else {
         DimensionType dimensiontype = serverlevelaccessor.dimensionType();
         int i = dimensiontype.monsterSpawnBlockLightLimit();
         if (i < 15 && serverlevelaccessor.getBrightness(LightLayer.BLOCK, blockpos) > i) {
            return false;
         } else {
            int j = serverlevelaccessor.getLevel().isThundering() ? serverlevelaccessor.getMaxLocalRawBrightness(blockpos, 10) : serverlevelaccessor.getMaxLocalRawBrightness(blockpos);
            return j <= dimensiontype.monsterSpawnLightTest().sample(randomsource);
         }
      }
   }

   public static boolean checkMonsterSpawnRules(EntityType<? extends Monster> entitytype, ServerLevelAccessor serverlevelaccessor, MobSpawnType mobspawntype, BlockPos blockpos, RandomSource randomsource) {
      return serverlevelaccessor.getDifficulty() != Difficulty.PEACEFUL && isDarkEnoughToSpawn(serverlevelaccessor, blockpos, randomsource) && checkMobSpawnRules(entitytype, serverlevelaccessor, mobspawntype, blockpos, randomsource);
   }

   public static boolean checkAnyLightMonsterSpawnRules(EntityType<? extends Monster> entitytype, LevelAccessor levelaccessor, MobSpawnType mobspawntype, BlockPos blockpos, RandomSource randomsource) {
      return levelaccessor.getDifficulty() != Difficulty.PEACEFUL && checkMobSpawnRules(entitytype, levelaccessor, mobspawntype, blockpos, randomsource);
   }

   public static AttributeSupplier.Builder createMonsterAttributes() {
      return Mob.createMobAttributes().add(Attributes.ATTACK_DAMAGE);
   }

   public boolean shouldDropExperience() {
      return true;
   }

   protected boolean shouldDropLoot() {
      return true;
   }

   public boolean isPreventingPlayerRest(Player player) {
      return true;
   }

   public ItemStack getProjectile(ItemStack itemstack) {
      if (itemstack.getItem() instanceof ProjectileWeaponItem) {
         Predicate<ItemStack> predicate = ((ProjectileWeaponItem)itemstack.getItem()).getSupportedHeldProjectiles();
         ItemStack itemstack1 = ProjectileWeaponItem.getHeldProjectile(this, predicate);
         return itemstack1.isEmpty() ? new ItemStack(Items.ARROW) : itemstack1;
      } else {
         return ItemStack.EMPTY;
      }
   }
}
