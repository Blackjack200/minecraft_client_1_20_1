package net.minecraft.commands.arguments.selector;

import com.google.common.primitives.Doubles;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import javax.annotation.Nullable;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.WrappedMinMaxBounds;
import net.minecraft.commands.arguments.selector.options.EntitySelectorOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class EntitySelectorParser {
   public static final char SYNTAX_SELECTOR_START = '@';
   private static final char SYNTAX_OPTIONS_START = '[';
   private static final char SYNTAX_OPTIONS_END = ']';
   public static final char SYNTAX_OPTIONS_KEY_VALUE_SEPARATOR = '=';
   private static final char SYNTAX_OPTIONS_SEPARATOR = ',';
   public static final char SYNTAX_NOT = '!';
   public static final char SYNTAX_TAG = '#';
   private static final char SELECTOR_NEAREST_PLAYER = 'p';
   private static final char SELECTOR_ALL_PLAYERS = 'a';
   private static final char SELECTOR_RANDOM_PLAYERS = 'r';
   private static final char SELECTOR_CURRENT_ENTITY = 's';
   private static final char SELECTOR_ALL_ENTITIES = 'e';
   public static final SimpleCommandExceptionType ERROR_INVALID_NAME_OR_UUID = new SimpleCommandExceptionType(Component.translatable("argument.entity.invalid"));
   public static final DynamicCommandExceptionType ERROR_UNKNOWN_SELECTOR_TYPE = new DynamicCommandExceptionType((object) -> Component.translatable("argument.entity.selector.unknown", object));
   public static final SimpleCommandExceptionType ERROR_SELECTORS_NOT_ALLOWED = new SimpleCommandExceptionType(Component.translatable("argument.entity.selector.not_allowed"));
   public static final SimpleCommandExceptionType ERROR_MISSING_SELECTOR_TYPE = new SimpleCommandExceptionType(Component.translatable("argument.entity.selector.missing"));
   public static final SimpleCommandExceptionType ERROR_EXPECTED_END_OF_OPTIONS = new SimpleCommandExceptionType(Component.translatable("argument.entity.options.unterminated"));
   public static final DynamicCommandExceptionType ERROR_EXPECTED_OPTION_VALUE = new DynamicCommandExceptionType((object) -> Component.translatable("argument.entity.options.valueless", object));
   public static final BiConsumer<Vec3, List<? extends Entity>> ORDER_NEAREST = (vec3, list) -> list.sort((entity, entity1) -> Doubles.compare(entity.distanceToSqr(vec3), entity1.distanceToSqr(vec3)));
   public static final BiConsumer<Vec3, List<? extends Entity>> ORDER_FURTHEST = (vec3, list) -> list.sort((entity, entity1) -> Doubles.compare(entity1.distanceToSqr(vec3), entity.distanceToSqr(vec3)));
   public static final BiConsumer<Vec3, List<? extends Entity>> ORDER_RANDOM = (vec3, list) -> Collections.shuffle(list);
   public static final BiFunction<SuggestionsBuilder, Consumer<SuggestionsBuilder>, CompletableFuture<Suggestions>> SUGGEST_NOTHING = (suggestionsbuilder, consumer) -> suggestionsbuilder.buildFuture();
   private final StringReader reader;
   private final boolean allowSelectors;
   private int maxResults;
   private boolean includesEntities;
   private boolean worldLimited;
   private MinMaxBounds.Doubles distance = MinMaxBounds.Doubles.ANY;
   private MinMaxBounds.Ints level = MinMaxBounds.Ints.ANY;
   @Nullable
   private Double x;
   @Nullable
   private Double y;
   @Nullable
   private Double z;
   @Nullable
   private Double deltaX;
   @Nullable
   private Double deltaY;
   @Nullable
   private Double deltaZ;
   private WrappedMinMaxBounds rotX = WrappedMinMaxBounds.ANY;
   private WrappedMinMaxBounds rotY = WrappedMinMaxBounds.ANY;
   private Predicate<Entity> predicate = (entity) -> true;
   private BiConsumer<Vec3, List<? extends Entity>> order = EntitySelector.ORDER_ARBITRARY;
   private boolean currentEntity;
   @Nullable
   private String playerName;
   private int startPosition;
   @Nullable
   private UUID entityUUID;
   private BiFunction<SuggestionsBuilder, Consumer<SuggestionsBuilder>, CompletableFuture<Suggestions>> suggestions = SUGGEST_NOTHING;
   private boolean hasNameEquals;
   private boolean hasNameNotEquals;
   private boolean isLimited;
   private boolean isSorted;
   private boolean hasGamemodeEquals;
   private boolean hasGamemodeNotEquals;
   private boolean hasTeamEquals;
   private boolean hasTeamNotEquals;
   @Nullable
   private EntityType<?> type;
   private boolean typeInverse;
   private boolean hasScores;
   private boolean hasAdvancements;
   private boolean usesSelectors;

   public EntitySelectorParser(StringReader stringreader) {
      this(stringreader, true);
   }

   public EntitySelectorParser(StringReader stringreader, boolean flag) {
      this.reader = stringreader;
      this.allowSelectors = flag;
   }

   public EntitySelector getSelector() {
      AABB aabb1;
      if (this.deltaX == null && this.deltaY == null && this.deltaZ == null) {
         if (this.distance.getMax() != null) {
            double d0 = this.distance.getMax();
            aabb1 = new AABB(-d0, -d0, -d0, d0 + 1.0D, d0 + 1.0D, d0 + 1.0D);
         } else {
            aabb1 = null;
         }
      } else {
         aabb1 = this.createAabb(this.deltaX == null ? 0.0D : this.deltaX, this.deltaY == null ? 0.0D : this.deltaY, this.deltaZ == null ? 0.0D : this.deltaZ);
      }

      Function<Vec3, Vec3> function;
      if (this.x == null && this.y == null && this.z == null) {
         function = (vec31) -> vec31;
      } else {
         function = (vec3) -> new Vec3(this.x == null ? vec3.x : this.x, this.y == null ? vec3.y : this.y, this.z == null ? vec3.z : this.z);
      }

      return new EntitySelector(this.maxResults, this.includesEntities, this.worldLimited, this.predicate, this.distance, function, aabb1, this.order, this.currentEntity, this.playerName, this.entityUUID, this.type, this.usesSelectors);
   }

   private AABB createAabb(double d0, double d1, double d2) {
      boolean flag = d0 < 0.0D;
      boolean flag1 = d1 < 0.0D;
      boolean flag2 = d2 < 0.0D;
      double d3 = flag ? d0 : 0.0D;
      double d4 = flag1 ? d1 : 0.0D;
      double d5 = flag2 ? d2 : 0.0D;
      double d6 = (flag ? 0.0D : d0) + 1.0D;
      double d7 = (flag1 ? 0.0D : d1) + 1.0D;
      double d8 = (flag2 ? 0.0D : d2) + 1.0D;
      return new AABB(d3, d4, d5, d6, d7, d8);
   }

   private void finalizePredicates() {
      if (this.rotX != WrappedMinMaxBounds.ANY) {
         this.predicate = this.predicate.and(this.createRotationPredicate(this.rotX, Entity::getXRot));
      }

      if (this.rotY != WrappedMinMaxBounds.ANY) {
         this.predicate = this.predicate.and(this.createRotationPredicate(this.rotY, Entity::getYRot));
      }

      if (!this.level.isAny()) {
         this.predicate = this.predicate.and((entity) -> !(entity instanceof ServerPlayer) ? false : this.level.matches(((ServerPlayer)entity).experienceLevel));
      }

   }

   private Predicate<Entity> createRotationPredicate(WrappedMinMaxBounds wrappedminmaxbounds, ToDoubleFunction<Entity> todoublefunction) {
      double d0 = (double)Mth.wrapDegrees(wrappedminmaxbounds.getMin() == null ? 0.0F : wrappedminmaxbounds.getMin());
      double d1 = (double)Mth.wrapDegrees(wrappedminmaxbounds.getMax() == null ? 359.0F : wrappedminmaxbounds.getMax());
      return (entity) -> {
         double d4 = Mth.wrapDegrees(todoublefunction.applyAsDouble(entity));
         if (d0 > d1) {
            return d4 >= d0 || d4 <= d1;
         } else {
            return d4 >= d0 && d4 <= d1;
         }
      };
   }

   protected void parseSelector() throws CommandSyntaxException {
      this.usesSelectors = true;
      this.suggestions = this::suggestSelector;
      if (!this.reader.canRead()) {
         throw ERROR_MISSING_SELECTOR_TYPE.createWithContext(this.reader);
      } else {
         int i = this.reader.getCursor();
         char c0 = this.reader.read();
         if (c0 == 'p') {
            this.maxResults = 1;
            this.includesEntities = false;
            this.order = ORDER_NEAREST;
            this.limitToType(EntityType.PLAYER);
         } else if (c0 == 'a') {
            this.maxResults = Integer.MAX_VALUE;
            this.includesEntities = false;
            this.order = EntitySelector.ORDER_ARBITRARY;
            this.limitToType(EntityType.PLAYER);
         } else if (c0 == 'r') {
            this.maxResults = 1;
            this.includesEntities = false;
            this.order = ORDER_RANDOM;
            this.limitToType(EntityType.PLAYER);
         } else if (c0 == 's') {
            this.maxResults = 1;
            this.includesEntities = true;
            this.currentEntity = true;
         } else {
            if (c0 != 'e') {
               this.reader.setCursor(i);
               throw ERROR_UNKNOWN_SELECTOR_TYPE.createWithContext(this.reader, "@" + String.valueOf(c0));
            }

            this.maxResults = Integer.MAX_VALUE;
            this.includesEntities = true;
            this.order = EntitySelector.ORDER_ARBITRARY;
            this.predicate = Entity::isAlive;
         }

         this.suggestions = this::suggestOpenOptions;
         if (this.reader.canRead() && this.reader.peek() == '[') {
            this.reader.skip();
            this.suggestions = this::suggestOptionsKeyOrClose;
            this.parseOptions();
         }

      }
   }

   protected void parseNameOrUUID() throws CommandSyntaxException {
      if (this.reader.canRead()) {
         this.suggestions = this::suggestName;
      }

      int i = this.reader.getCursor();
      String s = this.reader.readString();

      try {
         this.entityUUID = UUID.fromString(s);
         this.includesEntities = true;
      } catch (IllegalArgumentException var4) {
         if (s.isEmpty() || s.length() > 16) {
            this.reader.setCursor(i);
            throw ERROR_INVALID_NAME_OR_UUID.createWithContext(this.reader);
         }

         this.includesEntities = false;
         this.playerName = s;
      }

      this.maxResults = 1;
   }

   protected void parseOptions() throws CommandSyntaxException {
      this.suggestions = this::suggestOptionsKey;
      this.reader.skipWhitespace();

      while(true) {
         if (this.reader.canRead() && this.reader.peek() != ']') {
            this.reader.skipWhitespace();
            int i = this.reader.getCursor();
            String s = this.reader.readString();
            EntitySelectorOptions.Modifier entityselectoroptions_modifier = EntitySelectorOptions.get(this, s, i);
            this.reader.skipWhitespace();
            if (!this.reader.canRead() || this.reader.peek() != '=') {
               this.reader.setCursor(i);
               throw ERROR_EXPECTED_OPTION_VALUE.createWithContext(this.reader, s);
            }

            this.reader.skip();
            this.reader.skipWhitespace();
            this.suggestions = SUGGEST_NOTHING;
            entityselectoroptions_modifier.handle(this);
            this.reader.skipWhitespace();
            this.suggestions = this::suggestOptionsNextOrClose;
            if (!this.reader.canRead()) {
               continue;
            }

            if (this.reader.peek() == ',') {
               this.reader.skip();
               this.suggestions = this::suggestOptionsKey;
               continue;
            }

            if (this.reader.peek() != ']') {
               throw ERROR_EXPECTED_END_OF_OPTIONS.createWithContext(this.reader);
            }
         }

         if (this.reader.canRead()) {
            this.reader.skip();
            this.suggestions = SUGGEST_NOTHING;
            return;
         }

         throw ERROR_EXPECTED_END_OF_OPTIONS.createWithContext(this.reader);
      }
   }

   public boolean shouldInvertValue() {
      this.reader.skipWhitespace();
      if (this.reader.canRead() && this.reader.peek() == '!') {
         this.reader.skip();
         this.reader.skipWhitespace();
         return true;
      } else {
         return false;
      }
   }

   public boolean isTag() {
      this.reader.skipWhitespace();
      if (this.reader.canRead() && this.reader.peek() == '#') {
         this.reader.skip();
         this.reader.skipWhitespace();
         return true;
      } else {
         return false;
      }
   }

   public StringReader getReader() {
      return this.reader;
   }

   public void addPredicate(Predicate<Entity> predicate) {
      this.predicate = this.predicate.and(predicate);
   }

   public void setWorldLimited() {
      this.worldLimited = true;
   }

   public MinMaxBounds.Doubles getDistance() {
      return this.distance;
   }

   public void setDistance(MinMaxBounds.Doubles minmaxbounds_doubles) {
      this.distance = minmaxbounds_doubles;
   }

   public MinMaxBounds.Ints getLevel() {
      return this.level;
   }

   public void setLevel(MinMaxBounds.Ints minmaxbounds_ints) {
      this.level = minmaxbounds_ints;
   }

   public WrappedMinMaxBounds getRotX() {
      return this.rotX;
   }

   public void setRotX(WrappedMinMaxBounds wrappedminmaxbounds) {
      this.rotX = wrappedminmaxbounds;
   }

   public WrappedMinMaxBounds getRotY() {
      return this.rotY;
   }

   public void setRotY(WrappedMinMaxBounds wrappedminmaxbounds) {
      this.rotY = wrappedminmaxbounds;
   }

   @Nullable
   public Double getX() {
      return this.x;
   }

   @Nullable
   public Double getY() {
      return this.y;
   }

   @Nullable
   public Double getZ() {
      return this.z;
   }

   public void setX(double d0) {
      this.x = d0;
   }

   public void setY(double d0) {
      this.y = d0;
   }

   public void setZ(double d0) {
      this.z = d0;
   }

   public void setDeltaX(double d0) {
      this.deltaX = d0;
   }

   public void setDeltaY(double d0) {
      this.deltaY = d0;
   }

   public void setDeltaZ(double d0) {
      this.deltaZ = d0;
   }

   @Nullable
   public Double getDeltaX() {
      return this.deltaX;
   }

   @Nullable
   public Double getDeltaY() {
      return this.deltaY;
   }

   @Nullable
   public Double getDeltaZ() {
      return this.deltaZ;
   }

   public void setMaxResults(int i) {
      this.maxResults = i;
   }

   public void setIncludesEntities(boolean flag) {
      this.includesEntities = flag;
   }

   public BiConsumer<Vec3, List<? extends Entity>> getOrder() {
      return this.order;
   }

   public void setOrder(BiConsumer<Vec3, List<? extends Entity>> biconsumer) {
      this.order = biconsumer;
   }

   public EntitySelector parse() throws CommandSyntaxException {
      this.startPosition = this.reader.getCursor();
      this.suggestions = this::suggestNameOrSelector;
      if (this.reader.canRead() && this.reader.peek() == '@') {
         if (!this.allowSelectors) {
            throw ERROR_SELECTORS_NOT_ALLOWED.createWithContext(this.reader);
         }

         this.reader.skip();
         this.parseSelector();
      } else {
         this.parseNameOrUUID();
      }

      this.finalizePredicates();
      return this.getSelector();
   }

   private static void fillSelectorSuggestions(SuggestionsBuilder suggestionsbuilder) {
      suggestionsbuilder.suggest("@p", Component.translatable("argument.entity.selector.nearestPlayer"));
      suggestionsbuilder.suggest("@a", Component.translatable("argument.entity.selector.allPlayers"));
      suggestionsbuilder.suggest("@r", Component.translatable("argument.entity.selector.randomPlayer"));
      suggestionsbuilder.suggest("@s", Component.translatable("argument.entity.selector.self"));
      suggestionsbuilder.suggest("@e", Component.translatable("argument.entity.selector.allEntities"));
   }

   private CompletableFuture<Suggestions> suggestNameOrSelector(SuggestionsBuilder suggestionsbuilder, Consumer<SuggestionsBuilder> consumer) {
      consumer.accept(suggestionsbuilder);
      if (this.allowSelectors) {
         fillSelectorSuggestions(suggestionsbuilder);
      }

      return suggestionsbuilder.buildFuture();
   }

   private CompletableFuture<Suggestions> suggestName(SuggestionsBuilder suggestionsbuilder, Consumer<SuggestionsBuilder> consumer) {
      SuggestionsBuilder suggestionsbuilder1 = suggestionsbuilder.createOffset(this.startPosition);
      consumer.accept(suggestionsbuilder1);
      return suggestionsbuilder.add(suggestionsbuilder1).buildFuture();
   }

   private CompletableFuture<Suggestions> suggestSelector(SuggestionsBuilder suggestionsbuilder, Consumer<SuggestionsBuilder> consumer) {
      SuggestionsBuilder suggestionsbuilder1 = suggestionsbuilder.createOffset(suggestionsbuilder.getStart() - 1);
      fillSelectorSuggestions(suggestionsbuilder1);
      suggestionsbuilder.add(suggestionsbuilder1);
      return suggestionsbuilder.buildFuture();
   }

   private CompletableFuture<Suggestions> suggestOpenOptions(SuggestionsBuilder suggestionsbuilder2, Consumer<SuggestionsBuilder> consumer1) {
      suggestionsbuilder2.suggest(String.valueOf('['));
      return suggestionsbuilder2.buildFuture();
   }

   private CompletableFuture<Suggestions> suggestOptionsKeyOrClose(SuggestionsBuilder suggestionsbuilder3, Consumer<SuggestionsBuilder> consumer2) {
      suggestionsbuilder3.suggest(String.valueOf(']'));
      EntitySelectorOptions.suggestNames(this, suggestionsbuilder3);
      return suggestionsbuilder3.buildFuture();
   }

   private CompletableFuture<Suggestions> suggestOptionsKey(SuggestionsBuilder suggestionsbuilder, Consumer<SuggestionsBuilder> consumer) {
      EntitySelectorOptions.suggestNames(this, suggestionsbuilder);
      return suggestionsbuilder.buildFuture();
   }

   private CompletableFuture<Suggestions> suggestOptionsNextOrClose(SuggestionsBuilder suggestionsbuilder1, Consumer<SuggestionsBuilder> consumer1) {
      suggestionsbuilder1.suggest(String.valueOf(','));
      suggestionsbuilder1.suggest(String.valueOf(']'));
      return suggestionsbuilder1.buildFuture();
   }

   private CompletableFuture<Suggestions> suggestEquals(SuggestionsBuilder suggestionsbuilder, Consumer<SuggestionsBuilder> consumer) {
      suggestionsbuilder.suggest(String.valueOf('='));
      return suggestionsbuilder.buildFuture();
   }

   public boolean isCurrentEntity() {
      return this.currentEntity;
   }

   public void setSuggestions(BiFunction<SuggestionsBuilder, Consumer<SuggestionsBuilder>, CompletableFuture<Suggestions>> bifunction) {
      this.suggestions = bifunction;
   }

   public CompletableFuture<Suggestions> fillSuggestions(SuggestionsBuilder suggestionsbuilder, Consumer<SuggestionsBuilder> consumer) {
      return this.suggestions.apply(suggestionsbuilder.createOffset(this.reader.getCursor()), consumer);
   }

   public boolean hasNameEquals() {
      return this.hasNameEquals;
   }

   public void setHasNameEquals(boolean flag) {
      this.hasNameEquals = flag;
   }

   public boolean hasNameNotEquals() {
      return this.hasNameNotEquals;
   }

   public void setHasNameNotEquals(boolean flag) {
      this.hasNameNotEquals = flag;
   }

   public boolean isLimited() {
      return this.isLimited;
   }

   public void setLimited(boolean flag) {
      this.isLimited = flag;
   }

   public boolean isSorted() {
      return this.isSorted;
   }

   public void setSorted(boolean flag) {
      this.isSorted = flag;
   }

   public boolean hasGamemodeEquals() {
      return this.hasGamemodeEquals;
   }

   public void setHasGamemodeEquals(boolean flag) {
      this.hasGamemodeEquals = flag;
   }

   public boolean hasGamemodeNotEquals() {
      return this.hasGamemodeNotEquals;
   }

   public void setHasGamemodeNotEquals(boolean flag) {
      this.hasGamemodeNotEquals = flag;
   }

   public boolean hasTeamEquals() {
      return this.hasTeamEquals;
   }

   public void setHasTeamEquals(boolean flag) {
      this.hasTeamEquals = flag;
   }

   public boolean hasTeamNotEquals() {
      return this.hasTeamNotEquals;
   }

   public void setHasTeamNotEquals(boolean flag) {
      this.hasTeamNotEquals = flag;
   }

   public void limitToType(EntityType<?> entitytype) {
      this.type = entitytype;
   }

   public void setTypeLimitedInversely() {
      this.typeInverse = true;
   }

   public boolean isTypeLimited() {
      return this.type != null;
   }

   public boolean isTypeLimitedInversely() {
      return this.typeInverse;
   }

   public boolean hasScores() {
      return this.hasScores;
   }

   public void setHasScores(boolean flag) {
      this.hasScores = flag;
   }

   public boolean hasAdvancements() {
      return this.hasAdvancements;
   }

   public void setHasAdvancements(boolean flag) {
      this.hasAdvancements = flag;
   }
}
