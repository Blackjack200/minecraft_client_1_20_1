package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.material.Fluids;

public abstract class FoliagePlacer {
   public static final Codec<FoliagePlacer> CODEC = BuiltInRegistries.FOLIAGE_PLACER_TYPE.byNameCodec().dispatch(FoliagePlacer::type, FoliagePlacerType::codec);
   protected final IntProvider radius;
   protected final IntProvider offset;

   protected static <P extends FoliagePlacer> Products.P2<RecordCodecBuilder.Mu<P>, IntProvider, IntProvider> foliagePlacerParts(RecordCodecBuilder.Instance<P> recordcodecbuilder_instance) {
      return recordcodecbuilder_instance.group(IntProvider.codec(0, 16).fieldOf("radius").forGetter((foliageplacer1) -> foliageplacer1.radius), IntProvider.codec(0, 16).fieldOf("offset").forGetter((foliageplacer) -> foliageplacer.offset));
   }

   public FoliagePlacer(IntProvider intprovider, IntProvider intprovider1) {
      this.radius = intprovider;
      this.offset = intprovider1;
   }

   protected abstract FoliagePlacerType<?> type();

   public void createFoliage(LevelSimulatedReader levelsimulatedreader, FoliagePlacer.FoliageSetter foliageplacer_foliagesetter, RandomSource randomsource, TreeConfiguration treeconfiguration, int i, FoliagePlacer.FoliageAttachment foliageplacer_foliageattachment, int j, int k) {
      this.createFoliage(levelsimulatedreader, foliageplacer_foliagesetter, randomsource, treeconfiguration, i, foliageplacer_foliageattachment, j, k, this.offset(randomsource));
   }

   protected abstract void createFoliage(LevelSimulatedReader levelsimulatedreader, FoliagePlacer.FoliageSetter foliageplacer_foliagesetter, RandomSource randomsource, TreeConfiguration treeconfiguration, int i, FoliagePlacer.FoliageAttachment foliageplacer_foliageattachment, int j, int k, int l);

   public abstract int foliageHeight(RandomSource randomsource, int i, TreeConfiguration treeconfiguration);

   public int foliageRadius(RandomSource randomsource, int i) {
      return this.radius.sample(randomsource);
   }

   private int offset(RandomSource randomsource) {
      return this.offset.sample(randomsource);
   }

   protected abstract boolean shouldSkipLocation(RandomSource randomsource, int i, int j, int k, int l, boolean flag);

   protected boolean shouldSkipLocationSigned(RandomSource randomsource, int i, int j, int k, int l, boolean flag) {
      int i1;
      int j1;
      if (flag) {
         i1 = Math.min(Math.abs(i), Math.abs(i - 1));
         j1 = Math.min(Math.abs(k), Math.abs(k - 1));
      } else {
         i1 = Math.abs(i);
         j1 = Math.abs(k);
      }

      return this.shouldSkipLocation(randomsource, i1, j, j1, l, flag);
   }

   protected void placeLeavesRow(LevelSimulatedReader levelsimulatedreader, FoliagePlacer.FoliageSetter foliageplacer_foliagesetter, RandomSource randomsource, TreeConfiguration treeconfiguration, BlockPos blockpos, int i, int j, boolean flag) {
      int k = flag ? 1 : 0;
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

      for(int l = -i; l <= i + k; ++l) {
         for(int i1 = -i; i1 <= i + k; ++i1) {
            if (!this.shouldSkipLocationSigned(randomsource, l, j, i1, i, flag)) {
               blockpos_mutableblockpos.setWithOffset(blockpos, l, j, i1);
               tryPlaceLeaf(levelsimulatedreader, foliageplacer_foliagesetter, randomsource, treeconfiguration, blockpos_mutableblockpos);
            }
         }
      }

   }

   protected final void placeLeavesRowWithHangingLeavesBelow(LevelSimulatedReader levelsimulatedreader, FoliagePlacer.FoliageSetter foliageplacer_foliagesetter, RandomSource randomsource, TreeConfiguration treeconfiguration, BlockPos blockpos, int i, int j, boolean flag, float f, float f1) {
      this.placeLeavesRow(levelsimulatedreader, foliageplacer_foliagesetter, randomsource, treeconfiguration, blockpos, i, j, flag);
      int k = flag ? 1 : 0;
      BlockPos blockpos1 = blockpos.below();
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         Direction direction1 = direction.getClockWise();
         int l = direction1.getAxisDirection() == Direction.AxisDirection.POSITIVE ? i + k : i;
         blockpos_mutableblockpos.setWithOffset(blockpos, 0, j - 1, 0).move(direction1, l).move(direction, -i);
         int i1 = -i;

         while(i1 < i + k) {
            boolean flag1 = foliageplacer_foliagesetter.isSet(blockpos_mutableblockpos.move(Direction.UP));
            blockpos_mutableblockpos.move(Direction.DOWN);
            if (flag1 && tryPlaceExtension(levelsimulatedreader, foliageplacer_foliagesetter, randomsource, treeconfiguration, f, blockpos1, blockpos_mutableblockpos)) {
               blockpos_mutableblockpos.move(Direction.DOWN);
               tryPlaceExtension(levelsimulatedreader, foliageplacer_foliagesetter, randomsource, treeconfiguration, f1, blockpos1, blockpos_mutableblockpos);
               blockpos_mutableblockpos.move(Direction.UP);
            }

            ++i1;
            blockpos_mutableblockpos.move(direction);
         }
      }

   }

   private static boolean tryPlaceExtension(LevelSimulatedReader levelsimulatedreader, FoliagePlacer.FoliageSetter foliageplacer_foliagesetter, RandomSource randomsource, TreeConfiguration treeconfiguration, float f, BlockPos blockpos, BlockPos.MutableBlockPos blockpos_mutableblockpos) {
      if (blockpos_mutableblockpos.distManhattan(blockpos) >= 7) {
         return false;
      } else {
         return randomsource.nextFloat() > f ? false : tryPlaceLeaf(levelsimulatedreader, foliageplacer_foliagesetter, randomsource, treeconfiguration, blockpos_mutableblockpos);
      }
   }

   protected static boolean tryPlaceLeaf(LevelSimulatedReader levelsimulatedreader, FoliagePlacer.FoliageSetter foliageplacer_foliagesetter, RandomSource randomsource, TreeConfiguration treeconfiguration, BlockPos blockpos) {
      if (!TreeFeature.validTreePos(levelsimulatedreader, blockpos)) {
         return false;
      } else {
         BlockState blockstate = treeconfiguration.foliageProvider.getState(randomsource, blockpos);
         if (blockstate.hasProperty(BlockStateProperties.WATERLOGGED)) {
            blockstate = blockstate.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(levelsimulatedreader.isFluidAtPosition(blockpos, (fluidstate) -> fluidstate.isSourceOfType(Fluids.WATER))));
         }

         foliageplacer_foliagesetter.set(blockpos, blockstate);
         return true;
      }
   }

   public static final class FoliageAttachment {
      private final BlockPos pos;
      private final int radiusOffset;
      private final boolean doubleTrunk;

      public FoliageAttachment(BlockPos blockpos, int i, boolean flag) {
         this.pos = blockpos;
         this.radiusOffset = i;
         this.doubleTrunk = flag;
      }

      public BlockPos pos() {
         return this.pos;
      }

      public int radiusOffset() {
         return this.radiusOffset;
      }

      public boolean doubleTrunk() {
         return this.doubleTrunk;
      }
   }

   public interface FoliageSetter {
      void set(BlockPos blockpos, BlockState blockstate);

      boolean isSet(BlockPos blockpos);
   }
}
