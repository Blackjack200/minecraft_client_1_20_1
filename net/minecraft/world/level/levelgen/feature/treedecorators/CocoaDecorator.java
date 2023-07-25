package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;

public class CocoaDecorator extends TreeDecorator {
   public static final Codec<CocoaDecorator> CODEC = Codec.floatRange(0.0F, 1.0F).fieldOf("probability").xmap(CocoaDecorator::new, (cocoadecorator) -> cocoadecorator.probability).codec();
   private final float probability;

   public CocoaDecorator(float f) {
      this.probability = f;
   }

   protected TreeDecoratorType<?> type() {
      return TreeDecoratorType.COCOA;
   }

   public void place(TreeDecorator.Context treedecorator_context) {
      RandomSource randomsource = treedecorator_context.random();
      if (!(randomsource.nextFloat() >= this.probability)) {
         List<BlockPos> list = treedecorator_context.logs();
         int i = list.get(0).getY();
         list.stream().filter((blockpos2) -> blockpos2.getY() - i <= 2).forEach((blockpos) -> {
            for(Direction direction : Direction.Plane.HORIZONTAL) {
               if (randomsource.nextFloat() <= 0.25F) {
                  Direction direction1 = direction.getOpposite();
                  BlockPos blockpos1 = blockpos.offset(direction1.getStepX(), 0, direction1.getStepZ());
                  if (treedecorator_context.isAir(blockpos1)) {
                     treedecorator_context.setBlock(blockpos1, Blocks.COCOA.defaultBlockState().setValue(CocoaBlock.AGE, Integer.valueOf(randomsource.nextInt(3))).setValue(CocoaBlock.FACING, direction));
                  }
               }
            }

         });
      }
   }
}
