package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class FallingBlock extends Block implements Fallable {
   public FallingBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
   }

   public void onPlace(BlockState blockstate, Level level, BlockPos blockpos, BlockState blockstate1, boolean flag) {
      level.scheduleTick(blockpos, this, this.getDelayAfterPlace());
   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      levelaccessor.scheduleTick(blockpos, this, this.getDelayAfterPlace());
      return super.updateShape(blockstate, direction, blockstate1, levelaccessor, blockpos, blockpos1);
   }

   public void tick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      if (isFree(serverlevel.getBlockState(blockpos.below())) && blockpos.getY() >= serverlevel.getMinBuildHeight()) {
         FallingBlockEntity fallingblockentity = FallingBlockEntity.fall(serverlevel, blockpos, blockstate);
         this.falling(fallingblockentity);
      }
   }

   protected void falling(FallingBlockEntity fallingblockentity) {
   }

   protected int getDelayAfterPlace() {
      return 2;
   }

   public static boolean isFree(BlockState blockstate) {
      return blockstate.isAir() || blockstate.is(BlockTags.FIRE) || blockstate.liquid() || blockstate.canBeReplaced();
   }

   public void animateTick(BlockState blockstate, Level level, BlockPos blockpos, RandomSource randomsource) {
      if (randomsource.nextInt(16) == 0) {
         BlockPos blockpos1 = blockpos.below();
         if (isFree(level.getBlockState(blockpos1))) {
            ParticleUtils.spawnParticleBelow(level, blockpos, randomsource, new BlockParticleOption(ParticleTypes.FALLING_DUST, blockstate));
         }
      }

   }

   public int getDustColor(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos) {
      return -16777216;
   }
}
