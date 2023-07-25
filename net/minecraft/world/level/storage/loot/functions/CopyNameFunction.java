package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Nameable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class CopyNameFunction extends LootItemConditionalFunction {
   final CopyNameFunction.NameSource source;

   CopyNameFunction(LootItemCondition[] alootitemcondition, CopyNameFunction.NameSource copynamefunction_namesource) {
      super(alootitemcondition);
      this.source = copynamefunction_namesource;
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.COPY_NAME;
   }

   public Set<LootContextParam<?>> getReferencedContextParams() {
      return ImmutableSet.of(this.source.param);
   }

   public ItemStack run(ItemStack itemstack, LootContext lootcontext) {
      Object object = lootcontext.getParamOrNull(this.source.param);
      if (object instanceof Nameable nameable) {
         if (nameable.hasCustomName()) {
            itemstack.setHoverName(nameable.getDisplayName());
         }
      }

      return itemstack;
   }

   public static LootItemConditionalFunction.Builder<?> copyName(CopyNameFunction.NameSource copynamefunction_namesource) {
      return simpleBuilder((alootitemcondition) -> new CopyNameFunction(alootitemcondition, copynamefunction_namesource));
   }

   public static enum NameSource {
      THIS("this", LootContextParams.THIS_ENTITY),
      KILLER("killer", LootContextParams.KILLER_ENTITY),
      KILLER_PLAYER("killer_player", LootContextParams.LAST_DAMAGE_PLAYER),
      BLOCK_ENTITY("block_entity", LootContextParams.BLOCK_ENTITY);

      public final String name;
      public final LootContextParam<?> param;

      private NameSource(String s, LootContextParam<?> lootcontextparam) {
         this.name = s;
         this.param = lootcontextparam;
      }

      public static CopyNameFunction.NameSource getByName(String s) {
         for(CopyNameFunction.NameSource copynamefunction_namesource : values()) {
            if (copynamefunction_namesource.name.equals(s)) {
               return copynamefunction_namesource;
            }
         }

         throw new IllegalArgumentException("Invalid name source " + s);
      }
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<CopyNameFunction> {
      public void serialize(JsonObject jsonobject, CopyNameFunction copynamefunction, JsonSerializationContext jsonserializationcontext) {
         super.serialize(jsonobject, copynamefunction, jsonserializationcontext);
         jsonobject.addProperty("source", copynamefunction.source.name);
      }

      public CopyNameFunction deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext, LootItemCondition[] alootitemcondition) {
         CopyNameFunction.NameSource copynamefunction_namesource = CopyNameFunction.NameSource.getByName(GsonHelper.getAsString(jsonobject, "source"));
         return new CopyNameFunction(alootitemcondition, copynamefunction_namesource);
      }
   }
}
