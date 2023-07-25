package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CandleCakeBlock extends AbstractCandleBlock {
   public static final BooleanProperty LIT = AbstractCandleBlock.LIT;
   protected static final float AABB_OFFSET = 1.0F;
   protected static final VoxelShape CAKE_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 8.0D, 15.0D);
   protected static final VoxelShape CANDLE_SHAPE = Block.box(7.0D, 8.0D, 7.0D, 9.0D, 14.0D, 9.0D);
   protected static final VoxelShape SHAPE = Shapes.or(CAKE_SHAPE, CANDLE_SHAPE);
   private static final Map<Block, CandleCakeBlock> BY_CANDLE = Maps.newHashMap();
   private static final Iterable<Vec3> PARTICLE_OFFSETS = ImmutableList.of(new Vec3(0.5D, 1.0D, 0.5D));

   protected CandleCakeBlock(Block block, BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(LIT, Boolean.valueOf(false)));
      BY_CANDLE.put(block, this);
   }

   protected Iterable<Vec3> getParticleOffsets(BlockState blockstate) {
      return PARTICLE_OFFSETS;
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return SHAPE;
   }

   public InteractionResult use(BlockState blockstate, Level level, BlockPos blockpos, Player player, InteractionHand interactionhand, BlockHitResult blockhitresult) {
      ItemStack itemstack = player.getItemInHand(interactionhand);
      if (!itemstack.is(Items.FLINT_AND_STEEL) && !itemstack.is(Items.FIRE_CHARGE)) {
         if (candleHit(blockhitresult) && player.getItemInHand(interactionhand).isEmpty() && blockstate.getValue(LIT)) {
            extinguish(player, blockstate, level, blockpos);
            return InteractionResult.sidedSuccess(level.isClientSide);
         } else {
            InteractionResult interactionresult = CakeBlock.eat(level, blockpos, Blocks.CAKE.defaultBlockState(), player);
            if (interactionresult.consumesAction()) {
               dropResources(blockstate, level, blockpos);
            }

            return interactionresult;
         }
      } else {
         return InteractionResult.PASS;
      }
   }

   private static boolean candleHit(BlockHitResult blockhitresult) {
      return blockhitresult.getLocation().y - (double)blockhitresult.getBlockPos().getY() > 0.5D;
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(LIT);
   }

   public ItemStack getCloneItemStack(BlockGetter blockgetter, BlockPos blockpos, BlockState blockstate) {
      return new ItemStack(Blocks.CAKE);
   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      return direction == Direction.DOWN && !blockstate.canSurvive(levelaccessor, blockpos) ? Blocks.AIR.defaultBlockState() : super.updateShape(blockstate, direction, blockstate1, levelaccessor, blockpos, blockpos1);
   }

   public boolean canSurvive(BlockState blockstate, LevelReader levelreader, BlockPos blockpos) {
      return levelreader.getBlockState(blockpos.below()).isSolid();
   }

   public int getAnalogOutputSignal(BlockState blockstate, Level level, BlockPos blockpos) {
      return CakeBlock.FULL_CAKE_SIGNAL;
   }

   public boolean hasAnalogOutputSignal(BlockState blockstate) {
      return true;
   }

   public boolean isPathfindable(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, PathComputationType pathcomputationtype) {
      return false;
   }

   public static BlockState byCandle(Block block) {
      return BY_CANDLE.get(block).defaultBlockState();
   }

   public static boolean canLight(BlockState blockstate) {
      return blockstate.is(BlockTags.CANDLE_CAKES, (blockbehaviour_blockstatebase) -> blockbehaviour_blockstatebase.hasProperty(LIT) && !blockstate.getValue(LIT));
   }
}
