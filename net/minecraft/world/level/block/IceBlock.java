package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class IceBlock extends HalfTransparentBlock {
   public IceBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
   }

   public static BlockState meltsInto() {
      return Blocks.WATER.defaultBlockState();
   }

   public void playerDestroy(Level level, Player player, BlockPos blockpos, BlockState blockstate, @Nullable BlockEntity blockentity, ItemStack itemstack) {
      super.playerDestroy(level, player, blockpos, blockstate, blockentity, itemstack);
      if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, itemstack) == 0) {
         if (level.dimensionType().ultraWarm()) {
            level.removeBlock(blockpos, false);
            return;
         }

         BlockState blockstate1 = level.getBlockState(blockpos.below());
         if (blockstate1.blocksMotion() || blockstate1.liquid()) {
            level.setBlockAndUpdate(blockpos, meltsInto());
         }
      }

   }

   public void randomTick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      if (serverlevel.getBrightness(LightLayer.BLOCK, blockpos) > 11 - blockstate.getLightBlock(serverlevel, blockpos)) {
         this.melt(blockstate, serverlevel, blockpos);
      }

   }

   protected void melt(BlockState blockstate, Level level, BlockPos blockpos) {
      if (level.dimensionType().ultraWarm()) {
         level.removeBlock(blockpos, false);
      } else {
         level.setBlockAndUpdate(blockpos, meltsInto());
         level.neighborChanged(blockpos, meltsInto().getBlock(), blockpos);
      }
   }
}
