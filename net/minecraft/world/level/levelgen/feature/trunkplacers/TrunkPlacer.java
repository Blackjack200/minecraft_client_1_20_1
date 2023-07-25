package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;

public abstract class TrunkPlacer {
   public static final Codec<TrunkPlacer> CODEC = BuiltInRegistries.TRUNK_PLACER_TYPE.byNameCodec().dispatch(TrunkPlacer::type, TrunkPlacerType::codec);
   private static final int MAX_BASE_HEIGHT = 32;
   private static final int MAX_RAND = 24;
   public static final int MAX_HEIGHT = 80;
   protected final int baseHeight;
   protected final int heightRandA;
   protected final int heightRandB;

   protected static <P extends TrunkPlacer> Products.P3<RecordCodecBuilder.Mu<P>, Integer, Integer, Integer> trunkPlacerParts(RecordCodecBuilder.Instance<P> recordcodecbuilder_instance) {
      return recordcodecbuilder_instance.group(Codec.intRange(0, 32).fieldOf("base_height").forGetter((trunkplacer2) -> trunkplacer2.baseHeight), Codec.intRange(0, 24).fieldOf("height_rand_a").forGetter((trunkplacer1) -> trunkplacer1.heightRandA), Codec.intRange(0, 24).fieldOf("height_rand_b").forGetter((trunkplacer) -> trunkplacer.heightRandB));
   }

   public TrunkPlacer(int i, int j, int k) {
      this.baseHeight = i;
      this.heightRandA = j;
      this.heightRandB = k;
   }

   protected abstract TrunkPlacerType<?> type();

   public abstract List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader levelsimulatedreader, BiConsumer<BlockPos, BlockState> biconsumer, RandomSource randomsource, int i, BlockPos blockpos, TreeConfiguration treeconfiguration);

   public int getTreeHeight(RandomSource randomsource) {
      return this.baseHeight + randomsource.nextInt(this.heightRandA + 1) + randomsource.nextInt(this.heightRandB + 1);
   }

   private static boolean isDirt(LevelSimulatedReader levelsimulatedreader, BlockPos blockpos) {
      return levelsimulatedreader.isStateAtPosition(blockpos, (blockstate) -> Feature.isDirt(blockstate) && !blockstate.is(Blocks.GRASS_BLOCK) && !blockstate.is(Blocks.MYCELIUM));
   }

   protected static void setDirtAt(LevelSimulatedReader levelsimulatedreader, BiConsumer<BlockPos, BlockState> biconsumer, RandomSource randomsource, BlockPos blockpos, TreeConfiguration treeconfiguration) {
      if (treeconfiguration.forceDirt || !isDirt(levelsimulatedreader, blockpos)) {
         biconsumer.accept(blockpos, treeconfiguration.dirtProvider.getState(randomsource, blockpos));
      }

   }

   protected boolean placeLog(LevelSimulatedReader levelsimulatedreader, BiConsumer<BlockPos, BlockState> biconsumer, RandomSource randomsource, BlockPos blockpos, TreeConfiguration treeconfiguration) {
      return this.placeLog(levelsimulatedreader, biconsumer, randomsource, blockpos, treeconfiguration, Function.identity());
   }

   protected boolean placeLog(LevelSimulatedReader levelsimulatedreader, BiConsumer<BlockPos, BlockState> biconsumer, RandomSource randomsource, BlockPos blockpos, TreeConfiguration treeconfiguration, Function<BlockState, BlockState> function) {
      if (this.validTreePos(levelsimulatedreader, blockpos)) {
         biconsumer.accept(blockpos, function.apply(treeconfiguration.trunkProvider.getState(randomsource, blockpos)));
         return true;
      } else {
         return false;
      }
   }

   protected void placeLogIfFree(LevelSimulatedReader levelsimulatedreader, BiConsumer<BlockPos, BlockState> biconsumer, RandomSource randomsource, BlockPos.MutableBlockPos blockpos_mutableblockpos, TreeConfiguration treeconfiguration) {
      if (this.isFree(levelsimulatedreader, blockpos_mutableblockpos)) {
         this.placeLog(levelsimulatedreader, biconsumer, randomsource, blockpos_mutableblockpos, treeconfiguration);
      }

   }

   protected boolean validTreePos(LevelSimulatedReader levelsimulatedreader, BlockPos blockpos) {
      return TreeFeature.validTreePos(levelsimulatedreader, blockpos);
   }

   public boolean isFree(LevelSimulatedReader levelsimulatedreader, BlockPos blockpos) {
      return this.validTreePos(levelsimulatedreader, blockpos) || levelsimulatedreader.isStateAtPosition(blockpos, (blockstate) -> blockstate.is(BlockTags.LOGS));
   }
}
