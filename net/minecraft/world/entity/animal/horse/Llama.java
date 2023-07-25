package net.minecraft.world.entity.animal.horse;

import com.mojang.serialization.Codec;
import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Container;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LlamaFollowCaravanGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.RunAroundLikeCrazyGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LlamaSpit;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WoolCarpetBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class Llama extends AbstractChestedHorse implements VariantHolder<Llama.Variant>, RangedAttackMob {
   private static final int MAX_STRENGTH = 5;
   private static final Ingredient FOOD_ITEMS = Ingredient.of(Items.WHEAT, Blocks.HAY_BLOCK.asItem());
   private static final EntityDataAccessor<Integer> DATA_STRENGTH_ID = SynchedEntityData.defineId(Llama.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> DATA_SWAG_ID = SynchedEntityData.defineId(Llama.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> DATA_VARIANT_ID = SynchedEntityData.defineId(Llama.class, EntityDataSerializers.INT);
   boolean didSpit;
   @Nullable
   private Llama caravanHead;
   @Nullable
   private Llama caravanTail;

   public Llama(EntityType<? extends Llama> entitytype, Level level) {
      super(entitytype, level);
   }

   public boolean isTraderLlama() {
      return false;
   }

   private void setStrength(int i) {
      this.entityData.set(DATA_STRENGTH_ID, Math.max(1, Math.min(5, i)));
   }

   private void setRandomStrength(RandomSource randomsource) {
      int i = randomsource.nextFloat() < 0.04F ? 5 : 3;
      this.setStrength(1 + randomsource.nextInt(i));
   }

   public int getStrength() {
      return this.entityData.get(DATA_STRENGTH_ID);
   }

   public void addAdditionalSaveData(CompoundTag compoundtag) {
      super.addAdditionalSaveData(compoundtag);
      compoundtag.putInt("Variant", this.getVariant().id);
      compoundtag.putInt("Strength", this.getStrength());
      if (!this.inventory.getItem(1).isEmpty()) {
         compoundtag.put("DecorItem", this.inventory.getItem(1).save(new CompoundTag()));
      }

   }

   public void readAdditionalSaveData(CompoundTag compoundtag) {
      this.setStrength(compoundtag.getInt("Strength"));
      super.readAdditionalSaveData(compoundtag);
      this.setVariant(Llama.Variant.byId(compoundtag.getInt("Variant")));
      if (compoundtag.contains("DecorItem", 10)) {
         this.inventory.setItem(1, ItemStack.of(compoundtag.getCompound("DecorItem")));
      }

      this.updateContainerEquipment();
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(1, new RunAroundLikeCrazyGoal(this, 1.2D));
      this.goalSelector.addGoal(2, new LlamaFollowCaravanGoal(this, (double)2.1F));
      this.goalSelector.addGoal(3, new RangedAttackGoal(this, 1.25D, 40, 20.0F));
      this.goalSelector.addGoal(3, new PanicGoal(this, 1.2D));
      this.goalSelector.addGoal(4, new BreedGoal(this, 1.0D));
      this.goalSelector.addGoal(5, new TemptGoal(this, 1.25D, Ingredient.of(Items.HAY_BLOCK), false));
      this.goalSelector.addGoal(6, new FollowParentGoal(this, 1.0D));
      this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.7D));
      this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 6.0F));
      this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
      this.targetSelector.addGoal(1, new Llama.LlamaHurtByTargetGoal(this));
      this.targetSelector.addGoal(2, new Llama.LlamaAttackWolfGoal(this));
   }

   public static AttributeSupplier.Builder createAttributes() {
      return createBaseChestedHorseAttributes().add(Attributes.FOLLOW_RANGE, 40.0D);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_STRENGTH_ID, 0);
      this.entityData.define(DATA_SWAG_ID, -1);
      this.entityData.define(DATA_VARIANT_ID, 0);
   }

   public Llama.Variant getVariant() {
      return Llama.Variant.byId(this.entityData.get(DATA_VARIANT_ID));
   }

   public void setVariant(Llama.Variant llama_variant) {
      this.entityData.set(DATA_VARIANT_ID, llama_variant.id);
   }

   protected int getInventorySize() {
      return this.hasChest() ? 2 + 3 * this.getInventoryColumns() : super.getInventorySize();
   }

   protected void positionRider(Entity entity, Entity.MoveFunction entity_movefunction) {
      if (this.hasPassenger(entity)) {
         float f = Mth.cos(this.yBodyRot * ((float)Math.PI / 180F));
         float f1 = Mth.sin(this.yBodyRot * ((float)Math.PI / 180F));
         float f2 = 0.3F;
         entity_movefunction.accept(entity, this.getX() + (double)(0.3F * f1), this.getY() + this.getPassengersRidingOffset() + entity.getMyRidingOffset(), this.getZ() - (double)(0.3F * f));
      }
   }

   public double getPassengersRidingOffset() {
      return (double)this.getBbHeight() * 0.6D;
   }

   @Nullable
   public LivingEntity getControllingPassenger() {
      return null;
   }

   public boolean isFood(ItemStack itemstack) {
      return FOOD_ITEMS.test(itemstack);
   }

   protected boolean handleEating(Player player, ItemStack itemstack) {
      int i = 0;
      int j = 0;
      float f = 0.0F;
      boolean flag = false;
      if (itemstack.is(Items.WHEAT)) {
         i = 10;
         j = 3;
         f = 2.0F;
      } else if (itemstack.is(Blocks.HAY_BLOCK.asItem())) {
         i = 90;
         j = 6;
         f = 10.0F;
         if (this.isTamed() && this.getAge() == 0 && this.canFallInLove()) {
            flag = true;
            this.setInLove(player);
         }
      }

      if (this.getHealth() < this.getMaxHealth() && f > 0.0F) {
         this.heal(f);
         flag = true;
      }

      if (this.isBaby() && i > 0) {
         this.level().addParticle(ParticleTypes.HAPPY_VILLAGER, this.getRandomX(1.0D), this.getRandomY() + 0.5D, this.getRandomZ(1.0D), 0.0D, 0.0D, 0.0D);
         if (!this.level().isClientSide) {
            this.ageUp(i);
         }

         flag = true;
      }

      if (j > 0 && (flag || !this.isTamed()) && this.getTemper() < this.getMaxTemper()) {
         flag = true;
         if (!this.level().isClientSide) {
            this.modifyTemper(j);
         }
      }

      if (flag && !this.isSilent()) {
         SoundEvent soundevent = this.getEatingSound();
         if (soundevent != null) {
            this.level().playSound((Player)null, this.getX(), this.getY(), this.getZ(), this.getEatingSound(), this.getSoundSource(), 1.0F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
         }
      }

      return flag;
   }

   public boolean isImmobile() {
      return this.isDeadOrDying() || this.isEating();
   }

   @Nullable
   public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverlevelaccessor, DifficultyInstance difficultyinstance, MobSpawnType mobspawntype, @Nullable SpawnGroupData spawngroupdata, @Nullable CompoundTag compoundtag) {
      RandomSource randomsource = serverlevelaccessor.getRandom();
      this.setRandomStrength(randomsource);
      Llama.Variant llama_variant;
      if (spawngroupdata instanceof Llama.LlamaGroupData) {
         llama_variant = ((Llama.LlamaGroupData)spawngroupdata).variant;
      } else {
         llama_variant = Util.getRandom(Llama.Variant.values(), randomsource);
         spawngroupdata = new Llama.LlamaGroupData(llama_variant);
      }

      this.setVariant(llama_variant);
      return super.finalizeSpawn(serverlevelaccessor, difficultyinstance, mobspawntype, spawngroupdata, compoundtag);
   }

   protected boolean canPerformRearing() {
      return false;
   }

   protected SoundEvent getAngrySound() {
      return SoundEvents.LLAMA_ANGRY;
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.LLAMA_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource damagesource) {
      return SoundEvents.LLAMA_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.LLAMA_DEATH;
   }

   @Nullable
   protected SoundEvent getEatingSound() {
      return SoundEvents.LLAMA_EAT;
   }

   protected void playStepSound(BlockPos blockpos, BlockState blockstate) {
      this.playSound(SoundEvents.LLAMA_STEP, 0.15F, 1.0F);
   }

   protected void playChestEquipsSound() {
      this.playSound(SoundEvents.LLAMA_CHEST, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
   }

   public int getInventoryColumns() {
      return this.getStrength();
   }

   public boolean canWearArmor() {
      return true;
   }

   public boolean isWearingArmor() {
      return !this.inventory.getItem(1).isEmpty();
   }

   public boolean isArmor(ItemStack itemstack) {
      return itemstack.is(ItemTags.WOOL_CARPETS);
   }

   public boolean isSaddleable() {
      return false;
   }

   public void containerChanged(Container container) {
      DyeColor dyecolor = this.getSwag();
      super.containerChanged(container);
      DyeColor dyecolor1 = this.getSwag();
      if (this.tickCount > 20 && dyecolor1 != null && dyecolor1 != dyecolor) {
         this.playSound(SoundEvents.LLAMA_SWAG, 0.5F, 1.0F);
      }

   }

   protected void updateContainerEquipment() {
      if (!this.level().isClientSide) {
         super.updateContainerEquipment();
         this.setSwag(getDyeColor(this.inventory.getItem(1)));
      }
   }

   private void setSwag(@Nullable DyeColor dyecolor) {
      this.entityData.set(DATA_SWAG_ID, dyecolor == null ? -1 : dyecolor.getId());
   }

   @Nullable
   private static DyeColor getDyeColor(ItemStack itemstack) {
      Block block = Block.byItem(itemstack.getItem());
      return block instanceof WoolCarpetBlock ? ((WoolCarpetBlock)block).getColor() : null;
   }

   @Nullable
   public DyeColor getSwag() {
      int i = this.entityData.get(DATA_SWAG_ID);
      return i == -1 ? null : DyeColor.byId(i);
   }

   public int getMaxTemper() {
      return 30;
   }

   public boolean canMate(Animal animal) {
      return animal != this && animal instanceof Llama && this.canParent() && ((Llama)animal).canParent();
   }

   @Nullable
   public Llama getBreedOffspring(ServerLevel serverlevel, AgeableMob ageablemob) {
      Llama llama = this.makeNewLlama();
      if (llama != null) {
         this.setOffspringAttributes(ageablemob, llama);
         Llama llama1 = (Llama)ageablemob;
         int i = this.random.nextInt(Math.max(this.getStrength(), llama1.getStrength())) + 1;
         if (this.random.nextFloat() < 0.03F) {
            ++i;
         }

         llama.setStrength(i);
         llama.setVariant(this.random.nextBoolean() ? this.getVariant() : llama1.getVariant());
      }

      return llama;
   }

   @Nullable
   protected Llama makeNewLlama() {
      return EntityType.LLAMA.create(this.level());
   }

   private void spit(LivingEntity livingentity) {
      LlamaSpit llamaspit = new LlamaSpit(this.level(), this);
      double d0 = livingentity.getX() - this.getX();
      double d1 = livingentity.getY(0.3333333333333333D) - llamaspit.getY();
      double d2 = livingentity.getZ() - this.getZ();
      double d3 = Math.sqrt(d0 * d0 + d2 * d2) * (double)0.2F;
      llamaspit.shoot(d0, d1 + d3, d2, 1.5F, 10.0F);
      if (!this.isSilent()) {
         this.level().playSound((Player)null, this.getX(), this.getY(), this.getZ(), SoundEvents.LLAMA_SPIT, this.getSoundSource(), 1.0F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
      }

      this.level().addFreshEntity(llamaspit);
      this.didSpit = true;
   }

   void setDidSpit(boolean flag) {
      this.didSpit = flag;
   }

   public boolean causeFallDamage(float f, float f1, DamageSource damagesource) {
      int i = this.calculateFallDamage(f, f1);
      if (i <= 0) {
         return false;
      } else {
         if (f >= 6.0F) {
            this.hurt(damagesource, (float)i);
            if (this.isVehicle()) {
               for(Entity entity : this.getIndirectPassengers()) {
                  entity.hurt(damagesource, (float)i);
               }
            }
         }

         this.playBlockFallSound();
         return true;
      }
   }

   public void leaveCaravan() {
      if (this.caravanHead != null) {
         this.caravanHead.caravanTail = null;
      }

      this.caravanHead = null;
   }

   public void joinCaravan(Llama llama) {
      this.caravanHead = llama;
      this.caravanHead.caravanTail = this;
   }

   public boolean hasCaravanTail() {
      return this.caravanTail != null;
   }

   public boolean inCaravan() {
      return this.caravanHead != null;
   }

   @Nullable
   public Llama getCaravanHead() {
      return this.caravanHead;
   }

   protected double followLeashSpeed() {
      return 2.0D;
   }

   protected void followMommy() {
      if (!this.inCaravan() && this.isBaby()) {
         super.followMommy();
      }

   }

   public boolean canEatGrass() {
      return false;
   }

   public void performRangedAttack(LivingEntity livingentity, float f) {
      this.spit(livingentity);
   }

   public Vec3 getLeashOffset() {
      return new Vec3(0.0D, 0.75D * (double)this.getEyeHeight(), (double)this.getBbWidth() * 0.5D);
   }

   static class LlamaAttackWolfGoal extends NearestAttackableTargetGoal<Wolf> {
      public LlamaAttackWolfGoal(Llama llama) {
         super(llama, Wolf.class, 16, false, true, (livingentity) -> !((Wolf)livingentity).isTame());
      }

      protected double getFollowDistance() {
         return super.getFollowDistance() * 0.25D;
      }
   }

   static class LlamaGroupData extends AgeableMob.AgeableMobGroupData {
      public final Llama.Variant variant;

      LlamaGroupData(Llama.Variant llama_variant) {
         super(true);
         this.variant = llama_variant;
      }
   }

   static class LlamaHurtByTargetGoal extends HurtByTargetGoal {
      public LlamaHurtByTargetGoal(Llama llama) {
         super(llama);
      }

      public boolean canContinueToUse() {
         if (this.mob instanceof Llama) {
            Llama llama = (Llama)this.mob;
            if (llama.didSpit) {
               llama.setDidSpit(false);
               return false;
            }
         }

         return super.canContinueToUse();
      }
   }

   public static enum Variant implements StringRepresentable {
      CREAMY(0, "creamy"),
      WHITE(1, "white"),
      BROWN(2, "brown"),
      GRAY(3, "gray");

      public static final Codec<Llama.Variant> CODEC = StringRepresentable.fromEnum(Llama.Variant::values);
      private static final IntFunction<Llama.Variant> BY_ID = ByIdMap.continuous(Llama.Variant::getId, values(), ByIdMap.OutOfBoundsStrategy.CLAMP);
      final int id;
      private final String name;

      private Variant(int i, String s) {
         this.id = i;
         this.name = s;
      }

      public int getId() {
         return this.id;
      }

      public static Llama.Variant byId(int i) {
         return BY_ID.apply(i);
      }

      public String getSerializedName() {
         return this.name;
      }
   }
}
