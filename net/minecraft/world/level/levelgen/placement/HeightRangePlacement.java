package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.TrapezoidHeight;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;

public class HeightRangePlacement extends PlacementModifier {
   public static final Codec<HeightRangePlacement> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(HeightProvider.CODEC.fieldOf("height").forGetter((heightrangeplacement) -> heightrangeplacement.height)).apply(recordcodecbuilder_instance, HeightRangePlacement::new));
   private final HeightProvider height;

   private HeightRangePlacement(HeightProvider heightprovider) {
      this.height = heightprovider;
   }

   public static HeightRangePlacement of(HeightProvider heightprovider) {
      return new HeightRangePlacement(heightprovider);
   }

   public static HeightRangePlacement uniform(VerticalAnchor verticalanchor, VerticalAnchor verticalanchor1) {
      return of(UniformHeight.of(verticalanchor, verticalanchor1));
   }

   public static HeightRangePlacement triangle(VerticalAnchor verticalanchor, VerticalAnchor verticalanchor1) {
      return of(TrapezoidHeight.of(verticalanchor, verticalanchor1));
   }

   public Stream<BlockPos> getPositions(PlacementContext placementcontext, RandomSource randomsource, BlockPos blockpos) {
      return Stream.of(blockpos.atY(this.height.sample(randomsource, placementcontext)));
   }

   public PlacementModifierType<?> type() {
      return PlacementModifierType.HEIGHT_RANGE;
   }
}
