package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FarmBlock extends Block {
   public static final IntegerProperty MOISTURE = BlockStateProperties.MOISTURE;
   protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 15.0D, 16.0D);
   public static final int MAX_MOISTURE = 7;

   protected FarmBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(MOISTURE, Integer.valueOf(0)));
   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      if (direction == Direction.UP && !blockstate.canSurvive(levelaccessor, blockpos)) {
         levelaccessor.scheduleTick(blockpos, this, 1);
      }

      return super.updateShape(blockstate, direction, blockstate1, levelaccessor, blockpos, blockpos1);
   }

   public boolean canSurvive(BlockState blockstate, LevelReader levelreader, BlockPos blockpos) {
      BlockState blockstate1 = levelreader.getBlockState(blockpos.above());
      return !blockstate1.isSolid() || blockstate1.getBlock() instanceof FenceGateBlock || blockstate1.getBlock() instanceof MovingPistonBlock;
   }

   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      return !this.defaultBlockState().canSurvive(blockplacecontext.getLevel(), blockplacecontext.getClickedPos()) ? Blocks.DIRT.defaultBlockState() : super.getStateForPlacement(blockplacecontext);
   }

   public boolean useShapeForLightOcclusion(BlockState blockstate) {
      return true;
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return SHAPE;
   }

   public void tick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      if (!blockstate.canSurvive(serverlevel, blockpos)) {
         turnToDirt((Entity)null, blockstate, serverlevel, blockpos);
      }

   }

   public void randomTick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      int i = blockstate.getValue(MOISTURE);
      if (!isNearWater(serverlevel, blockpos) && !serverlevel.isRainingAt(blockpos.above())) {
         if (i > 0) {
            serverlevel.setBlock(blockpos, blockstate.setValue(MOISTURE, Integer.valueOf(i - 1)), 2);
         } else if (!shouldMaintainFarmland(serverlevel, blockpos)) {
            turnToDirt((Entity)null, blockstate, serverlevel, blockpos);
         }
      } else if (i < 7) {
         serverlevel.setBlock(blockpos, blockstate.setValue(MOISTURE, Integer.valueOf(7)), 2);
      }

   }

   public void fallOn(Level level, BlockState blockstate, BlockPos blockpos, Entity entity, float f) {
      if (!level.isClientSide && level.random.nextFloat() < f - 0.5F && entity instanceof LivingEntity && (entity instanceof Player || level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) && entity.getBbWidth() * entity.getBbWidth() * entity.getBbHeight() > 0.512F) {
         turnToDirt(entity, blockstate, level, blockpos);
      }

      super.fallOn(level, blockstate, blockpos, entity, f);
   }

   public static void turnToDirt(@Nullable Entity entity, BlockState blockstate, Level level, BlockPos blockpos) {
      BlockState blockstate1 = pushEntitiesUp(blockstate, Blocks.DIRT.defaultBlockState(), level, blockpos);
      level.setBlockAndUpdate(blockpos, blockstate1);
      level.gameEvent(GameEvent.BLOCK_CHANGE, blockpos, GameEvent.Context.of(entity, blockstate1));
   }

   private static boolean shouldMaintainFarmland(BlockGetter blockgetter, BlockPos blockpos) {
      return blockgetter.getBlockState(blockpos.above()).is(BlockTags.MAINTAINS_FARMLAND);
   }

   private static boolean isNearWater(LevelReader levelreader, BlockPos blockpos) {
      for(BlockPos blockpos1 : BlockPos.betweenClosed(blockpos.offset(-4, 0, -4), blockpos.offset(4, 1, 4))) {
         if (levelreader.getFluidState(blockpos1).is(FluidTags.WATER)) {
            return true;
         }
      }

      return false;
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(MOISTURE);
   }

   public boolean isPathfindable(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, PathComputationType pathcomputationtype) {
      return false;
   }
}
