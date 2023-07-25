package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class BonusLevelTableCondition implements LootItemCondition {
   final Enchantment enchantment;
   final float[] values;

   BonusLevelTableCondition(Enchantment enchantment, float[] afloat) {
      this.enchantment = enchantment;
      this.values = afloat;
   }

   public LootItemConditionType getType() {
      return LootItemConditions.TABLE_BONUS;
   }

   public Set<LootContextParam<?>> getReferencedContextParams() {
      return ImmutableSet.of(LootContextParams.TOOL);
   }

   public boolean test(LootContext lootcontext) {
      ItemStack itemstack = lootcontext.getParamOrNull(LootContextParams.TOOL);
      int i = itemstack != null ? EnchantmentHelper.getItemEnchantmentLevel(this.enchantment, itemstack) : 0;
      float f = this.values[Math.min(i, this.values.length - 1)];
      return lootcontext.getRandom().nextFloat() < f;
   }

   public static LootItemCondition.Builder bonusLevelFlatChance(Enchantment enchantment, float... afloat) {
      return () -> new BonusLevelTableCondition(enchantment, afloat);
   }

   public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<BonusLevelTableCondition> {
      public void serialize(JsonObject jsonobject, BonusLevelTableCondition bonusleveltablecondition, JsonSerializationContext jsonserializationcontext) {
         jsonobject.addProperty("enchantment", BuiltInRegistries.ENCHANTMENT.getKey(bonusleveltablecondition.enchantment).toString());
         jsonobject.add("chances", jsonserializationcontext.serialize(bonusleveltablecondition.values));
      }

      public BonusLevelTableCondition deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext) {
         ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(jsonobject, "enchantment"));
         Enchantment enchantment = BuiltInRegistries.ENCHANTMENT.getOptional(resourcelocation).orElseThrow(() -> new JsonParseException("Invalid enchantment id: " + resourcelocation));
         float[] afloat = GsonHelper.getAsObject(jsonobject, "chances", jsondeserializationcontext, float[].class);
         return new BonusLevelTableCondition(enchantment, afloat);
      }
   }
}
