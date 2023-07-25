package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;

public class DataPackCommand {
   private static final DynamicCommandExceptionType ERROR_UNKNOWN_PACK = new DynamicCommandExceptionType((object) -> Component.translatable("commands.datapack.unknown", object));
   private static final DynamicCommandExceptionType ERROR_PACK_ALREADY_ENABLED = new DynamicCommandExceptionType((object) -> Component.translatable("commands.datapack.enable.failed", object));
   private static final DynamicCommandExceptionType ERROR_PACK_ALREADY_DISABLED = new DynamicCommandExceptionType((object) -> Component.translatable("commands.datapack.disable.failed", object));
   private static final Dynamic2CommandExceptionType ERROR_PACK_FEATURES_NOT_ENABLED = new Dynamic2CommandExceptionType((object, object1) -> Component.translatable("commands.datapack.enable.failed.no_flags", object, object1));
   private static final SuggestionProvider<CommandSourceStack> SELECTED_PACKS = (commandcontext, suggestionsbuilder) -> SharedSuggestionProvider.suggest(commandcontext.getSource().getServer().getPackRepository().getSelectedIds().stream().map(StringArgumentType::escapeIfRequired), suggestionsbuilder);
   private static final SuggestionProvider<CommandSourceStack> UNSELECTED_PACKS = (commandcontext, suggestionsbuilder) -> {
      PackRepository packrepository = commandcontext.getSource().getServer().getPackRepository();
      Collection<String> collection = packrepository.getSelectedIds();
      FeatureFlagSet featureflagset = commandcontext.getSource().enabledFeatures();
      return SharedSuggestionProvider.suggest(packrepository.getAvailablePacks().stream().filter((pack) -> pack.getRequestedFeatures().isSubsetOf(featureflagset)).map(Pack::getId).filter((s) -> !collection.contains(s)).map(StringArgumentType::escapeIfRequired), suggestionsbuilder);
   };

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      commanddispatcher.register(Commands.literal("datapack").requires((commandsourcestack) -> commandsourcestack.hasPermission(2)).then(Commands.literal("enable").then(Commands.argument("name", StringArgumentType.string()).suggests(UNSELECTED_PACKS).executes((commandcontext10) -> enablePack(commandcontext10.getSource(), getPack(commandcontext10, "name", true), (list3, pack3) -> pack3.getDefaultPosition().insert(list3, pack3, (pack4) -> pack4, false))).then(Commands.literal("after").then(Commands.argument("existing", StringArgumentType.string()).suggests(SELECTED_PACKS).executes((commandcontext8) -> enablePack(commandcontext8.getSource(), getPack(commandcontext8, "name", true), (list2, pack2) -> list2.add(list2.indexOf(getPack(commandcontext8, "existing", false)) + 1, pack2))))).then(Commands.literal("before").then(Commands.argument("existing", StringArgumentType.string()).suggests(SELECTED_PACKS).executes((commandcontext6) -> enablePack(commandcontext6.getSource(), getPack(commandcontext6, "name", true), (list1, pack1) -> list1.add(list1.indexOf(getPack(commandcontext6, "existing", false)), pack1))))).then(Commands.literal("last").executes((commandcontext5) -> enablePack(commandcontext5.getSource(), getPack(commandcontext5, "name", true), List::add))).then(Commands.literal("first").executes((commandcontext4) -> enablePack(commandcontext4.getSource(), getPack(commandcontext4, "name", true), (list, pack) -> list.add(0, pack)))))).then(Commands.literal("disable").then(Commands.argument("name", StringArgumentType.string()).suggests(SELECTED_PACKS).executes((commandcontext3) -> disablePack(commandcontext3.getSource(), getPack(commandcontext3, "name", false))))).then(Commands.literal("list").executes((commandcontext2) -> listPacks(commandcontext2.getSource())).then(Commands.literal("available").executes((commandcontext1) -> listAvailablePacks(commandcontext1.getSource()))).then(Commands.literal("enabled").executes((commandcontext) -> listEnabledPacks(commandcontext.getSource())))));
   }

   private static int enablePack(CommandSourceStack commandsourcestack, Pack pack, DataPackCommand.Inserter datapackcommand_inserter) throws CommandSyntaxException {
      PackRepository packrepository = commandsourcestack.getServer().getPackRepository();
      List<Pack> list = Lists.newArrayList(packrepository.getSelectedPacks());
      datapackcommand_inserter.apply(list, pack);
      commandsourcestack.sendSuccess(() -> Component.translatable("commands.datapack.modify.enable", pack.getChatLink(true)), true);
      ReloadCommand.reloadPacks(list.stream().map(Pack::getId).collect(Collectors.toList()), commandsourcestack);
      return list.size();
   }

   private static int disablePack(CommandSourceStack commandsourcestack, Pack pack) {
      PackRepository packrepository = commandsourcestack.getServer().getPackRepository();
      List<Pack> list = Lists.newArrayList(packrepository.getSelectedPacks());
      list.remove(pack);
      commandsourcestack.sendSuccess(() -> Component.translatable("commands.datapack.modify.disable", pack.getChatLink(true)), true);
      ReloadCommand.reloadPacks(list.stream().map(Pack::getId).collect(Collectors.toList()), commandsourcestack);
      return list.size();
   }

   private static int listPacks(CommandSourceStack commandsourcestack) {
      return listEnabledPacks(commandsourcestack) + listAvailablePacks(commandsourcestack);
   }

   private static int listAvailablePacks(CommandSourceStack commandsourcestack) {
      PackRepository packrepository = commandsourcestack.getServer().getPackRepository();
      packrepository.reload();
      Collection<Pack> collection = packrepository.getSelectedPacks();
      Collection<Pack> collection1 = packrepository.getAvailablePacks();
      FeatureFlagSet featureflagset = commandsourcestack.enabledFeatures();
      List<Pack> list = collection1.stream().filter((pack1) -> !collection.contains(pack1) && pack1.getRequestedFeatures().isSubsetOf(featureflagset)).toList();
      if (list.isEmpty()) {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.datapack.list.available.none"), false);
      } else {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.datapack.list.available.success", list.size(), ComponentUtils.formatList(list, (pack) -> pack.getChatLink(false))), false);
      }

      return list.size();
   }

   private static int listEnabledPacks(CommandSourceStack commandsourcestack) {
      PackRepository packrepository = commandsourcestack.getServer().getPackRepository();
      packrepository.reload();
      Collection<? extends Pack> collection = packrepository.getSelectedPacks();
      if (collection.isEmpty()) {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.datapack.list.enabled.none"), false);
      } else {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.datapack.list.enabled.success", collection.size(), ComponentUtils.formatList(collection, (pack) -> pack.getChatLink(true))), false);
      }

      return collection.size();
   }

   private static Pack getPack(CommandContext<CommandSourceStack> commandcontext, String s, boolean flag) throws CommandSyntaxException {
      String s1 = StringArgumentType.getString(commandcontext, s);
      PackRepository packrepository = commandcontext.getSource().getServer().getPackRepository();
      Pack pack = packrepository.getPack(s1);
      if (pack == null) {
         throw ERROR_UNKNOWN_PACK.create(s1);
      } else {
         boolean flag1 = packrepository.getSelectedPacks().contains(pack);
         if (flag && flag1) {
            throw ERROR_PACK_ALREADY_ENABLED.create(s1);
         } else if (!flag && !flag1) {
            throw ERROR_PACK_ALREADY_DISABLED.create(s1);
         } else {
            FeatureFlagSet featureflagset = commandcontext.getSource().enabledFeatures();
            FeatureFlagSet featureflagset1 = pack.getRequestedFeatures();
            if (!featureflagset1.isSubsetOf(featureflagset)) {
               throw ERROR_PACK_FEATURES_NOT_ENABLED.create(s1, FeatureFlags.printMissingFlags(featureflagset, featureflagset1));
            } else {
               return pack;
            }
         }
      }
   }

   interface Inserter {
      void apply(List<Pack> list, Pack pack) throws CommandSyntaxException;
   }
}
