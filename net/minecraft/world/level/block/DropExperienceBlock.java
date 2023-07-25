package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class DropExperienceBlock extends Block {
   private final IntProvider xpRange;

   public DropExperienceBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      this(blockbehaviour_properties, ConstantInt.of(0));
   }

   public DropExperienceBlock(BlockBehaviour.Properties blockbehaviour_properties, IntProvider intprovider) {
      super(blockbehaviour_properties);
      this.xpRange = intprovider;
   }

   public void spawnAfterBreak(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, ItemStack itemstack, boolean flag) {
      super.spawnAfterBreak(blockstate, serverlevel, blockpos, itemstack, flag);
      if (flag) {
         this.tryDropExperience(serverlevel, blockpos, itemstack, this.xpRange);
      }

   }
}
