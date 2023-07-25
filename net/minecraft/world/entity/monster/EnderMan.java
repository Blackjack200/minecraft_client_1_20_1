package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class EnderMan extends Monster implements NeutralMob {
   private static final UUID SPEED_MODIFIER_ATTACKING_UUID = UUID.fromString("020E0DFB-87AE-4653-9556-831010E291A0");
   private static final AttributeModifier SPEED_MODIFIER_ATTACKING = new AttributeModifier(SPEED_MODIFIER_ATTACKING_UUID, "Attacking speed boost", (double)0.15F, AttributeModifier.Operation.ADDITION);
   private static final int DELAY_BETWEEN_CREEPY_STARE_SOUND = 400;
   private static final int MIN_DEAGGRESSION_TIME = 600;
   private static final EntityDataAccessor<Optional<BlockState>> DATA_CARRY_STATE = SynchedEntityData.defineId(EnderMan.class, EntityDataSerializers.OPTIONAL_BLOCK_STATE);
   private static final EntityDataAccessor<Boolean> DATA_CREEPY = SynchedEntityData.defineId(EnderMan.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> DATA_STARED_AT = SynchedEntityData.defineId(EnderMan.class, EntityDataSerializers.BOOLEAN);
   private int lastStareSound = Integer.MIN_VALUE;
   private int targetChangeTime;
   private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
   private int remainingPersistentAngerTime;
   @Nullable
   private UUID persistentAngerTarget;

   public EnderMan(EntityType<? extends EnderMan> entitytype, Level level) {
      super(entitytype, level);
      this.setMaxUpStep(1.0F);
      this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(1, new EnderMan.EndermanFreezeWhenLookedAt(this));
      this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false));
      this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D, 0.0F));
      this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
      this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
      this.goalSelector.addGoal(10, new EnderMan.EndermanLeaveBlockGoal(this));
      this.goalSelector.addGoal(11, new EnderMan.EndermanTakeBlockGoal(this));
      this.targetSelector.addGoal(1, new EnderMan.EndermanLookForPlayerGoal(this, this::isAngryAt));
      this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Endermite.class, true, false));
      this.targetSelector.addGoal(4, new ResetUniversalAngerTargetGoal<>(this, false));
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 40.0D).add(Attributes.MOVEMENT_SPEED, (double)0.3F).add(Attributes.ATTACK_DAMAGE, 7.0D).add(Attributes.FOLLOW_RANGE, 64.0D);
   }

   public void setTarget(@Nullable LivingEntity livingentity) {
      super.setTarget(livingentity);
      AttributeInstance attributeinstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
      if (livingentity == null) {
         this.targetChangeTime = 0;
         this.entityData.set(DATA_CREEPY, false);
         this.entityData.set(DATA_STARED_AT, false);
         attributeinstance.removeModifier(SPEED_MODIFIER_ATTACKING);
      } else {
         this.targetChangeTime = this.tickCount;
         this.entityData.set(DATA_CREEPY, true);
         if (!attributeinstance.hasModifier(SPEED_MODIFIER_ATTACKING)) {
            attributeinstance.addTransientModifier(SPEED_MODIFIER_ATTACKING);
         }
      }

   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_CARRY_STATE, Optional.empty());
      this.entityData.define(DATA_CREEPY, false);
      this.entityData.define(DATA_STARED_AT, false);
   }

   public void startPersistentAngerTimer() {
      this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random));
   }

   public void setRemainingPersistentAngerTime(int i) {
      this.remainingPersistentAngerTime = i;
   }

   public int getRemainingPersistentAngerTime() {
      return this.remainingPersistentAngerTime;
   }

   public void setPersistentAngerTarget(@Nullable UUID uuid) {
      this.persistentAngerTarget = uuid;
   }

   @Nullable
   public UUID getPersistentAngerTarget() {
      return this.persistentAngerTarget;
   }

   public void playStareSound() {
      if (this.tickCount >= this.lastStareSound + 400) {
         this.lastStareSound = this.tickCount;
         if (!this.isSilent()) {
            this.level().playLocalSound(this.getX(), this.getEyeY(), this.getZ(), SoundEvents.ENDERMAN_STARE, this.getSoundSource(), 2.5F, 1.0F, false);
         }
      }

   }

   public void onSyncedDataUpdated(EntityDataAccessor<?> entitydataaccessor) {
      if (DATA_CREEPY.equals(entitydataaccessor) && this.hasBeenStaredAt() && this.level().isClientSide) {
         this.playStareSound();
      }

      super.onSyncedDataUpdated(entitydataaccessor);
   }

   public void addAdditionalSaveData(CompoundTag compoundtag) {
      super.addAdditionalSaveData(compoundtag);
      BlockState blockstate = this.getCarriedBlock();
      if (blockstate != null) {
         compoundtag.put("carriedBlockState", NbtUtils.writeBlockState(blockstate));
      }

      this.addPersistentAngerSaveData(compoundtag);
   }

   public void readAdditionalSaveData(CompoundTag compoundtag) {
      super.readAdditionalSaveData(compoundtag);
      BlockState blockstate = null;
      if (compoundtag.contains("carriedBlockState", 10)) {
         blockstate = NbtUtils.readBlockState(this.level().holderLookup(Registries.BLOCK), compoundtag.getCompound("carriedBlockState"));
         if (blockstate.isAir()) {
            blockstate = null;
         }
      }

      this.setCarriedBlock(blockstate);
      this.readPersistentAngerSaveData(this.level(), compoundtag);
   }

   boolean isLookingAtMe(Player player) {
      ItemStack itemstack = player.getInventory().armor.get(3);
      if (itemstack.is(Blocks.CARVED_PUMPKIN.asItem())) {
         return false;
      } else {
         Vec3 vec3 = player.getViewVector(1.0F).normalize();
         Vec3 vec31 = new Vec3(this.getX() - player.getX(), this.getEyeY() - player.getEyeY(), this.getZ() - player.getZ());
         double d0 = vec31.length();
         vec31 = vec31.normalize();
         double d1 = vec3.dot(vec31);
         return d1 > 1.0D - 0.025D / d0 ? player.hasLineOfSight(this) : false;
      }
   }

   protected float getStandingEyeHeight(Pose pose, EntityDimensions entitydimensions) {
      return 2.55F;
   }

   public void aiStep() {
      if (this.level().isClientSide) {
         for(int i = 0; i < 2; ++i) {
            this.level().addParticle(ParticleTypes.PORTAL, this.getRandomX(0.5D), this.getRandomY() - 0.25D, this.getRandomZ(0.5D), (this.random.nextDouble() - 0.5D) * 2.0D, -this.random.nextDouble(), (this.random.nextDouble() - 0.5D) * 2.0D);
         }
      }

      this.jumping = false;
      if (!this.level().isClientSide) {
         this.updatePersistentAnger((ServerLevel)this.level(), true);
      }

      super.aiStep();
   }

   public boolean isSensitiveToWater() {
      return true;
   }

   protected void customServerAiStep() {
      if (this.level().isDay() && this.tickCount >= this.targetChangeTime + 600) {
         float f = this.getLightLevelDependentMagicValue();
         if (f > 0.5F && this.level().canSeeSky(this.blockPosition()) && this.random.nextFloat() * 30.0F < (f - 0.4F) * 2.0F) {
            this.setTarget((LivingEntity)null);
            this.teleport();
         }
      }

      super.customServerAiStep();
   }

   protected boolean teleport() {
      if (!this.level().isClientSide() && this.isAlive()) {
         double d0 = this.getX() + (this.random.nextDouble() - 0.5D) * 64.0D;
         double d1 = this.getY() + (double)(this.random.nextInt(64) - 32);
         double d2 = this.getZ() + (this.random.nextDouble() - 0.5D) * 64.0D;
         return this.teleport(d0, d1, d2);
      } else {
         return false;
      }
   }

   boolean teleportTowards(Entity entity) {
      Vec3 vec3 = new Vec3(this.getX() - entity.getX(), this.getY(0.5D) - entity.getEyeY(), this.getZ() - entity.getZ());
      vec3 = vec3.normalize();
      double d0 = 16.0D;
      double d1 = this.getX() + (this.random.nextDouble() - 0.5D) * 8.0D - vec3.x * 16.0D;
      double d2 = this.getY() + (double)(this.random.nextInt(16) - 8) - vec3.y * 16.0D;
      double d3 = this.getZ() + (this.random.nextDouble() - 0.5D) * 8.0D - vec3.z * 16.0D;
      return this.teleport(d1, d2, d3);
   }

   private boolean teleport(double d0, double d1, double d2) {
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos(d0, d1, d2);

      while(blockpos_mutableblockpos.getY() > this.level().getMinBuildHeight() && !this.level().getBlockState(blockpos_mutableblockpos).blocksMotion()) {
         blockpos_mutableblockpos.move(Direction.DOWN);
      }

      BlockState blockstate = this.level().getBlockState(blockpos_mutableblockpos);
      boolean flag = blockstate.blocksMotion();
      boolean flag1 = blockstate.getFluidState().is(FluidTags.WATER);
      if (flag && !flag1) {
         Vec3 vec3 = this.position();
         boolean flag2 = this.randomTeleport(d0, d1, d2, true);
         if (flag2) {
            this.level().gameEvent(GameEvent.TELEPORT, vec3, GameEvent.Context.of(this));
            if (!this.isSilent()) {
               this.level().playSound((Player)null, this.xo, this.yo, this.zo, SoundEvents.ENDERMAN_TELEPORT, this.getSoundSource(), 1.0F, 1.0F);
               this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
            }
         }

         return flag2;
      } else {
         return false;
      }
   }

   protected SoundEvent getAmbientSound() {
      return this.isCreepy() ? SoundEvents.ENDERMAN_SCREAM : SoundEvents.ENDERMAN_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource damagesource) {
      return SoundEvents.ENDERMAN_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENDERMAN_DEATH;
   }

   protected void dropCustomDeathLoot(DamageSource damagesource, int i, boolean flag) {
      super.dropCustomDeathLoot(damagesource, i, flag);
      BlockState blockstate = this.getCarriedBlock();
      if (blockstate != null) {
         ItemStack itemstack = new ItemStack(Items.DIAMOND_AXE);
         itemstack.enchant(Enchantments.SILK_TOUCH, 1);
         LootParams.Builder lootparams_builder = (new LootParams.Builder((ServerLevel)this.level())).withParameter(LootContextParams.ORIGIN, this.position()).withParameter(LootContextParams.TOOL, itemstack).withOptionalParameter(LootContextParams.THIS_ENTITY, this);

         for(ItemStack itemstack1 : blockstate.getDrops(lootparams_builder)) {
            this.spawnAtLocation(itemstack1);
         }
      }

   }

   public void setCarriedBlock(@Nullable BlockState blockstate) {
      this.entityData.set(DATA_CARRY_STATE, Optional.ofNullable(blockstate));
   }

   @Nullable
   public BlockState getCarriedBlock() {
      return this.entityData.get(DATA_CARRY_STATE).orElse((BlockState)null);
   }

   public boolean hurt(DamageSource damagesource, float f) {
      if (this.isInvulnerableTo(damagesource)) {
         return false;
      } else {
         boolean flag = damagesource.getDirectEntity() instanceof ThrownPotion;
         if (!damagesource.is(DamageTypeTags.IS_PROJECTILE) && !flag) {
            boolean flag2 = super.hurt(damagesource, f);
            if (!this.level().isClientSide() && !(damagesource.getEntity() instanceof LivingEntity) && this.random.nextInt(10) != 0) {
               this.teleport();
            }

            return flag2;
         } else {
            boolean flag1 = flag && this.hurtWithCleanWater(damagesource, (ThrownPotion)damagesource.getDirectEntity(), f);

            for(int i = 0; i < 64; ++i) {
               if (this.teleport()) {
                  return true;
               }
            }

            return flag1;
         }
      }
   }

   private boolean hurtWithCleanWater(DamageSource damagesource, ThrownPotion thrownpotion, float f) {
      ItemStack itemstack = thrownpotion.getItem();
      Potion potion = PotionUtils.getPotion(itemstack);
      List<MobEffectInstance> list = PotionUtils.getMobEffects(itemstack);
      boolean flag = potion == Potions.WATER && list.isEmpty();
      return flag ? super.hurt(damagesource, f) : false;
   }

   public boolean isCreepy() {
      return this.entityData.get(DATA_CREEPY);
   }

   public boolean hasBeenStaredAt() {
      return this.entityData.get(DATA_STARED_AT);
   }

   public void setBeingStaredAt() {
      this.entityData.set(DATA_STARED_AT, true);
   }

   public boolean requiresCustomPersistence() {
      return super.requiresCustomPersistence() || this.getCarriedBlock() != null;
   }

   static class EndermanFreezeWhenLookedAt extends Goal {
      private final EnderMan enderman;
      @Nullable
      private LivingEntity target;

      public EndermanFreezeWhenLookedAt(EnderMan enderman) {
         this.enderman = enderman;
         this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
      }

      public boolean canUse() {
         this.target = this.enderman.getTarget();
         if (!(this.target instanceof Player)) {
            return false;
         } else {
            double d0 = this.target.distanceToSqr(this.enderman);
            return d0 > 256.0D ? false : this.enderman.isLookingAtMe((Player)this.target);
         }
      }

      public void start() {
         this.enderman.getNavigation().stop();
      }

      public void tick() {
         this.enderman.getLookControl().setLookAt(this.target.getX(), this.target.getEyeY(), this.target.getZ());
      }
   }

   static class EndermanLeaveBlockGoal extends Goal {
      private final EnderMan enderman;

      public EndermanLeaveBlockGoal(EnderMan enderman) {
         this.enderman = enderman;
      }

      public boolean canUse() {
         if (this.enderman.getCarriedBlock() == null) {
            return false;
         } else if (!this.enderman.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            return false;
         } else {
            return this.enderman.getRandom().nextInt(reducedTickDelay(2000)) == 0;
         }
      }

      public void tick() {
         RandomSource randomsource = this.enderman.getRandom();
         Level level = this.enderman.level();
         int i = Mth.floor(this.enderman.getX() - 1.0D + randomsource.nextDouble() * 2.0D);
         int j = Mth.floor(this.enderman.getY() + randomsource.nextDouble() * 2.0D);
         int k = Mth.floor(this.enderman.getZ() - 1.0D + randomsource.nextDouble() * 2.0D);
         BlockPos blockpos = new BlockPos(i, j, k);
         BlockState blockstate = level.getBlockState(blockpos);
         BlockPos blockpos1 = blockpos.below();
         BlockState blockstate1 = level.getBlockState(blockpos1);
         BlockState blockstate2 = this.enderman.getCarriedBlock();
         if (blockstate2 != null) {
            blockstate2 = Block.updateFromNeighbourShapes(blockstate2, this.enderman.level(), blockpos);
            if (this.canPlaceBlock(level, blockpos, blockstate2, blockstate, blockstate1, blockpos1)) {
               level.setBlock(blockpos, blockstate2, 3);
               level.gameEvent(GameEvent.BLOCK_PLACE, blockpos, GameEvent.Context.of(this.enderman, blockstate2));
               this.enderman.setCarriedBlock((BlockState)null);
            }

         }
      }

      private boolean canPlaceBlock(Level level, BlockPos blockpos, BlockState blockstate, BlockState blockstate1, BlockState blockstate2, BlockPos blockpos1) {
         return blockstate1.isAir() && !blockstate2.isAir() && !blockstate2.is(Blocks.BEDROCK) && blockstate2.isCollisionShapeFullBlock(level, blockpos1) && blockstate.canSurvive(level, blockpos) && level.getEntities(this.enderman, AABB.unitCubeFromLowerCorner(Vec3.atLowerCornerOf(blockpos))).isEmpty();
      }
   }

   static class EndermanLookForPlayerGoal extends NearestAttackableTargetGoal<Player> {
      private final EnderMan enderman;
      @Nullable
      private Player pendingTarget;
      private int aggroTime;
      private int teleportTime;
      private final TargetingConditions startAggroTargetConditions;
      private final TargetingConditions continueAggroTargetConditions = TargetingConditions.forCombat().ignoreLineOfSight();
      private final Predicate<LivingEntity> isAngerInducing;

      public EndermanLookForPlayerGoal(EnderMan enderman, @Nullable Predicate<LivingEntity> predicate) {
         super(enderman, Player.class, 10, false, false, predicate);
         this.enderman = enderman;
         this.isAngerInducing = (livingentity) -> (enderman.isLookingAtMe((Player)livingentity) || enderman.isAngryAt(livingentity)) && !enderman.hasIndirectPassenger(livingentity);
         this.startAggroTargetConditions = TargetingConditions.forCombat().range(this.getFollowDistance()).selector(this.isAngerInducing);
      }

      public boolean canUse() {
         this.pendingTarget = this.enderman.level().getNearestPlayer(this.startAggroTargetConditions, this.enderman);
         return this.pendingTarget != null;
      }

      public void start() {
         this.aggroTime = this.adjustedTickDelay(5);
         this.teleportTime = 0;
         this.enderman.setBeingStaredAt();
      }

      public void stop() {
         this.pendingTarget = null;
         super.stop();
      }

      public boolean canContinueToUse() {
         if (this.pendingTarget != null) {
            if (!this.isAngerInducing.test(this.pendingTarget)) {
               return false;
            } else {
               this.enderman.lookAt(this.pendingTarget, 10.0F, 10.0F);
               return true;
            }
         } else {
            if (this.target != null) {
               if (this.enderman.hasIndirectPassenger(this.target)) {
                  return false;
               }

               if (this.continueAggroTargetConditions.test(this.enderman, this.target)) {
                  return true;
               }
            }

            return super.canContinueToUse();
         }
      }

      public void tick() {
         if (this.enderman.getTarget() == null) {
            super.setTarget((LivingEntity)null);
         }

         if (this.pendingTarget != null) {
            if (--this.aggroTime <= 0) {
               this.target = this.pendingTarget;
               this.pendingTarget = null;
               super.start();
            }
         } else {
            if (this.target != null && !this.enderman.isPassenger()) {
               if (this.enderman.isLookingAtMe((Player)this.target)) {
                  if (this.target.distanceToSqr(this.enderman) < 16.0D) {
                     this.enderman.teleport();
                  }

                  this.teleportTime = 0;
               } else if (this.target.distanceToSqr(this.enderman) > 256.0D && this.teleportTime++ >= this.adjustedTickDelay(30) && this.enderman.teleportTowards(this.target)) {
                  this.teleportTime = 0;
               }
            }

            super.tick();
         }

      }
   }

   static class EndermanTakeBlockGoal extends Goal {
      private final EnderMan enderman;

      public EndermanTakeBlockGoal(EnderMan enderman) {
         this.enderman = enderman;
      }

      public boolean canUse() {
         if (this.enderman.getCarriedBlock() != null) {
            return false;
         } else if (!this.enderman.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            return false;
         } else {
            return this.enderman.getRandom().nextInt(reducedTickDelay(20)) == 0;
         }
      }

      public void tick() {
         RandomSource randomsource = this.enderman.getRandom();
         Level level = this.enderman.level();
         int i = Mth.floor(this.enderman.getX() - 2.0D + randomsource.nextDouble() * 4.0D);
         int j = Mth.floor(this.enderman.getY() + randomsource.nextDouble() * 3.0D);
         int k = Mth.floor(this.enderman.getZ() - 2.0D + randomsource.nextDouble() * 4.0D);
         BlockPos blockpos = new BlockPos(i, j, k);
         BlockState blockstate = level.getBlockState(blockpos);
         Vec3 vec3 = new Vec3((double)this.enderman.getBlockX() + 0.5D, (double)j + 0.5D, (double)this.enderman.getBlockZ() + 0.5D);
         Vec3 vec31 = new Vec3((double)i + 0.5D, (double)j + 0.5D, (double)k + 0.5D);
         BlockHitResult blockhitresult = level.clip(new ClipContext(vec3, vec31, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this.enderman));
         boolean flag = blockhitresult.getBlockPos().equals(blockpos);
         if (blockstate.is(BlockTags.ENDERMAN_HOLDABLE) && flag) {
            level.removeBlock(blockpos, false);
            level.gameEvent(GameEvent.BLOCK_DESTROY, blockpos, GameEvent.Context.of(this.enderman, blockstate));
            this.enderman.setCarriedBlock(blockstate.getBlock().defaultBlockState());
         }

      }
   }
}
