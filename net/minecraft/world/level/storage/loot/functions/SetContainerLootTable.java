package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootDataId;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetContainerLootTable extends LootItemConditionalFunction {
   final ResourceLocation name;
   final long seed;
   final BlockEntityType<?> type;

   SetContainerLootTable(LootItemCondition[] alootitemcondition, ResourceLocation resourcelocation, long i, BlockEntityType<?> blockentitytype) {
      super(alootitemcondition);
      this.name = resourcelocation;
      this.seed = i;
      this.type = blockentitytype;
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.SET_LOOT_TABLE;
   }

   public ItemStack run(ItemStack itemstack, LootContext lootcontext) {
      if (itemstack.isEmpty()) {
         return itemstack;
      } else {
         CompoundTag compoundtag = BlockItem.getBlockEntityData(itemstack);
         if (compoundtag == null) {
            compoundtag = new CompoundTag();
         }

         compoundtag.putString("LootTable", this.name.toString());
         if (this.seed != 0L) {
            compoundtag.putLong("LootTableSeed", this.seed);
         }

         BlockItem.setBlockEntityData(itemstack, this.type, compoundtag);
         return itemstack;
      }
   }

   public void validate(ValidationContext validationcontext) {
      super.validate(validationcontext);
      LootDataId<LootTable> lootdataid = new LootDataId<>(LootDataType.TABLE, this.name);
      if (validationcontext.resolver().getElementOptional(lootdataid).isEmpty()) {
         validationcontext.reportProblem("Missing loot table used for container: " + this.name);
      }

   }

   public static LootItemConditionalFunction.Builder<?> withLootTable(BlockEntityType<?> blockentitytype, ResourceLocation resourcelocation) {
      return simpleBuilder((alootitemcondition) -> new SetContainerLootTable(alootitemcondition, resourcelocation, 0L, blockentitytype));
   }

   public static LootItemConditionalFunction.Builder<?> withLootTable(BlockEntityType<?> blockentitytype, ResourceLocation resourcelocation, long i) {
      return simpleBuilder((alootitemcondition) -> new SetContainerLootTable(alootitemcondition, resourcelocation, i, blockentitytype));
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<SetContainerLootTable> {
      public void serialize(JsonObject jsonobject, SetContainerLootTable setcontainerloottable, JsonSerializationContext jsonserializationcontext) {
         super.serialize(jsonobject, setcontainerloottable, jsonserializationcontext);
         jsonobject.addProperty("name", setcontainerloottable.name.toString());
         jsonobject.addProperty("type", BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(setcontainerloottable.type).toString());
         if (setcontainerloottable.seed != 0L) {
            jsonobject.addProperty("seed", setcontainerloottable.seed);
         }

      }

      public SetContainerLootTable deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext, LootItemCondition[] alootitemcondition) {
         ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(jsonobject, "name"));
         long i = GsonHelper.getAsLong(jsonobject, "seed", 0L);
         ResourceLocation resourcelocation1 = new ResourceLocation(GsonHelper.getAsString(jsonobject, "type"));
         BlockEntityType<?> blockentitytype = BuiltInRegistries.BLOCK_ENTITY_TYPE.getOptional(resourcelocation1).orElseThrow(() -> new JsonSyntaxException("Unknown block entity type id '" + resourcelocation1 + "'"));
         return new SetContainerLootTable(alootitemcondition, resourcelocation, i, blockentitytype);
      }
   }
}
