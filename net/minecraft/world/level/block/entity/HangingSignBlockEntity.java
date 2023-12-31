package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class HangingSignBlockEntity extends SignBlockEntity {
   private static final int MAX_TEXT_LINE_WIDTH = 60;
   private static final int TEXT_LINE_HEIGHT = 9;

   public HangingSignBlockEntity(BlockPos blockpos, BlockState blockstate) {
      super(BlockEntityType.HANGING_SIGN, blockpos, blockstate);
   }

   public int getTextLineHeight() {
      return 9;
   }

   public int getMaxTextLineWidth() {
      return 60;
   }
}
