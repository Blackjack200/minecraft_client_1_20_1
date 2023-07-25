package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class AlterGroundDecorator extends TreeDecorator {
   public static final Codec<AlterGroundDecorator> CODEC = BlockStateProvider.CODEC.fieldOf("provider").xmap(AlterGroundDecorator::new, (altergrounddecorator) -> altergrounddecorator.provider).codec();
   private final BlockStateProvider provider;

   public AlterGroundDecorator(BlockStateProvider blockstateprovider) {
      this.provider = blockstateprovider;
   }

   protected TreeDecoratorType<?> type() {
      return TreeDecoratorType.ALTER_GROUND;
   }

   public void place(TreeDecorator.Context treedecorator_context) {
      List<BlockPos> list = Lists.newArrayList();
      List<BlockPos> list1 = treedecorator_context.roots();
      List<BlockPos> list2 = treedecorator_context.logs();
      if (list1.isEmpty()) {
         list.addAll(list2);
      } else if (!list2.isEmpty() && list1.get(0).getY() == list2.get(0).getY()) {
         list.addAll(list2);
         list.addAll(list1);
      } else {
         list.addAll(list1);
      }

      if (!list.isEmpty()) {
         int i = list.get(0).getY();
         list.stream().filter((blockpos1) -> blockpos1.getY() == i).forEach((blockpos) -> {
            this.placeCircle(treedecorator_context, blockpos.west().north());
            this.placeCircle(treedecorator_context, blockpos.east(2).north());
            this.placeCircle(treedecorator_context, blockpos.west().south(2));
            this.placeCircle(treedecorator_context, blockpos.east(2).south(2));

            for(int j = 0; j < 5; ++j) {
               int k = treedecorator_context.random().nextInt(64);
               int l = k % 8;
               int i1 = k / 8;
               if (l == 0 || l == 7 || i1 == 0 || i1 == 7) {
                  this.placeCircle(treedecorator_context, blockpos.offset(-3 + l, 0, -3 + i1));
               }
            }

         });
      }
   }

   private void placeCircle(TreeDecorator.Context treedecorator_context, BlockPos blockpos) {
      for(int i = -2; i <= 2; ++i) {
         for(int j = -2; j <= 2; ++j) {
            if (Math.abs(i) != 2 || Math.abs(j) != 2) {
               this.placeBlockAt(treedecorator_context, blockpos.offset(i, 0, j));
            }
         }
      }

   }

   private void placeBlockAt(TreeDecorator.Context treedecorator_context, BlockPos blockpos) {
      for(int i = 2; i >= -3; --i) {
         BlockPos blockpos1 = blockpos.above(i);
         if (Feature.isGrassOrDirt(treedecorator_context.level(), blockpos1)) {
            treedecorator_context.setBlock(blockpos1, this.provider.getState(treedecorator_context.random(), blockpos));
            break;
         }

         if (!treedecorator_context.isAir(blockpos1) && i < 0) {
            break;
         }
      }

   }
}
