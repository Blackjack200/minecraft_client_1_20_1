package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SculkBehaviour;
import net.minecraft.world.level.block.SculkShriekerBlock;
import net.minecraft.world.level.block.SculkSpreader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.SculkPatchConfiguration;

public class SculkPatchFeature extends Feature<SculkPatchConfiguration> {
   public SculkPatchFeature(Codec<SculkPatchConfiguration> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<SculkPatchConfiguration> featureplacecontext) {
      WorldGenLevel worldgenlevel = featureplacecontext.level();
      BlockPos blockpos = featureplacecontext.origin();
      if (!this.canSpreadFrom(worldgenlevel, blockpos)) {
         return false;
      } else {
         SculkPatchConfiguration sculkpatchconfiguration = featureplacecontext.config();
         RandomSource randomsource = featureplacecontext.random();
         SculkSpreader sculkspreader = SculkSpreader.createWorldGenSpreader();
         int i = sculkpatchconfiguration.spreadRounds() + sculkpatchconfiguration.growthRounds();

         for(int j = 0; j < i; ++j) {
            for(int k = 0; k < sculkpatchconfiguration.chargeCount(); ++k) {
               sculkspreader.addCursors(blockpos, sculkpatchconfiguration.amountPerCharge());
            }

            boolean flag = j < sculkpatchconfiguration.spreadRounds();

            for(int l = 0; l < sculkpatchconfiguration.spreadAttempts(); ++l) {
               sculkspreader.updateCursors(worldgenlevel, blockpos, randomsource, flag);
            }

            sculkspreader.clear();
         }

         BlockPos blockpos1 = blockpos.below();
         if (randomsource.nextFloat() <= sculkpatchconfiguration.catalystChance() && worldgenlevel.getBlockState(blockpos1).isCollisionShapeFullBlock(worldgenlevel, blockpos1)) {
            worldgenlevel.setBlock(blockpos, Blocks.SCULK_CATALYST.defaultBlockState(), 3);
         }

         int i1 = sculkpatchconfiguration.extraRareGrowths().sample(randomsource);

         for(int j1 = 0; j1 < i1; ++j1) {
            BlockPos blockpos2 = blockpos.offset(randomsource.nextInt(5) - 2, 0, randomsource.nextInt(5) - 2);
            if (worldgenlevel.getBlockState(blockpos2).isAir() && worldgenlevel.getBlockState(blockpos2.below()).isFaceSturdy(worldgenlevel, blockpos2.below(), Direction.UP)) {
               worldgenlevel.setBlock(blockpos2, Blocks.SCULK_SHRIEKER.defaultBlockState().setValue(SculkShriekerBlock.CAN_SUMMON, Boolean.valueOf(true)), 3);
            }
         }

         return true;
      }
   }

   private boolean canSpreadFrom(LevelAccessor levelaccessor, BlockPos blockpos) {
      BlockState blockstate = levelaccessor.getBlockState(blockpos);
      if (blockstate.getBlock() instanceof SculkBehaviour) {
         return true;
      } else {
         return !blockstate.isAir() && (!blockstate.is(Blocks.WATER) || !blockstate.getFluidState().isSource()) ? false : Direction.stream().map(blockpos::relative).anyMatch((blockpos1) -> levelaccessor.getBlockState(blockpos1).isCollisionShapeFullBlock(levelaccessor, blockpos1));
      }
   }
}
