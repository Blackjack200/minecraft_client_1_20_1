package net.minecraft.world.level.levelgen.carver;

import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.WorldGenerationContext;

public class CarvingContext extends WorldGenerationContext {
   private final RegistryAccess registryAccess;
   private final NoiseChunk noiseChunk;
   private final RandomState randomState;
   private final SurfaceRules.RuleSource surfaceRule;

   public CarvingContext(NoiseBasedChunkGenerator noisebasedchunkgenerator, RegistryAccess registryaccess, LevelHeightAccessor levelheightaccessor, NoiseChunk noisechunk, RandomState randomstate, SurfaceRules.RuleSource surfacerules_rulesource) {
      super(noisebasedchunkgenerator, levelheightaccessor);
      this.registryAccess = registryaccess;
      this.noiseChunk = noisechunk;
      this.randomState = randomstate;
      this.surfaceRule = surfacerules_rulesource;
   }

   /** @deprecated */
   @Deprecated
   public Optional<BlockState> topMaterial(Function<BlockPos, Holder<Biome>> function, ChunkAccess chunkaccess, BlockPos blockpos, boolean flag) {
      return this.randomState.surfaceSystem().topMaterial(this.surfaceRule, this, function, chunkaccess, this.noiseChunk, blockpos, flag);
   }

   /** @deprecated */
   @Deprecated
   public RegistryAccess registryAccess() {
      return this.registryAccess;
   }

   public RandomState randomState() {
      return this.randomState;
   }
}
