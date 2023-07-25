package net.minecraft.world.level.block;

import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class LayeredCauldronBlock extends AbstractCauldronBlock {
   public static final int MIN_FILL_LEVEL = 1;
   public static final int MAX_FILL_LEVEL = 3;
   public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL_CAULDRON;
   private static final int BASE_CONTENT_HEIGHT = 6;
   private static final double HEIGHT_PER_LEVEL = 3.0D;
   public static final Predicate<Biome.Precipitation> RAIN = (biome_precipitation) -> biome_precipitation == Biome.Precipitation.RAIN;
   public static final Predicate<Biome.Precipitation> SNOW = (biome_precipitation) -> biome_precipitation == Biome.Precipitation.SNOW;
   private final Predicate<Biome.Precipitation> fillPredicate;

   public LayeredCauldronBlock(BlockBehaviour.Properties blockbehaviour_properties, Predicate<Biome.Precipitation> predicate, Map<Item, CauldronInteraction> map) {
      super(blockbehaviour_properties, map);
      this.fillPredicate = predicate;
      this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, Integer.valueOf(1)));
   }

   public boolean isFull(BlockState blockstate) {
      return blockstate.getValue(LEVEL) == 3;
   }

   protected boolean canReceiveStalactiteDrip(Fluid fluid) {
      return fluid == Fluids.WATER && this.fillPredicate == RAIN;
   }

   protected double getContentHeight(BlockState blockstate) {
      return (6.0D + (double)blockstate.getValue(LEVEL).intValue() * 3.0D) / 16.0D;
   }

   public void entityInside(BlockState blockstate, Level level, BlockPos blockpos, Entity entity) {
      if (!level.isClientSide && entity.isOnFire() && this.isEntityInsideContent(blockstate, blockpos, entity)) {
         entity.clearFire();
         if (entity.mayInteract(level, blockpos)) {
            this.handleEntityOnFireInside(blockstate, level, blockpos);
         }
      }

   }

   protected void handleEntityOnFireInside(BlockState blockstate, Level level, BlockPos blockpos) {
      lowerFillLevel(blockstate, level, blockpos);
   }

   public static void lowerFillLevel(BlockState blockstate, Level level, BlockPos blockpos) {
      int i = blockstate.getValue(LEVEL) - 1;
      BlockState blockstate1 = i == 0 ? Blocks.CAULDRON.defaultBlockState() : blockstate.setValue(LEVEL, Integer.valueOf(i));
      level.setBlockAndUpdate(blockpos, blockstate1);
      level.gameEvent(GameEvent.BLOCK_CHANGE, blockpos, GameEvent.Context.of(blockstate1));
   }

   public void handlePrecipitation(BlockState blockstate, Level level, BlockPos blockpos, Biome.Precipitation biome_precipitation) {
      if (CauldronBlock.shouldHandlePrecipitation(level, biome_precipitation) && blockstate.getValue(LEVEL) != 3 && this.fillPredicate.test(biome_precipitation)) {
         BlockState blockstate1 = blockstate.cycle(LEVEL);
         level.setBlockAndUpdate(blockpos, blockstate1);
         level.gameEvent(GameEvent.BLOCK_CHANGE, blockpos, GameEvent.Context.of(blockstate1));
      }
   }

   public int getAnalogOutputSignal(BlockState blockstate, Level level, BlockPos blockpos) {
      return blockstate.getValue(LEVEL);
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(LEVEL);
   }

   protected void receiveStalactiteDrip(BlockState blockstate, Level level, BlockPos blockpos, Fluid fluid) {
      if (!this.isFull(blockstate)) {
         BlockState blockstate1 = blockstate.setValue(LEVEL, Integer.valueOf(blockstate.getValue(LEVEL) + 1));
         level.setBlockAndUpdate(blockpos, blockstate1);
         level.gameEvent(GameEvent.BLOCK_CHANGE, blockpos, GameEvent.Context.of(blockstate1));
         level.levelEvent(1047, blockpos, 0);
      }
   }
}
