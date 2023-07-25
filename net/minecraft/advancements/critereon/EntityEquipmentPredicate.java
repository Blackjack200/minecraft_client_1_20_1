package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.Items;

public class EntityEquipmentPredicate {
   public static final EntityEquipmentPredicate ANY = new EntityEquipmentPredicate(ItemPredicate.ANY, ItemPredicate.ANY, ItemPredicate.ANY, ItemPredicate.ANY, ItemPredicate.ANY, ItemPredicate.ANY);
   public static final EntityEquipmentPredicate CAPTAIN = new EntityEquipmentPredicate(ItemPredicate.Builder.item().of(Items.WHITE_BANNER).hasNbt(Raid.getLeaderBannerInstance().getTag()).build(), ItemPredicate.ANY, ItemPredicate.ANY, ItemPredicate.ANY, ItemPredicate.ANY, ItemPredicate.ANY);
   private final ItemPredicate head;
   private final ItemPredicate chest;
   private final ItemPredicate legs;
   private final ItemPredicate feet;
   private final ItemPredicate mainhand;
   private final ItemPredicate offhand;

   public EntityEquipmentPredicate(ItemPredicate itempredicate, ItemPredicate itempredicate1, ItemPredicate itempredicate2, ItemPredicate itempredicate3, ItemPredicate itempredicate4, ItemPredicate itempredicate5) {
      this.head = itempredicate;
      this.chest = itempredicate1;
      this.legs = itempredicate2;
      this.feet = itempredicate3;
      this.mainhand = itempredicate4;
      this.offhand = itempredicate5;
   }

   public boolean matches(@Nullable Entity entity) {
      if (this == ANY) {
         return true;
      } else if (!(entity instanceof LivingEntity)) {
         return false;
      } else {
         LivingEntity livingentity = (LivingEntity)entity;
         if (!this.head.matches(livingentity.getItemBySlot(EquipmentSlot.HEAD))) {
            return false;
         } else if (!this.chest.matches(livingentity.getItemBySlot(EquipmentSlot.CHEST))) {
            return false;
         } else if (!this.legs.matches(livingentity.getItemBySlot(EquipmentSlot.LEGS))) {
            return false;
         } else if (!this.feet.matches(livingentity.getItemBySlot(EquipmentSlot.FEET))) {
            return false;
         } else if (!this.mainhand.matches(livingentity.getItemBySlot(EquipmentSlot.MAINHAND))) {
            return false;
         } else {
            return this.offhand.matches(livingentity.getItemBySlot(EquipmentSlot.OFFHAND));
         }
      }
   }

   public static EntityEquipmentPredicate fromJson(@Nullable JsonElement jsonelement) {
      if (jsonelement != null && !jsonelement.isJsonNull()) {
         JsonObject jsonobject = GsonHelper.convertToJsonObject(jsonelement, "equipment");
         ItemPredicate itempredicate = ItemPredicate.fromJson(jsonobject.get("head"));
         ItemPredicate itempredicate1 = ItemPredicate.fromJson(jsonobject.get("chest"));
         ItemPredicate itempredicate2 = ItemPredicate.fromJson(jsonobject.get("legs"));
         ItemPredicate itempredicate3 = ItemPredicate.fromJson(jsonobject.get("feet"));
         ItemPredicate itempredicate4 = ItemPredicate.fromJson(jsonobject.get("mainhand"));
         ItemPredicate itempredicate5 = ItemPredicate.fromJson(jsonobject.get("offhand"));
         return new EntityEquipmentPredicate(itempredicate, itempredicate1, itempredicate2, itempredicate3, itempredicate4, itempredicate5);
      } else {
         return ANY;
      }
   }

   public JsonElement serializeToJson() {
      if (this == ANY) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonobject = new JsonObject();
         jsonobject.add("head", this.head.serializeToJson());
         jsonobject.add("chest", this.chest.serializeToJson());
         jsonobject.add("legs", this.legs.serializeToJson());
         jsonobject.add("feet", this.feet.serializeToJson());
         jsonobject.add("mainhand", this.mainhand.serializeToJson());
         jsonobject.add("offhand", this.offhand.serializeToJson());
         return jsonobject;
      }
   }

   public static class Builder {
      private ItemPredicate head = ItemPredicate.ANY;
      private ItemPredicate chest = ItemPredicate.ANY;
      private ItemPredicate legs = ItemPredicate.ANY;
      private ItemPredicate feet = ItemPredicate.ANY;
      private ItemPredicate mainhand = ItemPredicate.ANY;
      private ItemPredicate offhand = ItemPredicate.ANY;

      public static EntityEquipmentPredicate.Builder equipment() {
         return new EntityEquipmentPredicate.Builder();
      }

      public EntityEquipmentPredicate.Builder head(ItemPredicate itempredicate) {
         this.head = itempredicate;
         return this;
      }

      public EntityEquipmentPredicate.Builder chest(ItemPredicate itempredicate) {
         this.chest = itempredicate;
         return this;
      }

      public EntityEquipmentPredicate.Builder legs(ItemPredicate itempredicate) {
         this.legs = itempredicate;
         return this;
      }

      public EntityEquipmentPredicate.Builder feet(ItemPredicate itempredicate) {
         this.feet = itempredicate;
         return this;
      }

      public EntityEquipmentPredicate.Builder mainhand(ItemPredicate itempredicate) {
         this.mainhand = itempredicate;
         return this;
      }

      public EntityEquipmentPredicate.Builder offhand(ItemPredicate itempredicate) {
         this.offhand = itempredicate;
         return this;
      }

      public EntityEquipmentPredicate build() {
         return new EntityEquipmentPredicate(this.head, this.chest, this.legs, this.feet, this.mainhand, this.offhand);
      }
   }
}
