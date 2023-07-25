package net.minecraft.core.dispenser;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.block.DispenserBlock;
import org.slf4j.Logger;

public class ShulkerBoxDispenseBehavior extends OptionalDispenseItemBehavior {
   private static final Logger LOGGER = LogUtils.getLogger();

   protected ItemStack execute(BlockSource blocksource, ItemStack itemstack) {
      this.setSuccess(false);
      Item item = itemstack.getItem();
      if (item instanceof BlockItem) {
         Direction direction = blocksource.getBlockState().getValue(DispenserBlock.FACING);
         BlockPos blockpos = blocksource.getPos().relative(direction);
         Direction direction1 = blocksource.getLevel().isEmptyBlock(blockpos.below()) ? direction : Direction.UP;

         try {
            this.setSuccess(((BlockItem)item).place(new DirectionalPlaceContext(blocksource.getLevel(), blockpos, direction, itemstack, direction1)).consumesAction());
         } catch (Exception var8) {
            LOGGER.error("Error trying to place shulker box at {}", blockpos, var8);
         }
      }

      return itemstack;
   }
}
