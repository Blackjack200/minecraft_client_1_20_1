package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Column;
import net.minecraft.world.level.levelgen.feature.configurations.UnderwaterMagmaConfiguration;
import net.minecraft.world.phys.AABB;

public class UnderwaterMagmaFeature extends Feature<UnderwaterMagmaConfiguration> {
   public UnderwaterMagmaFeature(Codec<UnderwaterMagmaConfiguration> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<UnderwaterMagmaConfiguration> featureplacecontext) {
      WorldGenLevel worldgenlevel = featureplacecontext.level();
      BlockPos blockpos = featureplacecontext.origin();
      UnderwaterMagmaConfiguration underwatermagmaconfiguration = featureplacecontext.config();
      RandomSource randomsource = featureplacecontext.random();
      OptionalInt optionalint = getFloorY(worldgenlevel, blockpos, underwatermagmaconfiguration);
      if (!optionalint.isPresent()) {
         return false;
      } else {
         BlockPos blockpos1 = blockpos.atY(optionalint.getAsInt());
         Vec3i vec3i = new Vec3i(underwatermagmaconfiguration.placementRadiusAroundFloor, underwatermagmaconfiguration.placementRadiusAroundFloor, underwatermagmaconfiguration.placementRadiusAroundFloor);
         AABB aabb = new AABB(blockpos1.subtract(vec3i), blockpos1.offset(vec3i));
         return BlockPos.betweenClosedStream(aabb).filter((blockpos4) -> randomsource.nextFloat() < underwatermagmaconfiguration.placementProbabilityPerValidPosition).filter((blockpos3) -> this.isValidPlacement(worldgenlevel, blockpos3)).mapToInt((blockpos2) -> {
            worldgenlevel.setBlock(blockpos2, Blocks.MAGMA_BLOCK.defaultBlockState(), 2);
            return 1;
         }).sum() > 0;
      }
   }

   private static OptionalInt getFloorY(WorldGenLevel worldgenlevel, BlockPos blockpos, UnderwaterMagmaConfiguration underwatermagmaconfiguration) {
      Predicate<BlockState> predicate = (blockstate1) -> blockstate1.is(Blocks.WATER);
      Predicate<BlockState> predicate1 = (blockstate) -> !blockstate.is(Blocks.WATER);
      Optional<Column> optional = Column.scan(worldgenlevel, blockpos, underwatermagmaconfiguration.floorSearchRange, predicate, predicate1);
      return optional.map(Column::getFloor).orElseGet(OptionalInt::empty);
   }

   private boolean isValidPlacement(WorldGenLevel worldgenlevel, BlockPos blockpos) {
      if (!this.isWaterOrAir(worldgenlevel, blockpos) && !this.isWaterOrAir(worldgenlevel, blockpos.below())) {
         for(Direction direction : Direction.Plane.HORIZONTAL) {
            if (this.isWaterOrAir(worldgenlevel, blockpos.relative(direction))) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private boolean isWaterOrAir(LevelAccessor levelaccessor, BlockPos blockpos) {
      BlockState blockstate = levelaccessor.getBlockState(blockpos);
      return blockstate.is(Blocks.WATER) || blockstate.isAir();
   }
}
