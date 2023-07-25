package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.block.state.BlockState;

public class WeightedStateProvider extends BlockStateProvider {
   public static final Codec<WeightedStateProvider> CODEC = SimpleWeightedRandomList.wrappedCodec(BlockState.CODEC).comapFlatMap(WeightedStateProvider::create, (weightedstateprovider) -> weightedstateprovider.weightedList).fieldOf("entries").codec();
   private final SimpleWeightedRandomList<BlockState> weightedList;

   private static DataResult<WeightedStateProvider> create(SimpleWeightedRandomList<BlockState> simpleweightedrandomlist) {
      return simpleweightedrandomlist.isEmpty() ? DataResult.error(() -> "WeightedStateProvider with no states") : DataResult.success(new WeightedStateProvider(simpleweightedrandomlist));
   }

   public WeightedStateProvider(SimpleWeightedRandomList<BlockState> simpleweightedrandomlist) {
      this.weightedList = simpleweightedrandomlist;
   }

   public WeightedStateProvider(SimpleWeightedRandomList.Builder<BlockState> simpleweightedrandomlist_builder) {
      this(simpleweightedrandomlist_builder.build());
   }

   protected BlockStateProviderType<?> type() {
      return BlockStateProviderType.WEIGHTED_STATE_PROVIDER;
   }

   public BlockState getState(RandomSource randomsource, BlockPos blockpos) {
      return this.weightedList.getRandomValue(randomsource).orElseThrow(IllegalStateException::new);
   }
}
