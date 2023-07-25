package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;

public class InSquarePlacement extends PlacementModifier {
   private static final InSquarePlacement INSTANCE = new InSquarePlacement();
   public static final Codec<InSquarePlacement> CODEC = Codec.unit(() -> INSTANCE);

   public static InSquarePlacement spread() {
      return INSTANCE;
   }

   public Stream<BlockPos> getPositions(PlacementContext placementcontext, RandomSource randomsource, BlockPos blockpos) {
      int i = randomsource.nextInt(16) + blockpos.getX();
      int j = randomsource.nextInt(16) + blockpos.getZ();
      return Stream.of(new BlockPos(i, blockpos.getY(), j));
   }

   public PlacementModifierType<?> type() {
      return PlacementModifierType.IN_SQUARE;
   }
}
