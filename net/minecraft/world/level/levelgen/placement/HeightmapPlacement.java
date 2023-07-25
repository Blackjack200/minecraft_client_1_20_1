package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;

public class HeightmapPlacement extends PlacementModifier {
   public static final Codec<HeightmapPlacement> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Heightmap.Types.CODEC.fieldOf("heightmap").forGetter((heightmapplacement) -> heightmapplacement.heightmap)).apply(recordcodecbuilder_instance, HeightmapPlacement::new));
   private final Heightmap.Types heightmap;

   private HeightmapPlacement(Heightmap.Types heightmap_types) {
      this.heightmap = heightmap_types;
   }

   public static HeightmapPlacement onHeightmap(Heightmap.Types heightmap_types) {
      return new HeightmapPlacement(heightmap_types);
   }

   public Stream<BlockPos> getPositions(PlacementContext placementcontext, RandomSource randomsource, BlockPos blockpos) {
      int i = blockpos.getX();
      int j = blockpos.getZ();
      int k = placementcontext.getHeight(this.heightmap, i, j);
      return k > placementcontext.getMinBuildHeight() ? Stream.of(new BlockPos(i, k, j)) : Stream.of();
   }

   public PlacementModifierType<?> type() {
      return PlacementModifierType.HEIGHTMAP;
   }
}
