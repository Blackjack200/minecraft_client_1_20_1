package net.minecraft.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class Containers {
   public static void dropContents(Level level, BlockPos blockpos, Container container) {
      dropContents(level, (double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ(), container);
   }

   public static void dropContents(Level level, Entity entity, Container container) {
      dropContents(level, entity.getX(), entity.getY(), entity.getZ(), container);
   }

   private static void dropContents(Level level, double d0, double d1, double d2, Container container) {
      for(int i = 0; i < container.getContainerSize(); ++i) {
         dropItemStack(level, d0, d1, d2, container.getItem(i));
      }

   }

   public static void dropContents(Level level, BlockPos blockpos, NonNullList<ItemStack> nonnulllist) {
      nonnulllist.forEach((itemstack) -> dropItemStack(level, (double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ(), itemstack));
   }

   public static void dropItemStack(Level level, double d0, double d1, double d2, ItemStack itemstack) {
      double d3 = (double)EntityType.ITEM.getWidth();
      double d4 = 1.0D - d3;
      double d5 = d3 / 2.0D;
      double d6 = Math.floor(d0) + level.random.nextDouble() * d4 + d5;
      double d7 = Math.floor(d1) + level.random.nextDouble() * d4;
      double d8 = Math.floor(d2) + level.random.nextDouble() * d4 + d5;

      while(!itemstack.isEmpty()) {
         ItemEntity itementity = new ItemEntity(level, d6, d7, d8, itemstack.split(level.random.nextInt(21) + 10));
         float f = 0.05F;
         itementity.setDeltaMovement(level.random.triangle(0.0D, 0.11485000171139836D), level.random.triangle(0.2D, 0.11485000171139836D), level.random.triangle(0.0D, 0.11485000171139836D));
         level.addFreshEntity(itementity);
      }

   }
}
