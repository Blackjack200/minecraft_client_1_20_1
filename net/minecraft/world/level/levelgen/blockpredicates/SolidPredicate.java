package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.state.BlockState;

/** @deprecated */
@Deprecated
public class SolidPredicate extends StateTestingPredicate {
   public static final Codec<SolidPredicate> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> stateTestingCodec(recordcodecbuilder_instance).apply(recordcodecbuilder_instance, SolidPredicate::new));

   public SolidPredicate(Vec3i vec3i) {
      super(vec3i);
   }

   protected boolean test(BlockState blockstate) {
      return blockstate.isSolid();
   }

   public BlockPredicateType<?> type() {
      return BlockPredicateType.SOLID;
   }
}
