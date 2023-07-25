package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;

public class RecipeCommand {
   private static final SimpleCommandExceptionType ERROR_GIVE_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.recipe.give.failed"));
   private static final SimpleCommandExceptionType ERROR_TAKE_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.recipe.take.failed"));

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      commanddispatcher.register(Commands.literal("recipe").requires((commandsourcestack) -> commandsourcestack.hasPermission(2)).then(Commands.literal("give").then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("recipe", ResourceLocationArgument.id()).suggests(SuggestionProviders.ALL_RECIPES).executes((commandcontext3) -> giveRecipes(commandcontext3.getSource(), EntityArgument.getPlayers(commandcontext3, "targets"), Collections.singleton(ResourceLocationArgument.getRecipe(commandcontext3, "recipe"))))).then(Commands.literal("*").executes((commandcontext2) -> giveRecipes(commandcontext2.getSource(), EntityArgument.getPlayers(commandcontext2, "targets"), commandcontext2.getSource().getServer().getRecipeManager().getRecipes()))))).then(Commands.literal("take").then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("recipe", ResourceLocationArgument.id()).suggests(SuggestionProviders.ALL_RECIPES).executes((commandcontext1) -> takeRecipes(commandcontext1.getSource(), EntityArgument.getPlayers(commandcontext1, "targets"), Collections.singleton(ResourceLocationArgument.getRecipe(commandcontext1, "recipe"))))).then(Commands.literal("*").executes((commandcontext) -> takeRecipes(commandcontext.getSource(), EntityArgument.getPlayers(commandcontext, "targets"), commandcontext.getSource().getServer().getRecipeManager().getRecipes()))))));
   }

   private static int giveRecipes(CommandSourceStack commandsourcestack, Collection<ServerPlayer> collection, Collection<Recipe<?>> collection1) throws CommandSyntaxException {
      int i = 0;

      for(ServerPlayer serverplayer : collection) {
         i += serverplayer.awardRecipes(collection1);
      }

      if (i == 0) {
         throw ERROR_GIVE_FAILED.create();
      } else {
         if (collection.size() == 1) {
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.recipe.give.success.single", collection1.size(), collection.iterator().next().getDisplayName()), true);
         } else {
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.recipe.give.success.multiple", collection1.size(), collection.size()), true);
         }

         return i;
      }
   }

   private static int takeRecipes(CommandSourceStack commandsourcestack, Collection<ServerPlayer> collection, Collection<Recipe<?>> collection1) throws CommandSyntaxException {
      int i = 0;

      for(ServerPlayer serverplayer : collection) {
         i += serverplayer.resetRecipes(collection1);
      }

      if (i == 0) {
         throw ERROR_TAKE_FAILED.create();
      } else {
         if (collection.size() == 1) {
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.recipe.take.success.single", collection1.size(), collection.iterator().next().getDisplayName()), true);
         } else {
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.recipe.take.success.multiple", collection1.size(), collection.size()), true);
         }

         return i;
      }
   }
}
