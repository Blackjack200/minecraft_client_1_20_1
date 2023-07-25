package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ApplyExplosionDecay extends LootItemConditionalFunction {
   ApplyExplosionDecay(LootItemCondition[] alootitemcondition) {
      super(alootitemcondition);
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.EXPLOSION_DECAY;
   }

   public ItemStack run(ItemStack itemstack, LootContext lootcontext) {
      Float ofloat = lootcontext.getParamOrNull(LootContextParams.EXPLOSION_RADIUS);
      if (ofloat != null) {
         RandomSource randomsource = lootcontext.getRandom();
         float f = 1.0F / ofloat;
         int i = itemstack.getCount();
         int j = 0;

         for(int k = 0; k < i; ++k) {
            if (randomsource.nextFloat() <= f) {
               ++j;
            }
         }

         itemstack.setCount(j);
      }

      return itemstack;
   }

   public static LootItemConditionalFunction.Builder<?> explosionDecay() {
      return simpleBuilder(ApplyExplosionDecay::new);
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<ApplyExplosionDecay> {
      public ApplyExplosionDecay deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext, LootItemCondition[] alootitemcondition) {
         return new ApplyExplosionDecay(alootitemcondition);
      }
   }
}
