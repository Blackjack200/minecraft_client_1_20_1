package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.VineBlock;

public class TrunkVineDecorator extends TreeDecorator {
   public static final Codec<TrunkVineDecorator> CODEC = Codec.unit(() -> TrunkVineDecorator.INSTANCE);
   public static final TrunkVineDecorator INSTANCE = new TrunkVineDecorator();

   protected TreeDecoratorType<?> type() {
      return TreeDecoratorType.TRUNK_VINE;
   }

   public void place(TreeDecorator.Context treedecorator_context) {
      RandomSource randomsource = treedecorator_context.random();
      treedecorator_context.logs().forEach((blockpos) -> {
         if (randomsource.nextInt(3) > 0) {
            BlockPos blockpos1 = blockpos.west();
            if (treedecorator_context.isAir(blockpos1)) {
               treedecorator_context.placeVine(blockpos1, VineBlock.EAST);
            }
         }

         if (randomsource.nextInt(3) > 0) {
            BlockPos blockpos2 = blockpos.east();
            if (treedecorator_context.isAir(blockpos2)) {
               treedecorator_context.placeVine(blockpos2, VineBlock.WEST);
            }
         }

         if (randomsource.nextInt(3) > 0) {
            BlockPos blockpos3 = blockpos.north();
            if (treedecorator_context.isAir(blockpos3)) {
               treedecorator_context.placeVine(blockpos3, VineBlock.SOUTH);
            }
         }

         if (randomsource.nextInt(3) > 0) {
            BlockPos blockpos4 = blockpos.south();
            if (treedecorator_context.isAir(blockpos4)) {
               treedecorator_context.placeVine(blockpos4, VineBlock.NORTH);
            }
         }

      });
   }
}
