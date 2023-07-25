package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;

class AnyOfPredicate extends CombiningPredicate {
   public static final Codec<AnyOfPredicate> CODEC = codec(AnyOfPredicate::new);

   public AnyOfPredicate(List<BlockPredicate> list) {
      super(list);
   }

   public boolean test(WorldGenLevel worldgenlevel, BlockPos blockpos) {
      for(BlockPredicate blockpredicate : this.predicates) {
         if (blockpredicate.test(worldgenlevel, blockpos)) {
            return true;
         }
      }

      return false;
   }

   public BlockPredicateType<?> type() {
      return BlockPredicateType.ANY_OF;
   }
}
