package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

class MatchingBlocksPredicate extends StateTestingPredicate {
   private final HolderSet<Block> blocks;
   public static final Codec<MatchingBlocksPredicate> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> stateTestingCodec(recordcodecbuilder_instance).and(RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("blocks").forGetter((matchingblockspredicate) -> matchingblockspredicate.blocks)).apply(recordcodecbuilder_instance, MatchingBlocksPredicate::new));

   public MatchingBlocksPredicate(Vec3i vec3i, HolderSet<Block> holderset) {
      super(vec3i);
      this.blocks = holderset;
   }

   protected boolean test(BlockState blockstate) {
      return blockstate.is(this.blocks);
   }

   public BlockPredicateType<?> type() {
      return BlockPredicateType.MATCHING_BLOCKS;
   }
}
