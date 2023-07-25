package net.minecraft.server.commands;

import com.google.common.collect.Sets;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.Set;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.world.entity.Entity;

public class TagCommand {
   private static final SimpleCommandExceptionType ERROR_ADD_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.tag.add.failed"));
   private static final SimpleCommandExceptionType ERROR_REMOVE_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.tag.remove.failed"));

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      commanddispatcher.register(Commands.literal("tag").requires((commandsourcestack) -> commandsourcestack.hasPermission(2)).then(Commands.argument("targets", EntityArgument.entities()).then(Commands.literal("add").then(Commands.argument("name", StringArgumentType.word()).executes((commandcontext3) -> addTag(commandcontext3.getSource(), EntityArgument.getEntities(commandcontext3, "targets"), StringArgumentType.getString(commandcontext3, "name"))))).then(Commands.literal("remove").then(Commands.argument("name", StringArgumentType.word()).suggests((commandcontext2, suggestionsbuilder) -> SharedSuggestionProvider.suggest(getTags(EntityArgument.getEntities(commandcontext2, "targets")), suggestionsbuilder)).executes((commandcontext1) -> removeTag(commandcontext1.getSource(), EntityArgument.getEntities(commandcontext1, "targets"), StringArgumentType.getString(commandcontext1, "name"))))).then(Commands.literal("list").executes((commandcontext) -> listTags(commandcontext.getSource(), EntityArgument.getEntities(commandcontext, "targets"))))));
   }

   private static Collection<String> getTags(Collection<? extends Entity> collection) {
      Set<String> set = Sets.newHashSet();

      for(Entity entity : collection) {
         set.addAll(entity.getTags());
      }

      return set;
   }

   private static int addTag(CommandSourceStack commandsourcestack, Collection<? extends Entity> collection, String s) throws CommandSyntaxException {
      int i = 0;

      for(Entity entity : collection) {
         if (entity.addTag(s)) {
            ++i;
         }
      }

      if (i == 0) {
         throw ERROR_ADD_FAILED.create();
      } else {
         if (collection.size() == 1) {
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.tag.add.success.single", s, collection.iterator().next().getDisplayName()), true);
         } else {
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.tag.add.success.multiple", s, collection.size()), true);
         }

         return i;
      }
   }

   private static int removeTag(CommandSourceStack commandsourcestack, Collection<? extends Entity> collection, String s) throws CommandSyntaxException {
      int i = 0;

      for(Entity entity : collection) {
         if (entity.removeTag(s)) {
            ++i;
         }
      }

      if (i == 0) {
         throw ERROR_REMOVE_FAILED.create();
      } else {
         if (collection.size() == 1) {
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.tag.remove.success.single", s, collection.iterator().next().getDisplayName()), true);
         } else {
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.tag.remove.success.multiple", s, collection.size()), true);
         }

         return i;
      }
   }

   private static int listTags(CommandSourceStack commandsourcestack, Collection<? extends Entity> collection) {
      Set<String> set = Sets.newHashSet();

      for(Entity entity : collection) {
         set.addAll(entity.getTags());
      }

      if (collection.size() == 1) {
         Entity entity1 = collection.iterator().next();
         if (set.isEmpty()) {
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.tag.list.single.empty", entity1.getDisplayName()), false);
         } else {
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.tag.list.single.success", entity1.getDisplayName(), set.size(), ComponentUtils.formatList(set)), false);
         }
      } else if (set.isEmpty()) {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.tag.list.multiple.empty", collection.size()), false);
      } else {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.tag.list.multiple.success", collection.size(), set.size(), ComponentUtils.formatList(set)), false);
      }

      return set.size();
   }
}
