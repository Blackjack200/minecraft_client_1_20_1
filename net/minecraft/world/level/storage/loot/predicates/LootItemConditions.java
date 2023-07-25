package net.minecraft.world.level.storage.loot.predicates;

import java.util.function.Predicate;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.GsonAdapterFactory;
import net.minecraft.world.level.storage.loot.Serializer;

public class LootItemConditions {
   public static final LootItemConditionType INVERTED = register("inverted", new InvertedLootItemCondition.Serializer());
   public static final LootItemConditionType ANY_OF = register("any_of", new AnyOfCondition.Serializer());
   public static final LootItemConditionType ALL_OF = register("all_of", new AllOfCondition.Serializer());
   public static final LootItemConditionType RANDOM_CHANCE = register("random_chance", new LootItemRandomChanceCondition.Serializer());
   public static final LootItemConditionType RANDOM_CHANCE_WITH_LOOTING = register("random_chance_with_looting", new LootItemRandomChanceWithLootingCondition.Serializer());
   public static final LootItemConditionType ENTITY_PROPERTIES = register("entity_properties", new LootItemEntityPropertyCondition.Serializer());
   public static final LootItemConditionType KILLED_BY_PLAYER = register("killed_by_player", new LootItemKilledByPlayerCondition.Serializer());
   public static final LootItemConditionType ENTITY_SCORES = register("entity_scores", new EntityHasScoreCondition.Serializer());
   public static final LootItemConditionType BLOCK_STATE_PROPERTY = register("block_state_property", new LootItemBlockStatePropertyCondition.Serializer());
   public static final LootItemConditionType MATCH_TOOL = register("match_tool", new MatchTool.Serializer());
   public static final LootItemConditionType TABLE_BONUS = register("table_bonus", new BonusLevelTableCondition.Serializer());
   public static final LootItemConditionType SURVIVES_EXPLOSION = register("survives_explosion", new ExplosionCondition.Serializer());
   public static final LootItemConditionType DAMAGE_SOURCE_PROPERTIES = register("damage_source_properties", new DamageSourceCondition.Serializer());
   public static final LootItemConditionType LOCATION_CHECK = register("location_check", new LocationCheck.Serializer());
   public static final LootItemConditionType WEATHER_CHECK = register("weather_check", new WeatherCheck.Serializer());
   public static final LootItemConditionType REFERENCE = register("reference", new ConditionReference.Serializer());
   public static final LootItemConditionType TIME_CHECK = register("time_check", new TimeCheck.Serializer());
   public static final LootItemConditionType VALUE_CHECK = register("value_check", new ValueCheckCondition.Serializer());

   private static LootItemConditionType register(String s, Serializer<? extends LootItemCondition> serializer) {
      return Registry.register(BuiltInRegistries.LOOT_CONDITION_TYPE, new ResourceLocation(s), new LootItemConditionType(serializer));
   }

   public static Object createGsonAdapter() {
      return GsonAdapterFactory.builder(BuiltInRegistries.LOOT_CONDITION_TYPE, "condition", "condition", LootItemCondition::getType).build();
   }

   public static <T> Predicate<T> andConditions(Predicate<T>[] apredicate) {
      Predicate var10000;
      switch (apredicate.length) {
         case 0:
            var10000 = (object1) -> true;
            break;
         case 1:
            var10000 = apredicate[0];
            break;
         case 2:
            var10000 = apredicate[0].and(apredicate[1]);
            break;
         default:
            var10000 = (object) -> {
               for(Predicate<T> predicate : apredicate) {
                  if (!predicate.test((T)object)) {
                     return false;
                  }
               }

               return true;
            };
      }

      return var10000;
   }

   public static <T> Predicate<T> orConditions(Predicate<T>[] apredicate) {
      Predicate var10000;
      switch (apredicate.length) {
         case 0:
            var10000 = (object1) -> false;
            break;
         case 1:
            var10000 = apredicate[0];
            break;
         case 2:
            var10000 = apredicate[0].or(apredicate[1]);
            break;
         default:
            var10000 = (object) -> {
               for(Predicate<T> predicate : apredicate) {
                  if (predicate.test((T)object)) {
                     return true;
                  }
               }

               return false;
            };
      }

      return var10000;
   }
}
