package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetContainerContents extends LootItemConditionalFunction {
   final List<LootPoolEntryContainer> entries;
   final BlockEntityType<?> type;

   SetContainerContents(LootItemCondition[] alootitemcondition, BlockEntityType<?> blockentitytype, List<LootPoolEntryContainer> list) {
      super(alootitemcondition);
      this.type = blockentitytype;
      this.entries = ImmutableList.copyOf(list);
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.SET_CONTENTS;
   }

   public ItemStack run(ItemStack itemstack, LootContext lootcontext) {
      if (itemstack.isEmpty()) {
         return itemstack;
      } else {
         NonNullList<ItemStack> nonnulllist = NonNullList.create();
         this.entries.forEach((lootpoolentrycontainer) -> lootpoolentrycontainer.expand(lootcontext, (lootpoolentry) -> lootpoolentry.createItemStack(LootTable.createStackSplitter(lootcontext.getLevel(), nonnulllist::add), lootcontext)));
         CompoundTag compoundtag = new CompoundTag();
         ContainerHelper.saveAllItems(compoundtag, nonnulllist);
         CompoundTag compoundtag1 = BlockItem.getBlockEntityData(itemstack);
         if (compoundtag1 == null) {
            compoundtag1 = compoundtag;
         } else {
            compoundtag1.merge(compoundtag);
         }

         BlockItem.setBlockEntityData(itemstack, this.type, compoundtag1);
         return itemstack;
      }
   }

   public void validate(ValidationContext validationcontext) {
      super.validate(validationcontext);

      for(int i = 0; i < this.entries.size(); ++i) {
         this.entries.get(i).validate(validationcontext.forChild(".entry[" + i + "]"));
      }

   }

   public static SetContainerContents.Builder setContents(BlockEntityType<?> blockentitytype) {
      return new SetContainerContents.Builder(blockentitytype);
   }

   public static class Builder extends LootItemConditionalFunction.Builder<SetContainerContents.Builder> {
      private final List<LootPoolEntryContainer> entries = Lists.newArrayList();
      private final BlockEntityType<?> type;

      public Builder(BlockEntityType<?> blockentitytype) {
         this.type = blockentitytype;
      }

      protected SetContainerContents.Builder getThis() {
         return this;
      }

      public SetContainerContents.Builder withEntry(LootPoolEntryContainer.Builder<?> lootpoolentrycontainer_builder) {
         this.entries.add(lootpoolentrycontainer_builder.build());
         return this;
      }

      public LootItemFunction build() {
         return new SetContainerContents(this.getConditions(), this.type, this.entries);
      }
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<SetContainerContents> {
      public void serialize(JsonObject jsonobject, SetContainerContents setcontainercontents, JsonSerializationContext jsonserializationcontext) {
         super.serialize(jsonobject, setcontainercontents, jsonserializationcontext);
         jsonobject.addProperty("type", BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(setcontainercontents.type).toString());
         jsonobject.add("entries", jsonserializationcontext.serialize(setcontainercontents.entries));
      }

      public SetContainerContents deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext, LootItemCondition[] alootitemcondition) {
         LootPoolEntryContainer[] alootpoolentrycontainer = GsonHelper.getAsObject(jsonobject, "entries", jsondeserializationcontext, LootPoolEntryContainer[].class);
         ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(jsonobject, "type"));
         BlockEntityType<?> blockentitytype = BuiltInRegistries.BLOCK_ENTITY_TYPE.getOptional(resourcelocation).orElseThrow(() -> new JsonSyntaxException("Unknown block entity type id '" + resourcelocation + "'"));
         return new SetContainerContents(alootitemcondition, blockentitytype, Arrays.asList(alootpoolentrycontainer));
      }
   }
}
