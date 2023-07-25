package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.Vanishable;
import net.minecraft.world.level.block.Block;

public enum EnchantmentCategory {
   ARMOR {
      public boolean canEnchant(Item item) {
         return item instanceof ArmorItem;
      }
   },
   ARMOR_FEET {
      public boolean canEnchant(Item item) {
         if (item instanceof ArmorItem armoritem) {
            if (armoritem.getEquipmentSlot() == EquipmentSlot.FEET) {
               return true;
            }
         }

         return false;
      }
   },
   ARMOR_LEGS {
      public boolean canEnchant(Item item) {
         if (item instanceof ArmorItem armoritem) {
            if (armoritem.getEquipmentSlot() == EquipmentSlot.LEGS) {
               return true;
            }
         }

         return false;
      }
   },
   ARMOR_CHEST {
      public boolean canEnchant(Item item) {
         if (item instanceof ArmorItem armoritem) {
            if (armoritem.getEquipmentSlot() == EquipmentSlot.CHEST) {
               return true;
            }
         }

         return false;
      }
   },
   ARMOR_HEAD {
      public boolean canEnchant(Item item) {
         if (item instanceof ArmorItem armoritem) {
            if (armoritem.getEquipmentSlot() == EquipmentSlot.HEAD) {
               return true;
            }
         }

         return false;
      }
   },
   WEAPON {
      public boolean canEnchant(Item item) {
         return item instanceof SwordItem;
      }
   },
   DIGGER {
      public boolean canEnchant(Item item) {
         return item instanceof DiggerItem;
      }
   },
   FISHING_ROD {
      public boolean canEnchant(Item item) {
         return item instanceof FishingRodItem;
      }
   },
   TRIDENT {
      public boolean canEnchant(Item item) {
         return item instanceof TridentItem;
      }
   },
   BREAKABLE {
      public boolean canEnchant(Item item) {
         return item.canBeDepleted();
      }
   },
   BOW {
      public boolean canEnchant(Item item) {
         return item instanceof BowItem;
      }
   },
   WEARABLE {
      public boolean canEnchant(Item item) {
         return item instanceof Equipable || Block.byItem(item) instanceof Equipable;
      }
   },
   CROSSBOW {
      public boolean canEnchant(Item item) {
         return item instanceof CrossbowItem;
      }
   },
   VANISHABLE {
      public boolean canEnchant(Item item) {
         return item instanceof Vanishable || Block.byItem(item) instanceof Vanishable || j.canEnchant(item);
      }
   };

   public abstract boolean canEnchant(Item item);
}
