package net.minecraft.advancements.critereon;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.ItemLike;

public class InventoryChangeTrigger extends SimpleCriterionTrigger<InventoryChangeTrigger.TriggerInstance> {
   static final ResourceLocation ID = new ResourceLocation("inventory_changed");

   public ResourceLocation getId() {
      return ID;
   }

   public InventoryChangeTrigger.TriggerInstance createInstance(JsonObject jsonobject, ContextAwarePredicate contextawarepredicate, DeserializationContext deserializationcontext) {
      JsonObject jsonobject1 = GsonHelper.getAsJsonObject(jsonobject, "slots", new JsonObject());
      MinMaxBounds.Ints minmaxbounds_ints = MinMaxBounds.Ints.fromJson(jsonobject1.get("occupied"));
      MinMaxBounds.Ints minmaxbounds_ints1 = MinMaxBounds.Ints.fromJson(jsonobject1.get("full"));
      MinMaxBounds.Ints minmaxbounds_ints2 = MinMaxBounds.Ints.fromJson(jsonobject1.get("empty"));
      ItemPredicate[] aitempredicate = ItemPredicate.fromJsonArray(jsonobject.get("items"));
      return new InventoryChangeTrigger.TriggerInstance(contextawarepredicate, minmaxbounds_ints, minmaxbounds_ints1, minmaxbounds_ints2, aitempredicate);
   }

   public void trigger(ServerPlayer serverplayer, Inventory inventory, ItemStack itemstack) {
      int i = 0;
      int j = 0;
      int k = 0;

      for(int l = 0; l < inventory.getContainerSize(); ++l) {
         ItemStack itemstack1 = inventory.getItem(l);
         if (itemstack1.isEmpty()) {
            ++j;
         } else {
            ++k;
            if (itemstack1.getCount() >= itemstack1.getMaxStackSize()) {
               ++i;
            }
         }
      }

      this.trigger(serverplayer, inventory, itemstack, i, j, k);
   }

   private void trigger(ServerPlayer serverplayer, Inventory inventory, ItemStack itemstack, int i, int j, int k) {
      this.trigger(serverplayer, (inventorychangetrigger_triggerinstance) -> inventorychangetrigger_triggerinstance.matches(inventory, itemstack, i, j, k));
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final MinMaxBounds.Ints slotsOccupied;
      private final MinMaxBounds.Ints slotsFull;
      private final MinMaxBounds.Ints slotsEmpty;
      private final ItemPredicate[] predicates;

      public TriggerInstance(ContextAwarePredicate contextawarepredicate, MinMaxBounds.Ints minmaxbounds_ints, MinMaxBounds.Ints minmaxbounds_ints1, MinMaxBounds.Ints minmaxbounds_ints2, ItemPredicate[] aitempredicate) {
         super(InventoryChangeTrigger.ID, contextawarepredicate);
         this.slotsOccupied = minmaxbounds_ints;
         this.slotsFull = minmaxbounds_ints1;
         this.slotsEmpty = minmaxbounds_ints2;
         this.predicates = aitempredicate;
      }

      public static InventoryChangeTrigger.TriggerInstance hasItems(ItemPredicate... aitempredicate) {
         return new InventoryChangeTrigger.TriggerInstance(ContextAwarePredicate.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, aitempredicate);
      }

      public static InventoryChangeTrigger.TriggerInstance hasItems(ItemLike... aitemlike) {
         ItemPredicate[] aitempredicate = new ItemPredicate[aitemlike.length];

         for(int i = 0; i < aitemlike.length; ++i) {
            aitempredicate[i] = new ItemPredicate((TagKey<Item>)null, ImmutableSet.of(aitemlike[i].asItem()), MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, EnchantmentPredicate.NONE, EnchantmentPredicate.NONE, (Potion)null, NbtPredicate.ANY);
         }

         return hasItems(aitempredicate);
      }

      public JsonObject serializeToJson(SerializationContext serializationcontext) {
         JsonObject jsonobject = super.serializeToJson(serializationcontext);
         if (!this.slotsOccupied.isAny() || !this.slotsFull.isAny() || !this.slotsEmpty.isAny()) {
            JsonObject jsonobject1 = new JsonObject();
            jsonobject1.add("occupied", this.slotsOccupied.serializeToJson());
            jsonobject1.add("full", this.slotsFull.serializeToJson());
            jsonobject1.add("empty", this.slotsEmpty.serializeToJson());
            jsonobject.add("slots", jsonobject1);
         }

         if (this.predicates.length > 0) {
            JsonArray jsonarray = new JsonArray();

            for(ItemPredicate itempredicate : this.predicates) {
               jsonarray.add(itempredicate.serializeToJson());
            }

            jsonobject.add("items", jsonarray);
         }

         return jsonobject;
      }

      public boolean matches(Inventory inventory, ItemStack itemstack, int i, int j, int k) {
         if (!this.slotsFull.matches(i)) {
            return false;
         } else if (!this.slotsEmpty.matches(j)) {
            return false;
         } else if (!this.slotsOccupied.matches(k)) {
            return false;
         } else {
            int l = this.predicates.length;
            if (l == 0) {
               return true;
            } else if (l != 1) {
               List<ItemPredicate> list = new ObjectArrayList<>(this.predicates);
               int i1 = inventory.getContainerSize();

               for(int j1 = 0; j1 < i1; ++j1) {
                  if (list.isEmpty()) {
                     return true;
                  }

                  ItemStack itemstack1 = inventory.getItem(j1);
                  if (!itemstack1.isEmpty()) {
                     list.removeIf((itempredicate) -> itempredicate.matches(itemstack1));
                  }
               }

               return list.isEmpty();
            } else {
               return !itemstack.isEmpty() && this.predicates[0].matches(itemstack);
            }
         }
      }
   }
}
