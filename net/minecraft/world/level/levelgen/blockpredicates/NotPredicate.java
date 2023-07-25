package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;

class NotPredicate implements BlockPredicate {
   public static final Codec<NotPredicate> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(BlockPredicate.CODEC.fieldOf("predicate").forGetter((notpredicate) -> notpredicate.predicate)).apply(recordcodecbuilder_instance, NotPredicate::new));
   private final BlockPredicate predicate;

   public NotPredicate(BlockPredicate blockpredicate) {
      this.predicate = blockpredicate;
   }

   public boolean test(WorldGenLevel worldgenlevel, BlockPos blockpos) {
      return !this.predicate.test(worldgenlevel, blockpos);
   }

   public BlockPredicateType<?> type() {
      return BlockPredicateType.NOT;
   }
}
