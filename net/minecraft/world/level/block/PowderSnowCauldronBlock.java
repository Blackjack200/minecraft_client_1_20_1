package net.minecraft.world.level.block;

import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class PowderSnowCauldronBlock extends LayeredCauldronBlock {
   public PowderSnowCauldronBlock(BlockBehaviour.Properties blockbehaviour_properties, Predicate<Biome.Precipitation> predicate, Map<Item, CauldronInteraction> map) {
      super(blockbehaviour_properties, predicate, map);
   }

   protected void handleEntityOnFireInside(BlockState blockstate, Level level, BlockPos blockpos) {
      lowerFillLevel(Blocks.WATER_CAULDRON.defaultBlockState().setValue(LEVEL, blockstate.getValue(LEVEL)), level, blockpos);
   }
}
