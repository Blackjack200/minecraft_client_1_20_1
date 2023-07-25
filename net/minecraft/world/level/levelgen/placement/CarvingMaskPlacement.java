package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.GenerationStep;

public class CarvingMaskPlacement extends PlacementModifier {
   public static final Codec<CarvingMaskPlacement> CODEC = GenerationStep.Carving.CODEC.fieldOf("step").xmap(CarvingMaskPlacement::new, (carvingmaskplacement) -> carvingmaskplacement.step).codec();
   private final GenerationStep.Carving step;

   private CarvingMaskPlacement(GenerationStep.Carving generationstep_carving) {
      this.step = generationstep_carving;
   }

   public static CarvingMaskPlacement forStep(GenerationStep.Carving generationstep_carving) {
      return new CarvingMaskPlacement(generationstep_carving);
   }

   public Stream<BlockPos> getPositions(PlacementContext placementcontext, RandomSource randomsource, BlockPos blockpos) {
      ChunkPos chunkpos = new ChunkPos(blockpos);
      return placementcontext.getCarvingMask(chunkpos, this.step).stream(chunkpos);
   }

   public PlacementModifierType<?> type() {
      return PlacementModifierType.CARVING_MASK_PLACEMENT;
   }
}
