package net.minecraft.world.entity.animal.horse;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HorseArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.SoundType;

public class Horse extends AbstractHorse implements VariantHolder<Variant> {
   private static final UUID ARMOR_MODIFIER_UUID = UUID.fromString("556E1665-8B10-40C8-8F9D-CF9B1667F295");
   private static final EntityDataAccessor<Integer> DATA_ID_TYPE_VARIANT = SynchedEntityData.defineId(Horse.class, EntityDataSerializers.INT);

   public Horse(EntityType<? extends Horse> entitytype, Level level) {
      super(entitytype, level);
   }

   protected void randomizeAttributes(RandomSource randomsource) {
      this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double)generateMaxHealth(randomsource::nextInt));
      this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(generateSpeed(randomsource::nextDouble));
      this.getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(generateJumpStrength(randomsource::nextDouble));
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_ID_TYPE_VARIANT, 0);
   }

   public void addAdditionalSaveData(CompoundTag compoundtag) {
      super.addAdditionalSaveData(compoundtag);
      compoundtag.putInt("Variant", this.getTypeVariant());
      if (!this.inventory.getItem(1).isEmpty()) {
         compoundtag.put("ArmorItem", this.inventory.getItem(1).save(new CompoundTag()));
      }

   }

   public ItemStack getArmor() {
      return this.getItemBySlot(EquipmentSlot.CHEST);
   }

   private void setArmor(ItemStack itemstack) {
      this.setItemSlot(EquipmentSlot.CHEST, itemstack);
      this.setDropChance(EquipmentSlot.CHEST, 0.0F);
   }

   public void readAdditionalSaveData(CompoundTag compoundtag) {
      super.readAdditionalSaveData(compoundtag);
      this.setTypeVariant(compoundtag.getInt("Variant"));
      if (compoundtag.contains("ArmorItem", 10)) {
         ItemStack itemstack = ItemStack.of(compoundtag.getCompound("ArmorItem"));
         if (!itemstack.isEmpty() && this.isArmor(itemstack)) {
            this.inventory.setItem(1, itemstack);
         }
      }

      this.updateContainerEquipment();
   }

   private void setTypeVariant(int i) {
      this.entityData.set(DATA_ID_TYPE_VARIANT, i);
   }

   private int getTypeVariant() {
      return this.entityData.get(DATA_ID_TYPE_VARIANT);
   }

   private void setVariantAndMarkings(Variant variant, Markings markings) {
      this.setTypeVariant(variant.getId() & 255 | markings.getId() << 8 & '\uff00');
   }

   public Variant getVariant() {
      return Variant.byId(this.getTypeVariant() & 255);
   }

   public void setVariant(Variant variant) {
      this.setTypeVariant(variant.getId() & 255 | this.getTypeVariant() & -256);
   }

   public Markings getMarkings() {
      return Markings.byId((this.getTypeVariant() & '\uff00') >> 8);
   }

   protected void updateContainerEquipment() {
      if (!this.level().isClientSide) {
         super.updateContainerEquipment();
         this.setArmorEquipment(this.inventory.getItem(1));
         this.setDropChance(EquipmentSlot.CHEST, 0.0F);
      }
   }

   private void setArmorEquipment(ItemStack itemstack) {
      this.setArmor(itemstack);
      if (!this.level().isClientSide) {
         this.getAttribute(Attributes.ARMOR).removeModifier(ARMOR_MODIFIER_UUID);
         if (this.isArmor(itemstack)) {
            int i = ((HorseArmorItem)itemstack.getItem()).getProtection();
            if (i != 0) {
               this.getAttribute(Attributes.ARMOR).addTransientModifier(new AttributeModifier(ARMOR_MODIFIER_UUID, "Horse armor bonus", (double)i, AttributeModifier.Operation.ADDITION));
            }
         }
      }

   }

   public void containerChanged(Container container) {
      ItemStack itemstack = this.getArmor();
      super.containerChanged(container);
      ItemStack itemstack1 = this.getArmor();
      if (this.tickCount > 20 && this.isArmor(itemstack1) && itemstack != itemstack1) {
         this.playSound(SoundEvents.HORSE_ARMOR, 0.5F, 1.0F);
      }

   }

   protected void playGallopSound(SoundType soundtype) {
      super.playGallopSound(soundtype);
      if (this.random.nextInt(10) == 0) {
         this.playSound(SoundEvents.HORSE_BREATHE, soundtype.getVolume() * 0.6F, soundtype.getPitch());
      }

   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.HORSE_AMBIENT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.HORSE_DEATH;
   }

   @Nullable
   protected SoundEvent getEatingSound() {
      return SoundEvents.HORSE_EAT;
   }

   protected SoundEvent getHurtSound(DamageSource damagesource) {
      return SoundEvents.HORSE_HURT;
   }

   protected SoundEvent getAngrySound() {
      return SoundEvents.HORSE_ANGRY;
   }

   public InteractionResult mobInteract(Player player, InteractionHand interactionhand) {
      boolean flag = !this.isBaby() && this.isTamed() && player.isSecondaryUseActive();
      if (!this.isVehicle() && !flag) {
         ItemStack itemstack = player.getItemInHand(interactionhand);
         if (!itemstack.isEmpty()) {
            if (this.isFood(itemstack)) {
               return this.fedFood(player, itemstack);
            }

            if (!this.isTamed()) {
               this.makeMad();
               return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
         }

         return super.mobInteract(player, interactionhand);
      } else {
         return super.mobInteract(player, interactionhand);
      }
   }

   public boolean canMate(Animal animal) {
      if (animal == this) {
         return false;
      } else if (!(animal instanceof Donkey) && !(animal instanceof Horse)) {
         return false;
      } else {
         return this.canParent() && ((AbstractHorse)animal).canParent();
      }
   }

   @Nullable
   public AgeableMob getBreedOffspring(ServerLevel serverlevel, AgeableMob ageablemob) {
      if (ageablemob instanceof Donkey) {
         Mule mule = EntityType.MULE.create(serverlevel);
         if (mule != null) {
            this.setOffspringAttributes(ageablemob, mule);
         }

         return mule;
      } else {
         Horse horse = (Horse)ageablemob;
         Horse horse1 = EntityType.HORSE.create(serverlevel);
         if (horse1 != null) {
            int i = this.random.nextInt(9);
            Variant variant;
            if (i < 4) {
               variant = this.getVariant();
            } else if (i < 8) {
               variant = horse.getVariant();
            } else {
               variant = Util.getRandom(Variant.values(), this.random);
            }

            int j = this.random.nextInt(5);
            Markings markings;
            if (j < 2) {
               markings = this.getMarkings();
            } else if (j < 4) {
               markings = horse.getMarkings();
            } else {
               markings = Util.getRandom(Markings.values(), this.random);
            }

            horse1.setVariantAndMarkings(variant, markings);
            this.setOffspringAttributes(ageablemob, horse1);
         }

         return horse1;
      }
   }

   public boolean canWearArmor() {
      return true;
   }

   public boolean isArmor(ItemStack itemstack) {
      return itemstack.getItem() instanceof HorseArmorItem;
   }

   @Nullable
   public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverlevelaccessor, DifficultyInstance difficultyinstance, MobSpawnType mobspawntype, @Nullable SpawnGroupData spawngroupdata, @Nullable CompoundTag compoundtag) {
      RandomSource randomsource = serverlevelaccessor.getRandom();
      Variant variant;
      if (spawngroupdata instanceof Horse.HorseGroupData) {
         variant = ((Horse.HorseGroupData)spawngroupdata).variant;
      } else {
         variant = Util.getRandom(Variant.values(), randomsource);
         spawngroupdata = new Horse.HorseGroupData(variant);
      }

      this.setVariantAndMarkings(variant, Util.getRandom(Markings.values(), randomsource));
      return super.finalizeSpawn(serverlevelaccessor, difficultyinstance, mobspawntype, spawngroupdata, compoundtag);
   }

   public static class HorseGroupData extends AgeableMob.AgeableMobGroupData {
      public final Variant variant;

      public HorseGroupData(Variant variant) {
         super(true);
         this.variant = variant;
      }
   }
}
