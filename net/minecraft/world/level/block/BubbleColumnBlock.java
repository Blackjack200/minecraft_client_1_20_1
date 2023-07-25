package net.minecraft.world.level.block;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BubbleColumnBlock extends Block implements BucketPickup {
   public static final BooleanProperty DRAG_DOWN = BlockStateProperties.DRAG;
   private static final int CHECK_PERIOD = 5;

   public BubbleColumnBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(DRAG_DOWN, Boolean.valueOf(true)));
   }

   public void entityInside(BlockState blockstate, Level level, BlockPos blockpos, Entity entity) {
      BlockState blockstate1 = level.getBlockState(blockpos.above());
      if (blockstate1.isAir()) {
         entity.onAboveBubbleCol(blockstate.getValue(DRAG_DOWN));
         if (!level.isClientSide) {
            ServerLevel serverlevel = (ServerLevel)level;

            for(int i = 0; i < 2; ++i) {
               serverlevel.sendParticles(ParticleTypes.SPLASH, (double)blockpos.getX() + level.random.nextDouble(), (double)(blockpos.getY() + 1), (double)blockpos.getZ() + level.random.nextDouble(), 1, 0.0D, 0.0D, 0.0D, 1.0D);
               serverlevel.sendParticles(ParticleTypes.BUBBLE, (double)blockpos.getX() + level.random.nextDouble(), (double)(blockpos.getY() + 1), (double)blockpos.getZ() + level.random.nextDouble(), 1, 0.0D, 0.01D, 0.0D, 0.2D);
            }
         }
      } else {
         entity.onInsideBubbleColumn(blockstate.getValue(DRAG_DOWN));
      }

   }

   public void tick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      updateColumn(serverlevel, blockpos, blockstate, serverlevel.getBlockState(blockpos.below()));
   }

   public FluidState getFluidState(BlockState blockstate) {
      return Fluids.WATER.getSource(false);
   }

   public static void updateColumn(LevelAccessor levelaccessor, BlockPos blockpos, BlockState blockstate) {
      updateColumn(levelaccessor, blockpos, levelaccessor.getBlockState(blockpos), blockstate);
   }

   public static void updateColumn(LevelAccessor levelaccessor, BlockPos blockpos, BlockState blockstate, BlockState blockstate1) {
      if (canExistIn(blockstate)) {
         BlockState blockstate2 = getColumnState(blockstate1);
         levelaccessor.setBlock(blockpos, blockstate2, 2);
         BlockPos.MutableBlockPos blockpos_mutableblockpos = blockpos.mutable().move(Direction.UP);

         while(canExistIn(levelaccessor.getBlockState(blockpos_mutableblockpos))) {
            if (!levelaccessor.setBlock(blockpos_mutableblockpos, blockstate2, 2)) {
               return;
            }

            blockpos_mutableblockpos.move(Direction.UP);
         }

      }
   }

   private static boolean canExistIn(BlockState blockstate) {
      return blockstate.is(Blocks.BUBBLE_COLUMN) || blockstate.is(Blocks.WATER) && blockstate.getFluidState().getAmount() >= 8 && blockstate.getFluidState().isSource();
   }

   private static BlockState getColumnState(BlockState blockstate) {
      if (blockstate.is(Blocks.BUBBLE_COLUMN)) {
         return blockstate;
      } else if (blockstate.is(Blocks.SOUL_SAND)) {
         return Blocks.BUBBLE_COLUMN.defaultBlockState().setValue(DRAG_DOWN, Boolean.valueOf(false));
      } else {
         return blockstate.is(Blocks.MAGMA_BLOCK) ? Blocks.BUBBLE_COLUMN.defaultBlockState().setValue(DRAG_DOWN, Boolean.valueOf(true)) : Blocks.WATER.defaultBlockState();
      }
   }

   public void animateTick(BlockState blockstate, Level level, BlockPos blockpos, RandomSource randomsource) {
      double d0 = (double)blockpos.getX();
      double d1 = (double)blockpos.getY();
      double d2 = (double)blockpos.getZ();
      if (blockstate.getValue(DRAG_DOWN)) {
         level.addAlwaysVisibleParticle(ParticleTypes.CURRENT_DOWN, d0 + 0.5D, d1 + 0.8D, d2, 0.0D, 0.0D, 0.0D);
         if (randomsource.nextInt(200) == 0) {
            level.playLocalSound(d0, d1, d2, SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_AMBIENT, SoundSource.BLOCKS, 0.2F + randomsource.nextFloat() * 0.2F, 0.9F + randomsource.nextFloat() * 0.15F, false);
         }
      } else {
         level.addAlwaysVisibleParticle(ParticleTypes.BUBBLE_COLUMN_UP, d0 + 0.5D, d1, d2 + 0.5D, 0.0D, 0.04D, 0.0D);
         level.addAlwaysVisibleParticle(ParticleTypes.BUBBLE_COLUMN_UP, d0 + (double)randomsource.nextFloat(), d1 + (double)randomsource.nextFloat(), d2 + (double)randomsource.nextFloat(), 0.0D, 0.04D, 0.0D);
         if (randomsource.nextInt(200) == 0) {
            level.playLocalSound(d0, d1, d2, SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundSource.BLOCKS, 0.2F + randomsource.nextFloat() * 0.2F, 0.9F + randomsource.nextFloat() * 0.15F, false);
         }
      }

   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      levelaccessor.scheduleTick(blockpos, Fluids.WATER, Fluids.WATER.getTickDelay(levelaccessor));
      if (!blockstate.canSurvive(levelaccessor, blockpos) || direction == Direction.DOWN || direction == Direction.UP && !blockstate1.is(Blocks.BUBBLE_COLUMN) && canExistIn(blockstate1)) {
         levelaccessor.scheduleTick(blockpos, this, 5);
      }

      return super.updateShape(blockstate, direction, blockstate1, levelaccessor, blockpos, blockpos1);
   }

   public boolean canSurvive(BlockState blockstate, LevelReader levelreader, BlockPos blockpos) {
      BlockState blockstate1 = levelreader.getBlockState(blockpos.below());
      return blockstate1.is(Blocks.BUBBLE_COLUMN) || blockstate1.is(Blocks.MAGMA_BLOCK) || blockstate1.is(Blocks.SOUL_SAND);
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return Shapes.empty();
   }

   public RenderShape getRenderShape(BlockState blockstate) {
      return RenderShape.INVISIBLE;
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(DRAG_DOWN);
   }

   public ItemStack pickupBlock(LevelAccessor levelaccessor, BlockPos blockpos, BlockState blockstate) {
      levelaccessor.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 11);
      return new ItemStack(Items.WATER_BUCKET);
   }

   public Optional<SoundEvent> getPickupSound() {
      return Fluids.WATER.getPickupSound();
   }
}
