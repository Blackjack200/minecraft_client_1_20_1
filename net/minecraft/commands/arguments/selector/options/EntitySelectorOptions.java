package net.minecraft.commands.arguments.selector.options;

import com.google.common.collect.Maps;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.WrappedMinMaxBounds;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;

public class EntitySelectorOptions {
   private static final Map<String, EntitySelectorOptions.Option> OPTIONS = Maps.newHashMap();
   public static final DynamicCommandExceptionType ERROR_UNKNOWN_OPTION = new DynamicCommandExceptionType((object) -> Component.translatable("argument.entity.options.unknown", object));
   public static final DynamicCommandExceptionType ERROR_INAPPLICABLE_OPTION = new DynamicCommandExceptionType((object) -> Component.translatable("argument.entity.options.inapplicable", object));
   public static final SimpleCommandExceptionType ERROR_RANGE_NEGATIVE = new SimpleCommandExceptionType(Component.translatable("argument.entity.options.distance.negative"));
   public static final SimpleCommandExceptionType ERROR_LEVEL_NEGATIVE = new SimpleCommandExceptionType(Component.translatable("argument.entity.options.level.negative"));
   public static final SimpleCommandExceptionType ERROR_LIMIT_TOO_SMALL = new SimpleCommandExceptionType(Component.translatable("argument.entity.options.limit.toosmall"));
   public static final DynamicCommandExceptionType ERROR_SORT_UNKNOWN = new DynamicCommandExceptionType((object) -> Component.translatable("argument.entity.options.sort.irreversible", object));
   public static final DynamicCommandExceptionType ERROR_GAME_MODE_INVALID = new DynamicCommandExceptionType((object) -> Component.translatable("argument.entity.options.mode.invalid", object));
   public static final DynamicCommandExceptionType ERROR_ENTITY_TYPE_INVALID = new DynamicCommandExceptionType((object) -> Component.translatable("argument.entity.options.type.invalid", object));

   private static void register(String s, EntitySelectorOptions.Modifier entityselectoroptions_modifier, Predicate<EntitySelectorParser> predicate, Component component) {
      OPTIONS.put(s, new EntitySelectorOptions.Option(entityselectoroptions_modifier, predicate, component));
   }

   public static void bootStrap() {
      if (OPTIONS.isEmpty()) {
         register("name", (entityselectorparser44) -> {
            int j2 = entityselectorparser44.getReader().getCursor();
            boolean flag19 = entityselectorparser44.shouldInvertValue();
            String s11 = entityselectorparser44.getReader().readString();
            if (entityselectorparser44.hasNameNotEquals() && !flag19) {
               entityselectorparser44.getReader().setCursor(j2);
               throw ERROR_INAPPLICABLE_OPTION.createWithContext(entityselectorparser44.getReader(), "name");
            } else {
               if (flag19) {
                  entityselectorparser44.setHasNameNotEquals(true);
               } else {
                  entityselectorparser44.setHasNameEquals(true);
               }

               entityselectorparser44.addPredicate((entity9) -> entity9.getName().getString().equals(s11) != flag19);
            }
         }, (entityselectorparser43) -> !entityselectorparser43.hasNameEquals(), Component.translatable("argument.entity.options.name.description"));
         register("distance", (entityselectorparser42) -> {
            int i2 = entityselectorparser42.getReader().getCursor();
            MinMaxBounds.Doubles minmaxbounds_doubles = MinMaxBounds.Doubles.fromReader(entityselectorparser42.getReader());
            if ((minmaxbounds_doubles.getMin() == null || !(minmaxbounds_doubles.getMin() < 0.0D)) && (minmaxbounds_doubles.getMax() == null || !(minmaxbounds_doubles.getMax() < 0.0D))) {
               entityselectorparser42.setDistance(minmaxbounds_doubles);
               entityselectorparser42.setWorldLimited();
            } else {
               entityselectorparser42.getReader().setCursor(i2);
               throw ERROR_RANGE_NEGATIVE.createWithContext(entityselectorparser42.getReader());
            }
         }, (entityselectorparser41) -> entityselectorparser41.getDistance().isAny(), Component.translatable("argument.entity.options.distance.description"));
         register("level", (entityselectorparser40) -> {
            int l1 = entityselectorparser40.getReader().getCursor();
            MinMaxBounds.Ints minmaxbounds_ints1 = MinMaxBounds.Ints.fromReader(entityselectorparser40.getReader());
            if ((minmaxbounds_ints1.getMin() == null || minmaxbounds_ints1.getMin() >= 0) && (minmaxbounds_ints1.getMax() == null || minmaxbounds_ints1.getMax() >= 0)) {
               entityselectorparser40.setLevel(minmaxbounds_ints1);
               entityselectorparser40.setIncludesEntities(false);
            } else {
               entityselectorparser40.getReader().setCursor(l1);
               throw ERROR_LEVEL_NEGATIVE.createWithContext(entityselectorparser40.getReader());
            }
         }, (entityselectorparser39) -> entityselectorparser39.getLevel().isAny(), Component.translatable("argument.entity.options.level.description"));
         register("x", (entityselectorparser38) -> {
            entityselectorparser38.setWorldLimited();
            entityselectorparser38.setX(entityselectorparser38.getReader().readDouble());
         }, (entityselectorparser37) -> entityselectorparser37.getX() == null, Component.translatable("argument.entity.options.x.description"));
         register("y", (entityselectorparser36) -> {
            entityselectorparser36.setWorldLimited();
            entityselectorparser36.setY(entityselectorparser36.getReader().readDouble());
         }, (entityselectorparser35) -> entityselectorparser35.getY() == null, Component.translatable("argument.entity.options.y.description"));
         register("z", (entityselectorparser34) -> {
            entityselectorparser34.setWorldLimited();
            entityselectorparser34.setZ(entityselectorparser34.getReader().readDouble());
         }, (entityselectorparser33) -> entityselectorparser33.getZ() == null, Component.translatable("argument.entity.options.z.description"));
         register("dx", (entityselectorparser32) -> {
            entityselectorparser32.setWorldLimited();
            entityselectorparser32.setDeltaX(entityselectorparser32.getReader().readDouble());
         }, (entityselectorparser31) -> entityselectorparser31.getDeltaX() == null, Component.translatable("argument.entity.options.dx.description"));
         register("dy", (entityselectorparser30) -> {
            entityselectorparser30.setWorldLimited();
            entityselectorparser30.setDeltaY(entityselectorparser30.getReader().readDouble());
         }, (entityselectorparser29) -> entityselectorparser29.getDeltaY() == null, Component.translatable("argument.entity.options.dy.description"));
         register("dz", (entityselectorparser28) -> {
            entityselectorparser28.setWorldLimited();
            entityselectorparser28.setDeltaZ(entityselectorparser28.getReader().readDouble());
         }, (entityselectorparser27) -> entityselectorparser27.getDeltaZ() == null, Component.translatable("argument.entity.options.dz.description"));
         register("x_rotation", (entityselectorparser26) -> entityselectorparser26.setRotX(WrappedMinMaxBounds.fromReader(entityselectorparser26.getReader(), true, Mth::wrapDegrees)), (entityselectorparser25) -> entityselectorparser25.getRotX() == WrappedMinMaxBounds.ANY, Component.translatable("argument.entity.options.x_rotation.description"));
         register("y_rotation", (entityselectorparser24) -> entityselectorparser24.setRotY(WrappedMinMaxBounds.fromReader(entityselectorparser24.getReader(), true, Mth::wrapDegrees)), (entityselectorparser23) -> entityselectorparser23.getRotY() == WrappedMinMaxBounds.ANY, Component.translatable("argument.entity.options.y_rotation.description"));
         register("limit", (entityselectorparser22) -> {
            int j1 = entityselectorparser22.getReader().getCursor();
            int k1 = entityselectorparser22.getReader().readInt();
            if (k1 < 1) {
               entityselectorparser22.getReader().setCursor(j1);
               throw ERROR_LIMIT_TOO_SMALL.createWithContext(entityselectorparser22.getReader());
            } else {
               entityselectorparser22.setMaxResults(k1);
               entityselectorparser22.setLimited(true);
            }
         }, (entityselectorparser21) -> !entityselectorparser21.isCurrentEntity() && !entityselectorparser21.isLimited(), Component.translatable("argument.entity.options.limit.description"));
         register("sort", (entityselectorparser20) -> {
            int i1 = entityselectorparser20.getReader().getCursor();
            String s10 = entityselectorparser20.getReader().readUnquotedString();
            entityselectorparser20.setSuggestions((suggestionsbuilder2, consumer2) -> SharedSuggestionProvider.suggest(Arrays.asList("nearest", "furthest", "random", "arbitrary"), suggestionsbuilder2));
            BiConsumer var10001;
            switch (s10) {
               case "nearest":
                  var10001 = EntitySelectorParser.ORDER_NEAREST;
                  break;
               case "furthest":
                  var10001 = EntitySelectorParser.ORDER_FURTHEST;
                  break;
               case "random":
                  var10001 = EntitySelectorParser.ORDER_RANDOM;
                  break;
               case "arbitrary":
                  var10001 = EntitySelector.ORDER_ARBITRARY;
                  break;
               default:
                  entityselectorparser20.getReader().setCursor(i1);
                  throw ERROR_SORT_UNKNOWN.createWithContext(entityselectorparser20.getReader(), s10);
            }

            entityselectorparser20.setOrder(var10001);
            entityselectorparser20.setSorted(true);
         }, (entityselectorparser19) -> !entityselectorparser19.isCurrentEntity() && !entityselectorparser19.isSorted(), Component.translatable("argument.entity.options.sort.description"));
         register("gamemode", (entityselectorparser17) -> {
            entityselectorparser17.setSuggestions((suggestionsbuilder1, consumer1) -> {
               String s9 = suggestionsbuilder1.getRemaining().toLowerCase(Locale.ROOT);
               boolean flag17 = !entityselectorparser17.hasGamemodeNotEquals();
               boolean flag18 = true;
               if (!s9.isEmpty()) {
                  if (s9.charAt(0) == '!') {
                     flag17 = false;
                     s9 = s9.substring(1);
                  } else {
                     flag18 = false;
                  }
               }

               for(GameType gametype3 : GameType.values()) {
                  if (gametype3.getName().toLowerCase(Locale.ROOT).startsWith(s9)) {
                     if (flag18) {
                        suggestionsbuilder1.suggest("!" + gametype3.getName());
                     }

                     if (flag17) {
                        suggestionsbuilder1.suggest(gametype3.getName());
                     }
                  }
               }

               return suggestionsbuilder1.buildFuture();
            });
            int l = entityselectorparser17.getReader().getCursor();
            boolean flag15 = entityselectorparser17.shouldInvertValue();
            if (entityselectorparser17.hasGamemodeNotEquals() && !flag15) {
               entityselectorparser17.getReader().setCursor(l);
               throw ERROR_INAPPLICABLE_OPTION.createWithContext(entityselectorparser17.getReader(), "gamemode");
            } else {
               String s8 = entityselectorparser17.getReader().readUnquotedString();
               GameType gametype = GameType.byName(s8, (GameType)null);
               if (gametype == null) {
                  entityselectorparser17.getReader().setCursor(l);
                  throw ERROR_GAME_MODE_INVALID.createWithContext(entityselectorparser17.getReader(), s8);
               } else {
                  entityselectorparser17.setIncludesEntities(false);
                  entityselectorparser17.addPredicate((entity8) -> {
                     if (!(entity8 instanceof ServerPlayer)) {
                        return false;
                     } else {
                        GameType gametype2 = ((ServerPlayer)entity8).gameMode.getGameModeForPlayer();
                        return flag15 ? gametype2 != gametype : gametype2 == gametype;
                     }
                  });
                  if (flag15) {
                     entityselectorparser17.setHasGamemodeNotEquals(true);
                  } else {
                     entityselectorparser17.setHasGamemodeEquals(true);
                  }

               }
            }
         }, (entityselectorparser16) -> !entityselectorparser16.hasGamemodeEquals(), Component.translatable("argument.entity.options.gamemode.description"));
         register("team", (entityselectorparser15) -> {
            boolean flag13 = entityselectorparser15.shouldInvertValue();
            String s5 = entityselectorparser15.getReader().readUnquotedString();
            entityselectorparser15.addPredicate((entity7) -> {
               if (!(entity7 instanceof LivingEntity)) {
                  return false;
               } else {
                  Team team = entity7.getTeam();
                  String s7 = team == null ? "" : team.getName();
                  return s7.equals(s5) != flag13;
               }
            });
            if (flag13) {
               entityselectorparser15.setHasTeamNotEquals(true);
            } else {
               entityselectorparser15.setHasTeamEquals(true);
            }

         }, (entityselectorparser14) -> !entityselectorparser14.hasTeamEquals(), Component.translatable("argument.entity.options.team.description"));
         register("type", (entityselectorparser11) -> {
            entityselectorparser11.setSuggestions((suggestionsbuilder, consumer) -> {
               SharedSuggestionProvider.suggestResource(BuiltInRegistries.ENTITY_TYPE.keySet(), suggestionsbuilder, String.valueOf('!'));
               SharedSuggestionProvider.suggestResource(BuiltInRegistries.ENTITY_TYPE.getTagNames().map(TagKey::location), suggestionsbuilder, "!#");
               if (!entityselectorparser11.isTypeLimitedInversely()) {
                  SharedSuggestionProvider.suggestResource(BuiltInRegistries.ENTITY_TYPE.keySet(), suggestionsbuilder);
                  SharedSuggestionProvider.suggestResource(BuiltInRegistries.ENTITY_TYPE.getTagNames().map(TagKey::location), suggestionsbuilder, String.valueOf('#'));
               }

               return suggestionsbuilder.buildFuture();
            });
            int j = entityselectorparser11.getReader().getCursor();
            boolean flag10 = entityselectorparser11.shouldInvertValue();
            if (entityselectorparser11.isTypeLimitedInversely() && !flag10) {
               entityselectorparser11.getReader().setCursor(j);
               throw ERROR_INAPPLICABLE_OPTION.createWithContext(entityselectorparser11.getReader(), "type");
            } else {
               if (flag10) {
                  entityselectorparser11.setTypeLimitedInversely();
               }

               if (entityselectorparser11.isTag()) {
                  TagKey<EntityType<?>> tagkey = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.read(entityselectorparser11.getReader()));
                  entityselectorparser11.addPredicate((entity6) -> entity6.getType().is(tagkey) != flag10);
               } else {
                  ResourceLocation resourcelocation3 = ResourceLocation.read(entityselectorparser11.getReader());
                  EntityType<?> entitytype = BuiltInRegistries.ENTITY_TYPE.getOptional(resourcelocation3).orElseThrow(() -> {
                     entityselectorparser11.getReader().setCursor(j);
                     return ERROR_ENTITY_TYPE_INVALID.createWithContext(entityselectorparser11.getReader(), resourcelocation3.toString());
                  });
                  if (Objects.equals(EntityType.PLAYER, entitytype) && !flag10) {
                     entityselectorparser11.setIncludesEntities(false);
                  }

                  entityselectorparser11.addPredicate((entity5) -> Objects.equals(entitytype, entity5.getType()) != flag10);
                  if (!flag10) {
                     entityselectorparser11.limitToType(entitytype);
                  }
               }

            }
         }, (entityselectorparser10) -> !entityselectorparser10.isTypeLimited(), Component.translatable("argument.entity.options.type.description"));
         register("tag", (entityselectorparser9) -> {
            boolean flag8 = entityselectorparser9.shouldInvertValue();
            String s3 = entityselectorparser9.getReader().readUnquotedString();
            entityselectorparser9.addPredicate((entity4) -> {
               if ("".equals(s3)) {
                  return entity4.getTags().isEmpty() != flag8;
               } else {
                  return entity4.getTags().contains(s3) != flag8;
               }
            });
         }, (entityselectorparser8) -> true, Component.translatable("argument.entity.options.tag.description"));
         register("nbt", (entityselectorparser7) -> {
            boolean flag6 = entityselectorparser7.shouldInvertValue();
            CompoundTag compoundtag = (new TagParser(entityselectorparser7.getReader())).readStruct();
            entityselectorparser7.addPredicate((entity3) -> {
               CompoundTag compoundtag2 = entity3.saveWithoutId(new CompoundTag());
               if (entity3 instanceof ServerPlayer) {
                  ItemStack itemstack = ((ServerPlayer)entity3).getInventory().getSelected();
                  if (!itemstack.isEmpty()) {
                     compoundtag2.put("SelectedItem", itemstack.save(new CompoundTag()));
                  }
               }

               return NbtUtils.compareNbt(compoundtag, compoundtag2, true) != flag6;
            });
         }, (entityselectorparser6) -> true, Component.translatable("argument.entity.options.nbt.description"));
         register("scores", (entityselectorparser5) -> {
            StringReader stringreader1 = entityselectorparser5.getReader();
            Map<String, MinMaxBounds.Ints> map4 = Maps.newHashMap();
            stringreader1.expect('{');
            stringreader1.skipWhitespace();

            while(stringreader1.canRead() && stringreader1.peek() != '}') {
               stringreader1.skipWhitespace();
               String s1 = stringreader1.readUnquotedString();
               stringreader1.skipWhitespace();
               stringreader1.expect('=');
               stringreader1.skipWhitespace();
               MinMaxBounds.Ints minmaxbounds_ints = MinMaxBounds.Ints.fromReader(stringreader1);
               map4.put(s1, minmaxbounds_ints);
               stringreader1.skipWhitespace();
               if (stringreader1.canRead() && stringreader1.peek() == ',') {
                  stringreader1.skip();
               }
            }

            stringreader1.expect('}');
            if (!map4.isEmpty()) {
               entityselectorparser5.addPredicate((entity2) -> {
                  Scoreboard scoreboard = entity2.getServer().getScoreboard();
                  String s2 = entity2.getScoreboardName();

                  for(Map.Entry<String, MinMaxBounds.Ints> map_entry2 : map4.entrySet()) {
                     Objective objective = scoreboard.getObjective(map_entry2.getKey());
                     if (objective == null) {
                        return false;
                     }

                     if (!scoreboard.hasPlayerScore(s2, objective)) {
                        return false;
                     }

                     Score score = scoreboard.getOrCreatePlayerScore(s2, objective);
                     int i = score.getScore();
                     if (!map_entry2.getValue().matches(i)) {
                        return false;
                     }
                  }

                  return true;
               });
            }

            entityselectorparser5.setHasScores(true);
         }, (entityselectorparser4) -> !entityselectorparser4.hasScores(), Component.translatable("argument.entity.options.scores.description"));
         register("advancements", (entityselectorparser3) -> {
            StringReader stringreader = entityselectorparser3.getReader();
            Map<ResourceLocation, Predicate<AdvancementProgress>> map = Maps.newHashMap();
            stringreader.expect('{');
            stringreader.skipWhitespace();

            while(stringreader.canRead() && stringreader.peek() != '}') {
               stringreader.skipWhitespace();
               ResourceLocation resourcelocation2 = ResourceLocation.read(stringreader);
               stringreader.skipWhitespace();
               stringreader.expect('=');
               stringreader.skipWhitespace();
               if (stringreader.canRead() && stringreader.peek() == '{') {
                  Map<String, Predicate<CriterionProgress>> map1 = Maps.newHashMap();
                  stringreader.skipWhitespace();
                  stringreader.expect('{');
                  stringreader.skipWhitespace();

                  while(stringreader.canRead() && stringreader.peek() != '}') {
                     stringreader.skipWhitespace();
                     String s = stringreader.readUnquotedString();
                     stringreader.skipWhitespace();
                     stringreader.expect('=');
                     stringreader.skipWhitespace();
                     boolean flag2 = stringreader.readBoolean();
                     map1.put(s, (criterionprogress1) -> criterionprogress1.isDone() == flag2);
                     stringreader.skipWhitespace();
                     if (stringreader.canRead() && stringreader.peek() == ',') {
                        stringreader.skip();
                     }
                  }

                  stringreader.skipWhitespace();
                  stringreader.expect('}');
                  stringreader.skipWhitespace();
                  map.put(resourcelocation2, (advancementprogress1) -> {
                     for(Map.Entry<String, Predicate<CriterionProgress>> map_entry1 : map1.entrySet()) {
                        CriterionProgress criterionprogress = advancementprogress1.getCriterion(map_entry1.getKey());
                        if (criterionprogress == null || !map_entry1.getValue().test(criterionprogress)) {
                           return false;
                        }
                     }

                     return true;
                  });
               } else {
                  boolean flag3 = stringreader.readBoolean();
                  map.put(resourcelocation2, (advancementprogress) -> advancementprogress.isDone() == flag3);
               }

               stringreader.skipWhitespace();
               if (stringreader.canRead() && stringreader.peek() == ',') {
                  stringreader.skip();
               }
            }

            stringreader.expect('}');
            if (!map.isEmpty()) {
               entityselectorparser3.addPredicate((entity1) -> {
                  if (!(entity1 instanceof ServerPlayer serverplayer)) {
                     return false;
                  } else {
                     PlayerAdvancements playeradvancements = serverplayer.getAdvancements();
                     ServerAdvancementManager serveradvancementmanager = serverplayer.getServer().getAdvancements();

                     for(Map.Entry<ResourceLocation, Predicate<AdvancementProgress>> map_entry : map.entrySet()) {
                        Advancement advancement = serveradvancementmanager.getAdvancement(map_entry.getKey());
                        if (advancement == null || !map_entry.getValue().test(playeradvancements.getOrStartProgress(advancement))) {
                           return false;
                        }
                     }

                     return true;
                  }
               });
               entityselectorparser3.setIncludesEntities(false);
            }

            entityselectorparser3.setHasAdvancements(true);
         }, (entityselectorparser2) -> !entityselectorparser2.hasAdvancements(), Component.translatable("argument.entity.options.advancements.description"));
         register("predicate", (entityselectorparser1) -> {
            boolean flag = entityselectorparser1.shouldInvertValue();
            ResourceLocation resourcelocation = ResourceLocation.read(entityselectorparser1.getReader());
            entityselectorparser1.addPredicate((entity) -> {
               if (!(entity.level() instanceof ServerLevel)) {
                  return false;
               } else {
                  ServerLevel serverlevel = (ServerLevel)entity.level();
                  LootItemCondition lootitemcondition = serverlevel.getServer().getLootData().getElement(LootDataType.PREDICATE, resourcelocation);
                  if (lootitemcondition == null) {
                     return false;
                  } else {
                     LootParams lootparams = (new LootParams.Builder(serverlevel)).withParameter(LootContextParams.THIS_ENTITY, entity).withParameter(LootContextParams.ORIGIN, entity.position()).create(LootContextParamSets.SELECTOR);
                     LootContext lootcontext = (new LootContext.Builder(lootparams)).create((ResourceLocation)null);
                     lootcontext.pushVisitedElement(LootContext.createVisitedEntry(lootitemcondition));
                     return flag ^ lootitemcondition.test(lootcontext);
                  }
               }
            });
         }, (entityselectorparser) -> true, Component.translatable("argument.entity.options.predicate.description"));
      }
   }

   public static EntitySelectorOptions.Modifier get(EntitySelectorParser entityselectorparser, String s, int i) throws CommandSyntaxException {
      EntitySelectorOptions.Option entityselectoroptions_option = OPTIONS.get(s);
      if (entityselectoroptions_option != null) {
         if (entityselectoroptions_option.canUse.test(entityselectorparser)) {
            return entityselectoroptions_option.modifier;
         } else {
            throw ERROR_INAPPLICABLE_OPTION.createWithContext(entityselectorparser.getReader(), s);
         }
      } else {
         entityselectorparser.getReader().setCursor(i);
         throw ERROR_UNKNOWN_OPTION.createWithContext(entityselectorparser.getReader(), s);
      }
   }

   public static void suggestNames(EntitySelectorParser entityselectorparser, SuggestionsBuilder suggestionsbuilder) {
      String s = suggestionsbuilder.getRemaining().toLowerCase(Locale.ROOT);

      for(Map.Entry<String, EntitySelectorOptions.Option> map_entry : OPTIONS.entrySet()) {
         if ((map_entry.getValue()).canUse.test(entityselectorparser) && map_entry.getKey().toLowerCase(Locale.ROOT).startsWith(s)) {
            suggestionsbuilder.suggest((String)map_entry.getKey() + "=", (map_entry.getValue()).description);
         }
      }

   }

   public interface Modifier {
      void handle(EntitySelectorParser entityselectorparser) throws CommandSyntaxException;
   }

   static record Option(EntitySelectorOptions.Modifier modifier, Predicate<EntitySelectorParser> canUse, Component description) {
      final EntitySelectorOptions.Modifier modifier;
      final Predicate<EntitySelectorParser> canUse;
      final Component description;
   }
}
