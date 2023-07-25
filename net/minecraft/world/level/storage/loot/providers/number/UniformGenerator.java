package net.minecraft.world.level.storage.loot.providers.number;

import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

public class UniformGenerator implements NumberProvider {
   final NumberProvider min;
   final NumberProvider max;

   UniformGenerator(NumberProvider numberprovider, NumberProvider numberprovider1) {
      this.min = numberprovider;
      this.max = numberprovider1;
   }

   public LootNumberProviderType getType() {
      return NumberProviders.UNIFORM;
   }

   public static UniformGenerator between(float f, float f1) {
      return new UniformGenerator(ConstantValue.exactly(f), ConstantValue.exactly(f1));
   }

   public int getInt(LootContext lootcontext) {
      return Mth.nextInt(lootcontext.getRandom(), this.min.getInt(lootcontext), this.max.getInt(lootcontext));
   }

   public float getFloat(LootContext lootcontext) {
      return Mth.nextFloat(lootcontext.getRandom(), this.min.getFloat(lootcontext), this.max.getFloat(lootcontext));
   }

   public Set<LootContextParam<?>> getReferencedContextParams() {
      return Sets.union(this.min.getReferencedContextParams(), this.max.getReferencedContextParams());
   }

   public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<UniformGenerator> {
      public UniformGenerator deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext) {
         NumberProvider numberprovider = GsonHelper.getAsObject(jsonobject, "min", jsondeserializationcontext, NumberProvider.class);
         NumberProvider numberprovider1 = GsonHelper.getAsObject(jsonobject, "max", jsondeserializationcontext, NumberProvider.class);
         return new UniformGenerator(numberprovider, numberprovider1);
      }

      public void serialize(JsonObject jsonobject, UniformGenerator uniformgenerator, JsonSerializationContext jsonserializationcontext) {
         jsonobject.add("min", jsonserializationcontext.serialize(uniformgenerator.min));
         jsonobject.add("max", jsonserializationcontext.serialize(uniformgenerator.max));
      }
   }
}
