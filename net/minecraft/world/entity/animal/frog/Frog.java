package net.minecraft.world.entity.animal.frog;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.FrogVariant;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.AmphibiousNodeEvaluator;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.Vec3;

public class Frog extends Animal implements VariantHolder<FrogVariant> {
   public static final Ingredient TEMPTATION_ITEM = Ingredient.of(Items.SLIME_BALL);
   protected static final ImmutableList<SensorType<? extends Sensor<? super Frog>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.HURT_BY, SensorType.FROG_ATTACKABLES, SensorType.FROG_TEMPTATIONS, SensorType.IS_IN_WATER);
   protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.LOOK_TARGET, MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.BREED_TARGET, MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryModuleType.ATTACK_TARGET, MemoryModuleType.TEMPTING_PLAYER, MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, MemoryModuleType.IS_TEMPTED, MemoryModuleType.HURT_BY, MemoryModuleType.HURT_BY_ENTITY, MemoryModuleType.NEAREST_ATTACKABLE, MemoryModuleType.IS_IN_WATER, MemoryModuleType.IS_PREGNANT, MemoryModuleType.IS_PANICKING, MemoryModuleType.UNREACHABLE_TONGUE_TARGETS);
   private static final EntityDataAccessor<FrogVariant> DATA_VARIANT_ID = SynchedEntityData.defineId(Frog.class, EntityDataSerializers.FROG_VARIANT);
   private static final EntityDataAccessor<OptionalInt> DATA_TONGUE_TARGET_ID = SynchedEntityData.defineId(Frog.class, EntityDataSerializers.OPTIONAL_UNSIGNED_INT);
   private static final int FROG_FALL_DAMAGE_REDUCTION = 5;
   public static final String VARIANT_KEY = "variant";
   public final AnimationState jumpAnimationState = new AnimationState();
   public final AnimationState croakAnimationState = new AnimationState();
   public final AnimationState tongueAnimationState = new AnimationState();
   public final AnimationState swimIdleAnimationState = new AnimationState();

   public Frog(EntityType<? extends Animal> entitytype, Level level) {
      super(entitytype, level);
      this.lookControl = new Frog.FrogLookControl(this);
      this.setPathfindingMalus(BlockPathTypes.WATER, 4.0F);
      this.setPathfindingMalus(BlockPathTypes.TRAPDOOR, -1.0F);
      this.moveControl = new SmoothSwimmingMoveControl(this, 85, 10, 0.02F, 0.1F, true);
      this.setMaxUpStep(1.0F);
   }

   protected Brain.Provider<Frog> brainProvider() {
      return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
   }

   protected Brain<?> makeBrain(Dynamic<?> dynamic) {
      return FrogAi.makeBrain(this.brainProvider().makeBrain(dynamic));
   }

   public Brain<Frog> getBrain() {
      return super.getBrain();
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_VARIANT_ID, FrogVariant.TEMPERATE);
      this.entityData.define(DATA_TONGUE_TARGET_ID, OptionalInt.empty());
   }

   public void eraseTongueTarget() {
      this.entityData.set(DATA_TONGUE_TARGET_ID, OptionalInt.empty());
   }

   public Optional<Entity> getTongueTarget() {
      return this.entityData.get(DATA_TONGUE_TARGET_ID).stream().mapToObj(this.level()::getEntity).filter(Objects::nonNull).findFirst();
   }

   public void setTongueTarget(Entity entity) {
      this.entityData.set(DATA_TONGUE_TARGET_ID, OptionalInt.of(entity.getId()));
   }

   public int getHeadRotSpeed() {
      return 35;
   }

   public int getMaxHeadYRot() {
      return 5;
   }

   public FrogVariant getVariant() {
      return this.entityData.get(DATA_VARIANT_ID);
   }

   public void setVariant(FrogVariant frogvariant) {
      this.entityData.set(DATA_VARIANT_ID, frogvariant);
   }

   public void addAdditionalSaveData(CompoundTag compoundtag) {
      super.addAdditionalSaveData(compoundtag);
      compoundtag.putString("variant", BuiltInRegistries.FROG_VARIANT.getKey(this.getVariant()).toString());
   }

   public void readAdditionalSaveData(CompoundTag compoundtag) {
      super.readAdditionalSaveData(compoundtag);
      FrogVariant frogvariant = BuiltInRegistries.FROG_VARIANT.get(ResourceLocation.tryParse(compoundtag.getString("variant")));
      if (frogvariant != null) {
         this.setVariant(frogvariant);
      }

   }

   public boolean canBreatheUnderwater() {
      return true;
   }

   protected void customServerAiStep() {
      this.level().getProfiler().push("frogBrain");
      this.getBrain().tick((ServerLevel)this.level(), this);
      this.level().getProfiler().pop();
      this.level().getProfiler().push("frogActivityUpdate");
      FrogAi.updateActivity(this);
      this.level().getProfiler().pop();
      super.customServerAiStep();
   }

   public void tick() {
      if (this.level().isClientSide()) {
         this.swimIdleAnimationState.animateWhen(this.isInWaterOrBubble() && !this.walkAnimation.isMoving(), this.tickCount);
      }

      super.tick();
   }

   public void onSyncedDataUpdated(EntityDataAccessor<?> entitydataaccessor) {
      if (DATA_POSE.equals(entitydataaccessor)) {
         Pose pose = this.getPose();
         if (pose == Pose.LONG_JUMPING) {
            this.jumpAnimationState.start(this.tickCount);
         } else {
            this.jumpAnimationState.stop();
         }

         if (pose == Pose.CROAKING) {
            this.croakAnimationState.start(this.tickCount);
         } else {
            this.croakAnimationState.stop();
         }

         if (pose == Pose.USING_TONGUE) {
            this.tongueAnimationState.start(this.tickCount);
         } else {
            this.tongueAnimationState.stop();
         }
      }

      super.onSyncedDataUpdated(entitydataaccessor);
   }

   protected void updateWalkAnimation(float f) {
      float f1;
      if (this.jumpAnimationState.isStarted()) {
         f1 = 0.0F;
      } else {
         f1 = Math.min(f * 25.0F, 1.0F);
      }

      this.walkAnimation.update(f1, 0.4F);
   }

   @Nullable
   public AgeableMob getBreedOffspring(ServerLevel serverlevel, AgeableMob ageablemob) {
      Frog frog = EntityType.FROG.create(serverlevel);
      if (frog != null) {
         FrogAi.initMemories(frog, serverlevel.getRandom());
      }

      return frog;
   }

   public boolean isBaby() {
      return false;
   }

   public void setBaby(boolean flag) {
   }

   public void spawnChildFromBreeding(ServerLevel serverlevel, Animal animal) {
      this.finalizeSpawnChildFromBreeding(serverlevel, animal, (AgeableMob)null);
      this.getBrain().setMemory(MemoryModuleType.IS_PREGNANT, Unit.INSTANCE);
   }

   public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverlevelaccessor, DifficultyInstance difficultyinstance, MobSpawnType mobspawntype, @Nullable SpawnGroupData spawngroupdata, @Nullable CompoundTag compoundtag) {
      Holder<Biome> holder = serverlevelaccessor.getBiome(this.blockPosition());
      if (holder.is(BiomeTags.SPAWNS_COLD_VARIANT_FROGS)) {
         this.setVariant(FrogVariant.COLD);
      } else if (holder.is(BiomeTags.SPAWNS_WARM_VARIANT_FROGS)) {
         this.setVariant(FrogVariant.WARM);
      } else {
         this.setVariant(FrogVariant.TEMPERATE);
      }

      FrogAi.initMemories(this, serverlevelaccessor.getRandom());
      return super.finalizeSpawn(serverlevelaccessor, difficultyinstance, mobspawntype, spawngroupdata, compoundtag);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 1.0D).add(Attributes.MAX_HEALTH, 10.0D).add(Attributes.ATTACK_DAMAGE, 10.0D);
   }

   @Nullable
   protected SoundEvent getAmbientSound() {
      return SoundEvents.FROG_AMBIENT;
   }

   @Nullable
   protected SoundEvent getHurtSound(DamageSource damagesource) {
      return SoundEvents.FROG_HURT;
   }

   @Nullable
   protected SoundEvent getDeathSound() {
      return SoundEvents.FROG_DEATH;
   }

   protected void playStepSound(BlockPos blockpos, BlockState blockstate) {
      this.playSound(SoundEvents.FROG_STEP, 0.15F, 1.0F);
   }

   public boolean isPushedByFluid() {
      return false;
   }

   protected void sendDebugPackets() {
      super.sendDebugPackets();
      DebugPackets.sendEntityBrain(this);
   }

   protected int calculateFallDamage(float f, float f1) {
      return super.calculateFallDamage(f, f1) - 5;
   }

   public void travel(Vec3 vec3) {
      if (this.isControlledByLocalInstance() && this.isInWater()) {
         this.moveRelative(this.getSpeed(), vec3);
         this.move(MoverType.SELF, this.getDeltaMovement());
         this.setDeltaMovement(this.getDeltaMovement().scale(0.9D));
      } else {
         super.travel(vec3);
      }

   }

   public static boolean canEat(LivingEntity livingentity) {
      if (livingentity instanceof Slime slime) {
         if (slime.getSize() != 1) {
            return false;
         }
      }

      return livingentity.getType().is(EntityTypeTags.FROG_FOOD);
   }

   protected PathNavigation createNavigation(Level level) {
      return new Frog.FrogPathNavigation(this, level);
   }

   public boolean isFood(ItemStack itemstack) {
      return TEMPTATION_ITEM.test(itemstack);
   }

   public static boolean checkFrogSpawnRules(EntityType<? extends Animal> entitytype, LevelAccessor levelaccessor, MobSpawnType mobspawntype, BlockPos blockpos, RandomSource randomsource) {
      return levelaccessor.getBlockState(blockpos.below()).is(BlockTags.FROGS_SPAWNABLE_ON) && isBrightEnoughToSpawn(levelaccessor, blockpos);
   }

   class FrogLookControl extends LookControl {
      FrogLookControl(Mob mob) {
         super(mob);
      }

      protected boolean resetXRotOnTick() {
         return Frog.this.getTongueTarget().isEmpty();
      }
   }

   static class FrogNodeEvaluator extends AmphibiousNodeEvaluator {
      private final BlockPos.MutableBlockPos belowPos = new BlockPos.MutableBlockPos();

      public FrogNodeEvaluator(boolean flag) {
         super(flag);
      }

      public Node getStart() {
         return !this.mob.isInWater() ? super.getStart() : this.getStartNode(new BlockPos(Mth.floor(this.mob.getBoundingBox().minX), Mth.floor(this.mob.getBoundingBox().minY), Mth.floor(this.mob.getBoundingBox().minZ)));
      }

      public BlockPathTypes getBlockPathType(BlockGetter blockgetter, int i, int j, int k) {
         this.belowPos.set(i, j - 1, k);
         BlockState blockstate = blockgetter.getBlockState(this.belowPos);
         return blockstate.is(BlockTags.FROG_PREFER_JUMP_TO) ? BlockPathTypes.OPEN : super.getBlockPathType(blockgetter, i, j, k);
      }
   }

   static class FrogPathNavigation extends AmphibiousPathNavigation {
      FrogPathNavigation(Frog frog, Level level) {
         super(frog, level);
      }

      public boolean canCutCorner(BlockPathTypes blockpathtypes) {
         return blockpathtypes != BlockPathTypes.WATER_BORDER && super.canCutCorner(blockpathtypes);
      }

      protected PathFinder createPathFinder(int i) {
         this.nodeEvaluator = new Frog.FrogNodeEvaluator(true);
         this.nodeEvaluator.setCanPassDoors(true);
         return new PathFinder(this.nodeEvaluator, i);
      }
   }
}
