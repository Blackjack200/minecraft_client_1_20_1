package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class AttachedToLeavesDecorator extends TreeDecorator {
   public static final Codec<AttachedToLeavesDecorator> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Codec.floatRange(0.0F, 1.0F).fieldOf("probability").forGetter((attachedtoleavesdecorator5) -> attachedtoleavesdecorator5.probability), Codec.intRange(0, 16).fieldOf("exclusion_radius_xz").forGetter((attachedtoleavesdecorator4) -> attachedtoleavesdecorator4.exclusionRadiusXZ), Codec.intRange(0, 16).fieldOf("exclusion_radius_y").forGetter((attachedtoleavesdecorator3) -> attachedtoleavesdecorator3.exclusionRadiusY), BlockStateProvider.CODEC.fieldOf("block_provider").forGetter((attachedtoleavesdecorator2) -> attachedtoleavesdecorator2.blockProvider), Codec.intRange(1, 16).fieldOf("required_empty_blocks").forGetter((attachedtoleavesdecorator1) -> attachedtoleavesdecorator1.requiredEmptyBlocks), ExtraCodecs.nonEmptyList(Direction.CODEC.listOf()).fieldOf("directions").forGetter((attachedtoleavesdecorator) -> attachedtoleavesdecorator.directions)).apply(recordcodecbuilder_instance, AttachedToLeavesDecorator::new));
   protected final float probability;
   protected final int exclusionRadiusXZ;
   protected final int exclusionRadiusY;
   protected final BlockStateProvider blockProvider;
   protected final int requiredEmptyBlocks;
   protected final List<Direction> directions;

   public AttachedToLeavesDecorator(float f, int i, int j, BlockStateProvider blockstateprovider, int k, List<Direction> list) {
      this.probability = f;
      this.exclusionRadiusXZ = i;
      this.exclusionRadiusY = j;
      this.blockProvider = blockstateprovider;
      this.requiredEmptyBlocks = k;
      this.directions = list;
   }

   public void place(TreeDecorator.Context treedecorator_context) {
      Set<BlockPos> set = new HashSet<>();
      RandomSource randomsource = treedecorator_context.random();

      for(BlockPos blockpos : Util.shuffledCopy(treedecorator_context.leaves(), randomsource)) {
         Direction direction = Util.getRandom(this.directions, randomsource);
         BlockPos blockpos1 = blockpos.relative(direction);
         if (!set.contains(blockpos1) && randomsource.nextFloat() < this.probability && this.hasRequiredEmptyBlocks(treedecorator_context, blockpos, direction)) {
            BlockPos blockpos2 = blockpos1.offset(-this.exclusionRadiusXZ, -this.exclusionRadiusY, -this.exclusionRadiusXZ);
            BlockPos blockpos3 = blockpos1.offset(this.exclusionRadiusXZ, this.exclusionRadiusY, this.exclusionRadiusXZ);

            for(BlockPos blockpos4 : BlockPos.betweenClosed(blockpos2, blockpos3)) {
               set.add(blockpos4.immutable());
            }

            treedecorator_context.setBlock(blockpos1, this.blockProvider.getState(randomsource, blockpos1));
         }
      }

   }

   private boolean hasRequiredEmptyBlocks(TreeDecorator.Context treedecorator_context, BlockPos blockpos, Direction direction) {
      for(int i = 1; i <= this.requiredEmptyBlocks; ++i) {
         BlockPos blockpos1 = blockpos.relative(direction, i);
         if (!treedecorator_context.isAir(blockpos1)) {
            return false;
         }
      }

      return true;
   }

   protected TreeDecoratorType<?> type() {
      return TreeDecoratorType.ATTACHED_TO_LEAVES;
   }
}
