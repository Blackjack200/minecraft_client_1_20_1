package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSourceImpl;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.DropperBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class DropperBlock extends DispenserBlock {
   private static final DispenseItemBehavior DISPENSE_BEHAVIOUR = new DefaultDispenseItemBehavior();

   public DropperBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
   }

   protected DispenseItemBehavior getDispenseMethod(ItemStack itemstack) {
      return DISPENSE_BEHAVIOUR;
   }

   public BlockEntity newBlockEntity(BlockPos blockpos, BlockState blockstate) {
      return new DropperBlockEntity(blockpos, blockstate);
   }

   protected void dispenseFrom(ServerLevel serverlevel, BlockPos blockpos) {
      BlockSourceImpl blocksourceimpl = new BlockSourceImpl(serverlevel, blockpos);
      DispenserBlockEntity dispenserblockentity = blocksourceimpl.getEntity();
      int i = dispenserblockentity.getRandomSlot(serverlevel.random);
      if (i < 0) {
         serverlevel.levelEvent(1001, blockpos, 0);
      } else {
         ItemStack itemstack = dispenserblockentity.getItem(i);
         if (!itemstack.isEmpty()) {
            Direction direction = serverlevel.getBlockState(blockpos).getValue(FACING);
            Container container = HopperBlockEntity.getContainerAt(serverlevel, blockpos.relative(direction));
            ItemStack itemstack1;
            if (container == null) {
               itemstack1 = DISPENSE_BEHAVIOUR.dispense(blocksourceimpl, itemstack);
            } else {
               itemstack1 = HopperBlockEntity.addItem(dispenserblockentity, container, itemstack.copy().split(1), direction.getOpposite());
               if (itemstack1.isEmpty()) {
                  itemstack1 = itemstack.copy();
                  itemstack1.shrink(1);
               } else {
                  itemstack1 = itemstack.copy();
               }
            }

            dispenserblockentity.setItem(i, itemstack1);
         }
      }
   }
}
