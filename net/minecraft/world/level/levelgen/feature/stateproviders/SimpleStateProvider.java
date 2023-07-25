package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public class SimpleStateProvider extends BlockStateProvider {
   public static final Codec<SimpleStateProvider> CODEC = BlockState.CODEC.fieldOf("state").xmap(SimpleStateProvider::new, (simplestateprovider) -> simplestateprovider.state).codec();
   private final BlockState state;

   protected SimpleStateProvider(BlockState blockstate) {
      this.state = blockstate;
   }

   protected BlockStateProviderType<?> type() {
      return BlockStateProviderType.SIMPLE_STATE_PROVIDER;
   }

   public BlockState getState(RandomSource randomsource, BlockPos blockpos) {
      return this.state;
   }
}
