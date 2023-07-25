package net.minecraft.world.level.block;

import java.util.List;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class DecoratedPotBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
   public static final ResourceLocation SHERDS_DYNAMIC_DROP_ID = new ResourceLocation("sherds");
   private static final VoxelShape BOUNDING_BOX = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);
   private static final DirectionProperty HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;
   private static final BooleanProperty CRACKED = BlockStateProperties.CRACKED;
   private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

   protected DecoratedPotBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(HORIZONTAL_FACING, Direction.NORTH).setValue(WATERLOGGED, Boolean.valueOf(false)).setValue(CRACKED, Boolean.valueOf(false)));
   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      if (blockstate.getValue(WATERLOGGED)) {
         levelaccessor.scheduleTick(blockpos, Fluids.WATER, Fluids.WATER.getTickDelay(levelaccessor));
      }

      return super.updateShape(blockstate, direction, blockstate1, levelaccessor, blockpos, blockpos1);
   }

   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      FluidState fluidstate = blockplacecontext.getLevel().getFluidState(blockplacecontext.getClickedPos());
      return this.defaultBlockState().setValue(HORIZONTAL_FACING, blockplacecontext.getHorizontalDirection()).setValue(WATERLOGGED, Boolean.valueOf(fluidstate.getType() == Fluids.WATER)).setValue(CRACKED, Boolean.valueOf(false));
   }

   public boolean isPathfindable(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, PathComputationType pathcomputationtype) {
      return false;
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return BOUNDING_BOX;
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(HORIZONTAL_FACING, WATERLOGGED, CRACKED);
   }

   public @Nullable BlockEntity newBlockEntity(BlockPos blockpos, BlockState blockstate) {
      return new DecoratedPotBlockEntity(blockpos, blockstate);
   }

   public List<ItemStack> getDrops(BlockState blockstate, LootParams.Builder lootparams_builder) {
      BlockEntity blockentity = lootparams_builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
      if (blockentity instanceof DecoratedPotBlockEntity decoratedpotblockentity) {
         lootparams_builder.withDynamicDrop(SHERDS_DYNAMIC_DROP_ID, (consumer) -> decoratedpotblockentity.getDecorations().sorted().map(Item::getDefaultInstance).forEach(consumer));
      }

      return super.getDrops(blockstate, lootparams_builder);
   }

   public void playerWillDestroy(Level level, BlockPos blockpos, BlockState blockstate, Player player) {
      ItemStack itemstack = player.getMainHandItem();
      BlockState blockstate1 = blockstate;
      if (itemstack.is(ItemTags.BREAKS_DECORATED_POTS) && !EnchantmentHelper.hasSilkTouch(itemstack)) {
         blockstate1 = blockstate.setValue(CRACKED, Boolean.valueOf(true));
         level.setBlock(blockpos, blockstate1, 4);
      }

      super.playerWillDestroy(level, blockpos, blockstate1, player);
   }

   public FluidState getFluidState(BlockState blockstate) {
      return blockstate.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockstate);
   }

   public SoundType getSoundType(BlockState blockstate) {
      return blockstate.getValue(CRACKED) ? SoundType.DECORATED_POT_CRACKED : SoundType.DECORATED_POT;
   }

   public void appendHoverText(ItemStack itemstack, @Nullable BlockGetter blockgetter, List<Component> list, TooltipFlag tooltipflag) {
      super.appendHoverText(itemstack, blockgetter, list, tooltipflag);
      DecoratedPotBlockEntity.Decorations decoratedpotblockentity_decorations = DecoratedPotBlockEntity.Decorations.load(BlockItem.getBlockEntityData(itemstack));
      if (!decoratedpotblockentity_decorations.equals(DecoratedPotBlockEntity.Decorations.EMPTY)) {
         list.add(CommonComponents.EMPTY);
         Stream.of(decoratedpotblockentity_decorations.front(), decoratedpotblockentity_decorations.left(), decoratedpotblockentity_decorations.right(), decoratedpotblockentity_decorations.back()).forEach((item) -> list.add((new ItemStack(item, 1)).getHoverName().plainCopy().withStyle(ChatFormatting.GRAY)));
      }
   }
}
