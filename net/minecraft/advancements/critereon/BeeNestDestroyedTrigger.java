package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BeeNestDestroyedTrigger extends SimpleCriterionTrigger<BeeNestDestroyedTrigger.TriggerInstance> {
   static final ResourceLocation ID = new ResourceLocation("bee_nest_destroyed");

   public ResourceLocation getId() {
      return ID;
   }

   public BeeNestDestroyedTrigger.TriggerInstance createInstance(JsonObject jsonobject, ContextAwarePredicate contextawarepredicate, DeserializationContext deserializationcontext) {
      Block block = deserializeBlock(jsonobject);
      ItemPredicate itempredicate = ItemPredicate.fromJson(jsonobject.get("item"));
      MinMaxBounds.Ints minmaxbounds_ints = MinMaxBounds.Ints.fromJson(jsonobject.get("num_bees_inside"));
      return new BeeNestDestroyedTrigger.TriggerInstance(contextawarepredicate, block, itempredicate, minmaxbounds_ints);
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

   public void trigger(ServerPlayer serverplayer, BlockState blockstate, ItemStack itemstack, int i) {
      this.trigger(serverplayer, (beenestdestroyedtrigger_triggerinstance) -> beenestdestroyedtrigger_triggerinstance.matches(blockstate, itemstack, i));
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      @Nullable
      private final Block block;
      private final ItemPredicate item;
      private final MinMaxBounds.Ints numBees;

      public TriggerInstance(ContextAwarePredicate contextawarepredicate, @Nullable Block block, ItemPredicate itempredicate, MinMaxBounds.Ints minmaxbounds_ints) {
         super(BeeNestDestroyedTrigger.ID, contextawarepredicate);
         this.block = block;
         this.item = itempredicate;
         this.numBees = minmaxbounds_ints;
      }

      public static BeeNestDestroyedTrigger.TriggerInstance destroyedBeeNest(Block block, ItemPredicate.Builder itempredicate_builder, MinMaxBounds.Ints minmaxbounds_ints) {
         return new BeeNestDestroyedTrigger.TriggerInstance(ContextAwarePredicate.ANY, block, itempredicate_builder.build(), minmaxbounds_ints);
      }

      public boolean matches(BlockState blockstate, ItemStack itemstack, int i) {
         if (this.block != null && !blockstate.is(this.block)) {
            return false;
         } else {
            return !this.item.matches(itemstack) ? false : this.numBees.matches(i);
         }
      }

      public JsonObject serializeToJson(SerializationContext serializationcontext) {
         JsonObject jsonobject = super.serializeToJson(serializationcontext);
         if (this.block != null) {
            jsonobject.addProperty("block", BuiltInRegistries.BLOCK.getKey(this.block).toString());
         }

         jsonobject.add("item", this.item.serializeToJson());
         jsonobject.add("num_bees_inside", this.numBees.serializeToJson());
         return jsonobject;
      }
   }
}
