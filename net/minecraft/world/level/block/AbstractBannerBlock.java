package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractBannerBlock extends BaseEntityBlock {
   private final DyeColor color;

   protected AbstractBannerBlock(DyeColor dyecolor, BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.color = dyecolor;
   }

   public boolean isPossibleToRespawnInThis(BlockState blockstate) {
      return true;
   }

   public BlockEntity newBlockEntity(BlockPos blockpos, BlockState blockstate) {
      return new BannerBlockEntity(blockpos, blockstate, this.color);
   }

   public void setPlacedBy(Level level, BlockPos blockpos, BlockState blockstate, @Nullable LivingEntity livingentity, ItemStack itemstack) {
      if (level.isClientSide) {
         level.getBlockEntity(blockpos, BlockEntityType.BANNER).ifPresent((bannerblockentity1) -> bannerblockentity1.fromItem(itemstack));
      } else if (itemstack.hasCustomHoverName()) {
         level.getBlockEntity(blockpos, BlockEntityType.BANNER).ifPresent((bannerblockentity) -> bannerblockentity.setCustomName(itemstack.getHoverName()));
      }

   }

   public ItemStack getCloneItemStack(BlockGetter blockgetter, BlockPos blockpos, BlockState blockstate) {
      BlockEntity blockentity = blockgetter.getBlockEntity(blockpos);
      return blockentity instanceof BannerBlockEntity ? ((BannerBlockEntity)blockentity).getItem() : super.getCloneItemStack(blockgetter, blockpos, blockstate);
   }

   public DyeColor getColor() {
      return this.color;
   }
}
