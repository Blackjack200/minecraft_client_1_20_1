package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SignItem extends StandingAndWallBlockItem {
   public SignItem(Item.Properties item_properties, Block block, Block block1) {
      super(block, block1, item_properties, Direction.DOWN);
   }

   public SignItem(Item.Properties item_properties, Block block, Block block1, Direction direction) {
      super(block, block1, item_properties, direction);
   }

   protected boolean updateCustomBlockEntityTag(BlockPos blockpos, Level level, @Nullable Player player, ItemStack itemstack, BlockState blockstate) {
      boolean flag = super.updateCustomBlockEntityTag(blockpos, level, player, itemstack, blockstate);
      if (!level.isClientSide && !flag && player != null) {
         BlockEntity var9 = level.getBlockEntity(blockpos);
         if (var9 instanceof SignBlockEntity) {
            SignBlockEntity signblockentity = (SignBlockEntity)var9;
            Block var10 = level.getBlockState(blockpos).getBlock();
            if (var10 instanceof SignBlock) {
               SignBlock signblock = (SignBlock)var10;
               signblock.openTextEdit(player, signblockentity, true);
            }
         }
      }

      return flag;
   }
}
