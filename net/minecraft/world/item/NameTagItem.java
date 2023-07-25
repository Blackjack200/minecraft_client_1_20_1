package net.minecraft.world.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

public class NameTagItem extends Item {
   public NameTagItem(Item.Properties item_properties) {
      super(item_properties);
   }

   public InteractionResult interactLivingEntity(ItemStack itemstack, Player player, LivingEntity livingentity, InteractionHand interactionhand) {
      if (itemstack.hasCustomHoverName() && !(livingentity instanceof Player)) {
         if (!player.level().isClientSide && livingentity.isAlive()) {
            livingentity.setCustomName(itemstack.getHoverName());
            if (livingentity instanceof Mob) {
               ((Mob)livingentity).setPersistenceRequired();
            }

            itemstack.shrink(1);
         }

         return InteractionResult.sidedSuccess(player.level().isClientSide);
      } else {
         return InteractionResult.PASS;
      }
   }
}
