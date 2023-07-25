package net.minecraft.world.level.block;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class BaseFireBlock extends Block {
   private static final int SECONDS_ON_FIRE = 8;
   private final float fireDamage;
   protected static final float AABB_OFFSET = 1.0F;
   protected static final VoxelShape DOWN_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D);

   public BaseFireBlock(BlockBehaviour.Properties blockbehaviour_properties, float f) {
      super(blockbehaviour_properties);
      this.fireDamage = f;
   }

   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      return getState(blockplacecontext.getLevel(), blockplacecontext.getClickedPos());
   }

   public static BlockState getState(BlockGetter blockgetter, BlockPos blockpos) {
      BlockPos blockpos1 = blockpos.below();
      BlockState blockstate = blockgetter.getBlockState(blockpos1);
      return SoulFireBlock.canSurviveOnBlock(blockstate) ? Blocks.SOUL_FIRE.defaultBlockState() : ((FireBlock)Blocks.FIRE).getStateForPlacement(blockgetter, blockpos);
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return DOWN_AABB;
   }

   public void animateTick(BlockState blockstate, Level level, BlockPos blockpos, RandomSource randomsource) {
      if (randomsource.nextInt(24) == 0) {
         level.playLocalSound((double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.5D, (double)blockpos.getZ() + 0.5D, SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS, 1.0F + randomsource.nextFloat(), randomsource.nextFloat() * 0.7F + 0.3F, false);
      }

      BlockPos blockpos1 = blockpos.below();
      BlockState blockstate1 = level.getBlockState(blockpos1);
      if (!this.canBurn(blockstate1) && !blockstate1.isFaceSturdy(level, blockpos1, Direction.UP)) {
         if (this.canBurn(level.getBlockState(blockpos.west()))) {
            for(int j = 0; j < 2; ++j) {
               double d3 = (double)blockpos.getX() + randomsource.nextDouble() * (double)0.1F;
               double d4 = (double)blockpos.getY() + randomsource.nextDouble();
               double d5 = (double)blockpos.getZ() + randomsource.nextDouble();
               level.addParticle(ParticleTypes.LARGE_SMOKE, d3, d4, d5, 0.0D, 0.0D, 0.0D);
            }
         }

         if (this.canBurn(level.getBlockState(blockpos.east()))) {
            for(int k = 0; k < 2; ++k) {
               double d6 = (double)(blockpos.getX() + 1) - randomsource.nextDouble() * (double)0.1F;
               double d7 = (double)blockpos.getY() + randomsource.nextDouble();
               double d8 = (double)blockpos.getZ() + randomsource.nextDouble();
               level.addParticle(ParticleTypes.LARGE_SMOKE, d6, d7, d8, 0.0D, 0.0D, 0.0D);
            }
         }

         if (this.canBurn(level.getBlockState(blockpos.north()))) {
            for(int l = 0; l < 2; ++l) {
               double d9 = (double)blockpos.getX() + randomsource.nextDouble();
               double d10 = (double)blockpos.getY() + randomsource.nextDouble();
               double d11 = (double)blockpos.getZ() + randomsource.nextDouble() * (double)0.1F;
               level.addParticle(ParticleTypes.LARGE_SMOKE, d9, d10, d11, 0.0D, 0.0D, 0.0D);
            }
         }

         if (this.canBurn(level.getBlockState(blockpos.south()))) {
            for(int i1 = 0; i1 < 2; ++i1) {
               double d12 = (double)blockpos.getX() + randomsource.nextDouble();
               double d13 = (double)blockpos.getY() + randomsource.nextDouble();
               double d14 = (double)(blockpos.getZ() + 1) - randomsource.nextDouble() * (double)0.1F;
               level.addParticle(ParticleTypes.LARGE_SMOKE, d12, d13, d14, 0.0D, 0.0D, 0.0D);
            }
         }

         if (this.canBurn(level.getBlockState(blockpos.above()))) {
            for(int j1 = 0; j1 < 2; ++j1) {
               double d15 = (double)blockpos.getX() + randomsource.nextDouble();
               double d16 = (double)(blockpos.getY() + 1) - randomsource.nextDouble() * (double)0.1F;
               double d17 = (double)blockpos.getZ() + randomsource.nextDouble();
               level.addParticle(ParticleTypes.LARGE_SMOKE, d15, d16, d17, 0.0D, 0.0D, 0.0D);
            }
         }
      } else {
         for(int i = 0; i < 3; ++i) {
            double d0 = (double)blockpos.getX() + randomsource.nextDouble();
            double d1 = (double)blockpos.getY() + randomsource.nextDouble() * 0.5D + 0.5D;
            double d2 = (double)blockpos.getZ() + randomsource.nextDouble();
            level.addParticle(ParticleTypes.LARGE_SMOKE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
         }
      }

   }

   protected abstract boolean canBurn(BlockState blockstate);

   public void entityInside(BlockState blockstate, Level level, BlockPos blockpos, Entity entity) {
      if (!entity.fireImmune()) {
         entity.setRemainingFireTicks(entity.getRemainingFireTicks() + 1);
         if (entity.getRemainingFireTicks() == 0) {
            entity.setSecondsOnFire(8);
         }
      }

      entity.hurt(level.damageSources().inFire(), this.fireDamage);
      super.entityInside(blockstate, level, blockpos, entity);
   }

   public void onPlace(BlockState blockstate, Level level, BlockPos blockpos, BlockState blockstate1, boolean flag) {
      if (!blockstate1.is(blockstate.getBlock())) {
         if (inPortalDimension(level)) {
            Optional<PortalShape> optional = PortalShape.findEmptyPortalShape(level, blockpos, Direction.Axis.X);
            if (optional.isPresent()) {
               optional.get().createPortalBlocks();
               return;
            }
         }

         if (!blockstate.canSurvive(level, blockpos)) {
            level.removeBlock(blockpos, false);
         }

      }
   }

   private static boolean inPortalDimension(Level level) {
      return level.dimension() == Level.OVERWORLD || level.dimension() == Level.NETHER;
   }

   protected void spawnDestroyParticles(Level level, Player player, BlockPos blockpos, BlockState blockstate) {
   }

   public void playerWillDestroy(Level level, BlockPos blockpos, BlockState blockstate, Player player) {
      if (!level.isClientSide()) {
         level.levelEvent((Player)null, 1009, blockpos, 0);
      }

      super.playerWillDestroy(level, blockpos, blockstate, player);
   }

   public static boolean canBePlacedAt(Level level, BlockPos blockpos, Direction direction) {
      BlockState blockstate = level.getBlockState(blockpos);
      if (!blockstate.isAir()) {
         return false;
      } else {
         return getState(level, blockpos).canSurvive(level, blockpos) || isPortal(level, blockpos, direction);
      }
   }

   private static boolean isPortal(Level level, BlockPos blockpos, Direction direction) {
      if (!inPortalDimension(level)) {
         return false;
      } else {
         BlockPos.MutableBlockPos blockpos_mutableblockpos = blockpos.mutable();
         boolean flag = false;

         for(Direction direction1 : Direction.values()) {
            if (level.getBlockState(blockpos_mutableblockpos.set(blockpos).move(direction1)).is(Blocks.OBSIDIAN)) {
               flag = true;
               break;
            }
         }

         if (!flag) {
            return false;
         } else {
            Direction.Axis direction_axis = direction.getAxis().isHorizontal() ? direction.getCounterClockWise().getAxis() : Direction.Plane.HORIZONTAL.getRandomAxis(level.random);
            return PortalShape.findEmptyPortalShape(level, blockpos, direction_axis).isPresent();
         }
      }
   }
}
