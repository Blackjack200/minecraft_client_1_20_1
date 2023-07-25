package net.minecraft.core.dispenser;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

public class BoatDispenseItemBehavior extends DefaultDispenseItemBehavior {
   private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();
   private final Boat.Type type;
   private final boolean isChestBoat;

   public BoatDispenseItemBehavior(Boat.Type boat_type) {
      this(boat_type, false);
   }

   public BoatDispenseItemBehavior(Boat.Type boat_type, boolean flag) {
      this.type = boat_type;
      this.isChestBoat = flag;
   }

   public ItemStack execute(BlockSource blocksource, ItemStack itemstack) {
      Direction direction = blocksource.getBlockState().getValue(DispenserBlock.FACING);
      Level level = blocksource.getLevel();
      double d0 = 0.5625D + (double)EntityType.BOAT.getWidth() / 2.0D;
      double d1 = blocksource.x() + (double)direction.getStepX() * d0;
      double d2 = blocksource.y() + (double)((float)direction.getStepY() * 1.125F);
      double d3 = blocksource.z() + (double)direction.getStepZ() * d0;
      BlockPos blockpos = blocksource.getPos().relative(direction);
      double d4;
      if (level.getFluidState(blockpos).is(FluidTags.WATER)) {
         d4 = 1.0D;
      } else {
         if (!level.getBlockState(blockpos).isAir() || !level.getFluidState(blockpos.below()).is(FluidTags.WATER)) {
            return this.defaultDispenseItemBehavior.dispense(blocksource, itemstack);
         }

         d4 = 0.0D;
      }

      Boat boat = (Boat)(this.isChestBoat ? new ChestBoat(level, d1, d2 + d4, d3) : new Boat(level, d1, d2 + d4, d3));
      boat.setVariant(this.type);
      boat.setYRot(direction.toYRot());
      level.addFreshEntity(boat);
      itemstack.shrink(1);
      return itemstack;
   }

   protected void playSound(BlockSource blocksource) {
      blocksource.getLevel().levelEvent(1000, blocksource.getPos(), 0);
   }
}
