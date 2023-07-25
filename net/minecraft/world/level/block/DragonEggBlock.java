package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DragonEggBlock extends FallingBlock {
   protected static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);

   public DragonEggBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return SHAPE;
   }

   public InteractionResult use(BlockState blockstate, Level level, BlockPos blockpos, Player player, InteractionHand interactionhand, BlockHitResult blockhitresult) {
      this.teleport(blockstate, level, blockpos);
      return InteractionResult.sidedSuccess(level.isClientSide);
   }

   public void attack(BlockState blockstate, Level level, BlockPos blockpos, Player player) {
      this.teleport(blockstate, level, blockpos);
   }

   private void teleport(BlockState blockstate, Level level, BlockPos blockpos) {
      WorldBorder worldborder = level.getWorldBorder();

      for(int i = 0; i < 1000; ++i) {
         BlockPos blockpos1 = blockpos.offset(level.random.nextInt(16) - level.random.nextInt(16), level.random.nextInt(8) - level.random.nextInt(8), level.random.nextInt(16) - level.random.nextInt(16));
         if (level.getBlockState(blockpos1).isAir() && worldborder.isWithinBounds(blockpos1)) {
            if (level.isClientSide) {
               for(int j = 0; j < 128; ++j) {
                  double d0 = level.random.nextDouble();
                  float f = (level.random.nextFloat() - 0.5F) * 0.2F;
                  float f1 = (level.random.nextFloat() - 0.5F) * 0.2F;
                  float f2 = (level.random.nextFloat() - 0.5F) * 0.2F;
                  double d1 = Mth.lerp(d0, (double)blockpos1.getX(), (double)blockpos.getX()) + (level.random.nextDouble() - 0.5D) + 0.5D;
                  double d2 = Mth.lerp(d0, (double)blockpos1.getY(), (double)blockpos.getY()) + level.random.nextDouble() - 0.5D;
                  double d3 = Mth.lerp(d0, (double)blockpos1.getZ(), (double)blockpos.getZ()) + (level.random.nextDouble() - 0.5D) + 0.5D;
                  level.addParticle(ParticleTypes.PORTAL, d1, d2, d3, (double)f, (double)f1, (double)f2);
               }
            } else {
               level.setBlock(blockpos1, blockstate, 2);
               level.removeBlock(blockpos, false);
            }

            return;
         }
      }

   }

   protected int getDelayAfterPlace() {
      return 5;
   }

   public boolean isPathfindable(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, PathComputationType pathcomputationtype) {
      return false;
   }
}
