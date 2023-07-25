package net.minecraft.world.level.block;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;

public class PlayerWallHeadBlock extends WallSkullBlock {
   protected PlayerWallHeadBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(SkullBlock.Types.PLAYER, blockbehaviour_properties);
   }

   public void setPlacedBy(Level level, BlockPos blockpos, BlockState blockstate, @Nullable LivingEntity livingentity, ItemStack itemstack) {
      Blocks.PLAYER_HEAD.setPlacedBy(level, blockpos, blockstate, livingentity, itemstack);
   }

   public List<ItemStack> getDrops(BlockState blockstate, LootParams.Builder lootparams_builder) {
      return Blocks.PLAYER_HEAD.getDrops(blockstate, lootparams_builder);
   }
}
