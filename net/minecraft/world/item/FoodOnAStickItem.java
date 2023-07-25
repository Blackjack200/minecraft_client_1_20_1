package net.minecraft.world.item;

import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ItemSteerable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class FoodOnAStickItem<T extends Entity & ItemSteerable> extends Item {
   private final EntityType<T> canInteractWith;
   private final int consumeItemDamage;

   public FoodOnAStickItem(Item.Properties item_properties, EntityType<T> entitytype, int i) {
      super(item_properties);
      this.canInteractWith = entitytype;
      this.consumeItemDamage = i;
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionhand) {
      ItemStack itemstack = player.getItemInHand(interactionhand);
      if (level.isClientSide) {
         return InteractionResultHolder.pass(itemstack);
      } else {
         Entity entity = player.getControlledVehicle();
         if (player.isPassenger() && entity instanceof ItemSteerable) {
            ItemSteerable itemsteerable = (ItemSteerable)entity;
            if (entity.getType() == this.canInteractWith && itemsteerable.boost()) {
               itemstack.hurtAndBreak(this.consumeItemDamage, player, (player1) -> player1.broadcastBreakEvent(interactionhand));
               if (itemstack.isEmpty()) {
                  ItemStack itemstack1 = new ItemStack(Items.FISHING_ROD);
                  itemstack1.setTag(itemstack.getTag());
                  return InteractionResultHolder.success(itemstack1);
               }

               return InteractionResultHolder.success(itemstack);
            }
         }

         player.awardStat(Stats.ITEM_USED.get(this));
         return InteractionResultHolder.pass(itemstack);
      }
   }
}
