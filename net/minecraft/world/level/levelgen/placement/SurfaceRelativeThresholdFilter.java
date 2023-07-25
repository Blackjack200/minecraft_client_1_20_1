package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;

public class SurfaceRelativeThresholdFilter extends PlacementFilter {
   public static final Codec<SurfaceRelativeThresholdFilter> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Heightmap.Types.CODEC.fieldOf("heightmap").forGetter((surfacerelativethresholdfilter2) -> surfacerelativethresholdfilter2.heightmap), Codec.INT.optionalFieldOf("min_inclusive", Integer.valueOf(Integer.MIN_VALUE)).forGetter((surfacerelativethresholdfilter1) -> surfacerelativethresholdfilter1.minInclusive), Codec.INT.optionalFieldOf("max_inclusive", Integer.valueOf(Integer.MAX_VALUE)).forGetter((surfacerelativethresholdfilter) -> surfacerelativethresholdfilter.maxInclusive)).apply(recordcodecbuilder_instance, SurfaceRelativeThresholdFilter::new));
   private final Heightmap.Types heightmap;
   private final int minInclusive;
   private final int maxInclusive;

   private SurfaceRelativeThresholdFilter(Heightmap.Types heightmap_types, int i, int j) {
      this.heightmap = heightmap_types;
      this.minInclusive = i;
      this.maxInclusive = j;
   }

   public static SurfaceRelativeThresholdFilter of(Heightmap.Types heightmap_types, int i, int j) {
      return new SurfaceRelativeThresholdFilter(heightmap_types, i, j);
   }

   protected boolean shouldPlace(PlacementContext placementcontext, RandomSource randomsource, BlockPos blockpos) {
      long i = (long)placementcontext.getHeight(this.heightmap, blockpos.getX(), blockpos.getZ());
      long j = i + (long)this.minInclusive;
      long k = i + (long)this.maxInclusive;
      return j <= (long)blockpos.getY() && (long)blockpos.getY() <= k;
   }

   public PlacementModifierType<?> type() {
      return PlacementModifierType.SURFACE_RELATIVE_THRESHOLD_FILTER;
   }
}
