package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetLoreFunction extends LootItemConditionalFunction {
   final boolean replace;
   final List<Component> lore;
   @Nullable
   final LootContext.EntityTarget resolutionContext;

   public SetLoreFunction(LootItemCondition[] alootitemcondition, boolean flag, List<Component> list, @Nullable LootContext.EntityTarget lootcontext_entitytarget) {
      super(alootitemcondition);
      this.replace = flag;
      this.lore = ImmutableList.copyOf(list);
      this.resolutionContext = lootcontext_entitytarget;
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.SET_LORE;
   }

   public Set<LootContextParam<?>> getReferencedContextParams() {
      return this.resolutionContext != null ? ImmutableSet.of(this.resolutionContext.getParam()) : ImmutableSet.of();
   }

   public ItemStack run(ItemStack itemstack, LootContext lootcontext) {
      ListTag listtag = this.getLoreTag(itemstack, !this.lore.isEmpty());
      if (listtag != null) {
         if (this.replace) {
            listtag.clear();
         }

         UnaryOperator<Component> unaryoperator = SetNameFunction.createResolver(lootcontext, this.resolutionContext);
         this.lore.stream().map(unaryoperator).map(Component.Serializer::toJson).map(StringTag::valueOf).forEach(listtag::add);
      }

      return itemstack;
   }

   @Nullable
   private ListTag getLoreTag(ItemStack itemstack, boolean flag) {
      CompoundTag compoundtag;
      if (itemstack.hasTag()) {
         compoundtag = itemstack.getTag();
      } else {
         if (!flag) {
            return null;
         }

         compoundtag = new CompoundTag();
         itemstack.setTag(compoundtag);
      }

      CompoundTag compoundtag3;
      if (compoundtag.contains("display", 10)) {
         compoundtag3 = compoundtag.getCompound("display");
      } else {
         if (!flag) {
            return null;
         }

         compoundtag3 = new CompoundTag();
         compoundtag.put("display", compoundtag3);
      }

      if (compoundtag3.contains("Lore", 9)) {
         return compoundtag3.getList("Lore", 8);
      } else if (flag) {
         ListTag listtag = new ListTag();
         compoundtag3.put("Lore", listtag);
         return listtag;
      } else {
         return null;
      }
   }

   public static SetLoreFunction.Builder setLore() {
      return new SetLoreFunction.Builder();
   }

   public static class Builder extends LootItemConditionalFunction.Builder<SetLoreFunction.Builder> {
      private boolean replace;
      private LootContext.EntityTarget resolutionContext;
      private final List<Component> lore = Lists.newArrayList();

      public SetLoreFunction.Builder setReplace(boolean flag) {
         this.replace = flag;
         return this;
      }

      public SetLoreFunction.Builder setResolutionContext(LootContext.EntityTarget lootcontext_entitytarget) {
         this.resolutionContext = lootcontext_entitytarget;
         return this;
      }

      public SetLoreFunction.Builder addLine(Component component) {
         this.lore.add(component);
         return this;
      }

      protected SetLoreFunction.Builder getThis() {
         return this;
      }

      public LootItemFunction build() {
         return new SetLoreFunction(this.getConditions(), this.replace, this.lore, this.resolutionContext);
      }
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<SetLoreFunction> {
      public void serialize(JsonObject jsonobject, SetLoreFunction setlorefunction, JsonSerializationContext jsonserializationcontext) {
         super.serialize(jsonobject, setlorefunction, jsonserializationcontext);
         jsonobject.addProperty("replace", setlorefunction.replace);
         JsonArray jsonarray = new JsonArray();

         for(Component component : setlorefunction.lore) {
            jsonarray.add(Component.Serializer.toJsonTree(component));
         }

         jsonobject.add("lore", jsonarray);
         if (setlorefunction.resolutionContext != null) {
            jsonobject.add("entity", jsonserializationcontext.serialize(setlorefunction.resolutionContext));
         }

      }

      public SetLoreFunction deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext, LootItemCondition[] alootitemcondition) {
         boolean flag = GsonHelper.getAsBoolean(jsonobject, "replace", false);
         List<Component> list = Streams.stream(GsonHelper.getAsJsonArray(jsonobject, "lore")).map(Component.Serializer::fromJson).collect(ImmutableList.toImmutableList());
         LootContext.EntityTarget lootcontext_entitytarget = GsonHelper.getAsObject(jsonobject, "entity", (LootContext.EntityTarget)null, jsondeserializationcontext, LootContext.EntityTarget.class);
         return new SetLoreFunction(alootitemcondition, flag, list, lootcontext_entitytarget);
      }
   }
}
