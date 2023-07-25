package net.minecraft.commands;

import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.mojang.logging.LogUtils;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.gametest.framework.TestCommand;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.commands.AdvancementCommands;
import net.minecraft.server.commands.AttributeCommand;
import net.minecraft.server.commands.BanIpCommands;
import net.minecraft.server.commands.BanListCommands;
import net.minecraft.server.commands.BanPlayerCommands;
import net.minecraft.server.commands.BossBarCommands;
import net.minecraft.server.commands.ClearInventoryCommands;
import net.minecraft.server.commands.CloneCommands;
import net.minecraft.server.commands.DamageCommand;
import net.minecraft.server.commands.DataPackCommand;
import net.minecraft.server.commands.DeOpCommands;
import net.minecraft.server.commands.DebugCommand;
import net.minecraft.server.commands.DefaultGameModeCommands;
import net.minecraft.server.commands.DifficultyCommand;
import net.minecraft.server.commands.EffectCommands;
import net.minecraft.server.commands.EmoteCommands;
import net.minecraft.server.commands.EnchantCommand;
import net.minecraft.server.commands.ExecuteCommand;
import net.minecraft.server.commands.ExperienceCommand;
import net.minecraft.server.commands.FillBiomeCommand;
import net.minecraft.server.commands.FillCommand;
import net.minecraft.server.commands.ForceLoadCommand;
import net.minecraft.server.commands.FunctionCommand;
import net.minecraft.server.commands.GameModeCommand;
import net.minecraft.server.commands.GameRuleCommand;
import net.minecraft.server.commands.GiveCommand;
import net.minecraft.server.commands.HelpCommand;
import net.minecraft.server.commands.ItemCommands;
import net.minecraft.server.commands.JfrCommand;
import net.minecraft.server.commands.KickCommand;
import net.minecraft.server.commands.KillCommand;
import net.minecraft.server.commands.ListPlayersCommand;
import net.minecraft.server.commands.LocateCommand;
import net.minecraft.server.commands.LootCommand;
import net.minecraft.server.commands.MsgCommand;
import net.minecraft.server.commands.OpCommand;
import net.minecraft.server.commands.PardonCommand;
import net.minecraft.server.commands.PardonIpCommand;
import net.minecraft.server.commands.ParticleCommand;
import net.minecraft.server.commands.PerfCommand;
import net.minecraft.server.commands.PlaceCommand;
import net.minecraft.server.commands.PlaySoundCommand;
import net.minecraft.server.commands.PublishCommand;
import net.minecraft.server.commands.RecipeCommand;
import net.minecraft.server.commands.ReloadCommand;
import net.minecraft.server.commands.ReturnCommand;
import net.minecraft.server.commands.RideCommand;
import net.minecraft.server.commands.SaveAllCommand;
import net.minecraft.server.commands.SaveOffCommand;
import net.minecraft.server.commands.SaveOnCommand;
import net.minecraft.server.commands.SayCommand;
import net.minecraft.server.commands.ScheduleCommand;
import net.minecraft.server.commands.ScoreboardCommand;
import net.minecraft.server.commands.SeedCommand;
import net.minecraft.server.commands.SetBlockCommand;
import net.minecraft.server.commands.SetPlayerIdleTimeoutCommand;
import net.minecraft.server.commands.SetSpawnCommand;
import net.minecraft.server.commands.SetWorldSpawnCommand;
import net.minecraft.server.commands.SpawnArmorTrimsCommand;
import net.minecraft.server.commands.SpectateCommand;
import net.minecraft.server.commands.SpreadPlayersCommand;
import net.minecraft.server.commands.StopCommand;
import net.minecraft.server.commands.StopSoundCommand;
import net.minecraft.server.commands.SummonCommand;
import net.minecraft.server.commands.TagCommand;
import net.minecraft.server.commands.TeamCommand;
import net.minecraft.server.commands.TeamMsgCommand;
import net.minecraft.server.commands.TeleportCommand;
import net.minecraft.server.commands.TellRawCommand;
import net.minecraft.server.commands.TimeCommand;
import net.minecraft.server.commands.TitleCommand;
import net.minecraft.server.commands.TriggerCommand;
import net.minecraft.server.commands.WeatherCommand;
import net.minecraft.server.commands.WhitelistCommand;
import net.minecraft.server.commands.WorldBorderCommand;
import net.minecraft.server.commands.data.DataCommands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import org.slf4j.Logger;

public class Commands {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final int LEVEL_ALL = 0;
   public static final int LEVEL_MODERATORS = 1;
   public static final int LEVEL_GAMEMASTERS = 2;
   public static final int LEVEL_ADMINS = 3;
   public static final int LEVEL_OWNERS = 4;
   private final CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();

   public Commands(Commands.CommandSelection commands_commandselection, CommandBuildContext commandbuildcontext) {
      AdvancementCommands.register(this.dispatcher);
      AttributeCommand.register(this.dispatcher, commandbuildcontext);
      ExecuteCommand.register(this.dispatcher, commandbuildcontext);
      BossBarCommands.register(this.dispatcher);
      ClearInventoryCommands.register(this.dispatcher, commandbuildcontext);
      CloneCommands.register(this.dispatcher, commandbuildcontext);
      DamageCommand.register(this.dispatcher, commandbuildcontext);
      DataCommands.register(this.dispatcher);
      DataPackCommand.register(this.dispatcher);
      DebugCommand.register(this.dispatcher);
      DefaultGameModeCommands.register(this.dispatcher);
      DifficultyCommand.register(this.dispatcher);
      EffectCommands.register(this.dispatcher, commandbuildcontext);
      EmoteCommands.register(this.dispatcher);
      EnchantCommand.register(this.dispatcher, commandbuildcontext);
      ExperienceCommand.register(this.dispatcher);
      FillCommand.register(this.dispatcher, commandbuildcontext);
      FillBiomeCommand.register(this.dispatcher, commandbuildcontext);
      ForceLoadCommand.register(this.dispatcher);
      FunctionCommand.register(this.dispatcher);
      GameModeCommand.register(this.dispatcher);
      GameRuleCommand.register(this.dispatcher);
      GiveCommand.register(this.dispatcher, commandbuildcontext);
      HelpCommand.register(this.dispatcher);
      ItemCommands.register(this.dispatcher, commandbuildcontext);
      KickCommand.register(this.dispatcher);
      KillCommand.register(this.dispatcher);
      ListPlayersCommand.register(this.dispatcher);
      LocateCommand.register(this.dispatcher, commandbuildcontext);
      LootCommand.register(this.dispatcher, commandbuildcontext);
      MsgCommand.register(this.dispatcher);
      ParticleCommand.register(this.dispatcher, commandbuildcontext);
      PlaceCommand.register(this.dispatcher);
      PlaySoundCommand.register(this.dispatcher);
      ReloadCommand.register(this.dispatcher);
      RecipeCommand.register(this.dispatcher);
      ReturnCommand.register(this.dispatcher);
      RideCommand.register(this.dispatcher);
      SayCommand.register(this.dispatcher);
      ScheduleCommand.register(this.dispatcher);
      ScoreboardCommand.register(this.dispatcher);
      SeedCommand.register(this.dispatcher, commands_commandselection != Commands.CommandSelection.INTEGRATED);
      SetBlockCommand.register(this.dispatcher, commandbuildcontext);
      SetSpawnCommand.register(this.dispatcher);
      SetWorldSpawnCommand.register(this.dispatcher);
      SpectateCommand.register(this.dispatcher);
      SpreadPlayersCommand.register(this.dispatcher);
      StopSoundCommand.register(this.dispatcher);
      SummonCommand.register(this.dispatcher, commandbuildcontext);
      TagCommand.register(this.dispatcher);
      TeamCommand.register(this.dispatcher);
      TeamMsgCommand.register(this.dispatcher);
      TeleportCommand.register(this.dispatcher);
      TellRawCommand.register(this.dispatcher);
      TimeCommand.register(this.dispatcher);
      TitleCommand.register(this.dispatcher);
      TriggerCommand.register(this.dispatcher);
      WeatherCommand.register(this.dispatcher);
      WorldBorderCommand.register(this.dispatcher);
      if (JvmProfiler.INSTANCE.isAvailable()) {
         JfrCommand.register(this.dispatcher);
      }

      if (SharedConstants.IS_RUNNING_IN_IDE) {
         TestCommand.register(this.dispatcher);
         SpawnArmorTrimsCommand.register(this.dispatcher);
      }

      if (commands_commandselection.includeDedicated) {
         BanIpCommands.register(this.dispatcher);
         BanListCommands.register(this.dispatcher);
         BanPlayerCommands.register(this.dispatcher);
         DeOpCommands.register(this.dispatcher);
         OpCommand.register(this.dispatcher);
         PardonCommand.register(this.dispatcher);
         PardonIpCommand.register(this.dispatcher);
         PerfCommand.register(this.dispatcher);
         SaveAllCommand.register(this.dispatcher);
         SaveOffCommand.register(this.dispatcher);
         SaveOnCommand.register(this.dispatcher);
         SetPlayerIdleTimeoutCommand.register(this.dispatcher);
         StopCommand.register(this.dispatcher);
         WhitelistCommand.register(this.dispatcher);
      }

      if (commands_commandselection.includeIntegrated) {
         PublishCommand.register(this.dispatcher);
      }

      this.dispatcher.setConsumer((commandcontext, flag, i) -> commandcontext.getSource().onCommandComplete(commandcontext, flag, i));
   }

   public static <S> ParseResults<S> mapSource(ParseResults<S> parseresults, UnaryOperator<S> unaryoperator) {
      CommandContextBuilder<S> commandcontextbuilder = parseresults.getContext();
      CommandContextBuilder<S> commandcontextbuilder1 = commandcontextbuilder.withSource(unaryoperator.apply(commandcontextbuilder.getSource()));
      return new ParseResults<>(commandcontextbuilder1, parseresults.getReader(), parseresults.getExceptions());
   }

   public int performPrefixedCommand(CommandSourceStack commandsourcestack, String s) {
      s = s.startsWith("/") ? s.substring(1) : s;
      return this.performCommand(this.dispatcher.parse(s, commandsourcestack), s);
   }

   public int performCommand(ParseResults<CommandSourceStack> parseresults, String s) {
      CommandSourceStack commandsourcestack = parseresults.getContext().getSource();
      commandsourcestack.getServer().getProfiler().push(() -> "/" + s);

      try {
         try {
            return this.dispatcher.execute(parseresults);
         } catch (CommandRuntimeException var13) {
            commandsourcestack.sendFailure(var13.getComponent());
            return 0;
         } catch (CommandSyntaxException var14) {
            commandsourcestack.sendFailure(ComponentUtils.fromMessage(var14.getRawMessage()));
            if (var14.getInput() != null && var14.getCursor() >= 0) {
               int i = Math.min(var14.getInput().length(), var14.getCursor());
               MutableComponent mutablecomponent = Component.empty().withStyle(ChatFormatting.GRAY).withStyle((style1) -> style1.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + s)));
               if (i > 10) {
                  mutablecomponent.append(CommonComponents.ELLIPSIS);
               }

               mutablecomponent.append(var14.getInput().substring(Math.max(0, i - 10), i));
               if (i < var14.getInput().length()) {
                  Component component = Component.literal(var14.getInput().substring(i)).withStyle(ChatFormatting.RED, ChatFormatting.UNDERLINE);
                  mutablecomponent.append(component);
               }

               mutablecomponent.append(Component.translatable("command.context.here").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
               commandsourcestack.sendFailure(mutablecomponent);
            }
         } catch (Exception var15) {
            MutableComponent mutablecomponent1 = Component.literal(var15.getMessage() == null ? var15.getClass().getName() : var15.getMessage());
            if (LOGGER.isDebugEnabled()) {
               LOGGER.error("Command exception: /{}", s, var15);
               StackTraceElement[] astacktraceelement = var15.getStackTrace();

               for(int j = 0; j < Math.min(astacktraceelement.length, 3); ++j) {
                  mutablecomponent1.append("\n\n").append(astacktraceelement[j].getMethodName()).append("\n ").append(astacktraceelement[j].getFileName()).append(":").append(String.valueOf(astacktraceelement[j].getLineNumber()));
               }
            }

            commandsourcestack.sendFailure(Component.translatable("command.failed").withStyle((style) -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, mutablecomponent1))));
            if (SharedConstants.IS_RUNNING_IN_IDE) {
               commandsourcestack.sendFailure(Component.literal(Util.describeError(var15)));
               LOGGER.error("'/{}' threw an exception", s, var15);
            }

            return 0;
         }

         return 0;
      } finally {
         commandsourcestack.getServer().getProfiler().pop();
      }
   }

   public void sendCommands(ServerPlayer serverplayer) {
      Map<CommandNode<CommandSourceStack>, CommandNode<SharedSuggestionProvider>> map = Maps.newHashMap();
      RootCommandNode<SharedSuggestionProvider> rootcommandnode = new RootCommandNode<>();
      map.put(this.dispatcher.getRoot(), rootcommandnode);
      this.fillUsableCommands(this.dispatcher.getRoot(), rootcommandnode, serverplayer.createCommandSourceStack(), map);
      serverplayer.connection.send(new ClientboundCommandsPacket(rootcommandnode));
   }

   private void fillUsableCommands(CommandNode<CommandSourceStack> commandnode, CommandNode<SharedSuggestionProvider> commandnode1, CommandSourceStack commandsourcestack, Map<CommandNode<CommandSourceStack>, CommandNode<SharedSuggestionProvider>> map) {
      for(CommandNode<CommandSourceStack> commandnode2 : commandnode.getChildren()) {
         if (commandnode2.canUse(commandsourcestack)) {
            ArgumentBuilder<SharedSuggestionProvider, ?> argumentbuilder = commandnode2.createBuilder();
            argumentbuilder.requires((sharedsuggestionprovider) -> true);
            if (argumentbuilder.getCommand() != null) {
               argumentbuilder.executes((commandcontext) -> 0);
            }

            if (argumentbuilder instanceof RequiredArgumentBuilder) {
               RequiredArgumentBuilder<SharedSuggestionProvider, ?> requiredargumentbuilder = (RequiredArgumentBuilder)argumentbuilder;
               if (requiredargumentbuilder.getSuggestionsProvider() != null) {
                  requiredargumentbuilder.suggests(SuggestionProviders.safelySwap(requiredargumentbuilder.getSuggestionsProvider()));
               }
            }

            if (argumentbuilder.getRedirect() != null) {
               argumentbuilder.redirect(map.get(argumentbuilder.getRedirect()));
            }

            CommandNode<SharedSuggestionProvider> commandnode3 = argumentbuilder.build();
            map.put(commandnode2, commandnode3);
            commandnode1.addChild(commandnode3);
            if (!commandnode2.getChildren().isEmpty()) {
               this.fillUsableCommands(commandnode2, commandnode3, commandsourcestack, map);
            }
         }
      }

   }

   public static LiteralArgumentBuilder<CommandSourceStack> literal(String s) {
      return LiteralArgumentBuilder.literal(s);
   }

   public static <T> RequiredArgumentBuilder<CommandSourceStack, T> argument(String s, ArgumentType<T> argumenttype) {
      return RequiredArgumentBuilder.argument(s, argumenttype);
   }

   public static Predicate<String> createValidator(Commands.ParseFunction commands_parsefunction) {
      return (s) -> {
         try {
            commands_parsefunction.parse(new StringReader(s));
            return true;
         } catch (CommandSyntaxException var3) {
            return false;
         }
      };
   }

   public CommandDispatcher<CommandSourceStack> getDispatcher() {
      return this.dispatcher;
   }

   @Nullable
   public static <S> CommandSyntaxException getParseException(ParseResults<S> parseresults) {
      if (!parseresults.getReader().canRead()) {
         return null;
      } else if (parseresults.getExceptions().size() == 1) {
         return parseresults.getExceptions().values().iterator().next();
      } else {
         return parseresults.getContext().getRange().isEmpty() ? CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext(parseresults.getReader()) : CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(parseresults.getReader());
      }
   }

   public static CommandBuildContext createValidationContext(final HolderLookup.Provider holderlookup_provider) {
      return new CommandBuildContext() {
         public <T> HolderLookup<T> holderLookup(ResourceKey<? extends Registry<T>> resourcekey) {
            final HolderLookup.RegistryLookup<T> holderlookup_registrylookup = holderlookup_provider.lookupOrThrow(resourcekey);
            return new HolderLookup.Delegate<T>(holderlookup_registrylookup) {
               public Optional<HolderSet.Named<T>> get(TagKey<T> tagkey) {
                  return Optional.of(this.getOrThrow(tagkey));
               }

               public HolderSet.Named<T> getOrThrow(TagKey<T> tagkey) {
                  Optional<HolderSet.Named<T>> optional = holderlookup_registrylookup.get(tagkey);
                  return optional.orElseGet(() -> HolderSet.emptyNamed(holderlookup_registrylookup, tagkey));
               }
            };
         }
      };
   }

   public static void validate() {
      CommandBuildContext commandbuildcontext = createValidationContext(VanillaRegistries.createLookup());
      CommandDispatcher<CommandSourceStack> commanddispatcher = (new Commands(Commands.CommandSelection.ALL, commandbuildcontext)).getDispatcher();
      RootCommandNode<CommandSourceStack> rootcommandnode = commanddispatcher.getRoot();
      commanddispatcher.findAmbiguities((commandnode, commandnode1, commandnode2, collection) -> LOGGER.warn("Ambiguity between arguments {} and {} with inputs: {}", commanddispatcher.getPath(commandnode1), commanddispatcher.getPath(commandnode2), collection));
      Set<ArgumentType<?>> set = ArgumentUtils.findUsedArgumentTypes(rootcommandnode);
      Set<ArgumentType<?>> set1 = set.stream().filter((argumenttype1) -> !ArgumentTypeInfos.isClassRecognized(argumenttype1.getClass())).collect(Collectors.toSet());
      if (!set1.isEmpty()) {
         LOGGER.warn("Missing type registration for following arguments:\n {}", set1.stream().map((argumenttype) -> "\t" + argumenttype).collect(Collectors.joining(",\n")));
         throw new IllegalStateException("Unregistered argument types");
      }
   }

   public static enum CommandSelection {
      ALL(true, true),
      DEDICATED(false, true),
      INTEGRATED(true, false);

      final boolean includeIntegrated;
      final boolean includeDedicated;

      private CommandSelection(boolean flag, boolean flag1) {
         this.includeIntegrated = flag;
         this.includeDedicated = flag1;
      }
   }

   @FunctionalInterface
   public interface ParseFunction {
      void parse(StringReader stringreader) throws CommandSyntaxException;
   }
}
