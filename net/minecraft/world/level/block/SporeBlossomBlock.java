package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SporeBlossomBlock extends Block {
   private static final VoxelShape SHAPE = Block.box(2.0D, 13.0D, 2.0D, 14.0D, 16.0D, 14.0D);
   private static final int ADD_PARTICLE_ATTEMPTS = 14;
   private static final int PARTICLE_XZ_RADIUS = 10;
   private static final int PARTICLE_Y_MAX = 10;

   public SporeBlossomBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
   }

   public boolean canSurvive(BlockState blockstate, LevelReader levelreader, BlockPos blockpos) {
      return Block.canSupportCenter(levelreader, blockpos.above(), Direction.DOWN) && !levelreader.isWaterAt(blockpos);
   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      return direction == Direction.UP && !this.canSurvive(blockstate, levelaccessor, blockpos) ? Blocks.AIR.defaultBlockState() : super.updateShape(blockstate, direction, blockstate1, levelaccessor, blockpos, blockpos1);
   }

   public void animateTick(BlockState blockstate, Level level, BlockPos blockpos, RandomSource randomsource) {
      int i = blockpos.getX();
      int j = blockpos.getY();
      int k = blockpos.getZ();
      double d0 = (double)i + randomsource.nextDouble();
      double d1 = (double)j + 0.7D;
      double d2 = (double)k + randomsource.nextDouble();
      level.addParticle(ParticleTypes.FALLING_SPORE_BLOSSOM, d0, d1, d2, 0.0D, 0.0D, 0.0D);
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

      for(int l = 0; l < 14; ++l) {
         blockpos_mutableblockpos.set(i + Mth.nextInt(randomsource, -10, 10), j - randomsource.nextInt(10), k + Mth.nextInt(randomsource, -10, 10));
         BlockState blockstate1 = level.getBlockState(blockpos_mutableblockpos);
         if (!blockstate1.isCollisionShapeFullBlock(level, blockpos_mutableblockpos)) {
            level.addParticle(ParticleTypes.SPORE_BLOSSOM_AIR, (double)blockpos_mutableblockpos.getX() + randomsource.nextDouble(), (double)blockpos_mutableblockpos.getY() + randomsource.nextDouble(), (double)blockpos_mutableblockpos.getZ() + randomsource.nextDouble(), 0.0D, 0.0D, 0.0D);
         }
      }

   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return SHAPE;
   }
}
