package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BaseEntityBlock extends Block implements EntityBlock {
   protected BaseEntityBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
   }

   public RenderShape getRenderShape(BlockState blockstate) {
      return RenderShape.INVISIBLE;
   }

   public boolean triggerEvent(BlockState blockstate, Level level, BlockPos blockpos, int i, int j) {
      super.triggerEvent(blockstate, level, blockpos, i, j);
      BlockEntity blockentity = level.getBlockEntity(blockpos);
      return blockentity == null ? false : blockentity.triggerEvent(i, j);
   }

   @Nullable
   public MenuProvider getMenuProvider(BlockState blockstate, Level level, BlockPos blockpos) {
      BlockEntity blockentity = level.getBlockEntity(blockpos);
      return blockentity instanceof MenuProvider ? (MenuProvider)blockentity : null;
   }

   @Nullable
   protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> blockentitytype, BlockEntityType<E> blockentitytype1, BlockEntityTicker<? super E> blockentityticker) {
      return blockentitytype1 == blockentitytype ? blockentityticker : null;
   }
}
