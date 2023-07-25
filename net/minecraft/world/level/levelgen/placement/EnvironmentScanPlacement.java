package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;

public class EnvironmentScanPlacement extends PlacementModifier {
   private final Direction directionOfSearch;
   private final BlockPredicate targetCondition;
   private final BlockPredicate allowedSearchCondition;
   private final int maxSteps;
   public static final Codec<EnvironmentScanPlacement> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Direction.VERTICAL_CODEC.fieldOf("direction_of_search").forGetter((environmentscanplacement3) -> environmentscanplacement3.directionOfSearch), BlockPredicate.CODEC.fieldOf("target_condition").forGetter((environmentscanplacement2) -> environmentscanplacement2.targetCondition), BlockPredicate.CODEC.optionalFieldOf("allowed_search_condition", BlockPredicate.alwaysTrue()).forGetter((environmentscanplacement1) -> environmentscanplacement1.allowedSearchCondition), Codec.intRange(1, 32).fieldOf("max_steps").forGetter((environmentscanplacement) -> environmentscanplacement.maxSteps)).apply(recordcodecbuilder_instance, EnvironmentScanPlacement::new));

   private EnvironmentScanPlacement(Direction direction, BlockPredicate blockpredicate, BlockPredicate blockpredicate1, int i) {
      this.directionOfSearch = direction;
      this.targetCondition = blockpredicate;
      this.allowedSearchCondition = blockpredicate1;
      this.maxSteps = i;
   }

   public static EnvironmentScanPlacement scanningFor(Direction direction, BlockPredicate blockpredicate, BlockPredicate blockpredicate1, int i) {
      return new EnvironmentScanPlacement(direction, blockpredicate, blockpredicate1, i);
   }

   public static EnvironmentScanPlacement scanningFor(Direction direction, BlockPredicate blockpredicate, int i) {
      return scanningFor(direction, blockpredicate, BlockPredicate.alwaysTrue(), i);
   }

   public Stream<BlockPos> getPositions(PlacementContext placementcontext, RandomSource randomsource, BlockPos blockpos) {
      BlockPos.MutableBlockPos blockpos_mutableblockpos = blockpos.mutable();
      WorldGenLevel worldgenlevel = placementcontext.getLevel();
      if (!this.allowedSearchCondition.test(worldgenlevel, blockpos_mutableblockpos)) {
         return Stream.of();
      } else {
         int i = 0;

         while(true) {
            if (i < this.maxSteps) {
               if (this.targetCondition.test(worldgenlevel, blockpos_mutableblockpos)) {
                  return Stream.of(blockpos_mutableblockpos);
               }

               blockpos_mutableblockpos.move(this.directionOfSearch);
               if (worldgenlevel.isOutsideBuildHeight(blockpos_mutableblockpos.getY())) {
                  return Stream.of();
               }

               if (this.allowedSearchCondition.test(worldgenlevel, blockpos_mutableblockpos)) {
                  ++i;
                  continue;
               }
            }

            if (this.targetCondition.test(worldgenlevel, blockpos_mutableblockpos)) {
               return Stream.of(blockpos_mutableblockpos);
            }

            return Stream.of();
         }
      }
   }

   public PlacementModifierType<?> type() {
      return PlacementModifierType.ENVIRONMENT_SCAN;
   }
}
