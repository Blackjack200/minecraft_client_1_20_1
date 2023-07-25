package net.minecraft.world.item;

import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.shapes.CollisionContext;

public class BlockItem extends Item {
   public static final String BLOCK_ENTITY_TAG = "BlockEntityTag";
   public static final String BLOCK_STATE_TAG = "BlockStateTag";
   /** @deprecated */
   @Deprecated
   private final Block block;

   public BlockItem(Block block, Item.Properties item_properties) {
      super(item_properties);
      this.block = block;
   }

   public InteractionResult useOn(UseOnContext useoncontext) {
      InteractionResult interactionresult = this.place(new BlockPlaceContext(useoncontext));
      if (!interactionresult.consumesAction() && this.isEdible()) {
         InteractionResult interactionresult1 = this.use(useoncontext.getLevel(), useoncontext.getPlayer(), useoncontext.getHand()).getResult();
         return interactionresult1 == InteractionResult.CONSUME ? InteractionResult.CONSUME_PARTIAL : interactionresult1;
      } else {
         return interactionresult;
      }
   }

   public InteractionResult place(BlockPlaceContext blockplacecontext) {
      if (!this.getBlock().isEnabled(blockplacecontext.getLevel().enabledFeatures())) {
         return InteractionResult.FAIL;
      } else if (!blockplacecontext.canPlace()) {
         return InteractionResult.FAIL;
      } else {
         BlockPlaceContext blockplacecontext1 = this.updatePlacementContext(blockplacecontext);
         if (blockplacecontext1 == null) {
            return InteractionResult.FAIL;
         } else {
            BlockState blockstate = this.getPlacementState(blockplacecontext1);
            if (blockstate == null) {
               return InteractionResult.FAIL;
            } else if (!this.placeBlock(blockplacecontext1, blockstate)) {
               return InteractionResult.FAIL;
            } else {
               BlockPos blockpos = blockplacecontext1.getClickedPos();
               Level level = blockplacecontext1.getLevel();
               Player player = blockplacecontext1.getPlayer();
               ItemStack itemstack = blockplacecontext1.getItemInHand();
               BlockState blockstate1 = level.getBlockState(blockpos);
               if (blockstate1.is(blockstate.getBlock())) {
                  blockstate1 = this.updateBlockStateFromTag(blockpos, level, itemstack, blockstate1);
                  this.updateCustomBlockEntityTag(blockpos, level, player, itemstack, blockstate1);
                  blockstate1.getBlock().setPlacedBy(level, blockpos, blockstate1, player, itemstack);
                  if (player instanceof ServerPlayer) {
                     CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer)player, blockpos, itemstack);
                  }
               }

               SoundType soundtype = blockstate1.getSoundType();
               level.playSound(player, blockpos, this.getPlaceSound(blockstate1), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
               level.gameEvent(GameEvent.BLOCK_PLACE, blockpos, GameEvent.Context.of(player, blockstate1));
               if (player == null || !player.getAbilities().instabuild) {
                  itemstack.shrink(1);
               }

               return InteractionResult.sidedSuccess(level.isClientSide);
            }
         }
      }
   }

   protected SoundEvent getPlaceSound(BlockState blockstate) {
      return blockstate.getSoundType().getPlaceSound();
   }

   @Nullable
   public BlockPlaceContext updatePlacementContext(BlockPlaceContext blockplacecontext) {
      return blockplacecontext;
   }

   protected boolean updateCustomBlockEntityTag(BlockPos blockpos, Level level, @Nullable Player player, ItemStack itemstack, BlockState blockstate) {
      return updateCustomBlockEntityTag(level, player, blockpos, itemstack);
   }

   @Nullable
   protected BlockState getPlacementState(BlockPlaceContext blockplacecontext) {
      BlockState blockstate = this.getBlock().getStateForPlacement(blockplacecontext);
      return blockstate != null && this.canPlace(blockplacecontext, blockstate) ? blockstate : null;
   }

   private BlockState updateBlockStateFromTag(BlockPos blockpos, Level level, ItemStack itemstack, BlockState blockstate) {
      BlockState blockstate1 = blockstate;
      CompoundTag compoundtag = itemstack.getTag();
      if (compoundtag != null) {
         CompoundTag compoundtag1 = compoundtag.getCompound("BlockStateTag");
         StateDefinition<Block, BlockState> statedefinition = blockstate.getBlock().getStateDefinition();

         for(String s : compoundtag1.getAllKeys()) {
            Property<?> property = statedefinition.getProperty(s);
            if (property != null) {
               String s1 = compoundtag1.get(s).getAsString();
               blockstate1 = updateState(blockstate1, property, s1);
            }
         }
      }

      if (blockstate1 != blockstate) {
         level.setBlock(blockpos, blockstate1, 2);
      }

      return blockstate1;
   }

   private static <T extends Comparable<T>> BlockState updateState(BlockState blockstate, Property<T> property, String s) {
      return property.getValue(s).map((comparable) -> blockstate.setValue(property, comparable)).orElse(blockstate);
   }

   protected boolean canPlace(BlockPlaceContext blockplacecontext, BlockState blockstate) {
      Player player = blockplacecontext.getPlayer();
      CollisionContext collisioncontext = player == null ? CollisionContext.empty() : CollisionContext.of(player);
      return (!this.mustSurvive() || blockstate.canSurvive(blockplacecontext.getLevel(), blockplacecontext.getClickedPos())) && blockplacecontext.getLevel().isUnobstructed(blockstate, blockplacecontext.getClickedPos(), collisioncontext);
   }

   protected boolean mustSurvive() {
      return true;
   }

   protected boolean placeBlock(BlockPlaceContext blockplacecontext, BlockState blockstate) {
      return blockplacecontext.getLevel().setBlock(blockplacecontext.getClickedPos(), blockstate, 11);
   }

   public static boolean updateCustomBlockEntityTag(Level level, @Nullable Player player, BlockPos blockpos, ItemStack itemstack) {
      MinecraftServer minecraftserver = level.getServer();
      if (minecraftserver == null) {
         return false;
      } else {
         CompoundTag compoundtag = getBlockEntityData(itemstack);
         if (compoundtag != null) {
            BlockEntity blockentity = level.getBlockEntity(blockpos);
            if (blockentity != null) {
               if (!level.isClientSide && blockentity.onlyOpCanSetNbt() && (player == null || !player.canUseGameMasterBlocks())) {
                  return false;
               }

               CompoundTag compoundtag1 = blockentity.saveWithoutMetadata();
               CompoundTag compoundtag2 = compoundtag1.copy();
               compoundtag1.merge(compoundtag);
               if (!compoundtag1.equals(compoundtag2)) {
                  blockentity.load(compoundtag1);
                  blockentity.setChanged();
                  return true;
               }
            }
         }

         return false;
      }
   }

   public String getDescriptionId() {
      return this.getBlock().getDescriptionId();
   }

   public void appendHoverText(ItemStack itemstack, @Nullable Level level, List<Component> list, TooltipFlag tooltipflag) {
      super.appendHoverText(itemstack, level, list, tooltipflag);
      this.getBlock().appendHoverText(itemstack, level, list, tooltipflag);
   }

   public Block getBlock() {
      return this.block;
   }

   public void registerBlocks(Map<Block, Item> map, Item item) {
      map.put(this.getBlock(), item);
   }

   public boolean canFitInsideContainerItems() {
      return !(this.block instanceof ShulkerBoxBlock);
   }

   public void onDestroyed(ItemEntity itementity) {
      if (this.block instanceof ShulkerBoxBlock) {
         ItemStack itemstack = itementity.getItem();
         CompoundTag compoundtag = getBlockEntityData(itemstack);
         if (compoundtag != null && compoundtag.contains("Items", 9)) {
            ListTag listtag = compoundtag.getList("Items", 10);
            ItemUtils.onContainerDestroyed(itementity, listtag.stream().map(CompoundTag.class::cast).map(ItemStack::of));
         }
      }

   }

   @Nullable
   public static CompoundTag getBlockEntityData(ItemStack itemstack) {
      return itemstack.getTagElement("BlockEntityTag");
   }

   public static void setBlockEntityData(ItemStack itemstack, BlockEntityType<?> blockentitytype, CompoundTag compoundtag) {
      if (compoundtag.isEmpty()) {
         itemstack.removeTagKey("BlockEntityTag");
      } else {
         BlockEntity.addEntityType(compoundtag, blockentitytype);
         itemstack.addTagElement("BlockEntityTag", compoundtag);
      }

   }

   public FeatureFlagSet requiredFeatures() {
      return this.getBlock().requiredFeatures();
   }
}
