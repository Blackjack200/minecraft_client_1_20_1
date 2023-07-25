package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;

public class SimpleBlockFeature extends Feature<SimpleBlockConfiguration> {
   public SimpleBlockFeature(Codec<SimpleBlockConfiguration> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<SimpleBlockConfiguration> featureplacecontext) {
      SimpleBlockConfiguration simpleblockconfiguration = featureplacecontext.config();
      WorldGenLevel worldgenlevel = featureplacecontext.level();
      BlockPos blockpos = featureplacecontext.origin();
      BlockState blockstate = simpleblockconfiguration.toPlace().getState(featureplacecontext.random(), blockpos);
      if (blockstate.canSurvive(worldgenlevel, blockpos)) {
         if (blockstate.getBlock() instanceof DoublePlantBlock) {
            if (!worldgenlevel.isEmptyBlock(blockpos.above())) {
               return false;
            }

            DoublePlantBlock.placeAt(worldgenlevel, blockstate, blockpos, 2);
         } else {
            worldgenlevel.setBlock(blockpos, blockstate, 2);
         }

         return true;
      } else {
         return false;
      }
   }
}
