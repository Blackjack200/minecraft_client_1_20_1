package net.minecraft.commands.arguments;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

public class ScoreHolderArgument implements ArgumentType<ScoreHolderArgument.Result> {
   public static final SuggestionProvider<CommandSourceStack> SUGGEST_SCORE_HOLDERS = (commandcontext, suggestionsbuilder) -> {
      StringReader stringreader = new StringReader(suggestionsbuilder.getInput());
      stringreader.setCursor(suggestionsbuilder.getStart());
      EntitySelectorParser entityselectorparser = new EntitySelectorParser(stringreader);

      try {
         entityselectorparser.parse();
      } catch (CommandSyntaxException var5) {
      }

      return entityselectorparser.fillSuggestions(suggestionsbuilder, (suggestionsbuilder1) -> SharedSuggestionProvider.suggest(commandcontext.getSource().getOnlinePlayerNames(), suggestionsbuilder1));
   };
   private static final Collection<String> EXAMPLES = Arrays.asList("Player", "0123", "*", "@e");
   private static final SimpleCommandExceptionType ERROR_NO_RESULTS = new SimpleCommandExceptionType(Component.translatable("argument.scoreHolder.empty"));
   final boolean multiple;

   public ScoreHolderArgument(boolean flag) {
      this.multiple = flag;
   }

   public static String getName(CommandContext<CommandSourceStack> commandcontext, String s) throws CommandSyntaxException {
      return getNames(commandcontext, s).iterator().next();
   }

   public static Collection<String> getNames(CommandContext<CommandSourceStack> commandcontext, String s) throws CommandSyntaxException {
      return getNames(commandcontext, s, Collections::emptyList);
   }

   public static Collection<String> getNamesWithDefaultWildcard(CommandContext<CommandSourceStack> commandcontext, String s) throws CommandSyntaxException {
      return getNames(commandcontext, s, commandcontext.getSource().getServer().getScoreboard()::getTrackedPlayers);
   }

   public static Collection<String> getNames(CommandContext<CommandSourceStack> commandcontext, String s, Supplier<Collection<String>> supplier) throws CommandSyntaxException {
      Collection<String> collection = commandcontext.getArgument(s, ScoreHolderArgument.Result.class).getNames(commandcontext.getSource(), supplier);
      if (collection.isEmpty()) {
         throw EntityArgument.NO_ENTITIES_FOUND.create();
      } else {
         return collection;
      }
   }

   public static ScoreHolderArgument scoreHolder() {
      return new ScoreHolderArgument(false);
   }

   public static ScoreHolderArgument scoreHolders() {
      return new ScoreHolderArgument(true);
   }

   public ScoreHolderArgument.Result parse(StringReader stringreader) throws CommandSyntaxException {
      if (stringreader.canRead() && stringreader.peek() == '@') {
         EntitySelectorParser entityselectorparser = new EntitySelectorParser(stringreader);
         EntitySelector entityselector = entityselectorparser.parse();
         if (!this.multiple && entityselector.getMaxResults() > 1) {
            throw EntityArgument.ERROR_NOT_SINGLE_ENTITY.create();
         } else {
            return new ScoreHolderArgument.SelectorResult(entityselector);
         }
      } else {
         int i = stringreader.getCursor();

         while(stringreader.canRead() && stringreader.peek() != ' ') {
            stringreader.skip();
         }

         String s = stringreader.getString().substring(i, stringreader.getCursor());
         if (s.equals("*")) {
            return (commandsourcestack1, supplier1) -> {
               Collection<String> collection2 = supplier1.get();
               if (collection2.isEmpty()) {
                  throw ERROR_NO_RESULTS.create();
               } else {
                  return collection2;
               }
            };
         } else {
            Collection<String> collection = Collections.singleton(s);
            return (commandsourcestack, supplier) -> collection;
         }
      }
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   public static class Info implements ArgumentTypeInfo<ScoreHolderArgument, ScoreHolderArgument.Info.Template> {
      private static final byte FLAG_MULTIPLE = 1;

      public void serializeToNetwork(ScoreHolderArgument.Info.Template scoreholderargument_info_template, FriendlyByteBuf friendlybytebuf) {
         int i = 0;
         if (scoreholderargument_info_template.multiple) {
            i |= 1;
         }

         friendlybytebuf.writeByte(i);
      }

      public ScoreHolderArgument.Info.Template deserializeFromNetwork(FriendlyByteBuf friendlybytebuf) {
         byte b0 = friendlybytebuf.readByte();
         boolean flag = (b0 & 1) != 0;
         return new ScoreHolderArgument.Info.Template(flag);
      }

      public void serializeToJson(ScoreHolderArgument.Info.Template scoreholderargument_info_template, JsonObject jsonobject) {
         jsonobject.addProperty("amount", scoreholderargument_info_template.multiple ? "multiple" : "single");
      }

      public ScoreHolderArgument.Info.Template unpack(ScoreHolderArgument scoreholderargument) {
         return new ScoreHolderArgument.Info.Template(scoreholderargument.multiple);
      }

      public final class Template implements ArgumentTypeInfo.Template<ScoreHolderArgument> {
         final boolean multiple;

         Template(boolean flag) {
            this.multiple = flag;
         }

         public ScoreHolderArgument instantiate(CommandBuildContext commandbuildcontext) {
            return new ScoreHolderArgument(this.multiple);
         }

         public ArgumentTypeInfo<ScoreHolderArgument, ?> type() {
            return Info.this;
         }
      }
   }

   @FunctionalInterface
   public interface Result {
      Collection<String> getNames(CommandSourceStack commandsourcestack, Supplier<Collection<String>> supplier) throws CommandSyntaxException;
   }

   public static class SelectorResult implements ScoreHolderArgument.Result {
      private final EntitySelector selector;

      public SelectorResult(EntitySelector entityselector) {
         this.selector = entityselector;
      }

      public Collection<String> getNames(CommandSourceStack commandsourcestack, Supplier<Collection<String>> supplier) throws CommandSyntaxException {
         List<? extends Entity> list = this.selector.findEntities(commandsourcestack);
         if (list.isEmpty()) {
            throw EntityArgument.NO_ENTITIES_FOUND.create();
         } else {
            List<String> list1 = Lists.newArrayList();

            for(Entity entity : list) {
               list1.add(entity.getScoreboardName());
            }

            return list1;
         }
      }
   }
}
