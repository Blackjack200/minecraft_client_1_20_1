package net.minecraft.world.item.crafting;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class Ingredient implements Predicate<ItemStack> {
   public static final Ingredient EMPTY = new Ingredient(Stream.empty());
   private final Ingredient.Value[] values;
   @Nullable
   private ItemStack[] itemStacks;
   @Nullable
   private IntList stackingIds;

   private Ingredient(Stream<? extends Ingredient.Value> stream) {
      this.values = stream.toArray((i) -> new Ingredient.Value[i]);
   }

   public ItemStack[] getItems() {
      if (this.itemStacks == null) {
         this.itemStacks = Arrays.stream(this.values).flatMap((ingredient_value) -> ingredient_value.getItems().stream()).distinct().toArray((i) -> new ItemStack[i]);
      }

      return this.itemStacks;
   }

   public boolean test(@Nullable ItemStack itemstack) {
      if (itemstack == null) {
         return false;
      } else if (this.isEmpty()) {
         return itemstack.isEmpty();
      } else {
         for(ItemStack itemstack1 : this.getItems()) {
            if (itemstack1.is(itemstack.getItem())) {
               return true;
            }
         }

         return false;
      }
   }

   public IntList getStackingIds() {
      if (this.stackingIds == null) {
         ItemStack[] aitemstack = this.getItems();
         this.stackingIds = new IntArrayList(aitemstack.length);

         for(ItemStack itemstack : aitemstack) {
            this.stackingIds.add(StackedContents.getStackingIndex(itemstack));
         }

         this.stackingIds.sort(IntComparators.NATURAL_COMPARATOR);
      }

      return this.stackingIds;
   }

   public void toNetwork(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeCollection(Arrays.asList(this.getItems()), FriendlyByteBuf::writeItem);
   }

   public JsonElement toJson() {
      if (this.values.length == 1) {
         return this.values[0].serialize();
      } else {
         JsonArray jsonarray = new JsonArray();

         for(Ingredient.Value ingredient_value : this.values) {
            jsonarray.add(ingredient_value.serialize());
         }

         return jsonarray;
      }
   }

   public boolean isEmpty() {
      return this.values.length == 0;
   }

   private static Ingredient fromValues(Stream<? extends Ingredient.Value> stream) {
      Ingredient ingredient = new Ingredient(stream);
      return ingredient.isEmpty() ? EMPTY : ingredient;
   }

   public static Ingredient of() {
      return EMPTY;
   }

   public static Ingredient of(ItemLike... aitemlike) {
      return of(Arrays.stream(aitemlike).map(ItemStack::new));
   }

   public static Ingredient of(ItemStack... aitemstack) {
      return of(Arrays.stream(aitemstack));
   }

   public static Ingredient of(Stream<ItemStack> stream) {
      return fromValues(stream.filter((itemstack) -> !itemstack.isEmpty()).map(Ingredient.ItemValue::new));
   }

   public static Ingredient of(TagKey<Item> tagkey) {
      return fromValues(Stream.of(new Ingredient.TagValue(tagkey)));
   }

   public static Ingredient fromNetwork(FriendlyByteBuf friendlybytebuf) {
      return fromValues(friendlybytebuf.<ItemStack>readList(FriendlyByteBuf::readItem).stream().map(Ingredient.ItemValue::new));
   }

   public static Ingredient fromJson(@Nullable JsonElement jsonelement) {
      return fromJson(jsonelement, true);
   }

   public static Ingredient fromJson(@Nullable JsonElement jsonelement, boolean flag) {
      if (jsonelement != null && !jsonelement.isJsonNull()) {
         if (jsonelement.isJsonObject()) {
            return fromValues(Stream.of(valueFromJson(jsonelement.getAsJsonObject())));
         } else if (jsonelement.isJsonArray()) {
            JsonArray jsonarray = jsonelement.getAsJsonArray();
            if (jsonarray.size() == 0 && !flag) {
               throw new JsonSyntaxException("Item array cannot be empty, at least one item must be defined");
            } else {
               return fromValues(StreamSupport.stream(jsonarray.spliterator(), false).map((jsonelement1) -> valueFromJson(GsonHelper.convertToJsonObject(jsonelement1, "item"))));
            }
         } else {
            throw new JsonSyntaxException("Expected item to be object or array of objects");
         }
      } else {
         throw new JsonSyntaxException("Item cannot be null");
      }
   }

   private static Ingredient.Value valueFromJson(JsonObject jsonobject) {
      if (jsonobject.has("item") && jsonobject.has("tag")) {
         throw new JsonParseException("An ingredient entry is either a tag or an item, not both");
      } else if (jsonobject.has("item")) {
         Item item = ShapedRecipe.itemFromJson(jsonobject);
         return new Ingredient.ItemValue(new ItemStack(item));
      } else if (jsonobject.has("tag")) {
         ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(jsonobject, "tag"));
         TagKey<Item> tagkey = TagKey.create(Registries.ITEM, resourcelocation);
         return new Ingredient.TagValue(tagkey);
      } else {
         throw new JsonParseException("An ingredient entry needs either a tag or an item");
      }
   }

   static class ItemValue implements Ingredient.Value {
      private final ItemStack item;

      ItemValue(ItemStack itemstack) {
         this.item = itemstack;
      }

      public Collection<ItemStack> getItems() {
         return Collections.singleton(this.item);
      }

      public JsonObject serialize() {
         JsonObject jsonobject = new JsonObject();
         jsonobject.addProperty("item", BuiltInRegistries.ITEM.getKey(this.item.getItem()).toString());
         return jsonobject;
      }
   }

   static class TagValue implements Ingredient.Value {
      private final TagKey<Item> tag;

      TagValue(TagKey<Item> tagkey) {
         this.tag = tagkey;
      }

      public Collection<ItemStack> getItems() {
         List<ItemStack> list = Lists.newArrayList();

         for(Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(this.tag)) {
            list.add(new ItemStack(holder));
         }

         return list;
      }

      public JsonObject serialize() {
         JsonObject jsonobject = new JsonObject();
         jsonobject.addProperty("tag", this.tag.location().toString());
         return jsonobject;
      }
   }

   interface Value {
      Collection<ItemStack> getItems();

      JsonObject serialize();
   }
}
