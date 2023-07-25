package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HangingSignBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CeilingHangingSignBlock extends SignBlock {
   public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;
   public static final BooleanProperty ATTACHED = BlockStateProperties.ATTACHED;
   protected static final float AABB_OFFSET = 5.0F;
   protected static final VoxelShape SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 16.0D, 13.0D);
   private static final Map<Integer, VoxelShape> AABBS = Maps.newHashMap(ImmutableMap.of(0, Block.box(1.0D, 0.0D, 7.0D, 15.0D, 10.0D, 9.0D), 4, Block.box(7.0D, 0.0D, 1.0D, 9.0D, 10.0D, 15.0D), 8, Block.box(1.0D, 0.0D, 7.0D, 15.0D, 10.0D, 9.0D), 12, Block.box(7.0D, 0.0D, 1.0D, 9.0D, 10.0D, 15.0D)));

   public CeilingHangingSignBlock(BlockBehaviour.Properties blockbehaviour_properties, WoodType woodtype) {
      super(blockbehaviour_properties.sound(woodtype.hangingSignSoundType()), woodtype);
      this.registerDefaultState(this.stateDefinition.any().setValue(ROTATION, Integer.valueOf(0)).setValue(ATTACHED, Boolean.valueOf(false)).setValue(WATERLOGGED, Boolean.valueOf(false)));
   }

   public InteractionResult use(BlockState blockstate, Level level, BlockPos blockpos, Player player, InteractionHand interactionhand, BlockHitResult blockhitresult) {
      BlockEntity itemstack = level.getBlockEntity(blockpos);
      if (itemstack instanceof SignBlockEntity signblockentity) {
         ItemStack itemstack = player.getItemInHand(interactionhand);
         if (this.shouldTryToChainAnotherHangingSign(player, blockhitresult, signblockentity, itemstack)) {
            return InteractionResult.PASS;
         }
      }

      return super.use(blockstate, level, blockpos, player, interactionhand, blockhitresult);
   }

   private boolean shouldTryToChainAnotherHangingSign(Player player, BlockHitResult blockhitresult, SignBlockEntity signblockentity, ItemStack itemstack) {
      return !signblockentity.canExecuteClickCommands(signblockentity.isFacingFrontText(player), player) && itemstack.getItem() instanceof HangingSignItem && blockhitresult.getDirection().equals(Direction.DOWN);
   }

   public boolean canSurvive(BlockState blockstate, LevelReader levelreader, BlockPos blockpos) {
      return levelreader.getBlockState(blockpos.above()).isFaceSturdy(levelreader, blockpos.above(), Direction.DOWN, SupportType.CENTER);
   }

   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      Level level = blockplacecontext.getLevel();
      FluidState fluidstate = level.getFluidState(blockplacecontext.getClickedPos());
      BlockPos blockpos = blockplacecontext.getClickedPos().above();
      BlockState blockstate = level.getBlockState(blockpos);
      boolean flag = blockstate.is(BlockTags.ALL_HANGING_SIGNS);
      Direction direction = Direction.fromYRot((double)blockplacecontext.getRotation());
      boolean flag1 = !Block.isFaceFull(blockstate.getCollisionShape(level, blockpos), Direction.DOWN) || blockplacecontext.isSecondaryUseActive();
      if (flag && !blockplacecontext.isSecondaryUseActive()) {
         if (blockstate.hasProperty(WallHangingSignBlock.FACING)) {
            Direction direction1 = blockstate.getValue(WallHangingSignBlock.FACING);
            if (direction1.getAxis().test(direction)) {
               flag1 = false;
            }
         } else if (blockstate.hasProperty(ROTATION)) {
            Optional<Direction> optional = RotationSegment.convertToDirection(blockstate.getValue(ROTATION));
            if (optional.isPresent() && optional.get().getAxis().test(direction)) {
               flag1 = false;
            }
         }
      }

      int i = !flag1 ? RotationSegment.convertToSegment(direction.getOpposite()) : RotationSegment.convertToSegment(blockplacecontext.getRotation() + 180.0F);
      return this.defaultBlockState().setValue(ATTACHED, Boolean.valueOf(flag1)).setValue(ROTATION, Integer.valueOf(i)).setValue(WATERLOGGED, Boolean.valueOf(fluidstate.getType() == Fluids.WATER));
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      VoxelShape voxelshape = AABBS.get(blockstate.getValue(ROTATION));
      return voxelshape == null ? SHAPE : voxelshape;
   }

   public VoxelShape getBlockSupportShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos) {
      return this.getShape(blockstate, blockgetter, blockpos, CollisionContext.empty());
   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      return direction == Direction.UP && !this.canSurvive(blockstate, levelaccessor, blockpos) ? Blocks.AIR.defaultBlockState() : super.updateShape(blockstate, direction, blockstate1, levelaccessor, blockpos, blockpos1);
   }

   public float getYRotationDegrees(BlockState blockstate) {
      return RotationSegment.convertToDegrees(blockstate.getValue(ROTATION));
   }

   public BlockState rotate(BlockState blockstate, Rotation rotation) {
      return blockstate.setValue(ROTATION, Integer.valueOf(rotation.rotate(blockstate.getValue(ROTATION), 16)));
   }

   public BlockState mirror(BlockState blockstate, Mirror mirror) {
      return blockstate.setValue(ROTATION, Integer.valueOf(mirror.mirror(blockstate.getValue(ROTATION), 16)));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(ROTATION, ATTACHED, WATERLOGGED);
   }

   public BlockEntity newBlockEntity(BlockPos blockpos, BlockState blockstate) {
      return new HangingSignBlockEntity(blockpos, blockstate);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockstate, BlockEntityType<T> blockentitytype) {
      return createTickerHelper(blockentitytype, BlockEntityType.HANGING_SIGN, SignBlockEntity::tick);
   }
}
