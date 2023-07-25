package net.minecraft.client.renderer.block.model.multipart;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Streams;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.client.renderer.block.model.MultiVariant;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public class Selector {
   private final Condition condition;
   private final MultiVariant variant;

   public Selector(Condition condition, MultiVariant multivariant) {
      if (condition == null) {
         throw new IllegalArgumentException("Missing condition for selector");
      } else if (multivariant == null) {
         throw new IllegalArgumentException("Missing variant for selector");
      } else {
         this.condition = condition;
         this.variant = multivariant;
      }
   }

   public MultiVariant getVariant() {
      return this.variant;
   }

   public Predicate<BlockState> getPredicate(StateDefinition<Block, BlockState> statedefinition) {
      return this.condition.getPredicate(statedefinition);
   }

   public boolean equals(Object object) {
      return this == object;
   }

   public int hashCode() {
      return System.identityHashCode(this);
   }

   public static class Deserializer implements JsonDeserializer<Selector> {
      public Selector deserialize(JsonElement jsonelement, Type type, JsonDeserializationContext jsondeserializationcontext) throws JsonParseException {
         JsonObject jsonobject = jsonelement.getAsJsonObject();
         return new Selector(this.getSelector(jsonobject), jsondeserializationcontext.deserialize(jsonobject.get("apply"), MultiVariant.class));
      }

      private Condition getSelector(JsonObject jsonobject) {
         return jsonobject.has("when") ? getCondition(GsonHelper.getAsJsonObject(jsonobject, "when")) : Condition.TRUE;
      }

      @VisibleForTesting
      static Condition getCondition(JsonObject jsonobject) {
         Set<Map.Entry<String, JsonElement>> set = jsonobject.entrySet();
         if (set.isEmpty()) {
            throw new JsonParseException("No elements found in selector");
         } else if (set.size() == 1) {
            if (jsonobject.has("OR")) {
               List<Condition> list = Streams.stream(GsonHelper.getAsJsonArray(jsonobject, "OR")).map((jsonelement1) -> getCondition(jsonelement1.getAsJsonObject())).collect(Collectors.toList());
               return new OrCondition(list);
            } else if (jsonobject.has("AND")) {
               List<Condition> list1 = Streams.stream(GsonHelper.getAsJsonArray(jsonobject, "AND")).map((jsonelement) -> getCondition(jsonelement.getAsJsonObject())).collect(Collectors.toList());
               return new AndCondition(list1);
            } else {
               return getKeyValueCondition(set.iterator().next());
            }
         } else {
            return new AndCondition(set.stream().map(Selector.Deserializer::getKeyValueCondition).collect(Collectors.toList()));
         }
      }

      private static Condition getKeyValueCondition(Map.Entry<String, JsonElement> map_entry) {
         return new KeyValueCondition(map_entry.getKey(), map_entry.getValue().getAsString());
      }
   }
}
