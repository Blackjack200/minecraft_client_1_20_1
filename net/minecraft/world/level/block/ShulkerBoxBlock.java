package net.minecraft.world.level.block;

import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ShulkerBoxBlock extends BaseEntityBlock {
   private static final float OPEN_AABB_SIZE = 1.0F;
   private static final VoxelShape UP_OPEN_AABB = Block.box(0.0D, 15.0D, 0.0D, 16.0D, 16.0D, 16.0D);
   private static final VoxelShape DOWN_OPEN_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D);
   private static final VoxelShape WES_OPEN_AABB = Block.box(0.0D, 0.0D, 0.0D, 1.0D, 16.0D, 16.0D);
   private static final VoxelShape EAST_OPEN_AABB = Block.box(15.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
   private static final VoxelShape NORTH_OPEN_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 1.0D);
   private static final VoxelShape SOUTH_OPEN_AABB = Block.box(0.0D, 0.0D, 15.0D, 16.0D, 16.0D, 16.0D);
   private static final Map<Direction, VoxelShape> OPEN_SHAPE_BY_DIRECTION = Util.make(Maps.newEnumMap(Direction.class), (enummap) -> {
      enummap.put(Direction.NORTH, NORTH_OPEN_AABB);
      enummap.put(Direction.EAST, EAST_OPEN_AABB);
      enummap.put(Direction.SOUTH, SOUTH_OPEN_AABB);
      enummap.put(Direction.WEST, WES_OPEN_AABB);
      enummap.put(Direction.UP, UP_OPEN_AABB);
      enummap.put(Direction.DOWN, DOWN_OPEN_AABB);
   });
   public static final EnumProperty<Direction> FACING = DirectionalBlock.FACING;
   public static final ResourceLocation CONTENTS = new ResourceLocation("contents");
   @Nullable
   private final DyeColor color;

   public ShulkerBoxBlock(@Nullable DyeColor dyecolor, BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.color = dyecolor;
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
   }

   public BlockEntity newBlockEntity(BlockPos blockpos, BlockState blockstate) {
      return new ShulkerBoxBlockEntity(this.color, blockpos, blockstate);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockstate, BlockEntityType<T> blockentitytype) {
      return createTickerHelper(blockentitytype, BlockEntityType.SHULKER_BOX, ShulkerBoxBlockEntity::tick);
   }

   public RenderShape getRenderShape(BlockState blockstate) {
      return RenderShape.ENTITYBLOCK_ANIMATED;
   }

   public InteractionResult use(BlockState blockstate, Level level, BlockPos blockpos, Player player, InteractionHand interactionhand, BlockHitResult blockhitresult) {
      if (level.isClientSide) {
         return InteractionResult.SUCCESS;
      } else if (player.isSpectator()) {
         return InteractionResult.CONSUME;
      } else {
         BlockEntity blockentity = level.getBlockEntity(blockpos);
         if (blockentity instanceof ShulkerBoxBlockEntity) {
            ShulkerBoxBlockEntity shulkerboxblockentity = (ShulkerBoxBlockEntity)blockentity;
            if (canOpen(blockstate, level, blockpos, shulkerboxblockentity)) {
               player.openMenu(shulkerboxblockentity);
               player.awardStat(Stats.OPEN_SHULKER_BOX);
               PiglinAi.angerNearbyPiglins(player, true);
            }

            return InteractionResult.CONSUME;
         } else {
            return InteractionResult.PASS;
         }
      }
   }

   private static boolean canOpen(BlockState blockstate, Level level, BlockPos blockpos, ShulkerBoxBlockEntity shulkerboxblockentity) {
      if (shulkerboxblockentity.getAnimationStatus() != ShulkerBoxBlockEntity.AnimationStatus.CLOSED) {
         return true;
      } else {
         AABB aabb = Shulker.getProgressDeltaAabb(blockstate.getValue(FACING), 0.0F, 0.5F).move(blockpos).deflate(1.0E-6D);
         return level.noCollision(aabb);
      }
   }

   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      return this.defaultBlockState().setValue(FACING, blockplacecontext.getClickedFace());
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(FACING);
   }

   public void playerWillDestroy(Level level, BlockPos blockpos, BlockState blockstate, Player player) {
      BlockEntity blockentity = level.getBlockEntity(blockpos);
      if (blockentity instanceof ShulkerBoxBlockEntity shulkerboxblockentity) {
         if (!level.isClientSide && player.isCreative() && !shulkerboxblockentity.isEmpty()) {
            ItemStack itemstack = getColoredItemStack(this.getColor());
            blockentity.saveToItem(itemstack);
            if (shulkerboxblockentity.hasCustomName()) {
               itemstack.setHoverName(shulkerboxblockentity.getCustomName());
            }

            ItemEntity itementity = new ItemEntity(level, (double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.5D, (double)blockpos.getZ() + 0.5D, itemstack);
            itementity.setDefaultPickUpDelay();
            level.addFreshEntity(itementity);
         } else {
            shulkerboxblockentity.unpackLootTable(player);
         }
      }

      super.playerWillDestroy(level, blockpos, blockstate, player);
   }

   public List<ItemStack> getDrops(BlockState blockstate, LootParams.Builder lootparams_builder) {
      BlockEntity blockentity = lootparams_builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
      if (blockentity instanceof ShulkerBoxBlockEntity shulkerboxblockentity) {
         lootparams_builder = lootparams_builder.withDynamicDrop(CONTENTS, (consumer) -> {
            for(int i = 0; i < shulkerboxblockentity.getContainerSize(); ++i) {
               consumer.accept(shulkerboxblockentity.getItem(i));
            }

         });
      }

      return super.getDrops(blockstate, lootparams_builder);
   }

   public void setPlacedBy(Level level, BlockPos blockpos, BlockState blockstate, LivingEntity livingentity, ItemStack itemstack) {
      if (itemstack.hasCustomHoverName()) {
         BlockEntity blockentity = level.getBlockEntity(blockpos);
         if (blockentity instanceof ShulkerBoxBlockEntity) {
            ((ShulkerBoxBlockEntity)blockentity).setCustomName(itemstack.getHoverName());
         }
      }

   }

   public void onRemove(BlockState blockstate, Level level, BlockPos blockpos, BlockState blockstate1, boolean flag) {
      if (!blockstate.is(blockstate1.getBlock())) {
         BlockEntity blockentity = level.getBlockEntity(blockpos);
         if (blockentity instanceof ShulkerBoxBlockEntity) {
            level.updateNeighbourForOutputSignal(blockpos, blockstate.getBlock());
         }

         super.onRemove(blockstate, level, blockpos, blockstate1, flag);
      }
   }

   public void appendHoverText(ItemStack itemstack, @Nullable BlockGetter blockgetter, List<Component> list, TooltipFlag tooltipflag) {
      super.appendHoverText(itemstack, blockgetter, list, tooltipflag);
      CompoundTag compoundtag = BlockItem.getBlockEntityData(itemstack);
      if (compoundtag != null) {
         if (compoundtag.contains("LootTable", 8)) {
            list.add(Component.literal("???????"));
         }

         if (compoundtag.contains("Items", 9)) {
            NonNullList<ItemStack> nonnulllist = NonNullList.withSize(27, ItemStack.EMPTY);
            ContainerHelper.loadAllItems(compoundtag, nonnulllist);
            int i = 0;
            int j = 0;

            for(ItemStack itemstack1 : nonnulllist) {
               if (!itemstack1.isEmpty()) {
                  ++j;
                  if (i <= 4) {
                     ++i;
                     MutableComponent mutablecomponent = itemstack1.getHoverName().copy();
                     mutablecomponent.append(" x").append(String.valueOf(itemstack1.getCount()));
                     list.add(mutablecomponent);
                  }
               }
            }

            if (j - i > 0) {
               list.add(Component.translatable("container.shulkerBox.more", j - i).withStyle(ChatFormatting.ITALIC));
            }
         }
      }

   }

   public VoxelShape getBlockSupportShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos) {
      BlockEntity blockentity = blockgetter.getBlockEntity(blockpos);
      if (blockentity instanceof ShulkerBoxBlockEntity shulkerboxblockentity) {
         if (!shulkerboxblockentity.isClosed()) {
            return OPEN_SHAPE_BY_DIRECTION.get(blockstate.getValue(FACING).getOpposite());
         }
      }

      return Shapes.block();
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      BlockEntity blockentity = blockgetter.getBlockEntity(blockpos);
      return blockentity instanceof ShulkerBoxBlockEntity ? Shapes.create(((ShulkerBoxBlockEntity)blockentity).getBoundingBox(blockstate)) : Shapes.block();
   }

   public boolean hasAnalogOutputSignal(BlockState blockstate) {
      return true;
   }

   public int getAnalogOutputSignal(BlockState blockstate, Level level, BlockPos blockpos) {
      return AbstractContainerMenu.getRedstoneSignalFromContainer((Container)level.getBlockEntity(blockpos));
   }

   public ItemStack getCloneItemStack(BlockGetter blockgetter, BlockPos blockpos, BlockState blockstate) {
      ItemStack itemstack = super.getCloneItemStack(blockgetter, blockpos, blockstate);
      blockgetter.getBlockEntity(blockpos, BlockEntityType.SHULKER_BOX).ifPresent((shulkerboxblockentity) -> shulkerboxblockentity.saveToItem(itemstack));
      return itemstack;
   }

   @Nullable
   public static DyeColor getColorFromItem(Item item) {
      return getColorFromBlock(Block.byItem(item));
   }

   @Nullable
   public static DyeColor getColorFromBlock(Block block) {
      return block instanceof ShulkerBoxBlock ? ((ShulkerBoxBlock)block).getColor() : null;
   }

   public static Block getBlockByColor(@Nullable DyeColor dyecolor) {
      if (dyecolor == null) {
         return Blocks.SHULKER_BOX;
      } else {
         switch (dyecolor) {
            case WHITE:
               return Blocks.WHITE_SHULKER_BOX;
            case ORANGE:
               return Blocks.ORANGE_SHULKER_BOX;
            case MAGENTA:
               return Blocks.MAGENTA_SHULKER_BOX;
            case LIGHT_BLUE:
               return Blocks.LIGHT_BLUE_SHULKER_BOX;
            case YELLOW:
               return Blocks.YELLOW_SHULKER_BOX;
            case LIME:
               return Blocks.LIME_SHULKER_BOX;
            case PINK:
               return Blocks.PINK_SHULKER_BOX;
            case GRAY:
               return Blocks.GRAY_SHULKER_BOX;
            case LIGHT_GRAY:
               return Blocks.LIGHT_GRAY_SHULKER_BOX;
            case CYAN:
               return Blocks.CYAN_SHULKER_BOX;
            case PURPLE:
            default:
               return Blocks.PURPLE_SHULKER_BOX;
            case BLUE:
               return Blocks.BLUE_SHULKER_BOX;
            case BROWN:
               return Blocks.BROWN_SHULKER_BOX;
            case GREEN:
               return Blocks.GREEN_SHULKER_BOX;
            case RED:
               return Blocks.RED_SHULKER_BOX;
            case BLACK:
               return Blocks.BLACK_SHULKER_BOX;
         }
      }
   }

   @Nullable
   public DyeColor getColor() {
      return this.color;
   }

   public static ItemStack getColoredItemStack(@Nullable DyeColor dyecolor) {
      return new ItemStack(getBlockByColor(dyecolor));
   }

   public BlockState rotate(BlockState blockstate, Rotation rotation) {
      return blockstate.setValue(FACING, rotation.rotate(blockstate.getValue(FACING)));
   }

   public BlockState mirror(BlockState blockstate, Mirror mirror) {
      return blockstate.rotate(mirror.getRotation(blockstate.getValue(FACING)));
   }
}
