package net.minecraft.world.level.block;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ChiseledBookShelfBlock extends BaseEntityBlock {
   private static final int MAX_BOOKS_IN_STORAGE = 6;
   public static final int BOOKS_PER_ROW = 3;
   public static final List<BooleanProperty> SLOT_OCCUPIED_PROPERTIES = List.of(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_0_OCCUPIED, BlockStateProperties.CHISELED_BOOKSHELF_SLOT_1_OCCUPIED, BlockStateProperties.CHISELED_BOOKSHELF_SLOT_2_OCCUPIED, BlockStateProperties.CHISELED_BOOKSHELF_SLOT_3_OCCUPIED, BlockStateProperties.CHISELED_BOOKSHELF_SLOT_4_OCCUPIED, BlockStateProperties.CHISELED_BOOKSHELF_SLOT_5_OCCUPIED);

   public ChiseledBookShelfBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      BlockState blockstate = this.stateDefinition.any().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH);

      for(BooleanProperty booleanproperty : SLOT_OCCUPIED_PROPERTIES) {
         blockstate = blockstate.setValue(booleanproperty, Boolean.valueOf(false));
      }

      this.registerDefaultState(blockstate);
   }

   public RenderShape getRenderShape(BlockState blockstate) {
      return RenderShape.MODEL;
   }

   public InteractionResult use(BlockState blockstate, Level level, BlockPos blockpos, Player player, InteractionHand interactionhand, BlockHitResult blockhitresult) {
      BlockEntity optional = level.getBlockEntity(blockpos);
      if (optional instanceof ChiseledBookShelfBlockEntity chiseledbookshelfblockentity) {
         Optional<Vec2> optional = getRelativeHitCoordinatesForBlockFace(blockhitresult, blockstate.getValue(HorizontalDirectionalBlock.FACING));
         if (optional.isEmpty()) {
            return InteractionResult.PASS;
         } else {
            int i = getHitSlot(optional.get());
            if (blockstate.getValue(SLOT_OCCUPIED_PROPERTIES.get(i))) {
               removeBook(level, blockpos, player, chiseledbookshelfblockentity, i);
               return InteractionResult.sidedSuccess(level.isClientSide);
            } else {
               ItemStack itemstack = player.getItemInHand(interactionhand);
               if (itemstack.is(ItemTags.BOOKSHELF_BOOKS)) {
                  addBook(level, blockpos, player, chiseledbookshelfblockentity, itemstack, i);
                  return InteractionResult.sidedSuccess(level.isClientSide);
               } else {
                  return InteractionResult.CONSUME;
               }
            }
         }
      } else {
         return InteractionResult.PASS;
      }
   }

   private static Optional<Vec2> getRelativeHitCoordinatesForBlockFace(BlockHitResult blockhitresult, Direction direction) {
      Direction direction1 = blockhitresult.getDirection();
      if (direction != direction1) {
         return Optional.empty();
      } else {
         BlockPos blockpos = blockhitresult.getBlockPos().relative(direction1);
         Vec3 vec3 = blockhitresult.getLocation().subtract((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ());
         double d0 = vec3.x();
         double d1 = vec3.y();
         double d2 = vec3.z();
         Optional var10000;
         switch (direction1) {
            case NORTH:
               var10000 = Optional.of(new Vec2((float)(1.0D - d0), (float)d1));
               break;
            case SOUTH:
               var10000 = Optional.of(new Vec2((float)d0, (float)d1));
               break;
            case WEST:
               var10000 = Optional.of(new Vec2((float)d2, (float)d1));
               break;
            case EAST:
               var10000 = Optional.of(new Vec2((float)(1.0D - d2), (float)d1));
               break;
            case DOWN:
            case UP:
               var10000 = Optional.empty();
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return var10000;
      }
   }

   private static int getHitSlot(Vec2 vec2) {
      int i = vec2.y >= 0.5F ? 0 : 1;
      int j = getSection(vec2.x);
      return j + i * 3;
   }

   private static int getSection(float f) {
      float f1 = 0.0625F;
      float f2 = 0.375F;
      if (f < 0.375F) {
         return 0;
      } else {
         float f3 = 0.6875F;
         return f < 0.6875F ? 1 : 2;
      }
   }

   private static void addBook(Level level, BlockPos blockpos, Player player, ChiseledBookShelfBlockEntity chiseledbookshelfblockentity, ItemStack itemstack, int i) {
      if (!level.isClientSide) {
         player.awardStat(Stats.ITEM_USED.get(itemstack.getItem()));
         SoundEvent soundevent = itemstack.is(Items.ENCHANTED_BOOK) ? SoundEvents.CHISELED_BOOKSHELF_INSERT_ENCHANTED : SoundEvents.CHISELED_BOOKSHELF_INSERT;
         chiseledbookshelfblockentity.setItem(i, itemstack.split(1));
         level.playSound((Player)null, blockpos, soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
         if (player.isCreative()) {
            itemstack.grow(1);
         }

         level.gameEvent(player, GameEvent.BLOCK_CHANGE, blockpos);
      }
   }

   private static void removeBook(Level level, BlockPos blockpos, Player player, ChiseledBookShelfBlockEntity chiseledbookshelfblockentity, int i) {
      if (!level.isClientSide) {
         ItemStack itemstack = chiseledbookshelfblockentity.removeItem(i, 1);
         SoundEvent soundevent = itemstack.is(Items.ENCHANTED_BOOK) ? SoundEvents.CHISELED_BOOKSHELF_PICKUP_ENCHANTED : SoundEvents.CHISELED_BOOKSHELF_PICKUP;
         level.playSound((Player)null, blockpos, soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
         if (!player.getInventory().add(itemstack)) {
            player.drop(itemstack, false);
         }

         level.gameEvent(player, GameEvent.BLOCK_CHANGE, blockpos);
      }
   }

   public @Nullable BlockEntity newBlockEntity(BlockPos blockpos, BlockState blockstate) {
      return new ChiseledBookShelfBlockEntity(blockpos, blockstate);
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(HorizontalDirectionalBlock.FACING);
      SLOT_OCCUPIED_PROPERTIES.forEach((property) -> statedefinition_builder.add(property));
   }

   public void onRemove(BlockState blockstate, Level level, BlockPos blockpos, BlockState blockstate1, boolean flag) {
      if (!blockstate.is(blockstate1.getBlock())) {
         BlockEntity blockentity = level.getBlockEntity(blockpos);
         if (blockentity instanceof ChiseledBookShelfBlockEntity) {
            ChiseledBookShelfBlockEntity chiseledbookshelfblockentity = (ChiseledBookShelfBlockEntity)blockentity;
            if (!chiseledbookshelfblockentity.isEmpty()) {
               for(int i = 0; i < 6; ++i) {
                  ItemStack itemstack = chiseledbookshelfblockentity.getItem(i);
                  if (!itemstack.isEmpty()) {
                     Containers.dropItemStack(level, (double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ(), itemstack);
                  }
               }

               chiseledbookshelfblockentity.clearContent();
               level.updateNeighbourForOutputSignal(blockpos, this);
            }
         }

         super.onRemove(blockstate, level, blockpos, blockstate1, flag);
      }
   }

   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      return this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, blockplacecontext.getHorizontalDirection().getOpposite());
   }

   public BlockState rotate(BlockState blockstate, Rotation rotation) {
      return blockstate.setValue(HorizontalDirectionalBlock.FACING, rotation.rotate(blockstate.getValue(HorizontalDirectionalBlock.FACING)));
   }

   public BlockState mirror(BlockState blockstate, Mirror mirror) {
      return blockstate.rotate(mirror.getRotation(blockstate.getValue(HorizontalDirectionalBlock.FACING)));
   }

   public boolean hasAnalogOutputSignal(BlockState blockstate) {
      return true;
   }

   public int getAnalogOutputSignal(BlockState blockstate, Level level, BlockPos blockpos) {
      if (level.isClientSide()) {
         return 0;
      } else {
         BlockEntity var5 = level.getBlockEntity(blockpos);
         if (var5 instanceof ChiseledBookShelfBlockEntity) {
            ChiseledBookShelfBlockEntity chiseledbookshelfblockentity = (ChiseledBookShelfBlockEntity)var5;
            return chiseledbookshelfblockentity.getLastInteractedSlot() + 1;
         } else {
            return 0;
         }
      }
   }
}
