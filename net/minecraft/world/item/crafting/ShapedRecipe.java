package net.minecraft.world.item.crafting;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class ShapedRecipe implements CraftingRecipe {
   final int width;
   final int height;
   final NonNullList<Ingredient> recipeItems;
   final ItemStack result;
   private final ResourceLocation id;
   final String group;
   final CraftingBookCategory category;
   final boolean showNotification;

   public ShapedRecipe(ResourceLocation resourcelocation, String s, CraftingBookCategory craftingbookcategory, int i, int j, NonNullList<Ingredient> nonnulllist, ItemStack itemstack, boolean flag) {
      this.id = resourcelocation;
      this.group = s;
      this.category = craftingbookcategory;
      this.width = i;
      this.height = j;
      this.recipeItems = nonnulllist;
      this.result = itemstack;
      this.showNotification = flag;
   }

   public ShapedRecipe(ResourceLocation resourcelocation, String s, CraftingBookCategory craftingbookcategory, int i, int j, NonNullList<Ingredient> nonnulllist, ItemStack itemstack) {
      this(resourcelocation, s, craftingbookcategory, i, j, nonnulllist, itemstack, true);
   }

   public ResourceLocation getId() {
      return this.id;
   }

   public RecipeSerializer<?> getSerializer() {
      return RecipeSerializer.SHAPED_RECIPE;
   }

   public String getGroup() {
      return this.group;
   }

   public CraftingBookCategory category() {
      return this.category;
   }

   public ItemStack getResultItem(RegistryAccess registryaccess) {
      return this.result;
   }

   public NonNullList<Ingredient> getIngredients() {
      return this.recipeItems;
   }

   public boolean showNotification() {
      return this.showNotification;
   }

   public boolean canCraftInDimensions(int i, int j) {
      return i >= this.width && j >= this.height;
   }

   public boolean matches(CraftingContainer craftingcontainer, Level level) {
      for(int i = 0; i <= craftingcontainer.getWidth() - this.width; ++i) {
         for(int j = 0; j <= craftingcontainer.getHeight() - this.height; ++j) {
            if (this.matches(craftingcontainer, i, j, true)) {
               return true;
            }

            if (this.matches(craftingcontainer, i, j, false)) {
               return true;
            }
         }
      }

      return false;
   }

   private boolean matches(CraftingContainer craftingcontainer, int i, int j, boolean flag) {
      for(int k = 0; k < craftingcontainer.getWidth(); ++k) {
         for(int l = 0; l < craftingcontainer.getHeight(); ++l) {
            int i1 = k - i;
            int j1 = l - j;
            Ingredient ingredient = Ingredient.EMPTY;
            if (i1 >= 0 && j1 >= 0 && i1 < this.width && j1 < this.height) {
               if (flag) {
                  ingredient = this.recipeItems.get(this.width - i1 - 1 + j1 * this.width);
               } else {
                  ingredient = this.recipeItems.get(i1 + j1 * this.width);
               }
            }

            if (!ingredient.test(craftingcontainer.getItem(k + l * craftingcontainer.getWidth()))) {
               return false;
            }
         }
      }

      return true;
   }

   public ItemStack assemble(CraftingContainer craftingcontainer, RegistryAccess registryaccess) {
      return this.getResultItem(registryaccess).copy();
   }

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }

   static NonNullList<Ingredient> dissolvePattern(String[] astring, Map<String, Ingredient> map, int i, int j) {
      NonNullList<Ingredient> nonnulllist = NonNullList.withSize(i * j, Ingredient.EMPTY);
      Set<String> set = Sets.newHashSet(map.keySet());
      set.remove(" ");

      for(int k = 0; k < astring.length; ++k) {
         for(int l = 0; l < astring[k].length(); ++l) {
            String s = astring[k].substring(l, l + 1);
            Ingredient ingredient = map.get(s);
            if (ingredient == null) {
               throw new JsonSyntaxException("Pattern references symbol '" + s + "' but it's not defined in the key");
            }

            set.remove(s);
            nonnulllist.set(l + i * k, ingredient);
         }
      }

      if (!set.isEmpty()) {
         throw new JsonSyntaxException("Key defines symbols that aren't used in pattern: " + set);
      } else {
         return nonnulllist;
      }
   }

   @VisibleForTesting
   static String[] shrink(String... astring) {
      int i = Integer.MAX_VALUE;
      int j = 0;
      int k = 0;
      int l = 0;

      for(int i1 = 0; i1 < astring.length; ++i1) {
         String s = astring[i1];
         i = Math.min(i, firstNonSpace(s));
         int j1 = lastNonSpace(s);
         j = Math.max(j, j1);
         if (j1 < 0) {
            if (k == i1) {
               ++k;
            }

            ++l;
         } else {
            l = 0;
         }
      }

      if (astring.length == l) {
         return new String[0];
      } else {
         String[] astring1 = new String[astring.length - l - k];

         for(int k1 = 0; k1 < astring1.length; ++k1) {
            astring1[k1] = astring[k1 + k].substring(i, j + 1);
         }

         return astring1;
      }
   }

   public boolean isIncomplete() {
      NonNullList<Ingredient> nonnulllist = this.getIngredients();
      return nonnulllist.isEmpty() || nonnulllist.stream().filter((ingredient1) -> !ingredient1.isEmpty()).anyMatch((ingredient) -> ingredient.getItems().length == 0);
   }

   private static int firstNonSpace(String s) {
      int i;
      for(i = 0; i < s.length() && s.charAt(i) == ' '; ++i) {
      }

      return i;
   }

   private static int lastNonSpace(String s) {
      int i;
      for(i = s.length() - 1; i >= 0 && s.charAt(i) == ' '; --i) {
      }

      return i;
   }

   static String[] patternFromJson(JsonArray jsonarray) {
      String[] astring = new String[jsonarray.size()];
      if (astring.length > 3) {
         throw new JsonSyntaxException("Invalid pattern: too many rows, 3 is maximum");
      } else if (astring.length == 0) {
         throw new JsonSyntaxException("Invalid pattern: empty pattern not allowed");
      } else {
         for(int i = 0; i < astring.length; ++i) {
            String s = GsonHelper.convertToString(jsonarray.get(i), "pattern[" + i + "]");
            if (s.length() > 3) {
               throw new JsonSyntaxException("Invalid pattern: too many columns, 3 is maximum");
            }

            if (i > 0 && astring[0].length() != s.length()) {
               throw new JsonSyntaxException("Invalid pattern: each row must be the same width");
            }

            astring[i] = s;
         }

         return astring;
      }
   }

   static Map<String, Ingredient> keyFromJson(JsonObject jsonobject) {
      Map<String, Ingredient> map = Maps.newHashMap();

      for(Map.Entry<String, JsonElement> map_entry : jsonobject.entrySet()) {
         if (map_entry.getKey().length() != 1) {
            throw new JsonSyntaxException("Invalid key entry: '" + (String)map_entry.getKey() + "' is an invalid symbol (must be 1 character only).");
         }

         if (" ".equals(map_entry.getKey())) {
            throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");
         }

         map.put(map_entry.getKey(), Ingredient.fromJson(map_entry.getValue(), false));
      }

      map.put(" ", Ingredient.EMPTY);
      return map;
   }

   public static ItemStack itemStackFromJson(JsonObject jsonobject) {
      Item item = itemFromJson(jsonobject);
      if (jsonobject.has("data")) {
         throw new JsonParseException("Disallowed data tag found");
      } else {
         int i = GsonHelper.getAsInt(jsonobject, "count", 1);
         if (i < 1) {
            throw new JsonSyntaxException("Invalid output count: " + i);
         } else {
            return new ItemStack(item, i);
         }
      }
   }

   public static Item itemFromJson(JsonObject jsonobject) {
      String s = GsonHelper.getAsString(jsonobject, "item");
      Item item = BuiltInRegistries.ITEM.getOptional(ResourceLocation.tryParse(s)).orElseThrow(() -> new JsonSyntaxException("Unknown item '" + s + "'"));
      if (item == Items.AIR) {
         throw new JsonSyntaxException("Empty ingredient not allowed here");
      } else {
         return item;
      }
   }

   public static class Serializer implements RecipeSerializer<ShapedRecipe> {
      public ShapedRecipe fromJson(ResourceLocation resourcelocation, JsonObject jsonobject) {
         String s = GsonHelper.getAsString(jsonobject, "group", "");
         CraftingBookCategory craftingbookcategory = CraftingBookCategory.CODEC.byName(GsonHelper.getAsString(jsonobject, "category", (String)null), CraftingBookCategory.MISC);
         Map<String, Ingredient> map = ShapedRecipe.keyFromJson(GsonHelper.getAsJsonObject(jsonobject, "key"));
         String[] astring = ShapedRecipe.shrink(ShapedRecipe.patternFromJson(GsonHelper.getAsJsonArray(jsonobject, "pattern")));
         int i = astring[0].length();
         int j = astring.length;
         NonNullList<Ingredient> nonnulllist = ShapedRecipe.dissolvePattern(astring, map, i, j);
         ItemStack itemstack = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(jsonobject, "result"));
         boolean flag = GsonHelper.getAsBoolean(jsonobject, "show_notification", true);
         return new ShapedRecipe(resourcelocation, s, craftingbookcategory, i, j, nonnulllist, itemstack, flag);
      }

      public ShapedRecipe fromNetwork(ResourceLocation resourcelocation, FriendlyByteBuf friendlybytebuf) {
         int i = friendlybytebuf.readVarInt();
         int j = friendlybytebuf.readVarInt();
         String s = friendlybytebuf.readUtf();
         CraftingBookCategory craftingbookcategory = friendlybytebuf.readEnum(CraftingBookCategory.class);
         NonNullList<Ingredient> nonnulllist = NonNullList.withSize(i * j, Ingredient.EMPTY);

         for(int k = 0; k < nonnulllist.size(); ++k) {
            nonnulllist.set(k, Ingredient.fromNetwork(friendlybytebuf));
         }

         ItemStack itemstack = friendlybytebuf.readItem();
         boolean flag = friendlybytebuf.readBoolean();
         return new ShapedRecipe(resourcelocation, s, craftingbookcategory, i, j, nonnulllist, itemstack, flag);
      }

      public void toNetwork(FriendlyByteBuf friendlybytebuf, ShapedRecipe shapedrecipe) {
         friendlybytebuf.writeVarInt(shapedrecipe.width);
         friendlybytebuf.writeVarInt(shapedrecipe.height);
         friendlybytebuf.writeUtf(shapedrecipe.group);
         friendlybytebuf.writeEnum(shapedrecipe.category);

         for(Ingredient ingredient : shapedrecipe.recipeItems) {
            ingredient.toNetwork(friendlybytebuf);
         }

         friendlybytebuf.writeItem(shapedrecipe.result);
         friendlybytebuf.writeBoolean(shapedrecipe.showNotification);
      }
   }
}
