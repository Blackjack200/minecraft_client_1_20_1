package net.minecraft.world.entity.animal;

import java.util.EnumSet;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public class Panda extends Animal {
   private static final EntityDataAccessor<Integer> UNHAPPY_COUNTER = SynchedEntityData.defineId(Panda.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> SNEEZE_COUNTER = SynchedEntityData.defineId(Panda.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> EAT_COUNTER = SynchedEntityData.defineId(Panda.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Byte> MAIN_GENE_ID = SynchedEntityData.defineId(Panda.class, EntityDataSerializers.BYTE);
   private static final EntityDataAccessor<Byte> HIDDEN_GENE_ID = SynchedEntityData.defineId(Panda.class, EntityDataSerializers.BYTE);
   private static final EntityDataAccessor<Byte> DATA_ID_FLAGS = SynchedEntityData.defineId(Panda.class, EntityDataSerializers.BYTE);
   static final TargetingConditions BREED_TARGETING = TargetingConditions.forNonCombat().range(8.0D);
   private static final int FLAG_SNEEZE = 2;
   private static final int FLAG_ROLL = 4;
   private static final int FLAG_SIT = 8;
   private static final int FLAG_ON_BACK = 16;
   private static final int EAT_TICK_INTERVAL = 5;
   public static final int TOTAL_ROLL_STEPS = 32;
   private static final int TOTAL_UNHAPPY_TIME = 32;
   boolean gotBamboo;
   boolean didBite;
   public int rollCounter;
   private Vec3 rollDelta;
   private float sitAmount;
   private float sitAmountO;
   private float onBackAmount;
   private float onBackAmountO;
   private float rollAmount;
   private float rollAmountO;
   Panda.PandaLookAtPlayerGoal lookAtPlayerGoal;
   static final Predicate<ItemEntity> PANDA_ITEMS = (itementity) -> {
      ItemStack itemstack = itementity.getItem();
      return (itemstack.is(Blocks.BAMBOO.asItem()) || itemstack.is(Blocks.CAKE.asItem())) && itementity.isAlive() && !itementity.hasPickUpDelay();
   };

   public Panda(EntityType<? extends Panda> entitytype, Level level) {
      super(entitytype, level);
      this.moveControl = new Panda.PandaMoveControl(this);
      if (!this.isBaby()) {
         this.setCanPickUpLoot(true);
      }

   }

   public boolean canTakeItem(ItemStack itemstack) {
      EquipmentSlot equipmentslot = Mob.getEquipmentSlotForItem(itemstack);
      if (!this.getItemBySlot(equipmentslot).isEmpty()) {
         return false;
      } else {
         return equipmentslot == EquipmentSlot.MAINHAND && super.canTakeItem(itemstack);
      }
   }

   public int getUnhappyCounter() {
      return this.entityData.get(UNHAPPY_COUNTER);
   }

   public void setUnhappyCounter(int i) {
      this.entityData.set(UNHAPPY_COUNTER, i);
   }

   public boolean isSneezing() {
      return this.getFlag(2);
   }

   public boolean isSitting() {
      return this.getFlag(8);
   }

   public void sit(boolean flag) {
      this.setFlag(8, flag);
   }

   public boolean isOnBack() {
      return this.getFlag(16);
   }

   public void setOnBack(boolean flag) {
      this.setFlag(16, flag);
   }

   public boolean isEating() {
      return this.entityData.get(EAT_COUNTER) > 0;
   }

   public void eat(boolean flag) {
      this.entityData.set(EAT_COUNTER, flag ? 1 : 0);
   }

   private int getEatCounter() {
      return this.entityData.get(EAT_COUNTER);
   }

   private void setEatCounter(int i) {
      this.entityData.set(EAT_COUNTER, i);
   }

   public void sneeze(boolean flag) {
      this.setFlag(2, flag);
      if (!flag) {
         this.setSneezeCounter(0);
      }

   }

   public int getSneezeCounter() {
      return this.entityData.get(SNEEZE_COUNTER);
   }

   public void setSneezeCounter(int i) {
      this.entityData.set(SNEEZE_COUNTER, i);
   }

   public Panda.Gene getMainGene() {
      return Panda.Gene.byId(this.entityData.get(MAIN_GENE_ID));
   }

   public void setMainGene(Panda.Gene panda_gene) {
      if (panda_gene.getId() > 6) {
         panda_gene = Panda.Gene.getRandom(this.random);
      }

      this.entityData.set(MAIN_GENE_ID, (byte)panda_gene.getId());
   }

   public Panda.Gene getHiddenGene() {
      return Panda.Gene.byId(this.entityData.get(HIDDEN_GENE_ID));
   }

   public void setHiddenGene(Panda.Gene panda_gene) {
      if (panda_gene.getId() > 6) {
         panda_gene = Panda.Gene.getRandom(this.random);
      }

      this.entityData.set(HIDDEN_GENE_ID, (byte)panda_gene.getId());
   }

   public boolean isRolling() {
      return this.getFlag(4);
   }

   public void roll(boolean flag) {
      this.setFlag(4, flag);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(UNHAPPY_COUNTER, 0);
      this.entityData.define(SNEEZE_COUNTER, 0);
      this.entityData.define(MAIN_GENE_ID, (byte)0);
      this.entityData.define(HIDDEN_GENE_ID, (byte)0);
      this.entityData.define(DATA_ID_FLAGS, (byte)0);
      this.entityData.define(EAT_COUNTER, 0);
   }

   private boolean getFlag(int i) {
      return (this.entityData.get(DATA_ID_FLAGS) & i) != 0;
   }

   private void setFlag(int i, boolean flag) {
      byte b0 = this.entityData.get(DATA_ID_FLAGS);
      if (flag) {
         this.entityData.set(DATA_ID_FLAGS, (byte)(b0 | i));
      } else {
         this.entityData.set(DATA_ID_FLAGS, (byte)(b0 & ~i));
      }

   }

   public void addAdditionalSaveData(CompoundTag compoundtag) {
      super.addAdditionalSaveData(compoundtag);
      compoundtag.putString("MainGene", this.getMainGene().getSerializedName());
      compoundtag.putString("HiddenGene", this.getHiddenGene().getSerializedName());
   }

   public void readAdditionalSaveData(CompoundTag compoundtag) {
      super.readAdditionalSaveData(compoundtag);
      this.setMainGene(Panda.Gene.byName(compoundtag.getString("MainGene")));
      this.setHiddenGene(Panda.Gene.byName(compoundtag.getString("HiddenGene")));
   }

   @Nullable
   public AgeableMob getBreedOffspring(ServerLevel serverlevel, AgeableMob ageablemob) {
      Panda panda = EntityType.PANDA.create(serverlevel);
      if (panda != null) {
         if (ageablemob instanceof Panda) {
            Panda panda1 = (Panda)ageablemob;
            panda.setGeneFromParents(this, panda1);
         }

         panda.setAttributes();
      }

      return panda;
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(2, new Panda.PandaPanicGoal(this, 2.0D));
      this.goalSelector.addGoal(2, new Panda.PandaBreedGoal(this, 1.0D));
      this.goalSelector.addGoal(3, new Panda.PandaAttackGoal(this, (double)1.2F, true));
      this.goalSelector.addGoal(4, new TemptGoal(this, 1.0D, Ingredient.of(Blocks.BAMBOO.asItem()), false));
      this.goalSelector.addGoal(6, new Panda.PandaAvoidGoal<>(this, Player.class, 8.0F, 2.0D, 2.0D));
      this.goalSelector.addGoal(6, new Panda.PandaAvoidGoal<>(this, Monster.class, 4.0F, 2.0D, 2.0D));
      this.goalSelector.addGoal(7, new Panda.PandaSitGoal());
      this.goalSelector.addGoal(8, new Panda.PandaLieOnBackGoal(this));
      this.goalSelector.addGoal(8, new Panda.PandaSneezeGoal(this));
      this.lookAtPlayerGoal = new Panda.PandaLookAtPlayerGoal(this, Player.class, 6.0F);
      this.goalSelector.addGoal(9, this.lookAtPlayerGoal);
      this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
      this.goalSelector.addGoal(12, new Panda.PandaRollGoal(this));
      this.goalSelector.addGoal(13, new FollowParentGoal(this, 1.25D));
      this.goalSelector.addGoal(14, new WaterAvoidingRandomStrollGoal(this, 1.0D));
      this.targetSelector.addGoal(1, (new Panda.PandaHurtByTargetGoal(this)).setAlertOthers(new Class[0]));
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, (double)0.15F).add(Attributes.ATTACK_DAMAGE, 6.0D);
   }

   public Panda.Gene getVariant() {
      return Panda.Gene.getVariantFromGenes(this.getMainGene(), this.getHiddenGene());
   }

   public boolean isLazy() {
      return this.getVariant() == Panda.Gene.LAZY;
   }

   public boolean isWorried() {
      return this.getVariant() == Panda.Gene.WORRIED;
   }

   public boolean isPlayful() {
      return this.getVariant() == Panda.Gene.PLAYFUL;
   }

   public boolean isBrown() {
      return this.getVariant() == Panda.Gene.BROWN;
   }

   public boolean isWeak() {
      return this.getVariant() == Panda.Gene.WEAK;
   }

   public boolean isAggressive() {
      return this.getVariant() == Panda.Gene.AGGRESSIVE;
   }

   public boolean canBeLeashed(Player player) {
      return false;
   }

   public boolean doHurtTarget(Entity entity) {
      this.playSound(SoundEvents.PANDA_BITE, 1.0F, 1.0F);
      if (!this.isAggressive()) {
         this.didBite = true;
      }

      return super.doHurtTarget(entity);
   }

   public void tick() {
      super.tick();
      if (this.isWorried()) {
         if (this.level().isThundering() && !this.isInWater()) {
            this.sit(true);
            this.eat(false);
         } else if (!this.isEating()) {
            this.sit(false);
         }
      }

      LivingEntity livingentity = this.getTarget();
      if (livingentity == null) {
         this.gotBamboo = false;
         this.didBite = false;
      }

      if (this.getUnhappyCounter() > 0) {
         if (livingentity != null) {
            this.lookAt(livingentity, 90.0F, 90.0F);
         }

         if (this.getUnhappyCounter() == 29 || this.getUnhappyCounter() == 14) {
            this.playSound(SoundEvents.PANDA_CANT_BREED, 1.0F, 1.0F);
         }

         this.setUnhappyCounter(this.getUnhappyCounter() - 1);
      }

      if (this.isSneezing()) {
         this.setSneezeCounter(this.getSneezeCounter() + 1);
         if (this.getSneezeCounter() > 20) {
            this.sneeze(false);
            this.afterSneeze();
         } else if (this.getSneezeCounter() == 1) {
            this.playSound(SoundEvents.PANDA_PRE_SNEEZE, 1.0F, 1.0F);
         }
      }

      if (this.isRolling()) {
         this.handleRoll();
      } else {
         this.rollCounter = 0;
      }

      if (this.isSitting()) {
         this.setXRot(0.0F);
      }

      this.updateSitAmount();
      this.handleEating();
      this.updateOnBackAnimation();
      this.updateRollAmount();
   }

   public boolean isScared() {
      return this.isWorried() && this.level().isThundering();
   }

   private void handleEating() {
      if (!this.isEating() && this.isSitting() && !this.isScared() && !this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty() && this.random.nextInt(80) == 1) {
         this.eat(true);
      } else if (this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty() || !this.isSitting()) {
         this.eat(false);
      }

      if (this.isEating()) {
         this.addEatingParticles();
         if (!this.level().isClientSide && this.getEatCounter() > 80 && this.random.nextInt(20) == 1) {
            if (this.getEatCounter() > 100 && this.isFoodOrCake(this.getItemBySlot(EquipmentSlot.MAINHAND))) {
               if (!this.level().isClientSide) {
                  this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                  this.gameEvent(GameEvent.EAT);
               }

               this.sit(false);
            }

            this.eat(false);
            return;
         }

         this.setEatCounter(this.getEatCounter() + 1);
      }

   }

   private void addEatingParticles() {
      if (this.getEatCounter() % 5 == 0) {
         this.playSound(SoundEvents.PANDA_EAT, 0.5F + 0.5F * (float)this.random.nextInt(2), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);

         for(int i = 0; i < 6; ++i) {
            Vec3 vec3 = new Vec3(((double)this.random.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, ((double)this.random.nextFloat() - 0.5D) * 0.1D);
            vec3 = vec3.xRot(-this.getXRot() * ((float)Math.PI / 180F));
            vec3 = vec3.yRot(-this.getYRot() * ((float)Math.PI / 180F));
            double d0 = (double)(-this.random.nextFloat()) * 0.6D - 0.3D;
            Vec3 vec31 = new Vec3(((double)this.random.nextFloat() - 0.5D) * 0.8D, d0, 1.0D + ((double)this.random.nextFloat() - 0.5D) * 0.4D);
            vec31 = vec31.yRot(-this.yBodyRot * ((float)Math.PI / 180F));
            vec31 = vec31.add(this.getX(), this.getEyeY() + 1.0D, this.getZ());
            this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, this.getItemBySlot(EquipmentSlot.MAINHAND)), vec31.x, vec31.y, vec31.z, vec3.x, vec3.y + 0.05D, vec3.z);
         }
      }

   }

   private void updateSitAmount() {
      this.sitAmountO = this.sitAmount;
      if (this.isSitting()) {
         this.sitAmount = Math.min(1.0F, this.sitAmount + 0.15F);
      } else {
         this.sitAmount = Math.max(0.0F, this.sitAmount - 0.19F);
      }

   }

   private void updateOnBackAnimation() {
      this.onBackAmountO = this.onBackAmount;
      if (this.isOnBack()) {
         this.onBackAmount = Math.min(1.0F, this.onBackAmount + 0.15F);
      } else {
         this.onBackAmount = Math.max(0.0F, this.onBackAmount - 0.19F);
      }

   }

   private void updateRollAmount() {
      this.rollAmountO = this.rollAmount;
      if (this.isRolling()) {
         this.rollAmount = Math.min(1.0F, this.rollAmount + 0.15F);
      } else {
         this.rollAmount = Math.max(0.0F, this.rollAmount - 0.19F);
      }

   }

   public float getSitAmount(float f) {
      return Mth.lerp(f, this.sitAmountO, this.sitAmount);
   }

   public float getLieOnBackAmount(float f) {
      return Mth.lerp(f, this.onBackAmountO, this.onBackAmount);
   }

   public float getRollAmount(float f) {
      return Mth.lerp(f, this.rollAmountO, this.rollAmount);
   }

   private void handleRoll() {
      ++this.rollCounter;
      if (this.rollCounter > 32) {
         this.roll(false);
      } else {
         if (!this.level().isClientSide) {
            Vec3 vec3 = this.getDeltaMovement();
            if (this.rollCounter == 1) {
               float f = this.getYRot() * ((float)Math.PI / 180F);
               float f1 = this.isBaby() ? 0.1F : 0.2F;
               this.rollDelta = new Vec3(vec3.x + (double)(-Mth.sin(f) * f1), 0.0D, vec3.z + (double)(Mth.cos(f) * f1));
               this.setDeltaMovement(this.rollDelta.add(0.0D, 0.27D, 0.0D));
            } else if ((float)this.rollCounter != 7.0F && (float)this.rollCounter != 15.0F && (float)this.rollCounter != 23.0F) {
               this.setDeltaMovement(this.rollDelta.x, vec3.y, this.rollDelta.z);
            } else {
               this.setDeltaMovement(0.0D, this.onGround() ? 0.27D : vec3.y, 0.0D);
            }
         }

      }
   }

   private void afterSneeze() {
      Vec3 vec3 = this.getDeltaMovement();
      this.level().addParticle(ParticleTypes.SNEEZE, this.getX() - (double)(this.getBbWidth() + 1.0F) * 0.5D * (double)Mth.sin(this.yBodyRot * ((float)Math.PI / 180F)), this.getEyeY() - (double)0.1F, this.getZ() + (double)(this.getBbWidth() + 1.0F) * 0.5D * (double)Mth.cos(this.yBodyRot * ((float)Math.PI / 180F)), vec3.x, 0.0D, vec3.z);
      this.playSound(SoundEvents.PANDA_SNEEZE, 1.0F, 1.0F);

      for(Panda panda : this.level().getEntitiesOfClass(Panda.class, this.getBoundingBox().inflate(10.0D))) {
         if (!panda.isBaby() && panda.onGround() && !panda.isInWater() && panda.canPerformAction()) {
            panda.jumpFromGround();
         }
      }

      if (!this.level().isClientSide() && this.random.nextInt(700) == 0 && this.level().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
         this.spawnAtLocation(Items.SLIME_BALL);
      }

   }

   protected void pickUpItem(ItemEntity itementity) {
      if (this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty() && PANDA_ITEMS.test(itementity)) {
         this.onItemPickup(itementity);
         ItemStack itemstack = itementity.getItem();
         this.setItemSlot(EquipmentSlot.MAINHAND, itemstack);
         this.setGuaranteedDrop(EquipmentSlot.MAINHAND);
         this.take(itementity, itemstack.getCount());
         itementity.discard();
      }

   }

   public boolean hurt(DamageSource damagesource, float f) {
      if (!this.level().isClientSide) {
         this.sit(false);
      }

      return super.hurt(damagesource, f);
   }

   @Nullable
   public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverlevelaccessor, DifficultyInstance difficultyinstance, MobSpawnType mobspawntype, @Nullable SpawnGroupData spawngroupdata, @Nullable CompoundTag compoundtag) {
      RandomSource randomsource = serverlevelaccessor.getRandom();
      this.setMainGene(Panda.Gene.getRandom(randomsource));
      this.setHiddenGene(Panda.Gene.getRandom(randomsource));
      this.setAttributes();
      if (spawngroupdata == null) {
         spawngroupdata = new AgeableMob.AgeableMobGroupData(0.2F);
      }

      return super.finalizeSpawn(serverlevelaccessor, difficultyinstance, mobspawntype, spawngroupdata, compoundtag);
   }

   public void setGeneFromParents(Panda panda, @Nullable Panda panda1) {
      if (panda1 == null) {
         if (this.random.nextBoolean()) {
            this.setMainGene(panda.getOneOfGenesRandomly());
            this.setHiddenGene(Panda.Gene.getRandom(this.random));
         } else {
            this.setMainGene(Panda.Gene.getRandom(this.random));
            this.setHiddenGene(panda.getOneOfGenesRandomly());
         }
      } else if (this.random.nextBoolean()) {
         this.setMainGene(panda.getOneOfGenesRandomly());
         this.setHiddenGene(panda1.getOneOfGenesRandomly());
      } else {
         this.setMainGene(panda1.getOneOfGenesRandomly());
         this.setHiddenGene(panda.getOneOfGenesRandomly());
      }

      if (this.random.nextInt(32) == 0) {
         this.setMainGene(Panda.Gene.getRandom(this.random));
      }

      if (this.random.nextInt(32) == 0) {
         this.setHiddenGene(Panda.Gene.getRandom(this.random));
      }

   }

   private Panda.Gene getOneOfGenesRandomly() {
      return this.random.nextBoolean() ? this.getMainGene() : this.getHiddenGene();
   }

   public void setAttributes() {
      if (this.isWeak()) {
         this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(10.0D);
      }

      if (this.isLazy()) {
         this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double)0.07F);
      }

   }

   void tryToSit() {
      if (!this.isInWater()) {
         this.setZza(0.0F);
         this.getNavigation().stop();
         this.sit(true);
      }

   }

   public InteractionResult mobInteract(Player player, InteractionHand interactionhand) {
      ItemStack itemstack = player.getItemInHand(interactionhand);
      if (this.isScared()) {
         return InteractionResult.PASS;
      } else if (this.isOnBack()) {
         this.setOnBack(false);
         return InteractionResult.sidedSuccess(this.level().isClientSide);
      } else if (this.isFood(itemstack)) {
         if (this.getTarget() != null) {
            this.gotBamboo = true;
         }

         if (this.isBaby()) {
            this.usePlayerItem(player, interactionhand, itemstack);
            this.ageUp((int)((float)(-this.getAge() / 20) * 0.1F), true);
         } else if (!this.level().isClientSide && this.getAge() == 0 && this.canFallInLove()) {
            this.usePlayerItem(player, interactionhand, itemstack);
            this.setInLove(player);
         } else {
            if (this.level().isClientSide || this.isSitting() || this.isInWater()) {
               return InteractionResult.PASS;
            }

            this.tryToSit();
            this.eat(true);
            ItemStack itemstack1 = this.getItemBySlot(EquipmentSlot.MAINHAND);
            if (!itemstack1.isEmpty() && !player.getAbilities().instabuild) {
               this.spawnAtLocation(itemstack1);
            }

            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(itemstack.getItem(), 1));
            this.usePlayerItem(player, interactionhand, itemstack);
         }

         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.PASS;
      }
   }

   @Nullable
   protected SoundEvent getAmbientSound() {
      if (this.isAggressive()) {
         return SoundEvents.PANDA_AGGRESSIVE_AMBIENT;
      } else {
         return this.isWorried() ? SoundEvents.PANDA_WORRIED_AMBIENT : SoundEvents.PANDA_AMBIENT;
      }
   }

   protected void playStepSound(BlockPos blockpos, BlockState blockstate) {
      this.playSound(SoundEvents.PANDA_STEP, 0.15F, 1.0F);
   }

   public boolean isFood(ItemStack itemstack) {
      return itemstack.is(Blocks.BAMBOO.asItem());
   }

   private boolean isFoodOrCake(ItemStack itemstack) {
      return this.isFood(itemstack) || itemstack.is(Blocks.CAKE.asItem());
   }

   @Nullable
   protected SoundEvent getDeathSound() {
      return SoundEvents.PANDA_DEATH;
   }

   @Nullable
   protected SoundEvent getHurtSound(DamageSource damagesource) {
      return SoundEvents.PANDA_HURT;
   }

   public boolean canPerformAction() {
      return !this.isOnBack() && !this.isScared() && !this.isEating() && !this.isRolling() && !this.isSitting();
   }

   public static enum Gene implements StringRepresentable {
      NORMAL(0, "normal", false),
      LAZY(1, "lazy", false),
      WORRIED(2, "worried", false),
      PLAYFUL(3, "playful", false),
      BROWN(4, "brown", true),
      WEAK(5, "weak", true),
      AGGRESSIVE(6, "aggressive", false);

      public static final StringRepresentable.EnumCodec<Panda.Gene> CODEC = StringRepresentable.fromEnum(Panda.Gene::values);
      private static final IntFunction<Panda.Gene> BY_ID = ByIdMap.continuous(Panda.Gene::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
      private static final int MAX_GENE = 6;
      private final int id;
      private final String name;
      private final boolean isRecessive;

      private Gene(int i, String s, boolean flag) {
         this.id = i;
         this.name = s;
         this.isRecessive = flag;
      }

      public int getId() {
         return this.id;
      }

      public String getSerializedName() {
         return this.name;
      }

      public boolean isRecessive() {
         return this.isRecessive;
      }

      static Panda.Gene getVariantFromGenes(Panda.Gene panda_gene, Panda.Gene panda_gene1) {
         if (panda_gene.isRecessive()) {
            return panda_gene == panda_gene1 ? panda_gene : NORMAL;
         } else {
            return panda_gene;
         }
      }

      public static Panda.Gene byId(int i) {
         return BY_ID.apply(i);
      }

      public static Panda.Gene byName(String s) {
         return CODEC.byName(s, NORMAL);
      }

      public static Panda.Gene getRandom(RandomSource randomsource) {
         int i = randomsource.nextInt(16);
         if (i == 0) {
            return LAZY;
         } else if (i == 1) {
            return WORRIED;
         } else if (i == 2) {
            return PLAYFUL;
         } else if (i == 4) {
            return AGGRESSIVE;
         } else if (i < 9) {
            return WEAK;
         } else {
            return i < 11 ? BROWN : NORMAL;
         }
      }
   }

   static class PandaAttackGoal extends MeleeAttackGoal {
      private final Panda panda;

      public PandaAttackGoal(Panda panda, double d0, boolean flag) {
         super(panda, d0, flag);
         this.panda = panda;
      }

      public boolean canUse() {
         return this.panda.canPerformAction() && super.canUse();
      }
   }

   static class PandaAvoidGoal<T extends LivingEntity> extends AvoidEntityGoal<T> {
      private final Panda panda;

      public PandaAvoidGoal(Panda panda, Class<T> oclass, float f, double d0, double d1) {
         super(panda, oclass, f, d0, d1, EntitySelector.NO_SPECTATORS::test);
         this.panda = panda;
      }

      public boolean canUse() {
         return this.panda.isWorried() && this.panda.canPerformAction() && super.canUse();
      }
   }

   static class PandaBreedGoal extends BreedGoal {
      private final Panda panda;
      private int unhappyCooldown;

      public PandaBreedGoal(Panda panda, double d0) {
         super(panda, d0);
         this.panda = panda;
      }

      public boolean canUse() {
         if (super.canUse() && this.panda.getUnhappyCounter() == 0) {
            if (!this.canFindBamboo()) {
               if (this.unhappyCooldown <= this.panda.tickCount) {
                  this.panda.setUnhappyCounter(32);
                  this.unhappyCooldown = this.panda.tickCount + 600;
                  if (this.panda.isEffectiveAi()) {
                     Player player = this.level.getNearestPlayer(Panda.BREED_TARGETING, this.panda);
                     this.panda.lookAtPlayerGoal.setTarget(player);
                  }
               }

               return false;
            } else {
               return true;
            }
         } else {
            return false;
         }
      }

      private boolean canFindBamboo() {
         BlockPos blockpos = this.panda.blockPosition();
         BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

         for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 8; ++j) {
               for(int k = 0; k <= j; k = k > 0 ? -k : 1 - k) {
                  for(int l = k < j && k > -j ? j : 0; l <= j; l = l > 0 ? -l : 1 - l) {
                     blockpos_mutableblockpos.setWithOffset(blockpos, k, i, l);
                     if (this.level.getBlockState(blockpos_mutableblockpos).is(Blocks.BAMBOO)) {
                        return true;
                     }
                  }
               }
            }
         }

         return false;
      }
   }

   static class PandaHurtByTargetGoal extends HurtByTargetGoal {
      private final Panda panda;

      public PandaHurtByTargetGoal(Panda panda, Class<?>... aclass) {
         super(panda, aclass);
         this.panda = panda;
      }

      public boolean canContinueToUse() {
         if (!this.panda.gotBamboo && !this.panda.didBite) {
            return super.canContinueToUse();
         } else {
            this.panda.setTarget((LivingEntity)null);
            return false;
         }
      }

      protected void alertOther(Mob mob, LivingEntity livingentity) {
         if (mob instanceof Panda && mob.isAggressive()) {
            mob.setTarget(livingentity);
         }

      }
   }

   static class PandaLieOnBackGoal extends Goal {
      private final Panda panda;
      private int cooldown;

      public PandaLieOnBackGoal(Panda panda) {
         this.panda = panda;
      }

      public boolean canUse() {
         return this.cooldown < this.panda.tickCount && this.panda.isLazy() && this.panda.canPerformAction() && this.panda.random.nextInt(reducedTickDelay(400)) == 1;
      }

      public boolean canContinueToUse() {
         if (!this.panda.isInWater() && (this.panda.isLazy() || this.panda.random.nextInt(reducedTickDelay(600)) != 1)) {
            return this.panda.random.nextInt(reducedTickDelay(2000)) != 1;
         } else {
            return false;
         }
      }

      public void start() {
         this.panda.setOnBack(true);
         this.cooldown = 0;
      }

      public void stop() {
         this.panda.setOnBack(false);
         this.cooldown = this.panda.tickCount + 200;
      }
   }

   static class PandaLookAtPlayerGoal extends LookAtPlayerGoal {
      private final Panda panda;

      public PandaLookAtPlayerGoal(Panda panda, Class<? extends LivingEntity> oclass, float f) {
         super(panda, oclass, f);
         this.panda = panda;
      }

      public void setTarget(LivingEntity livingentity) {
         this.lookAt = livingentity;
      }

      public boolean canContinueToUse() {
         return this.lookAt != null && super.canContinueToUse();
      }

      public boolean canUse() {
         if (this.mob.getRandom().nextFloat() >= this.probability) {
            return false;
         } else {
            if (this.lookAt == null) {
               if (this.lookAtType == Player.class) {
                  this.lookAt = this.mob.level().getNearestPlayer(this.lookAtContext, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
               } else {
                  this.lookAt = this.mob.level().getNearestEntity(this.mob.level().getEntitiesOfClass(this.lookAtType, this.mob.getBoundingBox().inflate((double)this.lookDistance, 3.0D, (double)this.lookDistance), (livingentity) -> true), this.lookAtContext, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
               }
            }

            return this.panda.canPerformAction() && this.lookAt != null;
         }
      }

      public void tick() {
         if (this.lookAt != null) {
            super.tick();
         }

      }
   }

   static class PandaMoveControl extends MoveControl {
      private final Panda panda;

      public PandaMoveControl(Panda panda) {
         super(panda);
         this.panda = panda;
      }

      public void tick() {
         if (this.panda.canPerformAction()) {
            super.tick();
         }
      }
   }

   static class PandaPanicGoal extends PanicGoal {
      private final Panda panda;

      public PandaPanicGoal(Panda panda, double d0) {
         super(panda, d0);
         this.panda = panda;
      }

      protected boolean shouldPanic() {
         return this.mob.isFreezing() || this.mob.isOnFire();
      }

      public boolean canContinueToUse() {
         if (this.panda.isSitting()) {
            this.panda.getNavigation().stop();
            return false;
         } else {
            return super.canContinueToUse();
         }
      }
   }

   static class PandaRollGoal extends Goal {
      private final Panda panda;

      public PandaRollGoal(Panda panda) {
         this.panda = panda;
         this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
      }

      public boolean canUse() {
         if ((this.panda.isBaby() || this.panda.isPlayful()) && this.panda.onGround()) {
            if (!this.panda.canPerformAction()) {
               return false;
            } else {
               float f = this.panda.getYRot() * ((float)Math.PI / 180F);
               float f1 = -Mth.sin(f);
               float f2 = Mth.cos(f);
               int i = (double)Math.abs(f1) > 0.5D ? Mth.sign((double)f1) : 0;
               int j = (double)Math.abs(f2) > 0.5D ? Mth.sign((double)f2) : 0;
               if (this.panda.level().getBlockState(this.panda.blockPosition().offset(i, -1, j)).isAir()) {
                  return true;
               } else if (this.panda.isPlayful() && this.panda.random.nextInt(reducedTickDelay(60)) == 1) {
                  return true;
               } else {
                  return this.panda.random.nextInt(reducedTickDelay(500)) == 1;
               }
            }
         } else {
            return false;
         }
      }

      public boolean canContinueToUse() {
         return false;
      }

      public void start() {
         this.panda.roll(true);
      }

      public boolean isInterruptable() {
         return false;
      }
   }

   class PandaSitGoal extends Goal {
      private int cooldown;

      public PandaSitGoal() {
         this.setFlags(EnumSet.of(Goal.Flag.MOVE));
      }

      public boolean canUse() {
         if (this.cooldown <= Panda.this.tickCount && !Panda.this.isBaby() && !Panda.this.isInWater() && Panda.this.canPerformAction() && Panda.this.getUnhappyCounter() <= 0) {
            List<ItemEntity> list = Panda.this.level().getEntitiesOfClass(ItemEntity.class, Panda.this.getBoundingBox().inflate(6.0D, 6.0D, 6.0D), Panda.PANDA_ITEMS);
            return !list.isEmpty() || !Panda.this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty();
         } else {
            return false;
         }
      }

      public boolean canContinueToUse() {
         if (!Panda.this.isInWater() && (Panda.this.isLazy() || Panda.this.random.nextInt(reducedTickDelay(600)) != 1)) {
            return Panda.this.random.nextInt(reducedTickDelay(2000)) != 1;
         } else {
            return false;
         }
      }

      public void tick() {
         if (!Panda.this.isSitting() && !Panda.this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
            Panda.this.tryToSit();
         }

      }

      public void start() {
         List<ItemEntity> list = Panda.this.level().getEntitiesOfClass(ItemEntity.class, Panda.this.getBoundingBox().inflate(8.0D, 8.0D, 8.0D), Panda.PANDA_ITEMS);
         if (!list.isEmpty() && Panda.this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
            Panda.this.getNavigation().moveTo(list.get(0), (double)1.2F);
         } else if (!Panda.this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
            Panda.this.tryToSit();
         }

         this.cooldown = 0;
      }

      public void stop() {
         ItemStack itemstack = Panda.this.getItemBySlot(EquipmentSlot.MAINHAND);
         if (!itemstack.isEmpty()) {
            Panda.this.spawnAtLocation(itemstack);
            Panda.this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            int i = Panda.this.isLazy() ? Panda.this.random.nextInt(50) + 10 : Panda.this.random.nextInt(150) + 10;
            this.cooldown = Panda.this.tickCount + i * 20;
         }

         Panda.this.sit(false);
      }
   }

   static class PandaSneezeGoal extends Goal {
      private final Panda panda;

      public PandaSneezeGoal(Panda panda) {
         this.panda = panda;
      }

      public boolean canUse() {
         if (this.panda.isBaby() && this.panda.canPerformAction()) {
            if (this.panda.isWeak() && this.panda.random.nextInt(reducedTickDelay(500)) == 1) {
               return true;
            } else {
               return this.panda.random.nextInt(reducedTickDelay(6000)) == 1;
            }
         } else {
            return false;
         }
      }

      public boolean canContinueToUse() {
         return false;
      }

      public void start() {
         this.panda.sneeze(true);
      }
   }
}
