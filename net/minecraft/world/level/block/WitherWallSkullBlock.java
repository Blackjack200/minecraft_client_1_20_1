package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class WitherWallSkullBlock extends WallSkullBlock {
   protected WitherWallSkullBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(SkullBlock.Types.WITHER_SKELETON, blockbehaviour_properties);
   }

   public void setPlacedBy(Level level, BlockPos blockpos, BlockState blockstate, @Nullable LivingEntity livingentity, ItemStack itemstack) {
      Blocks.WITHER_SKELETON_SKULL.setPlacedBy(level, blockpos, blockstate, livingentity, itemstack);
   }
}
