package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public class LootingEnchantFunction extends LootItemConditionalFunction {
   public static final int NO_LIMIT = 0;
   final NumberProvider value;
   final int limit;

   LootingEnchantFunction(LootItemCondition[] alootitemcondition, NumberProvider numberprovider, int i) {
      super(alootitemcondition);
      this.value = numberprovider;
      this.limit = i;
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.LOOTING_ENCHANT;
   }

   public Set<LootContextParam<?>> getReferencedContextParams() {
      return Sets.union(ImmutableSet.of(LootContextParams.KILLER_ENTITY), this.value.getReferencedContextParams());
   }

   boolean hasLimit() {
      return this.limit > 0;
   }

   public ItemStack run(ItemStack itemstack, LootContext lootcontext) {
      Entity entity = lootcontext.getParamOrNull(LootContextParams.KILLER_ENTITY);
      if (entity instanceof LivingEntity) {
         int i = EnchantmentHelper.getMobLooting((LivingEntity)entity);
         if (i == 0) {
            return itemstack;
         }

         float f = (float)i * this.value.getFloat(lootcontext);
         itemstack.grow(Math.round(f));
         if (this.hasLimit() && itemstack.getCount() > this.limit) {
            itemstack.setCount(this.limit);
         }
      }

      return itemstack;
   }

   public static LootingEnchantFunction.Builder lootingMultiplier(NumberProvider numberprovider) {
      return new LootingEnchantFunction.Builder(numberprovider);
   }

   public static class Builder extends LootItemConditionalFunction.Builder<LootingEnchantFunction.Builder> {
      private final NumberProvider count;
      private int limit = 0;

      public Builder(NumberProvider numberprovider) {
         this.count = numberprovider;
      }

      protected LootingEnchantFunction.Builder getThis() {
         return this;
      }

      public LootingEnchantFunction.Builder setLimit(int i) {
         this.limit = i;
         return this;
      }

      public LootItemFunction build() {
         return new LootingEnchantFunction(this.getConditions(), this.count, this.limit);
      }
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<LootingEnchantFunction> {
      public void serialize(JsonObject jsonobject, LootingEnchantFunction lootingenchantfunction, JsonSerializationContext jsonserializationcontext) {
         super.serialize(jsonobject, lootingenchantfunction, jsonserializationcontext);
         jsonobject.add("count", jsonserializationcontext.serialize(lootingenchantfunction.value));
         if (lootingenchantfunction.hasLimit()) {
            jsonobject.add("limit", jsonserializationcontext.serialize(lootingenchantfunction.limit));
         }

      }

      public LootingEnchantFunction deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext, LootItemCondition[] alootitemcondition) {
         int i = GsonHelper.getAsInt(jsonobject, "limit", 0);
         return new LootingEnchantFunction(alootitemcondition, GsonHelper.getAsObject(jsonobject, "count", jsondeserializationcontext, NumberProvider.class), i);
      }
   }
}
