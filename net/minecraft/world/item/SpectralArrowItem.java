package net.minecraft.world.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.level.Level;

public class SpectralArrowItem extends ArrowItem {
   public SpectralArrowItem(Item.Properties item_properties) {
      super(item_properties);
   }

   public AbstractArrow createArrow(Level level, ItemStack itemstack, LivingEntity livingentity) {
      return new SpectralArrow(level, livingentity);
   }
}
