package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;

public class ItemFrameItem extends HangingEntityItem {
   public ItemFrameItem(EntityType<? extends HangingEntity> entitytype, Item.Properties item_properties) {
      super(entitytype, item_properties);
   }

   protected boolean mayPlace(Player player, Direction direction, ItemStack itemstack, BlockPos blockpos) {
      return !player.level().isOutsideBuildHeight(blockpos) && player.mayUseItemAt(blockpos, direction, itemstack);
   }
}
