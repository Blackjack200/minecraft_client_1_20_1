package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class WeatheringCopperFullBlock extends Block implements WeatheringCopper {
   private final WeatheringCopper.WeatherState weatherState;

   public WeatheringCopperFullBlock(WeatheringCopper.WeatherState weatheringcopper_weatherstate, BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.weatherState = weatheringcopper_weatherstate;
   }

   public void randomTick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      this.onRandomTick(blockstate, serverlevel, blockpos, randomsource);
   }

   public boolean isRandomlyTicking(BlockState blockstate) {
      return WeatheringCopper.getNext(blockstate.getBlock()).isPresent();
   }

   public WeatheringCopper.WeatherState getAge() {
      return this.weatherState;
   }
}
