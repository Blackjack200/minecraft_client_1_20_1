package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;

class AllOfPredicate extends CombiningPredicate {
   public static final Codec<AllOfPredicate> CODEC = codec(AllOfPredicate::new);

   public AllOfPredicate(List<BlockPredicate> list) {
      super(list);
   }

   public boolean test(WorldGenLevel worldgenlevel, BlockPos blockpos) {
      for(BlockPredicate blockpredicate : this.predicates) {
         if (!blockpredicate.test(worldgenlevel, blockpos)) {
            return false;
         }
      }

      return true;
   }

   public BlockPredicateType<?> type() {
      return BlockPredicateType.ALL_OF;
   }
}
