package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;

public class CaveVinesBlock extends GrowingPlantHeadBlock implements BonemealableBlock, CaveVines {
   private static final float CHANCE_OF_BERRIES_ON_GROWTH = 0.11F;

   public CaveVinesBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties, Direction.DOWN, SHAPE, false, 0.1D);
      this.registerDefaultState(this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)).setValue(BERRIES, Boolean.valueOf(false)));
   }

   protected int getBlocksToGrowWhenBonemealed(RandomSource randomsource) {
      return 1;
   }

   protected boolean canGrowInto(BlockState blockstate) {
      return blockstate.isAir();
   }

   protected Block getBodyBlock() {
      return Blocks.CAVE_VINES_PLANT;
   }

   protected BlockState updateBodyAfterConvertedFromHead(BlockState blockstate, BlockState blockstate1) {
      return blockstate1.setValue(BERRIES, blockstate.getValue(BERRIES));
   }

   protected BlockState getGrowIntoState(BlockState blockstate, RandomSource randomsource) {
      return super.getGrowIntoState(blockstate, randomsource).setValue(BERRIES, Boolean.valueOf(randomsource.nextFloat() < 0.11F));
   }

   public ItemStack getCloneItemStack(BlockGetter blockgetter, BlockPos blockpos, BlockState blockstate) {
      return new ItemStack(Items.GLOW_BERRIES);
   }

   public InteractionResult use(BlockState blockstate, Level level, BlockPos blockpos, Player player, InteractionHand interactionhand, BlockHitResult blockhitresult) {
      return CaveVines.use(player, blockstate, level, blockpos);
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      super.createBlockStateDefinition(statedefinition_builder);
      statedefinition_builder.add(BERRIES);
   }

   public boolean isValidBonemealTarget(LevelReader levelreader, BlockPos blockpos, BlockState blockstate, boolean flag) {
      return !blockstate.getValue(BERRIES);
   }

   public boolean isBonemealSuccess(Level level, RandomSource randomsource, BlockPos blockpos, BlockState blockstate) {
      return true;
   }

   public void performBonemeal(ServerLevel serverlevel, RandomSource randomsource, BlockPos blockpos, BlockState blockstate) {
      serverlevel.setBlock(blockpos, blockstate.setValue(BERRIES, Boolean.valueOf(true)), 2);
   }
}
