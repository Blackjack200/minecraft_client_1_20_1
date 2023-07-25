package net.minecraft.world.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class BowlFoodItem extends Item {
   public BowlFoodItem(Item.Properties item_properties) {
      super(item_properties);
   }

   public ItemStack finishUsingItem(ItemStack itemstack, Level level, LivingEntity livingentity) {
      ItemStack itemstack1 = super.finishUsingItem(itemstack, level, livingentity);
      return livingentity instanceof Player && ((Player)livingentity).getAbilities().instabuild ? itemstack1 : new ItemStack(Items.BOWL);
   }
}
