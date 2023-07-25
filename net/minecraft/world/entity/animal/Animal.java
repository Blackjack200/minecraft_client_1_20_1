package net.minecraft.world.entity.animal;

import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

public abstract class Animal extends AgeableMob {
   protected static final int PARENT_AGE_AFTER_BREEDING = 6000;
   private int inLove;
   @Nullable
   private UUID loveCause;

   protected Animal(EntityType<? extends Animal> entitytype, Level level) {
      super(entitytype, level);
      this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 16.0F);
      this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, -1.0F);
   }

   protected void customServerAiStep() {
      if (this.getAge() != 0) {
         this.inLove = 0;
      }

      super.customServerAiStep();
   }

   public void aiStep() {
      super.aiStep();
      if (this.getAge() != 0) {
         this.inLove = 0;
      }

      if (this.inLove > 0) {
         --this.inLove;
         if (this.inLove % 10 == 0) {
            double d0 = this.random.nextGaussian() * 0.02D;
            double d1 = this.random.nextGaussian() * 0.02D;
            double d2 = this.random.nextGaussian() * 0.02D;
            this.level().addParticle(ParticleTypes.HEART, this.getRandomX(1.0D), this.getRandomY() + 0.5D, this.getRandomZ(1.0D), d0, d1, d2);
         }
      }

   }

   public boolean hurt(DamageSource damagesource, float f) {
      if (this.isInvulnerableTo(damagesource)) {
         return false;
      } else {
         this.inLove = 0;
         return super.hurt(damagesource, f);
      }
   }

   public float getWalkTargetValue(BlockPos blockpos, LevelReader levelreader) {
      return levelreader.getBlockState(blockpos.below()).is(Blocks.GRASS_BLOCK) ? 10.0F : levelreader.getPathfindingCostFromLightLevels(blockpos);
   }

   public void addAdditionalSaveData(CompoundTag compoundtag) {
      super.addAdditionalSaveData(compoundtag);
      compoundtag.putInt("InLove", this.inLove);
      if (this.loveCause != null) {
         compoundtag.putUUID("LoveCause", this.loveCause);
      }

   }

   public double getMyRidingOffset() {
      return 0.14D;
   }

   public void readAdditionalSaveData(CompoundTag compoundtag) {
      super.readAdditionalSaveData(compoundtag);
      this.inLove = compoundtag.getInt("InLove");
      this.loveCause = compoundtag.hasUUID("LoveCause") ? compoundtag.getUUID("LoveCause") : null;
   }

   public static boolean checkAnimalSpawnRules(EntityType<? extends Animal> entitytype, LevelAccessor levelaccessor, MobSpawnType mobspawntype, BlockPos blockpos, RandomSource randomsource) {
      return levelaccessor.getBlockState(blockpos.below()).is(BlockTags.ANIMALS_SPAWNABLE_ON) && isBrightEnoughToSpawn(levelaccessor, blockpos);
   }

   protected static boolean isBrightEnoughToSpawn(BlockAndTintGetter blockandtintgetter, BlockPos blockpos) {
      return blockandtintgetter.getRawBrightness(blockpos, 0) > 8;
   }

   public int getAmbientSoundInterval() {
      return 120;
   }

   public boolean removeWhenFarAway(double d0) {
      return false;
   }

   public int getExperienceReward() {
      return 1 + this.level().random.nextInt(3);
   }

   public boolean isFood(ItemStack itemstack) {
      return itemstack.is(Items.WHEAT);
   }

   public InteractionResult mobInteract(Player player, InteractionHand interactionhand) {
      ItemStack itemstack = player.getItemInHand(interactionhand);
      if (this.isFood(itemstack)) {
         int i = this.getAge();
         if (!this.level().isClientSide && i == 0 && this.canFallInLove()) {
            this.usePlayerItem(player, interactionhand, itemstack);
            this.setInLove(player);
            return InteractionResult.SUCCESS;
         }

         if (this.isBaby()) {
            this.usePlayerItem(player, interactionhand, itemstack);
            this.ageUp(getSpeedUpSecondsWhenFeeding(-i), true);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
         }

         if (this.level().isClientSide) {
            return InteractionResult.CONSUME;
         }
      }

      return super.mobInteract(player, interactionhand);
   }

   protected void usePlayerItem(Player player, InteractionHand interactionhand, ItemStack itemstack) {
      if (!player.getAbilities().instabuild) {
         itemstack.shrink(1);
      }

   }

   public boolean canFallInLove() {
      return this.inLove <= 0;
   }

   public void setInLove(@Nullable Player player) {
      this.inLove = 600;
      if (player != null) {
         this.loveCause = player.getUUID();
      }

      this.level().broadcastEntityEvent(this, (byte)18);
   }

   public void setInLoveTime(int i) {
      this.inLove = i;
   }

   public int getInLoveTime() {
      return this.inLove;
   }

   @Nullable
   public ServerPlayer getLoveCause() {
      if (this.loveCause == null) {
         return null;
      } else {
         Player player = this.level().getPlayerByUUID(this.loveCause);
         return player instanceof ServerPlayer ? (ServerPlayer)player : null;
      }
   }

   public boolean isInLove() {
      return this.inLove > 0;
   }

   public void resetLove() {
      this.inLove = 0;
   }

   public boolean canMate(Animal animal) {
      if (animal == this) {
         return false;
      } else if (animal.getClass() != this.getClass()) {
         return false;
      } else {
         return this.isInLove() && animal.isInLove();
      }
   }

   public void spawnChildFromBreeding(ServerLevel serverlevel, Animal animal) {
      AgeableMob ageablemob = this.getBreedOffspring(serverlevel, animal);
      if (ageablemob != null) {
         ageablemob.setBaby(true);
         ageablemob.moveTo(this.getX(), this.getY(), this.getZ(), 0.0F, 0.0F);
         this.finalizeSpawnChildFromBreeding(serverlevel, animal, ageablemob);
         serverlevel.addFreshEntityWithPassengers(ageablemob);
      }
   }

   public void finalizeSpawnChildFromBreeding(ServerLevel serverlevel, Animal animal, @Nullable AgeableMob ageablemob) {
      Optional.ofNullable(this.getLoveCause()).or(() -> Optional.ofNullable(animal.getLoveCause())).ifPresent((serverplayer) -> {
         serverplayer.awardStat(Stats.ANIMALS_BRED);
         CriteriaTriggers.BRED_ANIMALS.trigger(serverplayer, this, animal, ageablemob);
      });
      this.setAge(6000);
      animal.setAge(6000);
      this.resetLove();
      animal.resetLove();
      serverlevel.broadcastEntityEvent(this, (byte)18);
      if (serverlevel.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
         serverlevel.addFreshEntity(new ExperienceOrb(serverlevel, this.getX(), this.getY(), this.getZ(), this.getRandom().nextInt(7) + 1));
      }

   }

   public void handleEntityEvent(byte b0) {
      if (b0 == 18) {
         for(int i = 0; i < 7; ++i) {
            double d0 = this.random.nextGaussian() * 0.02D;
            double d1 = this.random.nextGaussian() * 0.02D;
            double d2 = this.random.nextGaussian() * 0.02D;
            this.level().addParticle(ParticleTypes.HEART, this.getRandomX(1.0D), this.getRandomY() + 0.5D, this.getRandomZ(1.0D), d0, d1, d2);
         }
      } else {
         super.handleEntityEvent(b0);
      }

   }
}
