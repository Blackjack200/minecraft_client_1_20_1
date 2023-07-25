package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collection;
import java.util.List;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class AdvancementCommands {
   private static final SuggestionProvider<CommandSourceStack> SUGGEST_ADVANCEMENTS = (commandcontext, suggestionsbuilder) -> {
      Collection<Advancement> collection = commandcontext.getSource().getServer().getAdvancements().getAllAdvancements();
      return SharedSuggestionProvider.suggestResource(collection.stream().map(Advancement::getId), suggestionsbuilder);
   };

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      commanddispatcher.register(Commands.literal("advancement").requires((commandsourcestack) -> commandsourcestack.hasPermission(2)).then(Commands.literal("grant").then(Commands.argument("targets", EntityArgument.players()).then(Commands.literal("only").then(Commands.argument("advancement", ResourceLocationArgument.id()).suggests(SUGGEST_ADVANCEMENTS).executes((commandcontext13) -> perform(commandcontext13.getSource(), EntityArgument.getPlayers(commandcontext13, "targets"), AdvancementCommands.Action.GRANT, getAdvancements(ResourceLocationArgument.getAdvancement(commandcontext13, "advancement"), AdvancementCommands.Mode.ONLY))).then(Commands.argument("criterion", StringArgumentType.greedyString()).suggests((commandcontext12, suggestionsbuilder1) -> SharedSuggestionProvider.suggest(ResourceLocationArgument.getAdvancement(commandcontext12, "advancement").getCriteria().keySet(), suggestionsbuilder1)).executes((commandcontext11) -> performCriterion(commandcontext11.getSource(), EntityArgument.getPlayers(commandcontext11, "targets"), AdvancementCommands.Action.GRANT, ResourceLocationArgument.getAdvancement(commandcontext11, "advancement"), StringArgumentType.getString(commandcontext11, "criterion")))))).then(Commands.literal("from").then(Commands.argument("advancement", ResourceLocationArgument.id()).suggests(SUGGEST_ADVANCEMENTS).executes((commandcontext10) -> perform(commandcontext10.getSource(), EntityArgument.getPlayers(commandcontext10, "targets"), AdvancementCommands.Action.GRANT, getAdvancements(ResourceLocationArgument.getAdvancement(commandcontext10, "advancement"), AdvancementCommands.Mode.FROM))))).then(Commands.literal("until").then(Commands.argument("advancement", ResourceLocationArgument.id()).suggests(SUGGEST_ADVANCEMENTS).executes((commandcontext9) -> perform(commandcontext9.getSource(), EntityArgument.getPlayers(commandcontext9, "targets"), AdvancementCommands.Action.GRANT, getAdvancements(ResourceLocationArgument.getAdvancement(commandcontext9, "advancement"), AdvancementCommands.Mode.UNTIL))))).then(Commands.literal("through").then(Commands.argument("advancement", ResourceLocationArgument.id()).suggests(SUGGEST_ADVANCEMENTS).executes((commandcontext8) -> perform(commandcontext8.getSource(), EntityArgument.getPlayers(commandcontext8, "targets"), AdvancementCommands.Action.GRANT, getAdvancements(ResourceLocationArgument.getAdvancement(commandcontext8, "advancement"), AdvancementCommands.Mode.THROUGH))))).then(Commands.literal("everything").executes((commandcontext7) -> perform(commandcontext7.getSource(), EntityArgument.getPlayers(commandcontext7, "targets"), AdvancementCommands.Action.GRANT, commandcontext7.getSource().getServer().getAdvancements().getAllAdvancements()))))).then(Commands.literal("revoke").then(Commands.argument("targets", EntityArgument.players()).then(Commands.literal("only").then(Commands.argument("advancement", ResourceLocationArgument.id()).suggests(SUGGEST_ADVANCEMENTS).executes((commandcontext6) -> perform(commandcontext6.getSource(), EntityArgument.getPlayers(commandcontext6, "targets"), AdvancementCommands.Action.REVOKE, getAdvancements(ResourceLocationArgument.getAdvancement(commandcontext6, "advancement"), AdvancementCommands.Mode.ONLY))).then(Commands.argument("criterion", StringArgumentType.greedyString()).suggests((commandcontext5, suggestionsbuilder) -> SharedSuggestionProvider.suggest(ResourceLocationArgument.getAdvancement(commandcontext5, "advancement").getCriteria().keySet(), suggestionsbuilder)).executes((commandcontext4) -> performCriterion(commandcontext4.getSource(), EntityArgument.getPlayers(commandcontext4, "targets"), AdvancementCommands.Action.REVOKE, ResourceLocationArgument.getAdvancement(commandcontext4, "advancement"), StringArgumentType.getString(commandcontext4, "criterion")))))).then(Commands.literal("from").then(Commands.argument("advancement", ResourceLocationArgument.id()).suggests(SUGGEST_ADVANCEMENTS).executes((commandcontext3) -> perform(commandcontext3.getSource(), EntityArgument.getPlayers(commandcontext3, "targets"), AdvancementCommands.Action.REVOKE, getAdvancements(ResourceLocationArgument.getAdvancement(commandcontext3, "advancement"), AdvancementCommands.Mode.FROM))))).then(Commands.literal("until").then(Commands.argument("advancement", ResourceLocationArgument.id()).suggests(SUGGEST_ADVANCEMENTS).executes((commandcontext2) -> perform(commandcontext2.getSource(), EntityArgument.getPlayers(commandcontext2, "targets"), AdvancementCommands.Action.REVOKE, getAdvancements(ResourceLocationArgument.getAdvancement(commandcontext2, "advancement"), AdvancementCommands.Mode.UNTIL))))).then(Commands.literal("through").then(Commands.argument("advancement", ResourceLocationArgument.id()).suggests(SUGGEST_ADVANCEMENTS).executes((commandcontext1) -> perform(commandcontext1.getSource(), EntityArgument.getPlayers(commandcontext1, "targets"), AdvancementCommands.Action.REVOKE, getAdvancements(ResourceLocationArgument.getAdvancement(commandcontext1, "advancement"), AdvancementCommands.Mode.THROUGH))))).then(Commands.literal("everything").executes((commandcontext) -> perform(commandcontext.getSource(), EntityArgument.getPlayers(commandcontext, "targets"), AdvancementCommands.Action.REVOKE, commandcontext.getSource().getServer().getAdvancements().getAllAdvancements()))))));
   }

   private static int perform(CommandSourceStack commandsourcestack, Collection<ServerPlayer> collection, AdvancementCommands.Action advancementcommands_action, Collection<Advancement> collection1) {
      int i = 0;

      for(ServerPlayer serverplayer : collection) {
         i += advancementcommands_action.perform(serverplayer, collection1);
      }

      if (i == 0) {
         if (collection1.size() == 1) {
            if (collection.size() == 1) {
               throw new CommandRuntimeException(Component.translatable(advancementcommands_action.getKey() + ".one.to.one.failure", collection1.iterator().next().getChatComponent(), collection.iterator().next().getDisplayName()));
            } else {
               throw new CommandRuntimeException(Component.translatable(advancementcommands_action.getKey() + ".one.to.many.failure", collection1.iterator().next().getChatComponent(), collection.size()));
            }
         } else if (collection.size() == 1) {
            throw new CommandRuntimeException(Component.translatable(advancementcommands_action.getKey() + ".many.to.one.failure", collection1.size(), collection.iterator().next().getDisplayName()));
         } else {
            throw new CommandRuntimeException(Component.translatable(advancementcommands_action.getKey() + ".many.to.many.failure", collection1.size(), collection.size()));
         }
      } else {
         if (collection1.size() == 1) {
            if (collection.size() == 1) {
               commandsourcestack.sendSuccess(() -> Component.translatable(advancementcommands_action.getKey() + ".one.to.one.success", collection1.iterator().next().getChatComponent(), collection.iterator().next().getDisplayName()), true);
            } else {
               commandsourcestack.sendSuccess(() -> Component.translatable(advancementcommands_action.getKey() + ".one.to.many.success", collection1.iterator().next().getChatComponent(), collection.size()), true);
            }
         } else if (collection.size() == 1) {
            commandsourcestack.sendSuccess(() -> Component.translatable(advancementcommands_action.getKey() + ".many.to.one.success", collection1.size(), collection.iterator().next().getDisplayName()), true);
         } else {
            commandsourcestack.sendSuccess(() -> Component.translatable(advancementcommands_action.getKey() + ".many.to.many.success", collection1.size(), collection.size()), true);
         }

         return i;
      }
   }

   private static int performCriterion(CommandSourceStack commandsourcestack, Collection<ServerPlayer> collection, AdvancementCommands.Action advancementcommands_action, Advancement advancement, String s) {
      int i = 0;
      if (!advancement.getCriteria().containsKey(s)) {
         throw new CommandRuntimeException(Component.translatable("commands.advancement.criterionNotFound", advancement.getChatComponent(), s));
      } else {
         for(ServerPlayer serverplayer : collection) {
            if (advancementcommands_action.performCriterion(serverplayer, advancement, s)) {
               ++i;
            }
         }

         if (i == 0) {
            if (collection.size() == 1) {
               throw new CommandRuntimeException(Component.translatable(advancementcommands_action.getKey() + ".criterion.to.one.failure", s, advancement.getChatComponent(), collection.iterator().next().getDisplayName()));
            } else {
               throw new CommandRuntimeException(Component.translatable(advancementcommands_action.getKey() + ".criterion.to.many.failure", s, advancement.getChatComponent(), collection.size()));
            }
         } else {
            if (collection.size() == 1) {
               commandsourcestack.sendSuccess(() -> Component.translatable(advancementcommands_action.getKey() + ".criterion.to.one.success", s, advancement.getChatComponent(), collection.iterator().next().getDisplayName()), true);
            } else {
               commandsourcestack.sendSuccess(() -> Component.translatable(advancementcommands_action.getKey() + ".criterion.to.many.success", s, advancement.getChatComponent(), collection.size()), true);
            }

            return i;
         }
      }
   }

   private static List<Advancement> getAdvancements(Advancement advancement, AdvancementCommands.Mode advancementcommands_mode) {
      List<Advancement> list = Lists.newArrayList();
      if (advancementcommands_mode.parents) {
         for(Advancement advancement1 = advancement.getParent(); advancement1 != null; advancement1 = advancement1.getParent()) {
            list.add(advancement1);
         }
      }

      list.add(advancement);
      if (advancementcommands_mode.children) {
         addChildren(advancement, list);
      }

      return list;
   }

   private static void addChildren(Advancement advancement, List<Advancement> list) {
      for(Advancement advancement1 : advancement.getChildren()) {
         list.add(advancement1);
         addChildren(advancement1, list);
      }

   }

   static enum Action {
      GRANT("grant") {
         protected boolean perform(ServerPlayer serverplayer, Advancement advancement) {
            AdvancementProgress advancementprogress = serverplayer.getAdvancements().getOrStartProgress(advancement);
            if (advancementprogress.isDone()) {
               return false;
            } else {
               for(String s : advancementprogress.getRemainingCriteria()) {
                  serverplayer.getAdvancements().award(advancement, s);
               }

               return true;
            }
         }

         protected boolean performCriterion(ServerPlayer serverplayer, Advancement advancement, String s) {
            return serverplayer.getAdvancements().award(advancement, s);
         }
      },
      REVOKE("revoke") {
         protected boolean perform(ServerPlayer serverplayer, Advancement advancement) {
            AdvancementProgress advancementprogress = serverplayer.getAdvancements().getOrStartProgress(advancement);
            if (!advancementprogress.hasProgress()) {
               return false;
            } else {
               for(String s : advancementprogress.getCompletedCriteria()) {
                  serverplayer.getAdvancements().revoke(advancement, s);
               }

               return true;
            }
         }

         protected boolean performCriterion(ServerPlayer serverplayer, Advancement advancement, String s) {
            return serverplayer.getAdvancements().revoke(advancement, s);
         }
      };

      private final String key;

      Action(String s) {
         this.key = "commands.advancement." + s;
      }

      public int perform(ServerPlayer serverplayer, Iterable<Advancement> iterable) {
         int i = 0;

         for(Advancement advancement : iterable) {
            if (this.perform(serverplayer, advancement)) {
               ++i;
            }
         }

         return i;
      }

      protected abstract boolean perform(ServerPlayer serverplayer, Advancement advancement);

      protected abstract boolean performCriterion(ServerPlayer serverplayer, Advancement advancement, String s);

      protected String getKey() {
         return this.key;
      }
   }

   static enum Mode {
      ONLY(false, false),
      THROUGH(true, true),
      FROM(false, true),
      UNTIL(true, false),
      EVERYTHING(true, true);

      final boolean parents;
      final boolean children;

      private Mode(boolean flag, boolean flag1) {
         this.parents = flag;
         this.children = flag1;
      }
   }
}
