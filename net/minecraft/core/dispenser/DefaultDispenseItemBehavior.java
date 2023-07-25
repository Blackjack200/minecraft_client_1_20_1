package net.minecraft.core.dispenser;

import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

public class DefaultDispenseItemBehavior implements DispenseItemBehavior {
   public final ItemStack dispense(BlockSource blocksource, ItemStack itemstack) {
      ItemStack itemstack1 = this.execute(blocksource, itemstack);
      this.playSound(blocksource);
      this.playAnimation(blocksource, blocksource.getBlockState().getValue(DispenserBlock.FACING));
      return itemstack1;
   }

   protected ItemStack execute(BlockSource blocksource, ItemStack itemstack) {
      Direction direction = blocksource.getBlockState().getValue(DispenserBlock.FACING);
      Position position = DispenserBlock.getDispensePosition(blocksource);
      ItemStack itemstack1 = itemstack.split(1);
      spawnItem(blocksource.getLevel(), itemstack1, 6, direction, position);
      return itemstack;
   }

   public static void spawnItem(Level level, ItemStack itemstack, int i, Direction direction, Position position) {
      double d0 = position.x();
      double d1 = position.y();
      double d2 = position.z();
      if (direction.getAxis() == Direction.Axis.Y) {
         d1 -= 0.125D;
      } else {
         d1 -= 0.15625D;
      }

      ItemEntity itementity = new ItemEntity(level, d0, d1, d2, itemstack);
      double d3 = level.random.nextDouble() * 0.1D + 0.2D;
      itementity.setDeltaMovement(level.random.triangle((double)direction.getStepX() * d3, 0.0172275D * (double)i), level.random.triangle(0.2D, 0.0172275D * (double)i), level.random.triangle((double)direction.getStepZ() * d3, 0.0172275D * (double)i));
      level.addFreshEntity(itementity);
   }

   protected void playSound(BlockSource blocksource) {
      blocksource.getLevel().levelEvent(1000, blocksource.getPos(), 0);
   }

   protected void playAnimation(BlockSource blocksource, Direction direction) {
      blocksource.getLevel().levelEvent(2000, blocksource.getPos(), direction.get3DDataValue());
   }
}
