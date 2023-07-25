package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/** @deprecated */
@Deprecated
public class CountOnEveryLayerPlacement extends PlacementModifier {
   public static final Codec<CountOnEveryLayerPlacement> CODEC = IntProvider.codec(0, 256).fieldOf("count").xmap(CountOnEveryLayerPlacement::new, (countoneverylayerplacement) -> countoneverylayerplacement.count).codec();
   private final IntProvider count;

   private CountOnEveryLayerPlacement(IntProvider intprovider) {
      this.count = intprovider;
   }

   public static CountOnEveryLayerPlacement of(IntProvider intprovider) {
      return new CountOnEveryLayerPlacement(intprovider);
   }

   public static CountOnEveryLayerPlacement of(int i) {
      return of(ConstantInt.of(i));
   }

   public Stream<BlockPos> getPositions(PlacementContext placementcontext, RandomSource randomsource, BlockPos blockpos) {
      Stream.Builder<BlockPos> stream_builder = Stream.builder();
      int i = 0;

      boolean flag;
      do {
         flag = false;

         for(int j = 0; j < this.count.sample(randomsource); ++j) {
            int k = randomsource.nextInt(16) + blockpos.getX();
            int l = randomsource.nextInt(16) + blockpos.getZ();
            int i1 = placementcontext.getHeight(Heightmap.Types.MOTION_BLOCKING, k, l);
            int j1 = findOnGroundYPosition(placementcontext, k, i1, l, i);
            if (j1 != Integer.MAX_VALUE) {
               stream_builder.add(new BlockPos(k, j1, l));
               flag = true;
            }
         }

         ++i;
      } while(flag);

      return stream_builder.build();
   }

   public PlacementModifierType<?> type() {
      return PlacementModifierType.COUNT_ON_EVERY_LAYER;
   }

   private static int findOnGroundYPosition(PlacementContext placementcontext, int i, int j, int k, int l) {
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos(i, j, k);
      int i1 = 0;
      BlockState blockstate = placementcontext.getBlockState(blockpos_mutableblockpos);

      for(int j1 = j; j1 >= placementcontext.getMinBuildHeight() + 1; --j1) {
         blockpos_mutableblockpos.setY(j1 - 1);
         BlockState blockstate1 = placementcontext.getBlockState(blockpos_mutableblockpos);
         if (!isEmpty(blockstate1) && isEmpty(blockstate) && !blockstate1.is(Blocks.BEDROCK)) {
            if (i1 == l) {
               return blockpos_mutableblockpos.getY() + 1;
            }

            ++i1;
         }

         blockstate = blockstate1;
      }

      return Integer.MAX_VALUE;
   }

   private static boolean isEmpty(BlockState blockstate) {
      return blockstate.isAir() || blockstate.is(Blocks.WATER) || blockstate.is(Blocks.LAVA);
   }
}
