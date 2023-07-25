package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProvider;

public class CopyNbtFunction extends LootItemConditionalFunction {
   final NbtProvider source;
   final List<CopyNbtFunction.CopyOperation> operations;

   CopyNbtFunction(LootItemCondition[] alootitemcondition, NbtProvider nbtprovider, List<CopyNbtFunction.CopyOperation> list) {
      super(alootitemcondition);
      this.source = nbtprovider;
      this.operations = ImmutableList.copyOf(list);
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.COPY_NBT;
   }

   static NbtPathArgument.NbtPath compileNbtPath(String s) {
      try {
         return (new NbtPathArgument()).parse(new StringReader(s));
      } catch (CommandSyntaxException var2) {
         throw new IllegalArgumentException("Failed to parse path " + s, var2);
      }
   }

   public Set<LootContextParam<?>> getReferencedContextParams() {
      return this.source.getReferencedContextParams();
   }

   public ItemStack run(ItemStack itemstack, LootContext lootcontext) {
      Tag tag = this.source.get(lootcontext);
      if (tag != null) {
         this.operations.forEach((copynbtfunction_copyoperation) -> copynbtfunction_copyoperation.apply(itemstack::getOrCreateTag, tag));
      }

      return itemstack;
   }

   public static CopyNbtFunction.Builder copyData(NbtProvider nbtprovider) {
      return new CopyNbtFunction.Builder(nbtprovider);
   }

   public static CopyNbtFunction.Builder copyData(LootContext.EntityTarget lootcontext_entitytarget) {
      return new CopyNbtFunction.Builder(ContextNbtProvider.forContextEntity(lootcontext_entitytarget));
   }

   public static class Builder extends LootItemConditionalFunction.Builder<CopyNbtFunction.Builder> {
      private final NbtProvider source;
      private final List<CopyNbtFunction.CopyOperation> ops = Lists.newArrayList();

      Builder(NbtProvider nbtprovider) {
         this.source = nbtprovider;
      }

      public CopyNbtFunction.Builder copy(String s, String s1, CopyNbtFunction.MergeStrategy copynbtfunction_mergestrategy) {
         this.ops.add(new CopyNbtFunction.CopyOperation(s, s1, copynbtfunction_mergestrategy));
         return this;
      }

      public CopyNbtFunction.Builder copy(String s, String s1) {
         return this.copy(s, s1, CopyNbtFunction.MergeStrategy.REPLACE);
      }

      protected CopyNbtFunction.Builder getThis() {
         return this;
      }

      public LootItemFunction build() {
         return new CopyNbtFunction(this.getConditions(), this.source, this.ops);
      }
   }

   static class CopyOperation {
      private final String sourcePathText;
      private final NbtPathArgument.NbtPath sourcePath;
      private final String targetPathText;
      private final NbtPathArgument.NbtPath targetPath;
      private final CopyNbtFunction.MergeStrategy op;

      CopyOperation(String s, String s1, CopyNbtFunction.MergeStrategy copynbtfunction_mergestrategy) {
         this.sourcePathText = s;
         this.sourcePath = CopyNbtFunction.compileNbtPath(s);
         this.targetPathText = s1;
         this.targetPath = CopyNbtFunction.compileNbtPath(s1);
         this.op = copynbtfunction_mergestrategy;
      }

      public void apply(Supplier<Tag> supplier, Tag tag) {
         try {
            List<Tag> list = this.sourcePath.get(tag);
            if (!list.isEmpty()) {
               this.op.merge(supplier.get(), this.targetPath, list);
            }
         } catch (CommandSyntaxException var4) {
         }

      }

      public JsonObject toJson() {
         JsonObject jsonobject = new JsonObject();
         jsonobject.addProperty("source", this.sourcePathText);
         jsonobject.addProperty("target", this.targetPathText);
         jsonobject.addProperty("op", this.op.name);
         return jsonobject;
      }

      public static CopyNbtFunction.CopyOperation fromJson(JsonObject jsonobject) {
         String s = GsonHelper.getAsString(jsonobject, "source");
         String s1 = GsonHelper.getAsString(jsonobject, "target");
         CopyNbtFunction.MergeStrategy copynbtfunction_mergestrategy = CopyNbtFunction.MergeStrategy.getByName(GsonHelper.getAsString(jsonobject, "op"));
         return new CopyNbtFunction.CopyOperation(s, s1, copynbtfunction_mergestrategy);
      }
   }

   public static enum MergeStrategy {
      REPLACE("replace") {
         public void merge(Tag tag, NbtPathArgument.NbtPath nbtpathargument_nbtpath, List<Tag> list) throws CommandSyntaxException {
            nbtpathargument_nbtpath.set(tag, Iterables.getLast(list));
         }
      },
      APPEND("append") {
         public void merge(Tag tag, NbtPathArgument.NbtPath nbtpathargument_nbtpath, List<Tag> list) throws CommandSyntaxException {
            List<Tag> list1 = nbtpathargument_nbtpath.getOrCreate(tag, ListTag::new);
            list1.forEach((tag1) -> {
               if (tag1 instanceof ListTag) {
                  list.forEach((tag3) -> ((ListTag)tag1).add(tag3.copy()));
               }

            });
         }
      },
      MERGE("merge") {
         public void merge(Tag tag, NbtPathArgument.NbtPath nbtpathargument_nbtpath, List<Tag> list) throws CommandSyntaxException {
            List<Tag> list1 = nbtpathargument_nbtpath.getOrCreate(tag, CompoundTag::new);
            list1.forEach((tag1) -> {
               if (tag1 instanceof CompoundTag) {
                  list.forEach((tag3) -> {
                     if (tag3 instanceof CompoundTag) {
                        ((CompoundTag)tag1).merge((CompoundTag)tag3);
                     }

                  });
               }

            });
         }
      };

      final String name;

      public abstract void merge(Tag tag, NbtPathArgument.NbtPath nbtpathargument_nbtpath, List<Tag> list) throws CommandSyntaxException;

      MergeStrategy(String s) {
         this.name = s;
      }

      public static CopyNbtFunction.MergeStrategy getByName(String s) {
         for(CopyNbtFunction.MergeStrategy copynbtfunction_mergestrategy : values()) {
            if (copynbtfunction_mergestrategy.name.equals(s)) {
               return copynbtfunction_mergestrategy;
            }
         }

         throw new IllegalArgumentException("Invalid merge strategy" + s);
      }
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<CopyNbtFunction> {
      public void serialize(JsonObject jsonobject, CopyNbtFunction copynbtfunction, JsonSerializationContext jsonserializationcontext) {
         super.serialize(jsonobject, copynbtfunction, jsonserializationcontext);
         jsonobject.add("source", jsonserializationcontext.serialize(copynbtfunction.source));
         JsonArray jsonarray = new JsonArray();
         copynbtfunction.operations.stream().map(CopyNbtFunction.CopyOperation::toJson).forEach(jsonarray::add);
         jsonobject.add("ops", jsonarray);
      }

      public CopyNbtFunction deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext, LootItemCondition[] alootitemcondition) {
         NbtProvider nbtprovider = GsonHelper.getAsObject(jsonobject, "source", jsondeserializationcontext, NbtProvider.class);
         List<CopyNbtFunction.CopyOperation> list = Lists.newArrayList();

         for(JsonElement jsonelement : GsonHelper.getAsJsonArray(jsonobject, "ops")) {
            JsonObject jsonobject1 = GsonHelper.convertToJsonObject(jsonelement, "op");
            list.add(CopyNbtFunction.CopyOperation.fromJson(jsonobject1));
         }

         return new CopyNbtFunction(alootitemcondition, nbtprovider, list);
      }
   }
}
