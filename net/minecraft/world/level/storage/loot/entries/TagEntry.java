package net.minecraft.world.level.storage.loot.entries;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class TagEntry extends LootPoolSingletonContainer {
   final TagKey<Item> tag;
   final boolean expand;

   TagEntry(TagKey<Item> tagkey, boolean flag, int i, int j, LootItemCondition[] alootitemcondition, LootItemFunction[] alootitemfunction) {
      super(i, j, alootitemcondition, alootitemfunction);
      this.tag = tagkey;
      this.expand = flag;
   }

   public LootPoolEntryType getType() {
      return LootPoolEntries.TAG;
   }

   public void createItemStack(Consumer<ItemStack> consumer, LootContext lootcontext) {
      BuiltInRegistries.ITEM.getTagOrEmpty(this.tag).forEach((holder) -> consumer.accept(new ItemStack(holder)));
   }

   private boolean expandTag(LootContext lootcontext, Consumer<LootPoolEntry> consumer) {
      if (!this.canRun(lootcontext)) {
         return false;
      } else {
         for(final Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(this.tag)) {
            consumer.accept(new LootPoolSingletonContainer.EntryBase() {
               public void createItemStack(Consumer<ItemStack> consumer, LootContext lootcontext) {
                  consumer.accept(new ItemStack(holder));
               }
            });
         }

         return true;
      }
   }

   public boolean expand(LootContext lootcontext, Consumer<LootPoolEntry> consumer) {
      return this.expand ? this.expandTag(lootcontext, consumer) : super.expand(lootcontext, consumer);
   }

   public static LootPoolSingletonContainer.Builder<?> tagContents(TagKey<Item> tagkey) {
      return simpleBuilder((i, j, alootitemcondition, alootitemfunction) -> new TagEntry(tagkey, false, i, j, alootitemcondition, alootitemfunction));
   }

   public static LootPoolSingletonContainer.Builder<?> expandTag(TagKey<Item> tagkey) {
      return simpleBuilder((i, j, alootitemcondition, alootitemfunction) -> new TagEntry(tagkey, true, i, j, alootitemcondition, alootitemfunction));
   }

   public static class Serializer extends LootPoolSingletonContainer.Serializer<TagEntry> {
      public void serializeCustom(JsonObject jsonobject, TagEntry tagentry, JsonSerializationContext jsonserializationcontext) {
         super.serializeCustom(jsonobject, tagentry, jsonserializationcontext);
         jsonobject.addProperty("name", tagentry.tag.location().toString());
         jsonobject.addProperty("expand", tagentry.expand);
      }

      protected TagEntry deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext, int i, int j, LootItemCondition[] alootitemcondition, LootItemFunction[] alootitemfunction) {
         ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(jsonobject, "name"));
         TagKey<Item> tagkey = TagKey.create(Registries.ITEM, resourcelocation);
         boolean flag = GsonHelper.getAsBoolean(jsonobject, "expand");
         return new TagEntry(tagkey, flag, i, j, alootitemcondition, alootitemfunction);
      }
   }
}
