package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public class EnchantWithLevelsFunction extends LootItemConditionalFunction {
   final NumberProvider levels;
   final boolean treasure;

   EnchantWithLevelsFunction(LootItemCondition[] alootitemcondition, NumberProvider numberprovider, boolean flag) {
      super(alootitemcondition);
      this.levels = numberprovider;
      this.treasure = flag;
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.ENCHANT_WITH_LEVELS;
   }

   public Set<LootContextParam<?>> getReferencedContextParams() {
      return this.levels.getReferencedContextParams();
   }

   public ItemStack run(ItemStack itemstack, LootContext lootcontext) {
      RandomSource randomsource = lootcontext.getRandom();
      return EnchantmentHelper.enchantItem(randomsource, itemstack, this.levels.getInt(lootcontext), this.treasure);
   }

   public static EnchantWithLevelsFunction.Builder enchantWithLevels(NumberProvider numberprovider) {
      return new EnchantWithLevelsFunction.Builder(numberprovider);
   }

   public static class Builder extends LootItemConditionalFunction.Builder<EnchantWithLevelsFunction.Builder> {
      private final NumberProvider levels;
      private boolean treasure;

      public Builder(NumberProvider numberprovider) {
         this.levels = numberprovider;
      }

      protected EnchantWithLevelsFunction.Builder getThis() {
         return this;
      }

      public EnchantWithLevelsFunction.Builder allowTreasure() {
         this.treasure = true;
         return this;
      }

      public LootItemFunction build() {
         return new EnchantWithLevelsFunction(this.getConditions(), this.levels, this.treasure);
      }
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<EnchantWithLevelsFunction> {
      public void serialize(JsonObject jsonobject, EnchantWithLevelsFunction enchantwithlevelsfunction, JsonSerializationContext jsonserializationcontext) {
         super.serialize(jsonobject, enchantwithlevelsfunction, jsonserializationcontext);
         jsonobject.add("levels", jsonserializationcontext.serialize(enchantwithlevelsfunction.levels));
         jsonobject.addProperty("treasure", enchantwithlevelsfunction.treasure);
      }

      public EnchantWithLevelsFunction deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext, LootItemCondition[] alootitemcondition) {
         NumberProvider numberprovider = GsonHelper.getAsObject(jsonobject, "levels", jsondeserializationcontext, NumberProvider.class);
         boolean flag = GsonHelper.getAsBoolean(jsonobject, "treasure", false);
         return new EnchantWithLevelsFunction(alootitemcondition, numberprovider, flag);
      }
   }
}
