package net.minecraft.world.level.block;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.frog.Tadpole;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FrogspawnBlock extends Block {
   private static final int MIN_TADPOLES_SPAWN = 2;
   private static final int MAX_TADPOLES_SPAWN = 5;
   private static final int DEFAULT_MIN_HATCH_TICK_DELAY = 3600;
   private static final int DEFAULT_MAX_HATCH_TICK_DELAY = 12000;
   protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 1.5D, 16.0D);
   private static int minHatchTickDelay = 3600;
   private static int maxHatchTickDelay = 12000;

   public FrogspawnBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return SHAPE;
   }

   public boolean canSurvive(BlockState blockstate, LevelReader levelreader, BlockPos blockpos) {
      return mayPlaceOn(levelreader, blockpos.below());
   }

   public void onPlace(BlockState blockstate, Level level, BlockPos blockpos, BlockState blockstate1, boolean flag) {
      level.scheduleTick(blockpos, this, getFrogspawnHatchDelay(level.getRandom()));
   }

   private static int getFrogspawnHatchDelay(RandomSource randomsource) {
      return randomsource.nextInt(minHatchTickDelay, maxHatchTickDelay);
   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      return !this.canSurvive(blockstate, levelaccessor, blockpos) ? Blocks.AIR.defaultBlockState() : super.updateShape(blockstate, direction, blockstate1, levelaccessor, blockpos, blockpos1);
   }

   public void tick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      if (!this.canSurvive(blockstate, serverlevel, blockpos)) {
         this.destroyBlock(serverlevel, blockpos);
      } else {
         this.hatchFrogspawn(serverlevel, blockpos, randomsource);
      }
   }

   public void entityInside(BlockState blockstate, Level level, BlockPos blockpos, Entity entity) {
      if (entity.getType().equals(EntityType.FALLING_BLOCK)) {
         this.destroyBlock(level, blockpos);
      }

   }

   private static boolean mayPlaceOn(BlockGetter blockgetter, BlockPos blockpos) {
      FluidState fluidstate = blockgetter.getFluidState(blockpos);
      FluidState fluidstate1 = blockgetter.getFluidState(blockpos.above());
      return fluidstate.getType() == Fluids.WATER && fluidstate1.getType() == Fluids.EMPTY;
   }

   private void hatchFrogspawn(ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      this.destroyBlock(serverlevel, blockpos);
      serverlevel.playSound((Player)null, blockpos, SoundEvents.FROGSPAWN_HATCH, SoundSource.BLOCKS, 1.0F, 1.0F);
      this.spawnTadpoles(serverlevel, blockpos, randomsource);
   }

   private void destroyBlock(Level level, BlockPos blockpos) {
      level.destroyBlock(blockpos, false);
   }

   private void spawnTadpoles(ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      int i = randomsource.nextInt(2, 6);

      for(int j = 1; j <= i; ++j) {
         Tadpole tadpole = EntityType.TADPOLE.create(serverlevel);
         if (tadpole != null) {
            double d0 = (double)blockpos.getX() + this.getRandomTadpolePositionOffset(randomsource);
            double d1 = (double)blockpos.getZ() + this.getRandomTadpolePositionOffset(randomsource);
            int k = randomsource.nextInt(1, 361);
            tadpole.moveTo(d0, (double)blockpos.getY() - 0.5D, d1, (float)k, 0.0F);
            tadpole.setPersistenceRequired();
            serverlevel.addFreshEntity(tadpole);
         }
      }

   }

   private double getRandomTadpolePositionOffset(RandomSource randomsource) {
      double d0 = (double)(Tadpole.HITBOX_WIDTH / 2.0F);
      return Mth.clamp(randomsource.nextDouble(), d0, 1.0D - d0);
   }

   @VisibleForTesting
   public static void setHatchDelay(int i, int j) {
      minHatchTickDelay = i;
      maxHatchTickDelay = j;
   }

   @VisibleForTesting
   public static void setDefaultHatchDelay() {
      minHatchTickDelay = 3600;
      maxHatchTickDelay = 12000;
   }
}
