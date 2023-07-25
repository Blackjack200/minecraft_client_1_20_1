package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.grower.AzaleaTreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AzaleaBlock extends BushBlock implements BonemealableBlock {
   private static final AzaleaTreeGrower TREE_GROWER = new AzaleaTreeGrower();
   private static final VoxelShape SHAPE = Shapes.or(Block.box(0.0D, 8.0D, 0.0D, 16.0D, 16.0D, 16.0D), Block.box(6.0D, 0.0D, 6.0D, 10.0D, 8.0D, 10.0D));

   protected AzaleaBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return SHAPE;
   }

   protected boolean mayPlaceOn(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos) {
      return blockstate.is(Blocks.CLAY) || super.mayPlaceOn(blockstate, blockgetter, blockpos);
   }

   public boolean isValidBonemealTarget(LevelReader levelreader, BlockPos blockpos, BlockState blockstate, boolean flag) {
      return levelreader.getFluidState(blockpos.above()).isEmpty();
   }

   public boolean isBonemealSuccess(Level level, RandomSource randomsource, BlockPos blockpos, BlockState blockstate) {
      return (double)level.random.nextFloat() < 0.45D;
   }

   public void performBonemeal(ServerLevel serverlevel, RandomSource randomsource, BlockPos blockpos, BlockState blockstate) {
      TREE_GROWER.growTree(serverlevel, serverlevel.getChunkSource().getGenerator(), blockpos, blockstate, randomsource);
   }
}
