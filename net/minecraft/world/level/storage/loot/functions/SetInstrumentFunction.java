package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.InstrumentItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetInstrumentFunction extends LootItemConditionalFunction {
   final TagKey<Instrument> options;

   SetInstrumentFunction(LootItemCondition[] alootitemcondition, TagKey<Instrument> tagkey) {
      super(alootitemcondition);
      this.options = tagkey;
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.SET_INSTRUMENT;
   }

   public ItemStack run(ItemStack itemstack, LootContext lootcontext) {
      InstrumentItem.setRandom(itemstack, this.options, lootcontext.getRandom());
      return itemstack;
   }

   public static LootItemConditionalFunction.Builder<?> setInstrumentOptions(TagKey<Instrument> tagkey) {
      return simpleBuilder((alootitemcondition) -> new SetInstrumentFunction(alootitemcondition, tagkey));
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<SetInstrumentFunction> {
      public void serialize(JsonObject jsonobject, SetInstrumentFunction setinstrumentfunction, JsonSerializationContext jsonserializationcontext) {
         super.serialize(jsonobject, setinstrumentfunction, jsonserializationcontext);
         jsonobject.addProperty("options", "#" + setinstrumentfunction.options.location());
      }

      public SetInstrumentFunction deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext, LootItemCondition[] alootitemcondition) {
         String s = GsonHelper.getAsString(jsonobject, "options");
         if (!s.startsWith("#")) {
            throw new JsonSyntaxException("Inline tag value not supported: " + s);
         } else {
            return new SetInstrumentFunction(alootitemcondition, TagKey.create(Registries.INSTRUMENT, new ResourceLocation(s.substring(1))));
         }
      }
   }
}
