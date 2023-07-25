package net.minecraft.world.level.storage.loot.providers.number;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.providers.score.ContextScoreboardNameProvider;
import net.minecraft.world.level.storage.loot.providers.score.ScoreboardNameProvider;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;

public class ScoreboardValue implements NumberProvider {
   final ScoreboardNameProvider target;
   final String score;
   final float scale;

   ScoreboardValue(ScoreboardNameProvider scoreboardnameprovider, String s, float f) {
      this.target = scoreboardnameprovider;
      this.score = s;
      this.scale = f;
   }

   public LootNumberProviderType getType() {
      return NumberProviders.SCORE;
   }

   public Set<LootContextParam<?>> getReferencedContextParams() {
      return this.target.getReferencedContextParams();
   }

   public static ScoreboardValue fromScoreboard(LootContext.EntityTarget lootcontext_entitytarget, String s) {
      return fromScoreboard(lootcontext_entitytarget, s, 1.0F);
   }

   public static ScoreboardValue fromScoreboard(LootContext.EntityTarget lootcontext_entitytarget, String s, float f) {
      return new ScoreboardValue(ContextScoreboardNameProvider.forTarget(lootcontext_entitytarget), s, f);
   }

   public float getFloat(LootContext lootcontext) {
      String s = this.target.getScoreboardName(lootcontext);
      if (s == null) {
         return 0.0F;
      } else {
         Scoreboard scoreboard = lootcontext.getLevel().getScoreboard();
         Objective objective = scoreboard.getObjective(this.score);
         if (objective == null) {
            return 0.0F;
         } else {
            return !scoreboard.hasPlayerScore(s, objective) ? 0.0F : (float)scoreboard.getOrCreatePlayerScore(s, objective).getScore() * this.scale;
         }
      }
   }

   public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<ScoreboardValue> {
      public ScoreboardValue deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext) {
         String s = GsonHelper.getAsString(jsonobject, "score");
         float f = GsonHelper.getAsFloat(jsonobject, "scale", 1.0F);
         ScoreboardNameProvider scoreboardnameprovider = GsonHelper.getAsObject(jsonobject, "target", jsondeserializationcontext, ScoreboardNameProvider.class);
         return new ScoreboardValue(scoreboardnameprovider, s, f);
      }

      public void serialize(JsonObject jsonobject, ScoreboardValue scoreboardvalue, JsonSerializationContext jsonserializationcontext) {
         jsonobject.addProperty("score", scoreboardvalue.score);
         jsonobject.add("target", jsonserializationcontext.serialize(scoreboardvalue.target));
         jsonobject.addProperty("scale", scoreboardvalue.scale);
      }
   }
}
