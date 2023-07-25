package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Function;

abstract class CombiningPredicate implements BlockPredicate {
   protected final List<BlockPredicate> predicates;

   protected CombiningPredicate(List<BlockPredicate> list) {
      this.predicates = list;
   }

   public static <T extends CombiningPredicate> Codec<T> codec(Function<List<BlockPredicate>, T> function) {
      return RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(BlockPredicate.CODEC.listOf().fieldOf("predicates").forGetter((combiningpredicate) -> combiningpredicate.predicates)).apply(recordcodecbuilder_instance, function));
   }
}
