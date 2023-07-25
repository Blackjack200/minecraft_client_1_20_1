package net.minecraft.world.level.storage.loot.providers.number;

import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

public final class BinomialDistributionGenerator implements NumberProvider {
   final NumberProvider n;
   final NumberProvider p;

   BinomialDistributionGenerator(NumberProvider numberprovider, NumberProvider numberprovider1) {
      this.n = numberprovider;
      this.p = numberprovider1;
   }

   public LootNumberProviderType getType() {
      return NumberProviders.BINOMIAL;
   }

   public int getInt(LootContext lootcontext) {
      int i = this.n.getInt(lootcontext);
      float f = this.p.getFloat(lootcontext);
      RandomSource randomsource = lootcontext.getRandom();
      int j = 0;

      for(int k = 0; k < i; ++k) {
         if (randomsource.nextFloat() < f) {
            ++j;
         }
      }

      return j;
   }

   public float getFloat(LootContext lootcontext) {
      return (float)this.getInt(lootcontext);
   }

   public static BinomialDistributionGenerator binomial(int i, float f) {
      return new BinomialDistributionGenerator(ConstantValue.exactly((float)i), ConstantValue.exactly(f));
   }

   public Set<LootContextParam<?>> getReferencedContextParams() {
      return Sets.union(this.n.getReferencedContextParams(), this.p.getReferencedContextParams());
   }

   public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<BinomialDistributionGenerator> {
      public BinomialDistributionGenerator deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext) {
         NumberProvider numberprovider = GsonHelper.getAsObject(jsonobject, "n", jsondeserializationcontext, NumberProvider.class);
         NumberProvider numberprovider1 = GsonHelper.getAsObject(jsonobject, "p", jsondeserializationcontext, NumberProvider.class);
         return new BinomialDistributionGenerator(numberprovider, numberprovider1);
      }

      public void serialize(JsonObject jsonobject, BinomialDistributionGenerator binomialdistributiongenerator, JsonSerializationContext jsonserializationcontext) {
         jsonobject.add("n", jsonserializationcontext.serialize(binomialdistributiongenerator.n));
         jsonobject.add("p", jsonserializationcontext.serialize(binomialdistributiongenerator.p));
      }
   }
}
