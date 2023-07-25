package net.minecraft.commands.arguments;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class GameProfileArgument implements ArgumentType<GameProfileArgument.Result> {
   private static final Collection<String> EXAMPLES = Arrays.asList("Player", "0123", "dd12be42-52a9-4a91-a8a1-11c01849e498", "@e");
   public static final SimpleCommandExceptionType ERROR_UNKNOWN_PLAYER = new SimpleCommandExceptionType(Component.translatable("argument.player.unknown"));

   public static Collection<GameProfile> getGameProfiles(CommandContext<CommandSourceStack> commandcontext, String s) throws CommandSyntaxException {
      return commandcontext.getArgument(s, GameProfileArgument.Result.class).getNames(commandcontext.getSource());
   }

   public static GameProfileArgument gameProfile() {
      return new GameProfileArgument();
   }

   public GameProfileArgument.Result parse(StringReader stringreader) throws CommandSyntaxException {
      if (stringreader.canRead() && stringreader.peek() == '@') {
         EntitySelectorParser entityselectorparser = new EntitySelectorParser(stringreader);
         EntitySelector entityselector = entityselectorparser.parse();
         if (entityselector.includesEntities()) {
            throw EntityArgument.ERROR_ONLY_PLAYERS_ALLOWED.create();
         } else {
            return new GameProfileArgument.SelectorResult(entityselector);
         }
      } else {
         int i = stringreader.getCursor();

         while(stringreader.canRead() && stringreader.peek() != ' ') {
            stringreader.skip();
         }

         String s = stringreader.getString().substring(i, stringreader.getCursor());
         return (commandsourcestack) -> {
            Optional<GameProfile> optional = commandsourcestack.getServer().getProfileCache().get(s);
            return Collections.singleton(optional.orElseThrow(ERROR_UNKNOWN_PLAYER::create));
         };
      }
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandcontext, SuggestionsBuilder suggestionsbuilder) {
      if (commandcontext.getSource() instanceof SharedSuggestionProvider) {
         StringReader stringreader = new StringReader(suggestionsbuilder.getInput());
         stringreader.setCursor(suggestionsbuilder.getStart());
         EntitySelectorParser entityselectorparser = new EntitySelectorParser(stringreader);

         try {
            entityselectorparser.parse();
         } catch (CommandSyntaxException var6) {
         }

         return entityselectorparser.fillSuggestions(suggestionsbuilder, (suggestionsbuilder1) -> SharedSuggestionProvider.suggest(((SharedSuggestionProvider)commandcontext.getSource()).getOnlinePlayerNames(), suggestionsbuilder1));
      } else {
         return Suggestions.empty();
      }
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   @FunctionalInterface
   public interface Result {
      Collection<GameProfile> getNames(CommandSourceStack commandsourcestack) throws CommandSyntaxException;
   }

   public static class SelectorResult implements GameProfileArgument.Result {
      private final EntitySelector selector;

      public SelectorResult(EntitySelector entityselector) {
         this.selector = entityselector;
      }

      public Collection<GameProfile> getNames(CommandSourceStack commandsourcestack) throws CommandSyntaxException {
         List<ServerPlayer> list = this.selector.findPlayers(commandsourcestack);
         if (list.isEmpty()) {
            throw EntityArgument.NO_PLAYERS_FOUND.create();
         } else {
            List<GameProfile> list1 = Lists.newArrayList();

            for(ServerPlayer serverplayer : list) {
               list1.add(serverplayer.getGameProfile());
            }

            return list1;
         }
      }
   }
}
