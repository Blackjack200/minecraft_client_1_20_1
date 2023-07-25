package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;

public class BlockPredicateFilter extends PlacementFilter {
   public static final Codec<BlockPredicateFilter> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(BlockPredicate.CODEC.fieldOf("predicate").forGetter((blockpredicatefilter) -> blockpredicatefilter.predicate)).apply(recordcodecbuilder_instance, BlockPredicateFilter::new));
   private final BlockPredicate predicate;

   private BlockPredicateFilter(BlockPredicate blockpredicate) {
      this.predicate = blockpredicate;
   }

   public static BlockPredicateFilter forPredicate(BlockPredicate blockpredicate) {
      return new BlockPredicateFilter(blockpredicate);
   }

   protected boolean shouldPlace(PlacementContext placementcontext, RandomSource randomsource, BlockPos blockpos) {
      return this.predicate.test(placementcontext.getLevel(), blockpos);
   }

   public PlacementModifierType<?> type() {
      return PlacementModifierType.BLOCK_PREDICATE_FILTER;
   }
}
