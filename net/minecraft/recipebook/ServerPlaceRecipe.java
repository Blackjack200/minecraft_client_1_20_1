package net.minecraft.recipebook;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.protocol.game.ClientboundPlaceGhostRecipePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import org.slf4j.Logger;

public class ServerPlaceRecipe<C extends Container> implements PlaceRecipe<Integer> {
   private static final Logger LOGGER = LogUtils.getLogger();
   protected final StackedContents stackedContents = new StackedContents();
   protected Inventory inventory;
   protected RecipeBookMenu<C> menu;

   public ServerPlaceRecipe(RecipeBookMenu<C> recipebookmenu) {
      this.menu = recipebookmenu;
   }

   public void recipeClicked(ServerPlayer serverplayer, @Nullable Recipe<C> recipe, boolean flag) {
      if (recipe != null && serverplayer.getRecipeBook().contains(recipe)) {
         this.inventory = serverplayer.getInventory();
         if (this.testClearGrid() || serverplayer.isCreative()) {
            this.stackedContents.clear();
            serverplayer.getInventory().fillStackedContents(this.stackedContents);
            this.menu.fillCraftSlotsStackedContents(this.stackedContents);
            if (this.stackedContents.canCraft(recipe, (IntList)null)) {
               this.handleRecipeClicked(recipe, flag);
            } else {
               this.clearGrid();
               serverplayer.connection.send(new ClientboundPlaceGhostRecipePacket(serverplayer.containerMenu.containerId, recipe));
            }

            serverplayer.getInventory().setChanged();
         }
      }
   }

   protected void clearGrid() {
      for(int i = 0; i < this.menu.getSize(); ++i) {
         if (this.menu.shouldMoveToInventory(i)) {
            ItemStack itemstack = this.menu.getSlot(i).getItem().copy();
            this.inventory.placeItemBackInInventory(itemstack, false);
            this.menu.getSlot(i).set(itemstack);
         }
      }

      this.menu.clearCraftingContent();
   }

   protected void handleRecipeClicked(Recipe<C> recipe, boolean flag) {
      boolean flag1 = this.menu.recipeMatches(recipe);
      int i = this.stackedContents.getBiggestCraftableStack(recipe, (IntList)null);
      if (flag1) {
         for(int j = 0; j < this.menu.getGridHeight() * this.menu.getGridWidth() + 1; ++j) {
            if (j != this.menu.getResultSlotIndex()) {
               ItemStack itemstack = this.menu.getSlot(j).getItem();
               if (!itemstack.isEmpty() && Math.min(i, itemstack.getMaxStackSize()) < itemstack.getCount() + 1) {
                  return;
               }
            }
         }
      }

      int k = this.getStackSize(flag, i, flag1);
      IntList intlist = new IntArrayList();
      if (this.stackedContents.canCraft(recipe, intlist, k)) {
         int l = k;

         for(int i1 : intlist) {
            int j1 = StackedContents.fromStackingIndex(i1).getMaxStackSize();
            if (j1 < l) {
               l = j1;
            }
         }

         if (this.stackedContents.canCraft(recipe, intlist, l)) {
            this.clearGrid();
            this.placeRecipe(this.menu.getGridWidth(), this.menu.getGridHeight(), this.menu.getResultSlotIndex(), recipe, intlist.iterator(), l);
         }
      }

   }

   public void addItemToSlot(Iterator<Integer> iterator, int i, int j, int k, int l) {
      Slot slot = this.menu.getSlot(i);
      ItemStack itemstack = StackedContents.fromStackingIndex(iterator.next());
      if (!itemstack.isEmpty()) {
         for(int i1 = 0; i1 < j; ++i1) {
            this.moveItemToGrid(slot, itemstack);
         }
      }

   }

   protected int getStackSize(boolean flag, int i, boolean flag1) {
      int j = 1;
      if (flag) {
         j = i;
      } else if (flag1) {
         j = 64;

         for(int k = 0; k < this.menu.getGridWidth() * this.menu.getGridHeight() + 1; ++k) {
            if (k != this.menu.getResultSlotIndex()) {
               ItemStack itemstack = this.menu.getSlot(k).getItem();
               if (!itemstack.isEmpty() && j > itemstack.getCount()) {
                  j = itemstack.getCount();
               }
            }
         }

         if (j < 64) {
            ++j;
         }
      }

      return j;
   }

   protected void moveItemToGrid(Slot slot, ItemStack itemstack) {
      int i = this.inventory.findSlotMatchingUnusedItem(itemstack);
      if (i != -1) {
         ItemStack itemstack1 = this.inventory.getItem(i);
         if (!itemstack1.isEmpty()) {
            if (itemstack1.getCount() > 1) {
               this.inventory.removeItem(i, 1);
            } else {
               this.inventory.removeItemNoUpdate(i);
            }

            if (slot.getItem().isEmpty()) {
               slot.set(itemstack1.copyWithCount(1));
            } else {
               slot.getItem().grow(1);
            }

         }
      }
   }

   private boolean testClearGrid() {
      List<ItemStack> list = Lists.newArrayList();
      int i = this.getAmountOfFreeSlotsInInventory();

      for(int j = 0; j < this.menu.getGridWidth() * this.menu.getGridHeight() + 1; ++j) {
         if (j != this.menu.getResultSlotIndex()) {
            ItemStack itemstack = this.menu.getSlot(j).getItem().copy();
            if (!itemstack.isEmpty()) {
               int k = this.inventory.getSlotWithRemainingSpace(itemstack);
               if (k == -1 && list.size() <= i) {
                  for(ItemStack itemstack1 : list) {
                     if (ItemStack.isSameItem(itemstack1, itemstack) && itemstack1.getCount() != itemstack1.getMaxStackSize() && itemstack1.getCount() + itemstack.getCount() <= itemstack1.getMaxStackSize()) {
                        itemstack1.grow(itemstack.getCount());
                        itemstack.setCount(0);
                        break;
                     }
                  }

                  if (!itemstack.isEmpty()) {
                     if (list.size() >= i) {
                        return false;
                     }

                     list.add(itemstack);
                  }
               } else if (k == -1) {
                  return false;
               }
            }
         }
      }

      return true;
   }

   private int getAmountOfFreeSlotsInInventory() {
      int i = 0;

      for(ItemStack itemstack : this.inventory.items) {
         if (itemstack.isEmpty()) {
            ++i;
         }
      }

      return i;
   }
}
