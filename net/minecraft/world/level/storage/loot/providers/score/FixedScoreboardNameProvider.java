package net.minecraft.world.level.storage.loot.providers.score;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

public class FixedScoreboardNameProvider implements ScoreboardNameProvider {
   final String name;

   FixedScoreboardNameProvider(String s) {
      this.name = s;
   }

   public static ScoreboardNameProvider forName(String s) {
      return new FixedScoreboardNameProvider(s);
   }

   public LootScoreProviderType getType() {
      return ScoreboardNameProviders.FIXED;
   }

   public String getName() {
      return this.name;
   }

   @Nullable
   public String getScoreboardName(LootContext lootcontext) {
      return this.name;
   }

   public Set<LootContextParam<?>> getReferencedContextParams() {
      return ImmutableSet.of();
   }

   public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<FixedScoreboardNameProvider> {
      public void serialize(JsonObject jsonobject, FixedScoreboardNameProvider fixedscoreboardnameprovider, JsonSerializationContext jsonserializationcontext) {
         jsonobject.addProperty("name", fixedscoreboardnameprovider.name);
      }

      public FixedScoreboardNameProvider deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext) {
         String s = GsonHelper.getAsString(jsonobject, "name");
         return new FixedScoreboardNameProvider(s);
      }
   }
}
