package net.minecraft.world.item.enchantment;

import com.google.common.collect.Maps;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.item.ItemStack;

public abstract class Enchantment {
   private final EquipmentSlot[] slots;
   private final Enchantment.Rarity rarity;
   public final EnchantmentCategory category;
   @Nullable
   protected String descriptionId;

   @Nullable
   public static Enchantment byId(int i) {
      return BuiltInRegistries.ENCHANTMENT.byId(i);
   }

   protected Enchantment(Enchantment.Rarity enchantment_rarity, EnchantmentCategory enchantmentcategory, EquipmentSlot[] aequipmentslot) {
      this.rarity = enchantment_rarity;
      this.category = enchantmentcategory;
      this.slots = aequipmentslot;
   }

   public Map<EquipmentSlot, ItemStack> getSlotItems(LivingEntity livingentity) {
      Map<EquipmentSlot, ItemStack> map = Maps.newEnumMap(EquipmentSlot.class);

      for(EquipmentSlot equipmentslot : this.slots) {
         ItemStack itemstack = livingentity.getItemBySlot(equipmentslot);
         if (!itemstack.isEmpty()) {
            map.put(equipmentslot, itemstack);
         }
      }

      return map;
   }

   public Enchantment.Rarity getRarity() {
      return this.rarity;
   }

   public int getMinLevel() {
      return 1;
   }

   public int getMaxLevel() {
      return 1;
   }

   public int getMinCost(int i) {
      return 1 + i * 10;
   }

   public int getMaxCost(int i) {
      return this.getMinCost(i) + 5;
   }

   public int getDamageProtection(int i, DamageSource damagesource) {
      return 0;
   }

   public float getDamageBonus(int i, MobType mobtype) {
      return 0.0F;
   }

   public final boolean isCompatibleWith(Enchantment enchantment) {
      return this.checkCompatibility(enchantment) && enchantment.checkCompatibility(this);
   }

   protected boolean checkCompatibility(Enchantment enchantment) {
      return this != enchantment;
   }

   protected String getOrCreateDescriptionId() {
      if (this.descriptionId == null) {
         this.descriptionId = Util.makeDescriptionId("enchantment", BuiltInRegistries.ENCHANTMENT.getKey(this));
      }

      return this.descriptionId;
   }

   public String getDescriptionId() {
      return this.getOrCreateDescriptionId();
   }

   public Component getFullname(int i) {
      MutableComponent mutablecomponent = Component.translatable(this.getDescriptionId());
      if (this.isCurse()) {
         mutablecomponent.withStyle(ChatFormatting.RED);
      } else {
         mutablecomponent.withStyle(ChatFormatting.GRAY);
      }

      if (i != 1 || this.getMaxLevel() != 1) {
         mutablecomponent.append(CommonComponents.SPACE).append(Component.translatable("enchantment.level." + i));
      }

      return mutablecomponent;
   }

   public boolean canEnchant(ItemStack itemstack) {
      return this.category.canEnchant(itemstack.getItem());
   }

   public void doPostAttack(LivingEntity livingentity, Entity entity, int i) {
   }

   public void doPostHurt(LivingEntity livingentity, Entity entity, int i) {
   }

   public boolean isTreasureOnly() {
      return false;
   }

   public boolean isCurse() {
      return false;
   }

   public boolean isTradeable() {
      return true;
   }

   public boolean isDiscoverable() {
      return true;
   }

   public static enum Rarity {
      COMMON(10),
      UNCOMMON(5),
      RARE(2),
      VERY_RARE(1);

      private final int weight;

      private Rarity(int i) {
         this.weight = i;
      }

      public int getWeight() {
         return this.weight;
      }
   }
}
