package net.minecraft.world.item;

import java.util.EnumMap;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.crafting.Ingredient;

public enum ArmorMaterials implements StringRepresentable, ArmorMaterial {
   LEATHER("leather", 5, Util.make(new EnumMap<>(ArmorItem.Type.class), (enummap) -> {
      enummap.put(ArmorItem.Type.BOOTS, 1);
      enummap.put(ArmorItem.Type.LEGGINGS, 2);
      enummap.put(ArmorItem.Type.CHESTPLATE, 3);
      enummap.put(ArmorItem.Type.HELMET, 1);
   }), 15, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, () -> Ingredient.of(Items.LEATHER)),
   CHAIN("chainmail", 15, Util.make(new EnumMap<>(ArmorItem.Type.class), (enummap) -> {
      enummap.put(ArmorItem.Type.BOOTS, 1);
      enummap.put(ArmorItem.Type.LEGGINGS, 4);
      enummap.put(ArmorItem.Type.CHESTPLATE, 5);
      enummap.put(ArmorItem.Type.HELMET, 2);
   }), 12, SoundEvents.ARMOR_EQUIP_CHAIN, 0.0F, 0.0F, () -> Ingredient.of(Items.IRON_INGOT)),
   IRON("iron", 15, Util.make(new EnumMap<>(ArmorItem.Type.class), (enummap) -> {
      enummap.put(ArmorItem.Type.BOOTS, 2);
      enummap.put(ArmorItem.Type.LEGGINGS, 5);
      enummap.put(ArmorItem.Type.CHESTPLATE, 6);
      enummap.put(ArmorItem.Type.HELMET, 2);
   }), 9, SoundEvents.ARMOR_EQUIP_IRON, 0.0F, 0.0F, () -> Ingredient.of(Items.IRON_INGOT)),
   GOLD("gold", 7, Util.make(new EnumMap<>(ArmorItem.Type.class), (enummap) -> {
      enummap.put(ArmorItem.Type.BOOTS, 1);
      enummap.put(ArmorItem.Type.LEGGINGS, 3);
      enummap.put(ArmorItem.Type.CHESTPLATE, 5);
      enummap.put(ArmorItem.Type.HELMET, 2);
   }), 25, SoundEvents.ARMOR_EQUIP_GOLD, 0.0F, 0.0F, () -> Ingredient.of(Items.GOLD_INGOT)),
   DIAMOND("diamond", 33, Util.make(new EnumMap<>(ArmorItem.Type.class), (enummap) -> {
      enummap.put(ArmorItem.Type.BOOTS, 3);
      enummap.put(ArmorItem.Type.LEGGINGS, 6);
      enummap.put(ArmorItem.Type.CHESTPLATE, 8);
      enummap.put(ArmorItem.Type.HELMET, 3);
   }), 10, SoundEvents.ARMOR_EQUIP_DIAMOND, 2.0F, 0.0F, () -> Ingredient.of(Items.DIAMOND)),
   TURTLE("turtle", 25, Util.make(new EnumMap<>(ArmorItem.Type.class), (enummap) -> {
      enummap.put(ArmorItem.Type.BOOTS, 2);
      enummap.put(ArmorItem.Type.LEGGINGS, 5);
      enummap.put(ArmorItem.Type.CHESTPLATE, 6);
      enummap.put(ArmorItem.Type.HELMET, 2);
   }), 9, SoundEvents.ARMOR_EQUIP_TURTLE, 0.0F, 0.0F, () -> Ingredient.of(Items.SCUTE)),
   NETHERITE("netherite", 37, Util.make(new EnumMap<>(ArmorItem.Type.class), (enummap) -> {
      enummap.put(ArmorItem.Type.BOOTS, 3);
      enummap.put(ArmorItem.Type.LEGGINGS, 6);
      enummap.put(ArmorItem.Type.CHESTPLATE, 8);
      enummap.put(ArmorItem.Type.HELMET, 3);
   }), 15, SoundEvents.ARMOR_EQUIP_NETHERITE, 3.0F, 0.1F, () -> Ingredient.of(Items.NETHERITE_INGOT));

   public static final StringRepresentable.EnumCodec<ArmorMaterials> CODEC = StringRepresentable.fromEnum(ArmorMaterials::values);
   private static final EnumMap<ArmorItem.Type, Integer> HEALTH_FUNCTION_FOR_TYPE = Util.make(new EnumMap<>(ArmorItem.Type.class), (enummap) -> {
      enummap.put(ArmorItem.Type.BOOTS, 13);
      enummap.put(ArmorItem.Type.LEGGINGS, 15);
      enummap.put(ArmorItem.Type.CHESTPLATE, 16);
      enummap.put(ArmorItem.Type.HELMET, 11);
   });
   private final String name;
   private final int durabilityMultiplier;
   private final EnumMap<ArmorItem.Type, Integer> protectionFunctionForType;
   private final int enchantmentValue;
   private final SoundEvent sound;
   private final float toughness;
   private final float knockbackResistance;
   private final LazyLoadedValue<Ingredient> repairIngredient;

   private ArmorMaterials(String s, int i, EnumMap<ArmorItem.Type, Integer> enummap, int j, SoundEvent soundevent, float f, float f1, Supplier<Ingredient> supplier) {
      this.name = s;
      this.durabilityMultiplier = i;
      this.protectionFunctionForType = enummap;
      this.enchantmentValue = j;
      this.sound = soundevent;
      this.toughness = f;
      this.knockbackResistance = f1;
      this.repairIngredient = new LazyLoadedValue<>(supplier);
   }

   public int getDurabilityForType(ArmorItem.Type armoritem_type) {
      return HEALTH_FUNCTION_FOR_TYPE.get(armoritem_type) * this.durabilityMultiplier;
   }

   public int getDefenseForType(ArmorItem.Type armoritem_type) {
      return this.protectionFunctionForType.get(armoritem_type);
   }

   public int getEnchantmentValue() {
      return this.enchantmentValue;
   }

   public SoundEvent getEquipSound() {
      return this.sound;
   }

   public Ingredient getRepairIngredient() {
      return this.repairIngredient.get();
   }

   public String getName() {
      return this.name;
   }

   public float getToughness() {
      return this.toughness;
   }

   public float getKnockbackResistance() {
      return this.knockbackResistance;
   }

   public String getSerializedName() {
      return this.name;
   }
}
