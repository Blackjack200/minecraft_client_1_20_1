package net.minecraft.commands.arguments;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class ObjectiveCriteriaArgument implements ArgumentType<ObjectiveCriteria> {
   private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo.bar.baz", "minecraft:foo");
   public static final DynamicCommandExceptionType ERROR_INVALID_VALUE = new DynamicCommandExceptionType((object) -> Component.translatable("argument.criteria.invalid", object));

   private ObjectiveCriteriaArgument() {
   }

   public static ObjectiveCriteriaArgument criteria() {
      return new ObjectiveCriteriaArgument();
   }

   public static ObjectiveCriteria getCriteria(CommandContext<CommandSourceStack> commandcontext, String s) {
      return commandcontext.getArgument(s, ObjectiveCriteria.class);
   }

   public ObjectiveCriteria parse(StringReader stringreader) throws CommandSyntaxException {
      int i = stringreader.getCursor();

      while(stringreader.canRead() && stringreader.peek() != ' ') {
         stringreader.skip();
      }

      String s = stringreader.getString().substring(i, stringreader.getCursor());
      return ObjectiveCriteria.byName(s).orElseThrow(() -> {
         stringreader.setCursor(i);
         return ERROR_INVALID_VALUE.create(s);
      });
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandcontext, SuggestionsBuilder suggestionsbuilder) {
      List<String> list = Lists.newArrayList(ObjectiveCriteria.getCustomCriteriaNames());

      for(StatType<?> stattype : BuiltInRegistries.STAT_TYPE) {
         for(Object object : stattype.getRegistry()) {
            String s = this.getName(stattype, object);
            list.add(s);
         }
      }

      return SharedSuggestionProvider.suggest(list, suggestionsbuilder);
   }

   public <T> String getName(StatType<T> stattype, Object object) {
      return Stat.buildName(stattype, (T)object);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}
