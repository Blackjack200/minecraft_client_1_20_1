package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class LeaveVineDecorator extends TreeDecorator {
   public static final Codec<LeaveVineDecorator> CODEC = Codec.floatRange(0.0F, 1.0F).fieldOf("probability").xmap(LeaveVineDecorator::new, (leavevinedecorator) -> leavevinedecorator.probability).codec();
   private final float probability;

   protected TreeDecoratorType<?> type() {
      return TreeDecoratorType.LEAVE_VINE;
   }

   public LeaveVineDecorator(float f) {
      this.probability = f;
   }

   public void place(TreeDecorator.Context treedecorator_context) {
      RandomSource randomsource = treedecorator_context.random();
      treedecorator_context.leaves().forEach((blockpos) -> {
         if (randomsource.nextFloat() < this.probability) {
            BlockPos blockpos1 = blockpos.west();
            if (treedecorator_context.isAir(blockpos1)) {
               addHangingVine(blockpos1, VineBlock.EAST, treedecorator_context);
            }
         }

         if (randomsource.nextFloat() < this.probability) {
            BlockPos blockpos2 = blockpos.east();
            if (treedecorator_context.isAir(blockpos2)) {
               addHangingVine(blockpos2, VineBlock.WEST, treedecorator_context);
            }
         }

         if (randomsource.nextFloat() < this.probability) {
            BlockPos blockpos3 = blockpos.north();
            if (treedecorator_context.isAir(blockpos3)) {
               addHangingVine(blockpos3, VineBlock.SOUTH, treedecorator_context);
            }
         }

         if (randomsource.nextFloat() < this.probability) {
            BlockPos blockpos4 = blockpos.south();
            if (treedecorator_context.isAir(blockpos4)) {
               addHangingVine(blockpos4, VineBlock.NORTH, treedecorator_context);
            }
         }

      });
   }

   private static void addHangingVine(BlockPos blockpos, BooleanProperty booleanproperty, TreeDecorator.Context treedecorator_context) {
      treedecorator_context.placeVine(blockpos, booleanproperty);
      int i = 4;

      for(BlockPos var4 = blockpos.below(); treedecorator_context.isAir(var4) && i > 0; --i) {
         treedecorator_context.placeVine(var4, booleanproperty);
         var4 = var4.below();
      }

   }
}
