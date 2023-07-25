package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;

public class SurfaceWaterDepthFilter extends PlacementFilter {
   public static final Codec<SurfaceWaterDepthFilter> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Codec.INT.fieldOf("max_water_depth").forGetter((surfacewaterdepthfilter) -> surfacewaterdepthfilter.maxWaterDepth)).apply(recordcodecbuilder_instance, SurfaceWaterDepthFilter::new));
   private final int maxWaterDepth;

   private SurfaceWaterDepthFilter(int i) {
      this.maxWaterDepth = i;
   }

   public static SurfaceWaterDepthFilter forMaxDepth(int i) {
      return new SurfaceWaterDepthFilter(i);
   }

   protected boolean shouldPlace(PlacementContext placementcontext, RandomSource randomsource, BlockPos blockpos) {
      int i = placementcontext.getHeight(Heightmap.Types.OCEAN_FLOOR, blockpos.getX(), blockpos.getZ());
      int j = placementcontext.getHeight(Heightmap.Types.WORLD_SURFACE, blockpos.getX(), blockpos.getZ());
      return j - i <= this.maxWaterDepth;
   }

   public PlacementModifierType<?> type() {
      return PlacementModifierType.SURFACE_WATER_DEPTH_FILTER;
   }
}
