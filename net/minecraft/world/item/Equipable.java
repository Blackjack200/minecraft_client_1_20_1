package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public interface Equipable extends Vanishable {
   EquipmentSlot getEquipmentSlot();

   default SoundEvent getEquipSound() {
      return SoundEvents.ARMOR_EQUIP_GENERIC;
   }

   default InteractionResultHolder<ItemStack> swapWithEquipmentSlot(Item item, Level level, Player player, InteractionHand interactionhand) {
      ItemStack itemstack = player.getItemInHand(interactionhand);
      EquipmentSlot equipmentslot = Mob.getEquipmentSlotForItem(itemstack);
      ItemStack itemstack1 = player.getItemBySlot(equipmentslot);
      if (!EnchantmentHelper.hasBindingCurse(itemstack1) && !ItemStack.matches(itemstack, itemstack1)) {
         if (!level.isClientSide()) {
            player.awardStat(Stats.ITEM_USED.get(item));
         }

         ItemStack itemstack2 = itemstack1.isEmpty() ? itemstack : itemstack1.copyAndClear();
         ItemStack itemstack3 = itemstack.copyAndClear();
         player.setItemSlot(equipmentslot, itemstack3);
         return InteractionResultHolder.sidedSuccess(itemstack2, level.isClientSide());
      } else {
         return InteractionResultHolder.fail(itemstack);
      }
   }

   @Nullable
   static Equipable get(ItemStack itemstack) {
      Item equipable1 = itemstack.getItem();
      if (equipable1 instanceof Equipable equipable) {
         return equipable;
      } else {
         Item var3 = itemstack.getItem();
         if (var3 instanceof BlockItem blockitem) {
            Block var6 = blockitem.getBlock();
            if (var6 instanceof Equipable equipable1) {
               return equipable1;
            }
         }

         return null;
      }
   }
}
