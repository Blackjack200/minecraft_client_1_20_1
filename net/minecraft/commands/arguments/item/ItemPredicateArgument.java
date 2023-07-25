package net.minecraft.commands.arguments.item;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.util.Either;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemPredicateArgument implements ArgumentType<ItemPredicateArgument.Result> {
   private static final Collection<String> EXAMPLES = Arrays.asList("stick", "minecraft:stick", "#stick", "#stick{foo=bar}");
   private final HolderLookup<Item> items;

   public ItemPredicateArgument(CommandBuildContext commandbuildcontext) {
      this.items = commandbuildcontext.holderLookup(Registries.ITEM);
   }

   public static ItemPredicateArgument itemPredicate(CommandBuildContext commandbuildcontext) {
      return new ItemPredicateArgument(commandbuildcontext);
   }

   public ItemPredicateArgument.Result parse(StringReader stringreader) throws CommandSyntaxException {
      Either<ItemParser.ItemResult, ItemParser.TagResult> either = ItemParser.parseForTesting(this.items, stringreader);
      return either.map((itemparser_itemresult) -> createResult((holder) -> holder == itemparser_itemresult.item(), itemparser_itemresult.nbt()), (itemparser_tagresult) -> createResult(itemparser_tagresult.tag()::contains, itemparser_tagresult.nbt()));
   }

   public static Predicate<ItemStack> getItemPredicate(CommandContext<CommandSourceStack> commandcontext, String s) {
      return commandcontext.getArgument(s, ItemPredicateArgument.Result.class);
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandcontext, SuggestionsBuilder suggestionsbuilder) {
      return ItemParser.fillSuggestions(this.items, suggestionsbuilder, true);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   private static ItemPredicateArgument.Result createResult(Predicate<Holder<Item>> predicate, @Nullable CompoundTag compoundtag) {
      return compoundtag != null ? (itemstack1) -> itemstack1.is(predicate) && NbtUtils.compareNbt(compoundtag, itemstack1.getTag(), true) : (itemstack) -> itemstack.is(predicate);
   }

   public interface Result extends Predicate<ItemStack> {
   }
}
