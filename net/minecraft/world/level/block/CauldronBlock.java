package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class CauldronBlock extends AbstractCauldronBlock {
   private static final float RAIN_FILL_CHANCE = 0.05F;
   private static final float POWDER_SNOW_FILL_CHANCE = 0.1F;

   public CauldronBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties, CauldronInteraction.EMPTY);
   }

   public boolean isFull(BlockState blockstate) {
      return false;
   }

   protected static boolean shouldHandlePrecipitation(Level level, Biome.Precipitation biome_precipitation) {
      if (biome_precipitation == Biome.Precipitation.RAIN) {
         return level.getRandom().nextFloat() < 0.05F;
      } else if (biome_precipitation == Biome.Precipitation.SNOW) {
         return level.getRandom().nextFloat() < 0.1F;
      } else {
         return false;
      }
   }

   public void handlePrecipitation(BlockState blockstate, Level level, BlockPos blockpos, Biome.Precipitation biome_precipitation) {
      if (shouldHandlePrecipitation(level, biome_precipitation)) {
         if (biome_precipitation == Biome.Precipitation.RAIN) {
            level.setBlockAndUpdate(blockpos, Blocks.WATER_CAULDRON.defaultBlockState());
            level.gameEvent((Entity)null, GameEvent.BLOCK_CHANGE, blockpos);
         } else if (biome_precipitation == Biome.Precipitation.SNOW) {
            level.setBlockAndUpdate(blockpos, Blocks.POWDER_SNOW_CAULDRON.defaultBlockState());
            level.gameEvent((Entity)null, GameEvent.BLOCK_CHANGE, blockpos);
         }

      }
   }

   protected boolean canReceiveStalactiteDrip(Fluid fluid) {
      return true;
   }

   protected void receiveStalactiteDrip(BlockState blockstate, Level level, BlockPos blockpos, Fluid fluid) {
      if (fluid == Fluids.WATER) {
         BlockState blockstate1 = Blocks.WATER_CAULDRON.defaultBlockState();
         level.setBlockAndUpdate(blockpos, blockstate1);
         level.gameEvent(GameEvent.BLOCK_CHANGE, blockpos, GameEvent.Context.of(blockstate1));
         level.levelEvent(1047, blockpos, 0);
      } else if (fluid == Fluids.LAVA) {
         BlockState blockstate2 = Blocks.LAVA_CAULDRON.defaultBlockState();
         level.setBlockAndUpdate(blockpos, blockstate2);
         level.gameEvent(GameEvent.BLOCK_CHANGE, blockpos, GameEvent.Context.of(blockstate2));
         level.levelEvent(1046, blockpos, 0);
      }

   }
}
