package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ApplyBonusCount extends LootItemConditionalFunction {
   static final Map<ResourceLocation, ApplyBonusCount.FormulaDeserializer> FORMULAS = Maps.newHashMap();
   final Enchantment enchantment;
   final ApplyBonusCount.Formula formula;

   ApplyBonusCount(LootItemCondition[] alootitemcondition, Enchantment enchantment, ApplyBonusCount.Formula applybonuscount_formula) {
      super(alootitemcondition);
      this.enchantment = enchantment;
      this.formula = applybonuscount_formula;
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.APPLY_BONUS;
   }

   public Set<LootContextParam<?>> getReferencedContextParams() {
      return ImmutableSet.of(LootContextParams.TOOL);
   }

   public ItemStack run(ItemStack itemstack, LootContext lootcontext) {
      ItemStack itemstack1 = lootcontext.getParamOrNull(LootContextParams.TOOL);
      if (itemstack1 != null) {
         int i = EnchantmentHelper.getItemEnchantmentLevel(this.enchantment, itemstack1);
         int j = this.formula.calculateNewCount(lootcontext.getRandom(), itemstack.getCount(), i);
         itemstack.setCount(j);
      }

      return itemstack;
   }

   public static LootItemConditionalFunction.Builder<?> addBonusBinomialDistributionCount(Enchantment enchantment, float f, int i) {
      return simpleBuilder((alootitemcondition) -> new ApplyBonusCount(alootitemcondition, enchantment, new ApplyBonusCount.BinomialWithBonusCount(i, f)));
   }

   public static LootItemConditionalFunction.Builder<?> addOreBonusCount(Enchantment enchantment) {
      return simpleBuilder((alootitemcondition) -> new ApplyBonusCount(alootitemcondition, enchantment, new ApplyBonusCount.OreDrops()));
   }

   public static LootItemConditionalFunction.Builder<?> addUniformBonusCount(Enchantment enchantment) {
      return simpleBuilder((alootitemcondition) -> new ApplyBonusCount(alootitemcondition, enchantment, new ApplyBonusCount.UniformBonusCount(1)));
   }

   public static LootItemConditionalFunction.Builder<?> addUniformBonusCount(Enchantment enchantment, int i) {
      return simpleBuilder((alootitemcondition) -> new ApplyBonusCount(alootitemcondition, enchantment, new ApplyBonusCount.UniformBonusCount(i)));
   }

   static {
      FORMULAS.put(ApplyBonusCount.BinomialWithBonusCount.TYPE, ApplyBonusCount.BinomialWithBonusCount::deserialize);
      FORMULAS.put(ApplyBonusCount.OreDrops.TYPE, ApplyBonusCount.OreDrops::deserialize);
      FORMULAS.put(ApplyBonusCount.UniformBonusCount.TYPE, ApplyBonusCount.UniformBonusCount::deserialize);
   }

   static final class BinomialWithBonusCount implements ApplyBonusCount.Formula {
      public static final ResourceLocation TYPE = new ResourceLocation("binomial_with_bonus_count");
      private final int extraRounds;
      private final float probability;

      public BinomialWithBonusCount(int i, float f) {
         this.extraRounds = i;
         this.probability = f;
      }

      public int calculateNewCount(RandomSource randomsource, int i, int j) {
         for(int k = 0; k < j + this.extraRounds; ++k) {
            if (randomsource.nextFloat() < this.probability) {
               ++i;
            }
         }

         return i;
      }

      public void serializeParams(JsonObject jsonobject, JsonSerializationContext jsonserializationcontext) {
         jsonobject.addProperty("extra", this.extraRounds);
         jsonobject.addProperty("probability", this.probability);
      }

      public static ApplyBonusCount.Formula deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext) {
         int i = GsonHelper.getAsInt(jsonobject, "extra");
         float f = GsonHelper.getAsFloat(jsonobject, "probability");
         return new ApplyBonusCount.BinomialWithBonusCount(i, f);
      }

      public ResourceLocation getType() {
         return TYPE;
      }
   }

   interface Formula {
      int calculateNewCount(RandomSource randomsource, int i, int j);

      void serializeParams(JsonObject jsonobject, JsonSerializationContext jsonserializationcontext);

      ResourceLocation getType();
   }

   interface FormulaDeserializer {
      ApplyBonusCount.Formula deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext);
   }

   static final class OreDrops implements ApplyBonusCount.Formula {
      public static final ResourceLocation TYPE = new ResourceLocation("ore_drops");

      public int calculateNewCount(RandomSource randomsource, int i, int j) {
         if (j > 0) {
            int k = randomsource.nextInt(j + 2) - 1;
            if (k < 0) {
               k = 0;
            }

            return i * (k + 1);
         } else {
            return i;
         }
      }

      public void serializeParams(JsonObject jsonobject, JsonSerializationContext jsonserializationcontext) {
      }

      public static ApplyBonusCount.Formula deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext) {
         return new ApplyBonusCount.OreDrops();
      }

      public ResourceLocation getType() {
         return TYPE;
      }
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<ApplyBonusCount> {
      public void serialize(JsonObject jsonobject, ApplyBonusCount applybonuscount, JsonSerializationContext jsonserializationcontext) {
         super.serialize(jsonobject, applybonuscount, jsonserializationcontext);
         jsonobject.addProperty("enchantment", BuiltInRegistries.ENCHANTMENT.getKey(applybonuscount.enchantment).toString());
         jsonobject.addProperty("formula", applybonuscount.formula.getType().toString());
         JsonObject jsonobject1 = new JsonObject();
         applybonuscount.formula.serializeParams(jsonobject1, jsonserializationcontext);
         if (jsonobject1.size() > 0) {
            jsonobject.add("parameters", jsonobject1);
         }

      }

      public ApplyBonusCount deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext, LootItemCondition[] alootitemcondition) {
         ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(jsonobject, "enchantment"));
         Enchantment enchantment = BuiltInRegistries.ENCHANTMENT.getOptional(resourcelocation).orElseThrow(() -> new JsonParseException("Invalid enchantment id: " + resourcelocation));
         ResourceLocation resourcelocation1 = new ResourceLocation(GsonHelper.getAsString(jsonobject, "formula"));
         ApplyBonusCount.FormulaDeserializer applybonuscount_formuladeserializer = ApplyBonusCount.FORMULAS.get(resourcelocation1);
         if (applybonuscount_formuladeserializer == null) {
            throw new JsonParseException("Invalid formula id: " + resourcelocation1);
         } else {
            ApplyBonusCount.Formula applybonuscount_formula;
            if (jsonobject.has("parameters")) {
               applybonuscount_formula = applybonuscount_formuladeserializer.deserialize(GsonHelper.getAsJsonObject(jsonobject, "parameters"), jsondeserializationcontext);
            } else {
               applybonuscount_formula = applybonuscount_formuladeserializer.deserialize(new JsonObject(), jsondeserializationcontext);
            }

            return new ApplyBonusCount(alootitemcondition, enchantment, applybonuscount_formula);
         }
      }
   }

   static final class UniformBonusCount implements ApplyBonusCount.Formula {
      public static final ResourceLocation TYPE = new ResourceLocation("uniform_bonus_count");
      private final int bonusMultiplier;

      public UniformBonusCount(int i) {
         this.bonusMultiplier = i;
      }

      public int calculateNewCount(RandomSource randomsource, int i, int j) {
         return i + randomsource.nextInt(this.bonusMultiplier * j + 1);
      }

      public void serializeParams(JsonObject jsonobject, JsonSerializationContext jsonserializationcontext) {
         jsonobject.addProperty("bonusMultiplier", this.bonusMultiplier);
      }

      public static ApplyBonusCount.Formula deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext) {
         int i = GsonHelper.getAsInt(jsonobject, "bonusMultiplier");
         return new ApplyBonusCount.UniformBonusCount(i);
      }

      public ResourceLocation getType() {
         return TYPE;
      }
   }
}
