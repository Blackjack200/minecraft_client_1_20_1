package net.minecraft.world.entity.projectile;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Set;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;

public class Arrow extends AbstractArrow {
   private static final int EXPOSED_POTION_DECAY_TIME = 600;
   private static final int NO_EFFECT_COLOR = -1;
   private static final EntityDataAccessor<Integer> ID_EFFECT_COLOR = SynchedEntityData.defineId(Arrow.class, EntityDataSerializers.INT);
   private static final byte EVENT_POTION_PUFF = 0;
   private Potion potion = Potions.EMPTY;
   private final Set<MobEffectInstance> effects = Sets.newHashSet();
   private boolean fixedColor;

   public Arrow(EntityType<? extends Arrow> entitytype, Level level) {
      super(entitytype, level);
   }

   public Arrow(Level level, double d0, double d1, double d2) {
      super(EntityType.ARROW, d0, d1, d2, level);
   }

   public Arrow(Level level, LivingEntity livingentity) {
      super(EntityType.ARROW, livingentity, level);
   }

   public void setEffectsFromItem(ItemStack itemstack) {
      if (itemstack.is(Items.TIPPED_ARROW)) {
         this.potion = PotionUtils.getPotion(itemstack);
         Collection<MobEffectInstance> collection = PotionUtils.getCustomEffects(itemstack);
         if (!collection.isEmpty()) {
            for(MobEffectInstance mobeffectinstance : collection) {
               this.effects.add(new MobEffectInstance(mobeffectinstance));
            }
         }

         int i = getCustomColor(itemstack);
         if (i == -1) {
            this.updateColor();
         } else {
            this.setFixedColor(i);
         }
      } else if (itemstack.is(Items.ARROW)) {
         this.potion = Potions.EMPTY;
         this.effects.clear();
         this.entityData.set(ID_EFFECT_COLOR, -1);
      }

   }

   public static int getCustomColor(ItemStack itemstack) {
      CompoundTag compoundtag = itemstack.getTag();
      return compoundtag != null && compoundtag.contains("CustomPotionColor", 99) ? compoundtag.getInt("CustomPotionColor") : -1;
   }

   private void updateColor() {
      this.fixedColor = false;
      if (this.potion == Potions.EMPTY && this.effects.isEmpty()) {
         this.entityData.set(ID_EFFECT_COLOR, -1);
      } else {
         this.entityData.set(ID_EFFECT_COLOR, PotionUtils.getColor(PotionUtils.getAllEffects(this.potion, this.effects)));
      }

   }

   public void addEffect(MobEffectInstance mobeffectinstance) {
      this.effects.add(mobeffectinstance);
      this.getEntityData().set(ID_EFFECT_COLOR, PotionUtils.getColor(PotionUtils.getAllEffects(this.potion, this.effects)));
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(ID_EFFECT_COLOR, -1);
   }

   public void tick() {
      super.tick();
      if (this.level().isClientSide) {
         if (this.inGround) {
            if (this.inGroundTime % 5 == 0) {
               this.makeParticle(1);
            }
         } else {
            this.makeParticle(2);
         }
      } else if (this.inGround && this.inGroundTime != 0 && !this.effects.isEmpty() && this.inGroundTime >= 600) {
         this.level().broadcastEntityEvent(this, (byte)0);
         this.potion = Potions.EMPTY;
         this.effects.clear();
         this.entityData.set(ID_EFFECT_COLOR, -1);
      }

   }

   private void makeParticle(int i) {
      int j = this.getColor();
      if (j != -1 && i > 0) {
         double d0 = (double)(j >> 16 & 255) / 255.0D;
         double d1 = (double)(j >> 8 & 255) / 255.0D;
         double d2 = (double)(j >> 0 & 255) / 255.0D;

         for(int k = 0; k < i; ++k) {
            this.level().addParticle(ParticleTypes.ENTITY_EFFECT, this.getRandomX(0.5D), this.getRandomY(), this.getRandomZ(0.5D), d0, d1, d2);
         }

      }
   }

   public int getColor() {
      return this.entityData.get(ID_EFFECT_COLOR);
   }

   private void setFixedColor(int i) {
      this.fixedColor = true;
      this.entityData.set(ID_EFFECT_COLOR, i);
   }

   public void addAdditionalSaveData(CompoundTag compoundtag) {
      super.addAdditionalSaveData(compoundtag);
      if (this.potion != Potions.EMPTY) {
         compoundtag.putString("Potion", BuiltInRegistries.POTION.getKey(this.potion).toString());
      }

      if (this.fixedColor) {
         compoundtag.putInt("Color", this.getColor());
      }

      if (!this.effects.isEmpty()) {
         ListTag listtag = new ListTag();

         for(MobEffectInstance mobeffectinstance : this.effects) {
            listtag.add(mobeffectinstance.save(new CompoundTag()));
         }

         compoundtag.put("CustomPotionEffects", listtag);
      }

   }

   public void readAdditionalSaveData(CompoundTag compoundtag) {
      super.readAdditionalSaveData(compoundtag);
      if (compoundtag.contains("Potion", 8)) {
         this.potion = PotionUtils.getPotion(compoundtag);
      }

      for(MobEffectInstance mobeffectinstance : PotionUtils.getCustomEffects(compoundtag)) {
         this.addEffect(mobeffectinstance);
      }

      if (compoundtag.contains("Color", 99)) {
         this.setFixedColor(compoundtag.getInt("Color"));
      } else {
         this.updateColor();
      }

   }

   protected void doPostHurtEffects(LivingEntity livingentity) {
      super.doPostHurtEffects(livingentity);
      Entity entity = this.getEffectSource();

      for(MobEffectInstance mobeffectinstance : this.potion.getEffects()) {
         livingentity.addEffect(new MobEffectInstance(mobeffectinstance.getEffect(), Math.max(mobeffectinstance.mapDuration((i) -> i / 8), 1), mobeffectinstance.getAmplifier(), mobeffectinstance.isAmbient(), mobeffectinstance.isVisible()), entity);
      }

      if (!this.effects.isEmpty()) {
         for(MobEffectInstance mobeffectinstance1 : this.effects) {
            livingentity.addEffect(mobeffectinstance1, entity);
         }
      }

   }

   protected ItemStack getPickupItem() {
      if (this.effects.isEmpty() && this.potion == Potions.EMPTY) {
         return new ItemStack(Items.ARROW);
      } else {
         ItemStack itemstack = new ItemStack(Items.TIPPED_ARROW);
         PotionUtils.setPotion(itemstack, this.potion);
         PotionUtils.setCustomEffects(itemstack, this.effects);
         if (this.fixedColor) {
            itemstack.getOrCreateTag().putInt("CustomPotionColor", this.getColor());
         }

         return itemstack;
      }
   }

   public void handleEntityEvent(byte b0) {
      if (b0 == 0) {
         int i = this.getColor();
         if (i != -1) {
            double d0 = (double)(i >> 16 & 255) / 255.0D;
            double d1 = (double)(i >> 8 & 255) / 255.0D;
            double d2 = (double)(i >> 0 & 255) / 255.0D;

            for(int j = 0; j < 20; ++j) {
               this.level().addParticle(ParticleTypes.ENTITY_EFFECT, this.getRandomX(0.5D), this.getRandomY(), this.getRandomZ(0.5D), d0, d1, d2);
            }
         }
      } else {
         super.handleEntityEvent(b0);
      }

   }
}
