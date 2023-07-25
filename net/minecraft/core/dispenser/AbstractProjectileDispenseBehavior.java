package net.minecraft.core.dispenser;

import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

public abstract class AbstractProjectileDispenseBehavior extends DefaultDispenseItemBehavior {
   public ItemStack execute(BlockSource blocksource, ItemStack itemstack) {
      Level level = blocksource.getLevel();
      Position position = DispenserBlock.getDispensePosition(blocksource);
      Direction direction = blocksource.getBlockState().getValue(DispenserBlock.FACING);
      Projectile projectile = this.getProjectile(level, position, itemstack);
      projectile.shoot((double)direction.getStepX(), (double)((float)direction.getStepY() + 0.1F), (double)direction.getStepZ(), this.getPower(), this.getUncertainty());
      level.addFreshEntity(projectile);
      itemstack.shrink(1);
      return itemstack;
   }

   protected void playSound(BlockSource blocksource) {
      blocksource.getLevel().levelEvent(1002, blocksource.getPos(), 0);
   }

   protected abstract Projectile getProjectile(Level level, Position position, ItemStack itemstack);

   protected float getUncertainty() {
      return 6.0F;
   }

   protected float getPower() {
      return 1.1F;
   }
}
