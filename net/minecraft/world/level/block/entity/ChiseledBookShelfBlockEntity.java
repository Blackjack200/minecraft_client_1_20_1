package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.slf4j.Logger;

public class ChiseledBookShelfBlockEntity extends BlockEntity implements Container {
   public static final int MAX_BOOKS_IN_STORAGE = 6;
   private static final Logger LOGGER = LogUtils.getLogger();
   private final NonNullList<ItemStack> items = NonNullList.withSize(6, ItemStack.EMPTY);
   private int lastInteractedSlot = -1;

   public ChiseledBookShelfBlockEntity(BlockPos blockpos, BlockState blockstate) {
      super(BlockEntityType.CHISELED_BOOKSHELF, blockpos, blockstate);
   }

   private void updateState(int i) {
      if (i >= 0 && i < 6) {
         this.lastInteractedSlot = i;
         BlockState blockstate = this.getBlockState();

         for(int j = 0; j < ChiseledBookShelfBlock.SLOT_OCCUPIED_PROPERTIES.size(); ++j) {
            boolean flag = !this.getItem(j).isEmpty();
            BooleanProperty booleanproperty = ChiseledBookShelfBlock.SLOT_OCCUPIED_PROPERTIES.get(j);
            blockstate = blockstate.setValue(booleanproperty, Boolean.valueOf(flag));
         }

         Objects.requireNonNull(this.level).setBlock(this.worldPosition, blockstate, 3);
      } else {
         LOGGER.error("Expected slot 0-5, got {}", (int)i);
      }
   }

   public void load(CompoundTag compoundtag) {
      this.items.clear();
      ContainerHelper.loadAllItems(compoundtag, this.items);
      this.lastInteractedSlot = compoundtag.getInt("last_interacted_slot");
   }

   protected void saveAdditional(CompoundTag compoundtag) {
      ContainerHelper.saveAllItems(compoundtag, this.items, true);
      compoundtag.putInt("last_interacted_slot", this.lastInteractedSlot);
   }

   public int count() {
      return (int)this.items.stream().filter(Predicate.not(ItemStack::isEmpty)).count();
   }

   public void clearContent() {
      this.items.clear();
   }

   public int getContainerSize() {
      return 6;
   }

   public boolean isEmpty() {
      return this.items.stream().allMatch(ItemStack::isEmpty);
   }

   public ItemStack getItem(int i) {
      return this.items.get(i);
   }

   public ItemStack removeItem(int i, int j) {
      ItemStack itemstack = Objects.requireNonNullElse(this.items.get(i), ItemStack.EMPTY);
      this.items.set(i, ItemStack.EMPTY);
      if (!itemstack.isEmpty()) {
         this.updateState(i);
      }

      return itemstack;
   }

   public ItemStack removeItemNoUpdate(int i) {
      return this.removeItem(i, 1);
   }

   public void setItem(int i, ItemStack itemstack) {
      if (itemstack.is(ItemTags.BOOKSHELF_BOOKS)) {
         this.items.set(i, itemstack);
         this.updateState(i);
      }

   }

   public boolean canTakeItem(Container container, int i, ItemStack itemstack) {
      return container.hasAnyMatching((itemstack2) -> {
         if (itemstack2.isEmpty()) {
            return true;
         } else {
            return ItemStack.isSameItemSameTags(itemstack, itemstack2) && itemstack2.getCount() + itemstack.getCount() <= Math.min(itemstack2.getMaxStackSize(), container.getMaxStackSize());
         }
      });
   }

   public int getMaxStackSize() {
      return 1;
   }

   public boolean stillValid(Player player) {
      return Container.stillValidBlockEntity(this, player);
   }

   public boolean canPlaceItem(int i, ItemStack itemstack) {
      return itemstack.is(ItemTags.BOOKSHELF_BOOKS) && this.getItem(i).isEmpty();
   }

   public int getLastInteractedSlot() {
      return this.lastInteractedSlot;
   }
}
