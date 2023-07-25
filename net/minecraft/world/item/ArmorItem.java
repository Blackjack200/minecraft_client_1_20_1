package net.minecraft.world.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.EnumMap;
import java.util.List;
import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.AABB;

public class ArmorItem extends Item implements Equipable {
   private static final EnumMap<ArmorItem.Type, UUID> ARMOR_MODIFIER_UUID_PER_TYPE = Util.make(new EnumMap<>(ArmorItem.Type.class), (enummap) -> {
      enummap.put(ArmorItem.Type.BOOTS, UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"));
      enummap.put(ArmorItem.Type.LEGGINGS, UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D"));
      enummap.put(ArmorItem.Type.CHESTPLATE, UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E"));
      enummap.put(ArmorItem.Type.HELMET, UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150"));
   });
   public static final DispenseItemBehavior DISPENSE_ITEM_BEHAVIOR = new DefaultDispenseItemBehavior() {
      protected ItemStack execute(BlockSource blocksource, ItemStack itemstack) {
         return ArmorItem.dispenseArmor(blocksource, itemstack) ? itemstack : super.execute(blocksource, itemstack);
      }
   };
   protected final ArmorItem.Type type;
   private final int defense;
   private final float toughness;
   protected final float knockbackResistance;
   protected final ArmorMaterial material;
   private final Multimap<Attribute, AttributeModifier> defaultModifiers;

   public static boolean dispenseArmor(BlockSource blocksource, ItemStack itemstack) {
      BlockPos blockpos = blocksource.getPos().relative(blocksource.getBlockState().getValue(DispenserBlock.FACING));
      List<LivingEntity> list = blocksource.getLevel().getEntitiesOfClass(LivingEntity.class, new AABB(blockpos), EntitySelector.NO_SPECTATORS.and(new EntitySelector.MobCanWearArmorEntitySelector(itemstack)));
      if (list.isEmpty()) {
         return false;
      } else {
         LivingEntity livingentity = list.get(0);
         EquipmentSlot equipmentslot = Mob.getEquipmentSlotForItem(itemstack);
         ItemStack itemstack1 = itemstack.split(1);
         livingentity.setItemSlot(equipmentslot, itemstack1);
         if (livingentity instanceof Mob) {
            ((Mob)livingentity).setDropChance(equipmentslot, 2.0F);
            ((Mob)livingentity).setPersistenceRequired();
         }

         return true;
      }
   }

   public ArmorItem(ArmorMaterial armormaterial, ArmorItem.Type armoritem_type, Item.Properties item_properties) {
      super(item_properties.defaultDurability(armormaterial.getDurabilityForType(armoritem_type)));
      this.material = armormaterial;
      this.type = armoritem_type;
      this.defense = armormaterial.getDefenseForType(armoritem_type);
      this.toughness = armormaterial.getToughness();
      this.knockbackResistance = armormaterial.getKnockbackResistance();
      DispenserBlock.registerBehavior(this, DISPENSE_ITEM_BEHAVIOR);
      ImmutableMultimap.Builder<Attribute, AttributeModifier> immutablemultimap_builder = ImmutableMultimap.builder();
      UUID uuid = ARMOR_MODIFIER_UUID_PER_TYPE.get(armoritem_type);
      immutablemultimap_builder.put(Attributes.ARMOR, new AttributeModifier(uuid, "Armor modifier", (double)this.defense, AttributeModifier.Operation.ADDITION));
      immutablemultimap_builder.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(uuid, "Armor toughness", (double)this.toughness, AttributeModifier.Operation.ADDITION));
      if (armormaterial == ArmorMaterials.NETHERITE) {
         immutablemultimap_builder.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(uuid, "Armor knockback resistance", (double)this.knockbackResistance, AttributeModifier.Operation.ADDITION));
      }

      this.defaultModifiers = immutablemultimap_builder.build();
   }

   public ArmorItem.Type getType() {
      return this.type;
   }

   public int getEnchantmentValue() {
      return this.material.getEnchantmentValue();
   }

   public ArmorMaterial getMaterial() {
      return this.material;
   }

   public boolean isValidRepairItem(ItemStack itemstack, ItemStack itemstack1) {
      return this.material.getRepairIngredient().test(itemstack1) || super.isValidRepairItem(itemstack, itemstack1);
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionhand) {
      return this.swapWithEquipmentSlot(this, level, player, interactionhand);
   }

   public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentslot) {
      return equipmentslot == this.type.getSlot() ? this.defaultModifiers : super.getDefaultAttributeModifiers(equipmentslot);
   }

   public int getDefense() {
      return this.defense;
   }

   public float getToughness() {
      return this.toughness;
   }

   public EquipmentSlot getEquipmentSlot() {
      return this.type.getSlot();
   }

   public SoundEvent getEquipSound() {
      return this.getMaterial().getEquipSound();
   }

   public static enum Type {
      HELMET(EquipmentSlot.HEAD, "helmet"),
      CHESTPLATE(EquipmentSlot.CHEST, "chestplate"),
      LEGGINGS(EquipmentSlot.LEGS, "leggings"),
      BOOTS(EquipmentSlot.FEET, "boots");

      private final EquipmentSlot slot;
      private final String name;

      private Type(EquipmentSlot equipmentslot, String s) {
         this.slot = equipmentslot;
         this.name = s;
      }

      public EquipmentSlot getSlot() {
         return this.slot;
      }

      public String getName() {
         return this.name;
      }
   }
}
