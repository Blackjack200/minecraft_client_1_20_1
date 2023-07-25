package net.minecraft.world.level.levelgen.placement;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;

public class PlacementContext extends WorldGenerationContext {
   private final WorldGenLevel level;
   private final ChunkGenerator generator;
   private final Optional<PlacedFeature> topFeature;

   public PlacementContext(WorldGenLevel worldgenlevel, ChunkGenerator chunkgenerator, Optional<PlacedFeature> optional) {
      super(chunkgenerator, worldgenlevel);
      this.level = worldgenlevel;
      this.generator = chunkgenerator;
      this.topFeature = optional;
   }

   public int getHeight(Heightmap.Types heightmap_types, int i, int j) {
      return this.level.getHeight(heightmap_types, i, j);
   }

   public CarvingMask getCarvingMask(ChunkPos chunkpos, GenerationStep.Carving generationstep_carving) {
      return ((ProtoChunk)this.level.getChunk(chunkpos.x, chunkpos.z)).getOrCreateCarvingMask(generationstep_carving);
   }

   public BlockState getBlockState(BlockPos blockpos) {
      return this.level.getBlockState(blockpos);
   }

   public int getMinBuildHeight() {
      return this.level.getMinBuildHeight();
   }

   public WorldGenLevel getLevel() {
      return this.level;
   }

   public Optional<PlacedFeature> topFeature() {
      return this.topFeature;
   }

   public ChunkGenerator generator() {
      return this.generator;
   }
}
