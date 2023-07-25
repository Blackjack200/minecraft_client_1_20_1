package net.minecraft.advancements.critereon;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.CatVariant;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.FrogVariant;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.animal.horse.Variant;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.phys.Vec3;

public interface EntitySubPredicate {
   EntitySubPredicate ANY = new EntitySubPredicate() {
      public boolean matches(Entity entity, ServerLevel serverlevel, @Nullable Vec3 vec3) {
         return true;
      }

      public JsonObject serializeCustomData() {
         return new JsonObject();
      }

      public EntitySubPredicate.Type type() {
         return EntitySubPredicate.Types.ANY;
      }
   };

   static EntitySubPredicate fromJson(@Nullable JsonElement jsonelement) {
      if (jsonelement != null && !jsonelement.isJsonNull()) {
         JsonObject jsonobject = GsonHelper.convertToJsonObject(jsonelement, "type_specific");
         String s = GsonHelper.getAsString(jsonobject, "type", (String)null);
         if (s == null) {
            return ANY;
         } else {
            EntitySubPredicate.Type entitysubpredicate_type = EntitySubPredicate.Types.TYPES.get(s);
            if (entitysubpredicate_type == null) {
               throw new JsonSyntaxException("Unknown sub-predicate type: " + s);
            } else {
               return entitysubpredicate_type.deserialize(jsonobject);
            }
         }
      } else {
         return ANY;
      }
   }

   boolean matches(Entity entity, ServerLevel serverlevel, @Nullable Vec3 vec3);

   JsonObject serializeCustomData();

   default JsonElement serialize() {
      if (this.type() == EntitySubPredicate.Types.ANY) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonobject = this.serializeCustomData();
         String s = EntitySubPredicate.Types.TYPES.inverse().get(this.type());
         jsonobject.addProperty("type", s);
         return jsonobject;
      }
   }

   EntitySubPredicate.Type type();

   static EntitySubPredicate variant(CatVariant catvariant) {
      return EntitySubPredicate.Types.CAT.createPredicate(catvariant);
   }

   static EntitySubPredicate variant(FrogVariant frogvariant) {
      return EntitySubPredicate.Types.FROG.createPredicate(frogvariant);
   }

   public interface Type {
      EntitySubPredicate deserialize(JsonObject jsonobject);
   }

   public static final class Types {
      public static final EntitySubPredicate.Type ANY = (jsonobject) -> EntitySubPredicate.ANY;
      public static final EntitySubPredicate.Type LIGHTNING = LighthingBoltPredicate::fromJson;
      public static final EntitySubPredicate.Type FISHING_HOOK = FishingHookPredicate::fromJson;
      public static final EntitySubPredicate.Type PLAYER = PlayerPredicate::fromJson;
      public static final EntitySubPredicate.Type SLIME = SlimePredicate::fromJson;
      public static final EntityVariantPredicate<CatVariant> CAT = EntityVariantPredicate.create(BuiltInRegistries.CAT_VARIANT, (entity) -> {
         Optional var10000;
         if (entity instanceof Cat cat) {
            var10000 = Optional.of(cat.getVariant());
         } else {
            var10000 = Optional.empty();
         }

         return var10000;
      });
      public static final EntityVariantPredicate<FrogVariant> FROG = EntityVariantPredicate.create(BuiltInRegistries.FROG_VARIANT, (entity) -> {
         Optional var10000;
         if (entity instanceof Frog frog) {
            var10000 = Optional.of(frog.getVariant());
         } else {
            var10000 = Optional.empty();
         }

         return var10000;
      });
      public static final EntityVariantPredicate<Axolotl.Variant> AXOLOTL = EntityVariantPredicate.create(Axolotl.Variant.CODEC, (entity) -> {
         Optional var10000;
         if (entity instanceof Axolotl axolotl) {
            var10000 = Optional.of(axolotl.getVariant());
         } else {
            var10000 = Optional.empty();
         }

         return var10000;
      });
      public static final EntityVariantPredicate<Boat.Type> BOAT = EntityVariantPredicate.create(Boat.Type.CODEC, (entity) -> {
         Optional var10000;
         if (entity instanceof Boat boat) {
            var10000 = Optional.of(boat.getVariant());
         } else {
            var10000 = Optional.empty();
         }

         return var10000;
      });
      public static final EntityVariantPredicate<Fox.Type> FOX = EntityVariantPredicate.create(Fox.Type.CODEC, (entity) -> {
         Optional var10000;
         if (entity instanceof Fox fox) {
            var10000 = Optional.of(fox.getVariant());
         } else {
            var10000 = Optional.empty();
         }

         return var10000;
      });
      public static final EntityVariantPredicate<MushroomCow.MushroomType> MOOSHROOM = EntityVariantPredicate.create(MushroomCow.MushroomType.CODEC, (entity) -> {
         Optional var10000;
         if (entity instanceof MushroomCow mushroomcow) {
            var10000 = Optional.of(mushroomcow.getVariant());
         } else {
            var10000 = Optional.empty();
         }

         return var10000;
      });
      public static final EntityVariantPredicate<Holder<PaintingVariant>> PAINTING = EntityVariantPredicate.create(BuiltInRegistries.PAINTING_VARIANT.holderByNameCodec(), (entity) -> {
         Optional var10000;
         if (entity instanceof Painting painting) {
            var10000 = Optional.of(painting.getVariant());
         } else {
            var10000 = Optional.empty();
         }

         return var10000;
      });
      public static final EntityVariantPredicate<Rabbit.Variant> RABBIT = EntityVariantPredicate.create(Rabbit.Variant.CODEC, (entity) -> {
         Optional var10000;
         if (entity instanceof Rabbit rabbit) {
            var10000 = Optional.of(rabbit.getVariant());
         } else {
            var10000 = Optional.empty();
         }

         return var10000;
      });
      public static final EntityVariantPredicate<Variant> HORSE = EntityVariantPredicate.create(Variant.CODEC, (entity) -> {
         Optional var10000;
         if (entity instanceof Horse horse) {
            var10000 = Optional.of(horse.getVariant());
         } else {
            var10000 = Optional.empty();
         }

         return var10000;
      });
      public static final EntityVariantPredicate<Llama.Variant> LLAMA = EntityVariantPredicate.create(Llama.Variant.CODEC, (entity) -> {
         Optional var10000;
         if (entity instanceof Llama llama) {
            var10000 = Optional.of(llama.getVariant());
         } else {
            var10000 = Optional.empty();
         }

         return var10000;
      });
      public static final EntityVariantPredicate<VillagerType> VILLAGER = EntityVariantPredicate.create(BuiltInRegistries.VILLAGER_TYPE.byNameCodec(), (entity) -> {
         Optional var10000;
         if (entity instanceof VillagerDataHolder villagerdataholder) {
            var10000 = Optional.of(villagerdataholder.getVariant());
         } else {
            var10000 = Optional.empty();
         }

         return var10000;
      });
      public static final EntityVariantPredicate<Parrot.Variant> PARROT = EntityVariantPredicate.create(Parrot.Variant.CODEC, (entity) -> {
         Optional var10000;
         if (entity instanceof Parrot parrot) {
            var10000 = Optional.of(parrot.getVariant());
         } else {
            var10000 = Optional.empty();
         }

         return var10000;
      });
      public static final EntityVariantPredicate<TropicalFish.Pattern> TROPICAL_FISH = EntityVariantPredicate.create(TropicalFish.Pattern.CODEC, (entity) -> {
         Optional var10000;
         if (entity instanceof TropicalFish tropicalfish) {
            var10000 = Optional.of(tropicalfish.getVariant());
         } else {
            var10000 = Optional.empty();
         }

         return var10000;
      });
      public static final BiMap<String, EntitySubPredicate.Type> TYPES = ImmutableBiMap.<String, EntitySubPredicate.Type>builder().put("any", ANY).put("lightning", LIGHTNING).put("fishing_hook", FISHING_HOOK).put("player", PLAYER).put("slime", SLIME).put("cat", CAT.type()).put("frog", FROG.type()).put("axolotl", AXOLOTL.type()).put("boat", BOAT.type()).put("fox", FOX.type()).put("mooshroom", MOOSHROOM.type()).put("painting", PAINTING.type()).put("rabbit", RABBIT.type()).put("horse", HORSE.type()).put("llama", LLAMA.type()).put("villager", VILLAGER.type()).put("parrot", PARROT.type()).put("tropical_fish", TROPICAL_FISH.type()).buildOrThrow();
   }
}
