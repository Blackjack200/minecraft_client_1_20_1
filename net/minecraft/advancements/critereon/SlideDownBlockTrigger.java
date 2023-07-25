package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class SlideDownBlockTrigger extends SimpleCriterionTrigger<SlideDownBlockTrigger.TriggerInstance> {
   static final ResourceLocation ID = new ResourceLocation("slide_down_block");

   public ResourceLocation getId() {
      return ID;
   }

   public SlideDownBlockTrigger.TriggerInstance createInstance(JsonObject jsonobject, ContextAwarePredicate contextawarepredicate, DeserializationContext deserializationcontext) {
      Block block = deserializeBlock(jsonobject);
      StatePropertiesPredicate statepropertiespredicate = StatePropertiesPredicate.fromJson(jsonobject.get("state"));
      if (block != null) {
         statepropertiespredicate.checkState(block.getStateDefinition(), (s) -> {
            throw new JsonSyntaxException("Block " + block + " has no property " + s);
         });
      }

      return new SlideDownBlockTrigger.TriggerInstance(contextawarepredicate, block, statepropertiespredicate);
   }

   @Nullable
   private static Block deserializeBlock(JsonObject jsonobject) {
      if (jsonobject.has("block")) {
         ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(jsonobject, "block"));
         return BuiltInRegistries.BLOCK.getOptional(resourcelocation).orElseThrow(() -> new JsonSyntaxException("Unknown block type '" + resourcelocation + "'"));
      } else {
         return null;
      }
   }

   public void trigger(ServerPlayer serverplayer, BlockState blockstate) {
      this.trigger(serverplayer, (slidedownblocktrigger_triggerinstance) -> slidedownblocktrigger_triggerinstance.matches(blockstate));
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      @Nullable
      private final Block block;
      private final StatePropertiesPredicate state;

      public TriggerInstance(ContextAwarePredicate contextawarepredicate, @Nullable Block block, StatePropertiesPredicate statepropertiespredicate) {
         super(SlideDownBlockTrigger.ID, contextawarepredicate);
         this.block = block;
         this.state = statepropertiespredicate;
      }

      public static SlideDownBlockTrigger.TriggerInstance slidesDownBlock(Block block) {
         return new SlideDownBlockTrigger.TriggerInstance(ContextAwarePredicate.ANY, block, StatePropertiesPredicate.ANY);
      }

      public JsonObject serializeToJson(SerializationContext serializationcontext) {
         JsonObject jsonobject = super.serializeToJson(serializationcontext);
         if (this.block != null) {
            jsonobject.addProperty("block", BuiltInRegistries.BLOCK.getKey(this.block).toString());
         }

         jsonobject.add("state", this.state.serializeToJson());
         return jsonobject;
      }

      public boolean matches(BlockState blockstate) {
         if (this.block != null && !blockstate.is(this.block)) {
            return false;
         } else {
            return this.state.matches(blockstate);
         }
      }
   }
}
