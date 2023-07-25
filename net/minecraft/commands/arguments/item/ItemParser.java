package net.minecraft.commands.arguments.item;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.util.Either;
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
import net.minecraft.world.item.Item;

public class ItemParser {
   private static final SimpleCommandExceptionType ERROR_NO_TAGS_ALLOWED = new SimpleCommandExceptionType(Component.translatable("argument.item.tag.disallowed"));
   private static final DynamicCommandExceptionType ERROR_UNKNOWN_ITEM = new DynamicCommandExceptionType((object) -> Component.translatable("argument.item.id.invalid", object));
   private static final DynamicCommandExceptionType ERROR_UNKNOWN_TAG = new DynamicCommandExceptionType((object) -> Component.translatable("arguments.item.tag.unknown", object));
   private static final char SYNTAX_START_NBT = '{';
   private static final char SYNTAX_TAG = '#';
   private static final Function<SuggestionsBuilder, CompletableFuture<Suggestions>> SUGGEST_NOTHING = SuggestionsBuilder::buildFuture;
   private final HolderLookup<Item> items;
   private final StringReader reader;
   private final boolean allowTags;
   private Either<Holder<Item>, HolderSet<Item>> result;
   @Nullable
   private CompoundTag nbt;
   private Function<SuggestionsBuilder, CompletableFuture<Suggestions>> suggestions = SUGGEST_NOTHING;

   private ItemParser(HolderLookup<Item> holderlookup, StringReader stringreader, boolean flag) {
      this.items = holderlookup;
      this.reader = stringreader;
      this.allowTags = flag;
   }

   public static ItemParser.ItemResult parseForItem(HolderLookup<Item> holderlookup, StringReader stringreader) throws CommandSyntaxException {
      int i = stringreader.getCursor();

      try {
         ItemParser itemparser = new ItemParser(holderlookup, stringreader, false);
         itemparser.parse();
         Holder<Item> holder = itemparser.result.left().orElseThrow(() -> new IllegalStateException("Parser returned unexpected tag name"));
         return new ItemParser.ItemResult(holder, itemparser.nbt);
      } catch (CommandSyntaxException var5) {
         stringreader.setCursor(i);
         throw var5;
      }
   }

   public static Either<ItemParser.ItemResult, ItemParser.TagResult> parseForTesting(HolderLookup<Item> holderlookup, StringReader stringreader) throws CommandSyntaxException {
      int i = stringreader.getCursor();

      try {
         ItemParser itemparser = new ItemParser(holderlookup, stringreader, true);
         itemparser.parse();
         return itemparser.result.mapBoth((holder) -> new ItemParser.ItemResult(holder, itemparser.nbt), (holderset) -> new ItemParser.TagResult(holderset, itemparser.nbt));
      } catch (CommandSyntaxException var4) {
         stringreader.setCursor(i);
         throw var4;
      }
   }

   public static CompletableFuture<Suggestions> fillSuggestions(HolderLookup<Item> holderlookup, SuggestionsBuilder suggestionsbuilder, boolean flag) {
      StringReader stringreader = new StringReader(suggestionsbuilder.getInput());
      stringreader.setCursor(suggestionsbuilder.getStart());
      ItemParser itemparser = new ItemParser(holderlookup, stringreader, flag);

      try {
         itemparser.parse();
      } catch (CommandSyntaxException var6) {
      }

      return itemparser.suggestions.apply(suggestionsbuilder.createOffset(stringreader.getCursor()));
   }

   private void readItem() throws CommandSyntaxException {
      int i = this.reader.getCursor();
      ResourceLocation resourcelocation = ResourceLocation.read(this.reader);
      Optional<? extends Holder<Item>> optional = this.items.get(ResourceKey.create(Registries.ITEM, resourcelocation));
      this.result = Either.left(optional.orElseThrow(() -> {
         this.reader.setCursor(i);
         return ERROR_UNKNOWN_ITEM.createWithContext(this.reader, resourcelocation);
      }));
   }

   private void readTag() throws CommandSyntaxException {
      if (!this.allowTags) {
         throw ERROR_NO_TAGS_ALLOWED.createWithContext(this.reader);
      } else {
         int i = this.reader.getCursor();
         this.reader.expect('#');
         this.suggestions = this::suggestTag;
         ResourceLocation resourcelocation = ResourceLocation.read(this.reader);
         Optional<? extends HolderSet<Item>> optional = this.items.get(TagKey.create(Registries.ITEM, resourcelocation));
         this.result = Either.right(optional.orElseThrow(() -> {
            this.reader.setCursor(i);
            return ERROR_UNKNOWN_TAG.createWithContext(this.reader, resourcelocation);
         }));
      }
   }

   private void readNbt() throws CommandSyntaxException {
      this.nbt = (new TagParser(this.reader)).readStruct();
   }

   private void parse() throws CommandSyntaxException {
      if (this.allowTags) {
         this.suggestions = this::suggestItemIdOrTag;
      } else {
         this.suggestions = this::suggestItem;
      }

      if (this.reader.canRead() && this.reader.peek() == '#') {
         this.readTag();
      } else {
         this.readItem();
      }

      this.suggestions = this::suggestOpenNbt;
      if (this.reader.canRead() && this.reader.peek() == '{') {
         this.suggestions = SUGGEST_NOTHING;
         this.readNbt();
      }

   }

   private CompletableFuture<Suggestions> suggestOpenNbt(SuggestionsBuilder suggestionsbuilder) {
      if (suggestionsbuilder.getRemaining().isEmpty()) {
         suggestionsbuilder.suggest(String.valueOf('{'));
      }

      return suggestionsbuilder.buildFuture();
   }

   private CompletableFuture<Suggestions> suggestTag(SuggestionsBuilder suggestionsbuilder) {
      return SharedSuggestionProvider.suggestResource(this.items.listTagIds().map(TagKey::location), suggestionsbuilder, String.valueOf('#'));
   }

   private CompletableFuture<Suggestions> suggestItem(SuggestionsBuilder suggestionsbuilder1) {
      return SharedSuggestionProvider.suggestResource(this.items.listElementIds().map(ResourceKey::location), suggestionsbuilder1);
   }

   private CompletableFuture<Suggestions> suggestItemIdOrTag(SuggestionsBuilder suggestionsbuilder2) {
      this.suggestTag(suggestionsbuilder2);
      return this.suggestItem(suggestionsbuilder2);
   }

   public static record ItemResult(Holder<Item> item, @Nullable CompoundTag nbt) {
   }

   public static record TagResult(HolderSet<Item> tag, @Nullable CompoundTag nbt) {
   }
}
