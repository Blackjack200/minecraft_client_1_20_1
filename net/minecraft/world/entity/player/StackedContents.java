package net.minecraft.world.entity.player;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.BitSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

public class StackedContents {
   private static final int EMPTY = 0;
   public final Int2IntMap contents = new Int2IntOpenHashMap();

   public void accountSimpleStack(ItemStack itemstack) {
      if (!itemstack.isDamaged() && !itemstack.isEnchanted() && !itemstack.hasCustomHoverName()) {
         this.accountStack(itemstack);
      }

   }

   public void accountStack(ItemStack itemstack) {
      this.accountStack(itemstack, 64);
   }

   public void accountStack(ItemStack itemstack, int i) {
      if (!itemstack.isEmpty()) {
         int j = getStackingIndex(itemstack);
         int k = Math.min(i, itemstack.getCount());
         this.put(j, k);
      }

   }

   public static int getStackingIndex(ItemStack itemstack) {
      return BuiltInRegistries.ITEM.getId(itemstack.getItem());
   }

   boolean has(int i) {
      return this.contents.get(i) > 0;
   }

   int take(int i, int j) {
      int k = this.contents.get(i);
      if (k >= j) {
         this.contents.put(i, k - j);
         return i;
      } else {
         return 0;
      }
   }

   void put(int i, int j) {
      this.contents.put(i, this.contents.get(i) + j);
   }

   public boolean canCraft(Recipe<?> recipe, @Nullable IntList intlist) {
      return this.canCraft(recipe, intlist, 1);
   }

   public boolean canCraft(Recipe<?> recipe, @Nullable IntList intlist, int i) {
      return (new StackedContents.RecipePicker(recipe)).tryPick(i, intlist);
   }

   public int getBiggestCraftableStack(Recipe<?> recipe, @Nullable IntList intlist) {
      return this.getBiggestCraftableStack(recipe, Integer.MAX_VALUE, intlist);
   }

   public int getBiggestCraftableStack(Recipe<?> recipe, int i, @Nullable IntList intlist) {
      return (new StackedContents.RecipePicker(recipe)).tryPickAll(i, intlist);
   }

   public static ItemStack fromStackingIndex(int i) {
      return i == 0 ? ItemStack.EMPTY : new ItemStack(Item.byId(i));
   }

   public void clear() {
      this.contents.clear();
   }

   class RecipePicker {
      private final Recipe<?> recipe;
      private final List<Ingredient> ingredients = Lists.newArrayList();
      private final int ingredientCount;
      private final int[] items;
      private final int itemCount;
      private final BitSet data;
      private final IntList path = new IntArrayList();

      public RecipePicker(Recipe<?> recipe) {
         this.recipe = recipe;
         this.ingredients.addAll(recipe.getIngredients());
         this.ingredients.removeIf(Ingredient::isEmpty);
         this.ingredientCount = this.ingredients.size();
         this.items = this.getUniqueAvailableIngredientItems();
         this.itemCount = this.items.length;
         this.data = new BitSet(this.ingredientCount + this.itemCount + this.ingredientCount + this.ingredientCount * this.itemCount);

         for(int i = 0; i < this.ingredients.size(); ++i) {
            IntList intlist = this.ingredients.get(i).getStackingIds();

            for(int j = 0; j < this.itemCount; ++j) {
               if (intlist.contains(this.items[j])) {
                  this.data.set(this.getIndex(true, j, i));
               }
            }
         }

      }

      public boolean tryPick(int i, @Nullable IntList intlist) {
         if (i <= 0) {
            return true;
         } else {
            int j;
            for(j = 0; this.dfs(i); ++j) {
               StackedContents.this.take(this.items[this.path.getInt(0)], i);
               int k = this.path.size() - 1;
               this.setSatisfied(this.path.getInt(k));

               for(int l = 0; l < k; ++l) {
                  this.toggleResidual((l & 1) == 0, this.path.get(l), this.path.get(l + 1));
               }

               this.path.clear();
               this.data.clear(0, this.ingredientCount + this.itemCount);
            }

            boolean flag = j == this.ingredientCount;
            boolean flag1 = flag && intlist != null;
            if (flag1) {
               intlist.clear();
            }

            this.data.clear(0, this.ingredientCount + this.itemCount + this.ingredientCount);
            int i1 = 0;
            List<Ingredient> list = this.recipe.getIngredients();

            for(int j1 = 0; j1 < list.size(); ++j1) {
               if (flag1 && list.get(j1).isEmpty()) {
                  intlist.add(0);
               } else {
                  for(int k1 = 0; k1 < this.itemCount; ++k1) {
                     if (this.hasResidual(false, i1, k1)) {
                        this.toggleResidual(true, k1, i1);
                        StackedContents.this.put(this.items[k1], i);
                        if (flag1) {
                           intlist.add(this.items[k1]);
                        }
                     }
                  }

                  ++i1;
               }
            }

            return flag;
         }
      }

      private int[] getUniqueAvailableIngredientItems() {
         IntCollection intcollection = new IntAVLTreeSet();

         for(Ingredient ingredient : this.ingredients) {
            intcollection.addAll(ingredient.getStackingIds());
         }

         IntIterator intiterator = intcollection.iterator();

         while(intiterator.hasNext()) {
            if (!StackedContents.this.has(intiterator.nextInt())) {
               intiterator.remove();
            }
         }

         return intcollection.toIntArray();
      }

      private boolean dfs(int i) {
         int j = this.itemCount;

         for(int k = 0; k < j; ++k) {
            if (StackedContents.this.contents.get(this.items[k]) >= i) {
               this.visit(false, k);

               while(!this.path.isEmpty()) {
                  int l = this.path.size();
                  boolean flag = (l & 1) == 1;
                  int i1 = this.path.getInt(l - 1);
                  if (!flag && !this.isSatisfied(i1)) {
                     break;
                  }

                  int j1 = flag ? this.ingredientCount : j;

                  for(int k1 = 0; k1 < j1; ++k1) {
                     if (!this.hasVisited(flag, k1) && this.hasConnection(flag, i1, k1) && this.hasResidual(flag, i1, k1)) {
                        this.visit(flag, k1);
                        break;
                     }
                  }

                  int l1 = this.path.size();
                  if (l1 == l) {
                     this.path.removeInt(l1 - 1);
                  }
               }

               if (!this.path.isEmpty()) {
                  return true;
               }
            }
         }

         return false;
      }

      private boolean isSatisfied(int i) {
         return this.data.get(this.getSatisfiedIndex(i));
      }

      private void setSatisfied(int i) {
         this.data.set(this.getSatisfiedIndex(i));
      }

      private int getSatisfiedIndex(int i) {
         return this.ingredientCount + this.itemCount + i;
      }

      private boolean hasConnection(boolean flag, int i, int j) {
         return this.data.get(this.getIndex(flag, i, j));
      }

      private boolean hasResidual(boolean flag, int i, int j) {
         return flag != this.data.get(1 + this.getIndex(flag, i, j));
      }

      private void toggleResidual(boolean flag, int i, int j) {
         this.data.flip(1 + this.getIndex(flag, i, j));
      }

      private int getIndex(boolean flag, int i, int j) {
         int k = flag ? i * this.ingredientCount + j : j * this.ingredientCount + i;
         return this.ingredientCount + this.itemCount + this.ingredientCount + 2 * k;
      }

      private void visit(boolean flag, int i) {
         this.data.set(this.getVisitedIndex(flag, i));
         this.path.add(i);
      }

      private boolean hasVisited(boolean flag, int i) {
         return this.data.get(this.getVisitedIndex(flag, i));
      }

      private int getVisitedIndex(boolean flag, int i) {
         return (flag ? 0 : this.ingredientCount) + i;
      }

      public int tryPickAll(int i, @Nullable IntList intlist) {
         int j = 0;
         int k = Math.min(i, this.getMinIngredientCount()) + 1;

         while(true) {
            int l = (j + k) / 2;
            if (this.tryPick(l, (IntList)null)) {
               if (k - j <= 1) {
                  if (l > 0) {
                     this.tryPick(l, intlist);
                  }

                  return l;
               }

               j = l;
            } else {
               k = l;
            }
         }
      }

      private int getMinIngredientCount() {
         int i = Integer.MAX_VALUE;

         for(Ingredient ingredient : this.ingredients) {
            int j = 0;

            for(int k : ingredient.getStackingIds()) {
               j = Math.max(j, StackedContents.this.contents.get(k));
            }

            if (i > 0) {
               i = Math.min(i, j);
            }
         }

         return i;
      }
   }
}
