package net.minecraft.world.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;

public class ArrowItem extends Item {
   public ArrowItem(Item.Properties item_properties) {
      super(item_properties);
   }

   public AbstractArrow createArrow(Level level, ItemStack itemstack, LivingEntity livingentity) {
      Arrow arrow = new Arrow(level, livingentity);
      arrow.setEffectsFromItem(itemstack);
      return arrow;
   }
}
