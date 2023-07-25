package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;

public class CountPlacement extends RepeatingPlacement {
   public static final Codec<CountPlacement> CODEC = IntProvider.codec(0, 256).fieldOf("count").xmap(CountPlacement::new, (countplacement) -> countplacement.count).codec();
   private final IntProvider count;

   private CountPlacement(IntProvider intprovider) {
      this.count = intprovider;
   }

   public static CountPlacement of(IntProvider intprovider) {
      return new CountPlacement(intprovider);
   }

   public static CountPlacement of(int i) {
      return of(ConstantInt.of(i));
   }

   protected int count(RandomSource randomsource, BlockPos blockpos) {
      return this.count.sample(randomsource);
   }

   public PlacementModifierType<?> type() {
      return PlacementModifierType.COUNT;
   }
}
