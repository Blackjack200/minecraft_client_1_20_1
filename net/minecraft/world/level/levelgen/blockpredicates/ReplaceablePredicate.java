package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.state.BlockState;

class ReplaceablePredicate extends StateTestingPredicate {
   public static final Codec<ReplaceablePredicate> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> stateTestingCodec(recordcodecbuilder_instance).apply(recordcodecbuilder_instance, ReplaceablePredicate::new));

   public ReplaceablePredicate(Vec3i vec3i) {
      super(vec3i);
   }

   protected boolean test(BlockState blockstate) {
      return blockstate.canBeReplaced();
   }

   public BlockPredicateType<?> type() {
      return BlockPredicateType.REPLACEABLE;
   }
}
