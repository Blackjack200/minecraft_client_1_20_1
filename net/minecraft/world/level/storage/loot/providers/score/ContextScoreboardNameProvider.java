package net.minecraft.world.level.storage.loot.providers.score;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.GsonAdapterFactory;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

public class ContextScoreboardNameProvider implements ScoreboardNameProvider {
   final LootContext.EntityTarget target;

   ContextScoreboardNameProvider(LootContext.EntityTarget lootcontext_entitytarget) {
      this.target = lootcontext_entitytarget;
   }

   public static ScoreboardNameProvider forTarget(LootContext.EntityTarget lootcontext_entitytarget) {
      return new ContextScoreboardNameProvider(lootcontext_entitytarget);
   }

   public LootScoreProviderType getType() {
      return ScoreboardNameProviders.CONTEXT;
   }

   @Nullable
   public String getScoreboardName(LootContext lootcontext) {
      Entity entity = lootcontext.getParamOrNull(this.target.getParam());
      return entity != null ? entity.getScoreboardName() : null;
   }

   public Set<LootContextParam<?>> getReferencedContextParams() {
      return ImmutableSet.of(this.target.getParam());
   }

   public static class InlineSerializer implements GsonAdapterFactory.InlineSerializer<ContextScoreboardNameProvider> {
      public JsonElement serialize(ContextScoreboardNameProvider contextscoreboardnameprovider, JsonSerializationContext jsonserializationcontext) {
         return jsonserializationcontext.serialize(contextscoreboardnameprovider.target);
      }

      public ContextScoreboardNameProvider deserialize(JsonElement jsonelement, JsonDeserializationContext jsondeserializationcontext) {
         LootContext.EntityTarget lootcontext_entitytarget = jsondeserializationcontext.deserialize(jsonelement, LootContext.EntityTarget.class);
         return new ContextScoreboardNameProvider(lootcontext_entitytarget);
      }
   }

   public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<ContextScoreboardNameProvider> {
      public void serialize(JsonObject jsonobject, ContextScoreboardNameProvider contextscoreboardnameprovider, JsonSerializationContext jsonserializationcontext) {
         jsonobject.addProperty("target", contextscoreboardnameprovider.target.name());
      }

      public ContextScoreboardNameProvider deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext) {
         LootContext.EntityTarget lootcontext_entitytarget = GsonHelper.getAsObject(jsonobject, "target", jsondeserializationcontext, LootContext.EntityTarget.class);
         return new ContextScoreboardNameProvider(lootcontext_entitytarget);
      }
   }
}
