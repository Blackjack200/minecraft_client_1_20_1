package net.minecraft.commands.arguments.item;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

public class ItemArgument implements ArgumentType<ItemInput> {
   private static final Collection<String> EXAMPLES = Arrays.asList("stick", "minecraft:stick", "stick{foo=bar}");
   private final HolderLookup<Item> items;

   public ItemArgument(CommandBuildContext commandbuildcontext) {
      this.items = commandbuildcontext.holderLookup(Registries.ITEM);
   }

   public static ItemArgument item(CommandBuildContext commandbuildcontext) {
      return new ItemArgument(commandbuildcontext);
   }

   public ItemInput parse(StringReader stringreader) throws CommandSyntaxException {
      ItemParser.ItemResult itemparser_itemresult = ItemParser.parseForItem(this.items, stringreader);
      return new ItemInput(itemparser_itemresult.item(), itemparser_itemresult.nbt());
   }

   public static <S> ItemInput getItem(CommandContext<S> commandcontext, String s) {
      return commandcontext.getArgument(s, ItemInput.class);
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandcontext, SuggestionsBuilder suggestionsbuilder) {
      return ItemParser.fillSuggestions(this.items, suggestionsbuilder, false);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}
