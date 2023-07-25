package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;

public class MultifaceGrowthConfiguration implements FeatureConfiguration {
   public static final Codec<MultifaceGrowthConfiguration> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").flatXmap(MultifaceGrowthConfiguration::apply, DataResult::success).orElse((MultifaceBlock)Blocks.GLOW_LICHEN).forGetter((multifacegrowthconfiguration6) -> multifacegrowthconfiguration6.placeBlock), Codec.intRange(1, 64).fieldOf("search_range").orElse(10).forGetter((multifacegrowthconfiguration5) -> multifacegrowthconfiguration5.searchRange), Codec.BOOL.fieldOf("can_place_on_floor").orElse(false).forGetter((multifacegrowthconfiguration4) -> multifacegrowthconfiguration4.canPlaceOnFloor), Codec.BOOL.fieldOf("can_place_on_ceiling").orElse(false).forGetter((multifacegrowthconfiguration3) -> multifacegrowthconfiguration3.canPlaceOnCeiling), Codec.BOOL.fieldOf("can_place_on_wall").orElse(false).forGetter((multifacegrowthconfiguration2) -> multifacegrowthconfiguration2.canPlaceOnWall), Codec.floatRange(0.0F, 1.0F).fieldOf("chance_of_spreading").orElse(0.5F).forGetter((multifacegrowthconfiguration1) -> multifacegrowthconfiguration1.chanceOfSpreading), RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("can_be_placed_on").forGetter((multifacegrowthconfiguration) -> multifacegrowthconfiguration.canBePlacedOn)).apply(recordcodecbuilder_instance, MultifaceGrowthConfiguration::new));
   public final MultifaceBlock placeBlock;
   public final int searchRange;
   public final boolean canPlaceOnFloor;
   public final boolean canPlaceOnCeiling;
   public final boolean canPlaceOnWall;
   public final float chanceOfSpreading;
   public final HolderSet<Block> canBePlacedOn;
   private final ObjectArrayList<Direction> validDirections;

   private static DataResult<MultifaceBlock> apply(Block block) {
      DataResult var10000;
      if (block instanceof MultifaceBlock multifaceblock) {
         var10000 = DataResult.success(multifaceblock);
      } else {
         var10000 = DataResult.error(() -> "Growth block should be a multiface block");
      }

      return var10000;
   }

   public MultifaceGrowthConfiguration(MultifaceBlock multifaceblock, int i, boolean flag, boolean flag1, boolean flag2, float f, HolderSet<Block> holderset) {
      this.placeBlock = multifaceblock;
      this.searchRange = i;
      this.canPlaceOnFloor = flag;
      this.canPlaceOnCeiling = flag1;
      this.canPlaceOnWall = flag2;
      this.chanceOfSpreading = f;
      this.canBePlacedOn = holderset;
      this.validDirections = new ObjectArrayList<>(6);
      if (flag1) {
         this.validDirections.add(Direction.UP);
      }

      if (flag) {
         this.validDirections.add(Direction.DOWN);
      }

      if (flag2) {
         Direction.Plane.HORIZONTAL.forEach(this.validDirections::add);
      }

   }

   public List<Direction> getShuffledDirectionsExcept(RandomSource randomsource, Direction direction) {
      return Util.toShuffledList(this.validDirections.stream().filter((direction2) -> direction2 != direction), randomsource);
   }

   public List<Direction> getShuffledDirections(RandomSource randomsource) {
      return Util.shuffledCopy(this.validDirections, randomsource);
   }
}
