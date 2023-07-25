package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SaplingBlock extends BushBlock implements BonemealableBlock {
   public static final IntegerProperty STAGE = BlockStateProperties.STAGE;
   protected static final float AABB_OFFSET = 6.0F;
   protected static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 12.0D, 14.0D);
   private final AbstractTreeGrower treeGrower;

   protected SaplingBlock(AbstractTreeGrower abstracttreegrower, BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.treeGrower = abstracttreegrower;
      this.registerDefaultState(this.stateDefinition.any().setValue(STAGE, Integer.valueOf(0)));
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return SHAPE;
   }

   public void randomTick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      if (serverlevel.getMaxLocalRawBrightness(blockpos.above()) >= 9 && randomsource.nextInt(7) == 0) {
         this.advanceTree(serverlevel, blockpos, blockstate, randomsource);
      }

   }

   public void advanceTree(ServerLevel serverlevel, BlockPos blockpos, BlockState blockstate, RandomSource randomsource) {
      if (blockstate.getValue(STAGE) == 0) {
         serverlevel.setBlock(blockpos, blockstate.cycle(STAGE), 4);
      } else {
         this.treeGrower.growTree(serverlevel, serverlevel.getChunkSource().getGenerator(), blockpos, blockstate, randomsource);
      }

   }

   public boolean isValidBonemealTarget(LevelReader levelreader, BlockPos blockpos, BlockState blockstate, boolean flag) {
      return true;
   }

   public boolean isBonemealSuccess(Level level, RandomSource randomsource, BlockPos blockpos, BlockState blockstate) {
      return (double)level.random.nextFloat() < 0.45D;
   }

   public void performBonemeal(ServerLevel serverlevel, RandomSource randomsource, BlockPos blockpos, BlockState blockstate) {
      this.advanceTree(serverlevel, blockpos, blockstate, randomsource);
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(STAGE);
   }
}
