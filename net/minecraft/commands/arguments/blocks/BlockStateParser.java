package net.minecraft.commands.arguments.blocks;

import com.google.common.collect.Maps;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.util.Either;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockStateParser {
   public static final SimpleCommandExceptionType ERROR_NO_TAGS_ALLOWED = new SimpleCommandExceptionType(Component.translatable("argument.block.tag.disallowed"));
   public static final DynamicCommandExceptionType ERROR_UNKNOWN_BLOCK = new DynamicCommandExceptionType((object) -> Component.translatable("argument.block.id.invalid", object));
   public static final Dynamic2CommandExceptionType ERROR_UNKNOWN_PROPERTY = new Dynamic2CommandExceptionType((object, object1) -> Component.translatable("argument.block.property.unknown", object, object1));
   public static final Dynamic2CommandExceptionType ERROR_DUPLICATE_PROPERTY = new Dynamic2CommandExceptionType((object, object1) -> Component.translatable("argument.block.property.duplicate", object1, object));
   public static final Dynamic3CommandExceptionType ERROR_INVALID_VALUE = new Dynamic3CommandExceptionType((object, object1, object2) -> Component.translatable("argument.block.property.invalid", object, object2, object1));
   public static final Dynamic2CommandExceptionType ERROR_EXPECTED_VALUE = new Dynamic2CommandExceptionType((object, object1) -> Component.translatable("argument.block.property.novalue", object, object1));
   public static final SimpleCommandExceptionType ERROR_EXPECTED_END_OF_PROPERTIES = new SimpleCommandExceptionType(Component.translatable("argument.block.property.unclosed"));
   public static final DynamicCommandExceptionType ERROR_UNKNOWN_TAG = new DynamicCommandExceptionType((object) -> Component.translatable("arguments.block.tag.unknown", object));
   private static final char SYNTAX_START_PROPERTIES = '[';
   private static final char SYNTAX_START_NBT = '{';
   private static final char SYNTAX_END_PROPERTIES = ']';
   private static final char SYNTAX_EQUALS = '=';
   private static final char SYNTAX_PROPERTY_SEPARATOR = ',';
   private static final char SYNTAX_TAG = '#';
   private static final Function<SuggestionsBuilder, CompletableFuture<Suggestions>> SUGGEST_NOTHING = SuggestionsBuilder::buildFuture;
   private final HolderLookup<Block> blocks;
   private final StringReader reader;
   private final boolean forTesting;
   private final boolean allowNbt;
   private final Map<Property<?>, Comparable<?>> properties = Maps.newHashMap();
   private final Map<String, String> vagueProperties = Maps.newHashMap();
   private ResourceLocation id = new ResourceLocation("");
   @Nullable
   private StateDefinition<Block, BlockState> definition;
   @Nullable
   private BlockState state;
   @Nullable
   private CompoundTag nbt;
   @Nullable
   private HolderSet<Block> tag;
   private Function<SuggestionsBuilder, CompletableFuture<Suggestions>> suggestions = SUGGEST_NOTHING;

   private BlockStateParser(HolderLookup<Block> holderlookup, StringReader stringreader, boolean flag, boolean flag1) {
      this.blocks = holderlookup;
      this.reader = stringreader;
      this.forTesting = flag;
      this.allowNbt = flag1;
   }

   public static BlockStateParser.BlockResult parseForBlock(HolderLookup<Block> holderlookup, String s, boolean flag) throws CommandSyntaxException {
      return parseForBlock(holderlookup, new StringReader(s), flag);
   }

   public static BlockStateParser.BlockResult parseForBlock(HolderLookup<Block> holderlookup, StringReader stringreader, boolean flag) throws CommandSyntaxException {
      int i = stringreader.getCursor();

      try {
         BlockStateParser blockstateparser = new BlockStateParser(holderlookup, stringreader, false, flag);
         blockstateparser.parse();
         return new BlockStateParser.BlockResult(blockstateparser.state, blockstateparser.properties, blockstateparser.nbt);
      } catch (CommandSyntaxException var5) {
         stringreader.setCursor(i);
         throw var5;
      }
   }

   public static Either<BlockStateParser.BlockResult, BlockStateParser.TagResult> parseForTesting(HolderLookup<Block> holderlookup, String s, boolean flag) throws CommandSyntaxException {
      return parseForTesting(holderlookup, new StringReader(s), flag);
   }

   public static Either<BlockStateParser.BlockResult, BlockStateParser.TagResult> parseForTesting(HolderLookup<Block> holderlookup, StringReader stringreader, boolean flag) throws CommandSyntaxException {
      int i = stringreader.getCursor();

      try {
         BlockStateParser blockstateparser = new BlockStateParser(holderlookup, stringreader, true, flag);
         blockstateparser.parse();
         return blockstateparser.tag != null ? Either.right(new BlockStateParser.TagResult(blockstateparser.tag, blockstateparser.vagueProperties, blockstateparser.nbt)) : Either.left(new BlockStateParser.BlockResult(blockstateparser.state, blockstateparser.properties, blockstateparser.nbt));
      } catch (CommandSyntaxException var5) {
         stringreader.setCursor(i);
         throw var5;
      }
   }

   public static CompletableFuture<Suggestions> fillSuggestions(HolderLookup<Block> holderlookup, SuggestionsBuilder suggestionsbuilder, boolean flag, boolean flag1) {
      StringReader stringreader = new StringReader(suggestionsbuilder.getInput());
      stringreader.setCursor(suggestionsbuilder.getStart());
      BlockStateParser blockstateparser = new BlockStateParser(holderlookup, stringreader, flag, flag1);

      try {
         blockstateparser.parse();
      } catch (CommandSyntaxException var7) {
      }

      return blockstateparser.suggestions.apply(suggestionsbuilder.createOffset(stringreader.getCursor()));
   }

   private void parse() throws CommandSyntaxException {
      if (this.forTesting) {
         this.suggestions = this::suggestBlockIdOrTag;
      } else {
         this.suggestions = this::suggestItem;
      }

      if (this.reader.canRead() && this.reader.peek() == '#') {
         this.readTag();
         this.suggestions = this::suggestOpenVaguePropertiesOrNbt;
         if (this.reader.canRead() && this.reader.peek() == '[') {
            this.readVagueProperties();
            this.suggestions = this::suggestOpenNbt;
         }
      } else {
         this.readBlock();
         this.suggestions = this::suggestOpenPropertiesOrNbt;
         if (this.reader.canRead() && this.reader.peek() == '[') {
            this.readProperties();
            this.suggestions = this::suggestOpenNbt;
         }
      }

      if (this.allowNbt && this.reader.canRead() && this.reader.peek() == '{') {
         this.suggestions = SUGGEST_NOTHING;
         this.readNbt();
      }

   }

   private CompletableFuture<Suggestions> suggestPropertyNameOrEnd(SuggestionsBuilder suggestionsbuilder) {
      if (suggestionsbuilder.getRemaining().isEmpty()) {
         suggestionsbuilder.suggest(String.valueOf(']'));
      }

      return this.suggestPropertyName(suggestionsbuilder);
   }

   private CompletableFuture<Suggestions> suggestVaguePropertyNameOrEnd(SuggestionsBuilder suggestionsbuilder) {
      if (suggestionsbuilder.getRemaining().isEmpty()) {
         suggestionsbuilder.suggest(String.valueOf(']'));
      }

      return this.suggestVaguePropertyName(suggestionsbuilder);
   }

   private CompletableFuture<Suggestions> suggestPropertyName(SuggestionsBuilder suggestionsbuilder) {
      String s = suggestionsbuilder.getRemaining().toLowerCase(Locale.ROOT);

      for(Property<?> property : this.state.getProperties()) {
         if (!this.properties.containsKey(property) && property.getName().startsWith(s)) {
            suggestionsbuilder.suggest(property.getName() + "=");
         }
      }

      return suggestionsbuilder.buildFuture();
   }

   private CompletableFuture<Suggestions> suggestVaguePropertyName(SuggestionsBuilder suggestionsbuilder) {
      String s = suggestionsbuilder.getRemaining().toLowerCase(Locale.ROOT);
      if (this.tag != null) {
         for(Holder<Block> holder : this.tag) {
            for(Property<?> property : holder.value().getStateDefinition().getProperties()) {
               if (!this.vagueProperties.containsKey(property.getName()) && property.getName().startsWith(s)) {
                  suggestionsbuilder.suggest(property.getName() + "=");
               }
            }
         }
      }

      return suggestionsbuilder.buildFuture();
   }

   private CompletableFuture<Suggestions> suggestOpenNbt(SuggestionsBuilder suggestionsbuilder) {
      if (suggestionsbuilder.getRemaining().isEmpty() && this.hasBlockEntity()) {
         suggestionsbuilder.suggest(String.valueOf('{'));
      }

      return suggestionsbuilder.buildFuture();
   }

   private boolean hasBlockEntity() {
      if (this.state != null) {
         return this.state.hasBlockEntity();
      } else {
         if (this.tag != null) {
            for(Holder<Block> holder : this.tag) {
               if (holder.value().defaultBlockState().hasBlockEntity()) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   private CompletableFuture<Suggestions> suggestEquals(SuggestionsBuilder suggestionsbuilder) {
      if (suggestionsbuilder.getRemaining().isEmpty()) {
         suggestionsbuilder.suggest(String.valueOf('='));
      }

      return suggestionsbuilder.buildFuture();
   }

   private CompletableFuture<Suggestions> suggestNextPropertyOrEnd(SuggestionsBuilder suggestionsbuilder) {
      if (suggestionsbuilder.getRemaining().isEmpty()) {
         suggestionsbuilder.suggest(String.valueOf(']'));
      }

      if (suggestionsbuilder.getRemaining().isEmpty() && this.properties.size() < this.state.getProperties().size()) {
         suggestionsbuilder.suggest(String.valueOf(','));
      }

      return suggestionsbuilder.buildFuture();
   }

   private static <T extends Comparable<T>> SuggestionsBuilder addSuggestions(SuggestionsBuilder suggestionsbuilder, Property<T> property) {
      for(T comparable : property.getPossibleValues()) {
         if (comparable instanceof Integer integer) {
            suggestionsbuilder.suggest(integer);
         } else {
            suggestionsbuilder.suggest(property.getName(comparable));
         }
      }

      return suggestionsbuilder;
   }

   private CompletableFuture<Suggestions> suggestVaguePropertyValue(SuggestionsBuilder suggestionsbuilder, String s) {
      boolean flag = false;
      if (this.tag != null) {
         for(Holder<Block> holder : this.tag) {
            Block block = holder.value();
            Property<?> property = block.getStateDefinition().getProperty(s);
            if (property != null) {
               addSuggestions(suggestionsbuilder, property);
            }

            if (!flag) {
               for(Property<?> property1 : block.getStateDefinition().getProperties()) {
                  if (!this.vagueProperties.containsKey(property1.getName())) {
                     flag = true;
                     break;
                  }
               }
            }
         }
      }

      if (flag) {
         suggestionsbuilder.suggest(String.valueOf(','));
      }

      suggestionsbuilder.suggest(String.valueOf(']'));
      return suggestionsbuilder.buildFuture();
   }

   private CompletableFuture<Suggestions> suggestOpenVaguePropertiesOrNbt(SuggestionsBuilder suggestionsbuilder1) {
      if (suggestionsbuilder1.getRemaining().isEmpty() && this.tag != null) {
         boolean flag = false;
         boolean flag1 = false;

         for(Holder<Block> holder : this.tag) {
            Block block = holder.value();
            flag |= !block.getStateDefinition().getProperties().isEmpty();
            flag1 |= block.defaultBlockState().hasBlockEntity();
            if (flag && flag1) {
               break;
            }
         }

         if (flag) {
            suggestionsbuilder1.suggest(String.valueOf('['));
         }

         if (flag1) {
            suggestionsbuilder1.suggest(String.valueOf('{'));
         }
      }

      return suggestionsbuilder1.buildFuture();
   }

   private CompletableFuture<Suggestions> suggestOpenPropertiesOrNbt(SuggestionsBuilder suggestionsbuilder2) {
      if (suggestionsbuilder2.getRemaining().isEmpty()) {
         if (!this.definition.getProperties().isEmpty()) {
            suggestionsbuilder2.suggest(String.valueOf('['));
         }

         if (this.state.hasBlockEntity()) {
            suggestionsbuilder2.suggest(String.valueOf('{'));
         }
      }

      return suggestionsbuilder2.buildFuture();
   }

   private CompletableFuture<Suggestions> suggestTag(SuggestionsBuilder suggestionsbuilder) {
      return SharedSuggestionProvider.suggestResource(this.blocks.listTagIds().map(TagKey::location), suggestionsbuilder, String.valueOf('#'));
   }

   private CompletableFuture<Suggestions> suggestItem(SuggestionsBuilder suggestionsbuilder3) {
      return SharedSuggestionProvider.suggestResource(this.blocks.listElementIds().map(ResourceKey::location), suggestionsbuilder3);
   }

   private CompletableFuture<Suggestions> suggestBlockIdOrTag(SuggestionsBuilder suggestionsbuilder4) {
      this.suggestTag(suggestionsbuilder4);
      this.suggestItem(suggestionsbuilder4);
      return suggestionsbuilder4.buildFuture();
   }

   private void readBlock() throws CommandSyntaxException {
      int i = this.reader.getCursor();
      this.id = ResourceLocation.read(this.reader);
      Block block = this.blocks.get(ResourceKey.create(Registries.BLOCK, this.id)).orElseThrow(() -> {
         this.reader.setCursor(i);
         return ERROR_UNKNOWN_BLOCK.createWithContext(this.reader, this.id.toString());
      }).value();
      this.definition = block.getStateDefinition();
      this.state = block.defaultBlockState();
   }

   private void readTag() throws CommandSyntaxException {
      if (!this.forTesting) {
         throw ERROR_NO_TAGS_ALLOWED.createWithContext(this.reader);
      } else {
         int i = this.reader.getCursor();
         this.reader.expect('#');
         this.suggestions = this::suggestTag;
         ResourceLocation resourcelocation = ResourceLocation.read(this.reader);
         this.tag = this.blocks.get(TagKey.create(Registries.BLOCK, resourcelocation)).orElseThrow(() -> {
            this.reader.setCursor(i);
            return ERROR_UNKNOWN_TAG.createWithContext(this.reader, resourcelocation.toString());
         });
      }
   }

   private void readProperties() throws CommandSyntaxException {
      this.reader.skip();
      this.suggestions = this::suggestPropertyNameOrEnd;
      this.reader.skipWhitespace();

      while(true) {
         if (this.reader.canRead() && this.reader.peek() != ']') {
            this.reader.skipWhitespace();
            int i = this.reader.getCursor();
            String s = this.reader.readString();
            Property<?> property = this.definition.getProperty(s);
            if (property == null) {
               this.reader.setCursor(i);
               throw ERROR_UNKNOWN_PROPERTY.createWithContext(this.reader, this.id.toString(), s);
            }

            if (this.properties.containsKey(property)) {
               this.reader.setCursor(i);
               throw ERROR_DUPLICATE_PROPERTY.createWithContext(this.reader, this.id.toString(), s);
            }

            this.reader.skipWhitespace();
            this.suggestions = this::suggestEquals;
            if (!this.reader.canRead() || this.reader.peek() != '=') {
               throw ERROR_EXPECTED_VALUE.createWithContext(this.reader, this.id.toString(), s);
            }

            this.reader.skip();
            this.reader.skipWhitespace();
            this.suggestions = (suggestionsbuilder) -> addSuggestions(suggestionsbuilder, property).buildFuture();
            int j = this.reader.getCursor();
            this.setValue(property, this.reader.readString(), j);
            this.suggestions = this::suggestNextPropertyOrEnd;
            this.reader.skipWhitespace();
            if (!this.reader.canRead()) {
               continue;
            }

            if (this.reader.peek() == ',') {
               this.reader.skip();
               this.suggestions = this::suggestPropertyName;
               continue;
            }

            if (this.reader.peek() != ']') {
               throw ERROR_EXPECTED_END_OF_PROPERTIES.createWithContext(this.reader);
            }
         }

         if (this.reader.canRead()) {
            this.reader.skip();
            return;
         }

         throw ERROR_EXPECTED_END_OF_PROPERTIES.createWithContext(this.reader);
      }
   }

   private void readVagueProperties() throws CommandSyntaxException {
      this.reader.skip();
      this.suggestions = this::suggestVaguePropertyNameOrEnd;
      int i = -1;
      this.reader.skipWhitespace();

      while(true) {
         if (this.reader.canRead() && this.reader.peek() != ']') {
            this.reader.skipWhitespace();
            int j = this.reader.getCursor();
            String s = this.reader.readString();
            if (this.vagueProperties.containsKey(s)) {
               this.reader.setCursor(j);
               throw ERROR_DUPLICATE_PROPERTY.createWithContext(this.reader, this.id.toString(), s);
            }

            this.reader.skipWhitespace();
            if (!this.reader.canRead() || this.reader.peek() != '=') {
               this.reader.setCursor(j);
               throw ERROR_EXPECTED_VALUE.createWithContext(this.reader, this.id.toString(), s);
            }

            this.reader.skip();
            this.reader.skipWhitespace();
            this.suggestions = (suggestionsbuilder) -> this.suggestVaguePropertyValue(suggestionsbuilder, s);
            i = this.reader.getCursor();
            String s1 = this.reader.readString();
            this.vagueProperties.put(s, s1);
            this.reader.skipWhitespace();
            if (!this.reader.canRead()) {
               continue;
            }

            i = -1;
            if (this.reader.peek() == ',') {
               this.reader.skip();
               this.suggestions = this::suggestVaguePropertyName;
               continue;
            }

            if (this.reader.peek() != ']') {
               throw ERROR_EXPECTED_END_OF_PROPERTIES.createWithContext(this.reader);
            }
         }

         if (this.reader.canRead()) {
            this.reader.skip();
            return;
         }

         if (i >= 0) {
            this.reader.setCursor(i);
         }

         throw ERROR_EXPECTED_END_OF_PROPERTIES.createWithContext(this.reader);
      }
   }

   private void readNbt() throws CommandSyntaxException {
      this.nbt = (new TagParser(this.reader)).readStruct();
   }

   private <T extends Comparable<T>> void setValue(Property<T> property, String s, int i) throws CommandSyntaxException {
      Optional<T> optional = property.getValue(s);
      if (optional.isPresent()) {
         this.state = this.state.setValue(property, optional.get());
         this.properties.put(property, optional.get());
      } else {
         this.reader.setCursor(i);
         throw ERROR_INVALID_VALUE.createWithContext(this.reader, this.id.toString(), property.getName(), s);
      }
   }

   public static String serialize(BlockState blockstate) {
      StringBuilder stringbuilder = new StringBuilder(blockstate.getBlockHolder().unwrapKey().map((resourcekey) -> resourcekey.location().toString()).orElse("air"));
      if (!blockstate.getProperties().isEmpty()) {
         stringbuilder.append('[');
         boolean flag = false;

         for(Map.Entry<Property<?>, Comparable<?>> map_entry : blockstate.getValues().entrySet()) {
            if (flag) {
               stringbuilder.append(',');
            }

            appendProperty(stringbuilder, map_entry.getKey(), map_entry.getValue());
            flag = true;
         }

         stringbuilder.append(']');
      }

      return stringbuilder.toString();
   }

   private static <T extends Comparable<T>> void appendProperty(StringBuilder stringbuilder, Property<T> property, Comparable<?> comparable) {
      stringbuilder.append(property.getName());
      stringbuilder.append('=');
      stringbuilder.append(property.getName((T)comparable));
   }

   public static record BlockResult(BlockState blockState, Map<Property<?>, Comparable<?>> properties, @Nullable CompoundTag nbt) {
   }

   public static record TagResult(HolderSet<Block> tag, Map<String, String> vagueProperties, @Nullable CompoundTag nbt) {
   }
}
