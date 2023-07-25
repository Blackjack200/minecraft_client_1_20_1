package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Column;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.LargeDripstoneConfiguration;
import net.minecraft.world.phys.Vec3;

public class LargeDripstoneFeature extends Feature<LargeDripstoneConfiguration> {
   public LargeDripstoneFeature(Codec<LargeDripstoneConfiguration> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<LargeDripstoneConfiguration> featureplacecontext) {
      WorldGenLevel worldgenlevel = featureplacecontext.level();
      BlockPos blockpos = featureplacecontext.origin();
      LargeDripstoneConfiguration largedripstoneconfiguration = featureplacecontext.config();
      RandomSource randomsource = featureplacecontext.random();
      if (!DripstoneUtils.isEmptyOrWater(worldgenlevel, blockpos)) {
         return false;
      } else {
         Optional<Column> optional = Column.scan(worldgenlevel, blockpos, largedripstoneconfiguration.floorToCeilingSearchRange, DripstoneUtils::isEmptyOrWater, DripstoneUtils::isDripstoneBaseOrLava);
         if (optional.isPresent() && optional.get() instanceof Column.Range) {
            Column.Range column_range = (Column.Range)optional.get();
            if (column_range.height() < 4) {
               return false;
            } else {
               int i = (int)((float)column_range.height() * largedripstoneconfiguration.maxColumnRadiusToCaveHeightRatio);
               int j = Mth.clamp(i, largedripstoneconfiguration.columnRadius.getMinValue(), largedripstoneconfiguration.columnRadius.getMaxValue());
               int k = Mth.randomBetweenInclusive(randomsource, largedripstoneconfiguration.columnRadius.getMinValue(), j);
               LargeDripstoneFeature.LargeDripstone largedripstonefeature_largedripstone = makeDripstone(blockpos.atY(column_range.ceiling() - 1), false, randomsource, k, largedripstoneconfiguration.stalactiteBluntness, largedripstoneconfiguration.heightScale);
               LargeDripstoneFeature.LargeDripstone largedripstonefeature_largedripstone1 = makeDripstone(blockpos.atY(column_range.floor() + 1), true, randomsource, k, largedripstoneconfiguration.stalagmiteBluntness, largedripstoneconfiguration.heightScale);
               LargeDripstoneFeature.WindOffsetter largedripstonefeature_windoffsetter;
               if (largedripstonefeature_largedripstone.isSuitableForWind(largedripstoneconfiguration) && largedripstonefeature_largedripstone1.isSuitableForWind(largedripstoneconfiguration)) {
                  largedripstonefeature_windoffsetter = new LargeDripstoneFeature.WindOffsetter(blockpos.getY(), randomsource, largedripstoneconfiguration.windSpeed);
               } else {
                  largedripstonefeature_windoffsetter = LargeDripstoneFeature.WindOffsetter.noWind();
               }

               boolean flag = largedripstonefeature_largedripstone.moveBackUntilBaseIsInsideStoneAndShrinkRadiusIfNecessary(worldgenlevel, largedripstonefeature_windoffsetter);
               boolean flag1 = largedripstonefeature_largedripstone1.moveBackUntilBaseIsInsideStoneAndShrinkRadiusIfNecessary(worldgenlevel, largedripstonefeature_windoffsetter);
               if (flag) {
                  largedripstonefeature_largedripstone.placeBlocks(worldgenlevel, randomsource, largedripstonefeature_windoffsetter);
               }

               if (flag1) {
                  largedripstonefeature_largedripstone1.placeBlocks(worldgenlevel, randomsource, largedripstonefeature_windoffsetter);
               }

               return true;
            }
         } else {
            return false;
         }
      }
   }

   private static LargeDripstoneFeature.LargeDripstone makeDripstone(BlockPos blockpos, boolean flag, RandomSource randomsource, int i, FloatProvider floatprovider, FloatProvider floatprovider1) {
      return new LargeDripstoneFeature.LargeDripstone(blockpos, flag, i, (double)floatprovider.sample(randomsource), (double)floatprovider1.sample(randomsource));
   }

   private void placeDebugMarkers(WorldGenLevel worldgenlevel, BlockPos blockpos, Column.Range column_range, LargeDripstoneFeature.WindOffsetter largedripstonefeature_windoffsetter) {
      worldgenlevel.setBlock(largedripstonefeature_windoffsetter.offset(blockpos.atY(column_range.ceiling() - 1)), Blocks.DIAMOND_BLOCK.defaultBlockState(), 2);
      worldgenlevel.setBlock(largedripstonefeature_windoffsetter.offset(blockpos.atY(column_range.floor() + 1)), Blocks.GOLD_BLOCK.defaultBlockState(), 2);

      for(BlockPos.MutableBlockPos blockpos_mutableblockpos = blockpos.atY(column_range.floor() + 2).mutable(); blockpos_mutableblockpos.getY() < column_range.ceiling() - 1; blockpos_mutableblockpos.move(Direction.UP)) {
         BlockPos blockpos1 = largedripstonefeature_windoffsetter.offset(blockpos_mutableblockpos);
         if (DripstoneUtils.isEmptyOrWater(worldgenlevel, blockpos1) || worldgenlevel.getBlockState(blockpos1).is(Blocks.DRIPSTONE_BLOCK)) {
            worldgenlevel.setBlock(blockpos1, Blocks.CREEPER_HEAD.defaultBlockState(), 2);
         }
      }

   }

   static final class LargeDripstone {
      private BlockPos root;
      private final boolean pointingUp;
      private int radius;
      private final double bluntness;
      private final double scale;

      LargeDripstone(BlockPos blockpos, boolean flag, int i, double d0, double d1) {
         this.root = blockpos;
         this.pointingUp = flag;
         this.radius = i;
         this.bluntness = d0;
         this.scale = d1;
      }

      private int getHeight() {
         return this.getHeightAtRadius(0.0F);
      }

      private int getMinY() {
         return this.pointingUp ? this.root.getY() : this.root.getY() - this.getHeight();
      }

      private int getMaxY() {
         return !this.pointingUp ? this.root.getY() : this.root.getY() + this.getHeight();
      }

      boolean moveBackUntilBaseIsInsideStoneAndShrinkRadiusIfNecessary(WorldGenLevel worldgenlevel, LargeDripstoneFeature.WindOffsetter largedripstonefeature_windoffsetter) {
         while(this.radius > 1) {
            BlockPos.MutableBlockPos blockpos_mutableblockpos = this.root.mutable();
            int i = Math.min(10, this.getHeight());

            for(int j = 0; j < i; ++j) {
               if (worldgenlevel.getBlockState(blockpos_mutableblockpos).is(Blocks.LAVA)) {
                  return false;
               }

               if (DripstoneUtils.isCircleMostlyEmbeddedInStone(worldgenlevel, largedripstonefeature_windoffsetter.offset(blockpos_mutableblockpos), this.radius)) {
                  this.root = blockpos_mutableblockpos;
                  return true;
               }

               blockpos_mutableblockpos.move(this.pointingUp ? Direction.DOWN : Direction.UP);
            }

            this.radius /= 2;
         }

         return false;
      }

      private int getHeightAtRadius(float f) {
         return (int)DripstoneUtils.getDripstoneHeight((double)f, (double)this.radius, this.scale, this.bluntness);
      }

      void placeBlocks(WorldGenLevel worldgenlevel, RandomSource randomsource, LargeDripstoneFeature.WindOffsetter largedripstonefeature_windoffsetter) {
         for(int i = -this.radius; i <= this.radius; ++i) {
            for(int j = -this.radius; j <= this.radius; ++j) {
               float f = Mth.sqrt((float)(i * i + j * j));
               if (!(f > (float)this.radius)) {
                  int k = this.getHeightAtRadius(f);
                  if (k > 0) {
                     if ((double)randomsource.nextFloat() < 0.2D) {
                        k = (int)((float)k * Mth.randomBetween(randomsource, 0.8F, 1.0F));
                     }

                     BlockPos.MutableBlockPos blockpos_mutableblockpos = this.root.offset(i, 0, j).mutable();
                     boolean flag = false;
                     int l = this.pointingUp ? worldgenlevel.getHeight(Heightmap.Types.WORLD_SURFACE_WG, blockpos_mutableblockpos.getX(), blockpos_mutableblockpos.getZ()) : Integer.MAX_VALUE;

                     for(int i1 = 0; i1 < k && blockpos_mutableblockpos.getY() < l; ++i1) {
                        BlockPos blockpos = largedripstonefeature_windoffsetter.offset(blockpos_mutableblockpos);
                        if (DripstoneUtils.isEmptyOrWaterOrLava(worldgenlevel, blockpos)) {
                           flag = true;
                           Block block = Blocks.DRIPSTONE_BLOCK;
                           worldgenlevel.setBlock(blockpos, block.defaultBlockState(), 2);
                        } else if (flag && worldgenlevel.getBlockState(blockpos).is(BlockTags.BASE_STONE_OVERWORLD)) {
                           break;
                        }

                        blockpos_mutableblockpos.move(this.pointingUp ? Direction.UP : Direction.DOWN);
                     }
                  }
               }
            }
         }

      }

      boolean isSuitableForWind(LargeDripstoneConfiguration largedripstoneconfiguration) {
         return this.radius >= largedripstoneconfiguration.minRadiusForWind && this.bluntness >= (double)largedripstoneconfiguration.minBluntnessForWind;
      }
   }

   static final class WindOffsetter {
      private final int originY;
      @Nullable
      private final Vec3 windSpeed;

      WindOffsetter(int i, RandomSource randomsource, FloatProvider floatprovider) {
         this.originY = i;
         float f = floatprovider.sample(randomsource);
         float f1 = Mth.randomBetween(randomsource, 0.0F, (float)Math.PI);
         this.windSpeed = new Vec3((double)(Mth.cos(f1) * f), 0.0D, (double)(Mth.sin(f1) * f));
      }

      private WindOffsetter() {
         this.originY = 0;
         this.windSpeed = null;
      }

      static LargeDripstoneFeature.WindOffsetter noWind() {
         return new LargeDripstoneFeature.WindOffsetter();
      }

      BlockPos offset(BlockPos blockpos) {
         if (this.windSpeed == null) {
            return blockpos;
         } else {
            int i = this.originY - blockpos.getY();
            Vec3 vec3 = this.windSpeed.scale((double)i);
            return blockpos.offset(Mth.floor(vec3.x), 0, Mth.floor(vec3.z));
         }
      }
   }
}
