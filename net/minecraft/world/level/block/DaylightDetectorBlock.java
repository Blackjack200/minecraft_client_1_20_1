package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.DaylightDetectorBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DaylightDetectorBlock extends BaseEntityBlock {
   public static final IntegerProperty POWER = BlockStateProperties.POWER;
   public static final BooleanProperty INVERTED = BlockStateProperties.INVERTED;
   protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 6.0D, 16.0D);

   public DaylightDetectorBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(POWER, Integer.valueOf(0)).setValue(INVERTED, Boolean.valueOf(false)));
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return SHAPE;
   }

   public boolean useShapeForLightOcclusion(BlockState blockstate) {
      return true;
   }

   public int getSignal(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, Direction direction) {
      return blockstate.getValue(POWER);
   }

   private static void updateSignalStrength(BlockState blockstate, Level level, BlockPos blockpos) {
      int i = level.getBrightness(LightLayer.SKY, blockpos) - level.getSkyDarken();
      float f = level.getSunAngle(1.0F);
      boolean flag = blockstate.getValue(INVERTED);
      if (flag) {
         i = 15 - i;
      } else if (i > 0) {
         float f1 = f < (float)Math.PI ? 0.0F : ((float)Math.PI * 2F);
         f += (f1 - f) * 0.2F;
         i = Math.round((float)i * Mth.cos(f));
      }

      i = Mth.clamp(i, 0, 15);
      if (blockstate.getValue(POWER) != i) {
         level.setBlock(blockpos, blockstate.setValue(POWER, Integer.valueOf(i)), 3);
      }

   }

   public InteractionResult use(BlockState blockstate, Level level, BlockPos blockpos, Player player, InteractionHand interactionhand, BlockHitResult blockhitresult) {
      if (player.mayBuild()) {
         if (level.isClientSide) {
            return InteractionResult.SUCCESS;
         } else {
            BlockState blockstate1 = blockstate.cycle(INVERTED);
            level.setBlock(blockpos, blockstate1, 4);
            level.gameEvent(GameEvent.BLOCK_CHANGE, blockpos, GameEvent.Context.of(player, blockstate1));
            updateSignalStrength(blockstate1, level, blockpos);
            return InteractionResult.CONSUME;
         }
      } else {
         return super.use(blockstate, level, blockpos, player, interactionhand, blockhitresult);
      }
   }

   public RenderShape getRenderShape(BlockState blockstate) {
      return RenderShape.MODEL;
   }

   public boolean isSignalSource(BlockState blockstate) {
      return true;
   }

   public BlockEntity newBlockEntity(BlockPos blockpos, BlockState blockstate) {
      return new DaylightDetectorBlockEntity(blockpos, blockstate);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockstate, BlockEntityType<T> blockentitytype) {
      return !level.isClientSide && level.dimensionType().hasSkyLight() ? createTickerHelper(blockentitytype, BlockEntityType.DAYLIGHT_DETECTOR, DaylightDetectorBlock::tickEntity) : null;
   }

   private static void tickEntity(Level level1, BlockPos blockpos, BlockState blockstate1, DaylightDetectorBlockEntity daylightdetectorblockentity) {
      if (level1.getGameTime() % 20L == 0L) {
         updateSignalStrength(blockstate1, level1, blockpos);
      }

   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(POWER, INVERTED);
   }
}
