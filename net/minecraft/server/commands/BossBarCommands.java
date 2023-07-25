package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.bossevents.CustomBossEvent;
import net.minecraft.server.bossevents.CustomBossEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class BossBarCommands {
   private static final DynamicCommandExceptionType ERROR_ALREADY_EXISTS = new DynamicCommandExceptionType((object) -> Component.translatable("commands.bossbar.create.failed", object));
   private static final DynamicCommandExceptionType ERROR_DOESNT_EXIST = new DynamicCommandExceptionType((object) -> Component.translatable("commands.bossbar.unknown", object));
   private static final SimpleCommandExceptionType ERROR_NO_PLAYER_CHANGE = new SimpleCommandExceptionType(Component.translatable("commands.bossbar.set.players.unchanged"));
   private static final SimpleCommandExceptionType ERROR_NO_NAME_CHANGE = new SimpleCommandExceptionType(Component.translatable("commands.bossbar.set.name.unchanged"));
   private static final SimpleCommandExceptionType ERROR_NO_COLOR_CHANGE = new SimpleCommandExceptionType(Component.translatable("commands.bossbar.set.color.unchanged"));
   private static final SimpleCommandExceptionType ERROR_NO_STYLE_CHANGE = new SimpleCommandExceptionType(Component.translatable("commands.bossbar.set.style.unchanged"));
   private static final SimpleCommandExceptionType ERROR_NO_VALUE_CHANGE = new SimpleCommandExceptionType(Component.translatable("commands.bossbar.set.value.unchanged"));
   private static final SimpleCommandExceptionType ERROR_NO_MAX_CHANGE = new SimpleCommandExceptionType(Component.translatable("commands.bossbar.set.max.unchanged"));
   private static final SimpleCommandExceptionType ERROR_ALREADY_HIDDEN = new SimpleCommandExceptionType(Component.translatable("commands.bossbar.set.visibility.unchanged.hidden"));
   private static final SimpleCommandExceptionType ERROR_ALREADY_VISIBLE = new SimpleCommandExceptionType(Component.translatable("commands.bossbar.set.visibility.unchanged.visible"));
   public static final SuggestionProvider<CommandSourceStack> SUGGEST_BOSS_BAR = (commandcontext, suggestionsbuilder) -> SharedSuggestionProvider.suggestResource(commandcontext.getSource().getServer().getCustomBossEvents().getIds(), suggestionsbuilder);

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      commanddispatcher.register(Commands.literal("bossbar").requires((commandsourcestack) -> commandsourcestack.hasPermission(2)).then(Commands.literal("add").then(Commands.argument("id", ResourceLocationArgument.id()).then(Commands.argument("name", ComponentArgument.textComponent()).executes((commandcontext24) -> createBar(commandcontext24.getSource(), ResourceLocationArgument.getId(commandcontext24, "id"), ComponentArgument.getComponent(commandcontext24, "name")))))).then(Commands.literal("remove").then(Commands.argument("id", ResourceLocationArgument.id()).suggests(SUGGEST_BOSS_BAR).executes((commandcontext23) -> removeBar(commandcontext23.getSource(), getBossBar(commandcontext23))))).then(Commands.literal("list").executes((commandcontext22) -> listBars(commandcontext22.getSource()))).then(Commands.literal("set").then(Commands.argument("id", ResourceLocationArgument.id()).suggests(SUGGEST_BOSS_BAR).then(Commands.literal("name").then(Commands.argument("name", ComponentArgument.textComponent()).executes((commandcontext21) -> setName(commandcontext21.getSource(), getBossBar(commandcontext21), ComponentArgument.getComponent(commandcontext21, "name"))))).then(Commands.literal("color").then(Commands.literal("pink").executes((commandcontext20) -> setColor(commandcontext20.getSource(), getBossBar(commandcontext20), BossEvent.BossBarColor.PINK))).then(Commands.literal("blue").executes((commandcontext19) -> setColor(commandcontext19.getSource(), getBossBar(commandcontext19), BossEvent.BossBarColor.BLUE))).then(Commands.literal("red").executes((commandcontext18) -> setColor(commandcontext18.getSource(), getBossBar(commandcontext18), BossEvent.BossBarColor.RED))).then(Commands.literal("green").executes((commandcontext17) -> setColor(commandcontext17.getSource(), getBossBar(commandcontext17), BossEvent.BossBarColor.GREEN))).then(Commands.literal("yellow").executes((commandcontext16) -> setColor(commandcontext16.getSource(), getBossBar(commandcontext16), BossEvent.BossBarColor.YELLOW))).then(Commands.literal("purple").executes((commandcontext15) -> setColor(commandcontext15.getSource(), getBossBar(commandcontext15), BossEvent.BossBarColor.PURPLE))).then(Commands.literal("white").executes((commandcontext14) -> setColor(commandcontext14.getSource(), getBossBar(commandcontext14), BossEvent.BossBarColor.WHITE)))).then(Commands.literal("style").then(Commands.literal("progress").executes((commandcontext13) -> setStyle(commandcontext13.getSource(), getBossBar(commandcontext13), BossEvent.BossBarOverlay.PROGRESS))).then(Commands.literal("notched_6").executes((commandcontext12) -> setStyle(commandcontext12.getSource(), getBossBar(commandcontext12), BossEvent.BossBarOverlay.NOTCHED_6))).then(Commands.literal("notched_10").executes((commandcontext11) -> setStyle(commandcontext11.getSource(), getBossBar(commandcontext11), BossEvent.BossBarOverlay.NOTCHED_10))).then(Commands.literal("notched_12").executes((commandcontext10) -> setStyle(commandcontext10.getSource(), getBossBar(commandcontext10), BossEvent.BossBarOverlay.NOTCHED_12))).then(Commands.literal("notched_20").executes((commandcontext9) -> setStyle(commandcontext9.getSource(), getBossBar(commandcontext9), BossEvent.BossBarOverlay.NOTCHED_20)))).then(Commands.literal("value").then(Commands.argument("value", IntegerArgumentType.integer(0)).executes((commandcontext8) -> setValue(commandcontext8.getSource(), getBossBar(commandcontext8), IntegerArgumentType.getInteger(commandcontext8, "value"))))).then(Commands.literal("max").then(Commands.argument("max", IntegerArgumentType.integer(1)).executes((commandcontext7) -> setMax(commandcontext7.getSource(), getBossBar(commandcontext7), IntegerArgumentType.getInteger(commandcontext7, "max"))))).then(Commands.literal("visible").then(Commands.argument("visible", BoolArgumentType.bool()).executes((commandcontext6) -> setVisible(commandcontext6.getSource(), getBossBar(commandcontext6), BoolArgumentType.getBool(commandcontext6, "visible"))))).then(Commands.literal("players").executes((commandcontext5) -> setPlayers(commandcontext5.getSource(), getBossBar(commandcontext5), Collections.emptyList())).then(Commands.argument("targets", EntityArgument.players()).executes((commandcontext4) -> setPlayers(commandcontext4.getSource(), getBossBar(commandcontext4), EntityArgument.getOptionalPlayers(commandcontext4, "targets"))))))).then(Commands.literal("get").then(Commands.argument("id", ResourceLocationArgument.id()).suggests(SUGGEST_BOSS_BAR).then(Commands.literal("value").executes((commandcontext3) -> getValue(commandcontext3.getSource(), getBossBar(commandcontext3)))).then(Commands.literal("max").executes((commandcontext2) -> getMax(commandcontext2.getSource(), getBossBar(commandcontext2)))).then(Commands.literal("visible").executes((commandcontext1) -> getVisible(commandcontext1.getSource(), getBossBar(commandcontext1)))).then(Commands.literal("players").executes((commandcontext) -> getPlayers(commandcontext.getSource(), getBossBar(commandcontext)))))));
   }

   private static int getValue(CommandSourceStack commandsourcestack, CustomBossEvent custombossevent) {
      commandsourcestack.sendSuccess(() -> Component.translatable("commands.bossbar.get.value", custombossevent.getDisplayName(), custombossevent.getValue()), true);
      return custombossevent.getValue();
   }

   private static int getMax(CommandSourceStack commandsourcestack, CustomBossEvent custombossevent) {
      commandsourcestack.sendSuccess(() -> Component.translatable("commands.bossbar.get.max", custombossevent.getDisplayName(), custombossevent.getMax()), true);
      return custombossevent.getMax();
   }

   private static int getVisible(CommandSourceStack commandsourcestack, CustomBossEvent custombossevent) {
      if (custombossevent.isVisible()) {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.bossbar.get.visible.visible", custombossevent.getDisplayName()), true);
         return 1;
      } else {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.bossbar.get.visible.hidden", custombossevent.getDisplayName()), true);
         return 0;
      }
   }

   private static int getPlayers(CommandSourceStack commandsourcestack, CustomBossEvent custombossevent) {
      if (custombossevent.getPlayers().isEmpty()) {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.bossbar.get.players.none", custombossevent.getDisplayName()), true);
      } else {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.bossbar.get.players.some", custombossevent.getDisplayName(), custombossevent.getPlayers().size(), ComponentUtils.formatList(custombossevent.getPlayers(), Player::getDisplayName)), true);
      }

      return custombossevent.getPlayers().size();
   }

   private static int setVisible(CommandSourceStack commandsourcestack, CustomBossEvent custombossevent, boolean flag) throws CommandSyntaxException {
      if (custombossevent.isVisible() == flag) {
         if (flag) {
            throw ERROR_ALREADY_VISIBLE.create();
         } else {
            throw ERROR_ALREADY_HIDDEN.create();
         }
      } else {
         custombossevent.setVisible(flag);
         if (flag) {
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.bossbar.set.visible.success.visible", custombossevent.getDisplayName()), true);
         } else {
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.bossbar.set.visible.success.hidden", custombossevent.getDisplayName()), true);
         }

         return 0;
      }
   }

   private static int setValue(CommandSourceStack commandsourcestack, CustomBossEvent custombossevent, int i) throws CommandSyntaxException {
      if (custombossevent.getValue() == i) {
         throw ERROR_NO_VALUE_CHANGE.create();
      } else {
         custombossevent.setValue(i);
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.bossbar.set.value.success", custombossevent.getDisplayName(), i), true);
         return i;
      }
   }

   private static int setMax(CommandSourceStack commandsourcestack, CustomBossEvent custombossevent, int i) throws CommandSyntaxException {
      if (custombossevent.getMax() == i) {
         throw ERROR_NO_MAX_CHANGE.create();
      } else {
         custombossevent.setMax(i);
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.bossbar.set.max.success", custombossevent.getDisplayName(), i), true);
         return i;
      }
   }

   private static int setColor(CommandSourceStack commandsourcestack, CustomBossEvent custombossevent, BossEvent.BossBarColor bossevent_bossbarcolor) throws CommandSyntaxException {
      if (custombossevent.getColor().equals(bossevent_bossbarcolor)) {
         throw ERROR_NO_COLOR_CHANGE.create();
      } else {
         custombossevent.setColor(bossevent_bossbarcolor);
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.bossbar.set.color.success", custombossevent.getDisplayName()), true);
         return 0;
      }
   }

   private static int setStyle(CommandSourceStack commandsourcestack, CustomBossEvent custombossevent, BossEvent.BossBarOverlay bossevent_bossbaroverlay) throws CommandSyntaxException {
      if (custombossevent.getOverlay().equals(bossevent_bossbaroverlay)) {
         throw ERROR_NO_STYLE_CHANGE.create();
      } else {
         custombossevent.setOverlay(bossevent_bossbaroverlay);
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.bossbar.set.style.success", custombossevent.getDisplayName()), true);
         return 0;
      }
   }

   private static int setName(CommandSourceStack commandsourcestack, CustomBossEvent custombossevent, Component component) throws CommandSyntaxException {
      Component component1 = ComponentUtils.updateForEntity(commandsourcestack, component, (Entity)null, 0);
      if (custombossevent.getName().equals(component1)) {
         throw ERROR_NO_NAME_CHANGE.create();
      } else {
         custombossevent.setName(component1);
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.bossbar.set.name.success", custombossevent.getDisplayName()), true);
         return 0;
      }
   }

   private static int setPlayers(CommandSourceStack commandsourcestack, CustomBossEvent custombossevent, Collection<ServerPlayer> collection) throws CommandSyntaxException {
      boolean flag = custombossevent.setPlayers(collection);
      if (!flag) {
         throw ERROR_NO_PLAYER_CHANGE.create();
      } else {
         if (custombossevent.getPlayers().isEmpty()) {
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.bossbar.set.players.success.none", custombossevent.getDisplayName()), true);
         } else {
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.bossbar.set.players.success.some", custombossevent.getDisplayName(), collection.size(), ComponentUtils.formatList(collection, Player::getDisplayName)), true);
         }

         return custombossevent.getPlayers().size();
      }
   }

   private static int listBars(CommandSourceStack commandsourcestack) {
      Collection<CustomBossEvent> collection = commandsourcestack.getServer().getCustomBossEvents().getEvents();
      if (collection.isEmpty()) {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.bossbar.list.bars.none"), false);
      } else {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.bossbar.list.bars.some", collection.size(), ComponentUtils.formatList(collection, CustomBossEvent::getDisplayName)), false);
      }

      return collection.size();
   }

   private static int createBar(CommandSourceStack commandsourcestack, ResourceLocation resourcelocation, Component component) throws CommandSyntaxException {
      CustomBossEvents custombossevents = commandsourcestack.getServer().getCustomBossEvents();
      if (custombossevents.get(resourcelocation) != null) {
         throw ERROR_ALREADY_EXISTS.create(resourcelocation.toString());
      } else {
         CustomBossEvent custombossevent = custombossevents.create(resourcelocation, ComponentUtils.updateForEntity(commandsourcestack, component, (Entity)null, 0));
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.bossbar.create.success", custombossevent.getDisplayName()), true);
         return custombossevents.getEvents().size();
      }
   }

   private static int removeBar(CommandSourceStack commandsourcestack, CustomBossEvent custombossevent) {
      CustomBossEvents custombossevents = commandsourcestack.getServer().getCustomBossEvents();
      custombossevent.removeAllPlayers();
      custombossevents.remove(custombossevent);
      commandsourcestack.sendSuccess(() -> Component.translatable("commands.bossbar.remove.success", custombossevent.getDisplayName()), true);
      return custombossevents.getEvents().size();
   }

   public static CustomBossEvent getBossBar(CommandContext<CommandSourceStack> commandcontext) throws CommandSyntaxException {
      ResourceLocation resourcelocation = ResourceLocationArgument.getId(commandcontext, "id");
      CustomBossEvent custombossevent = commandcontext.getSource().getServer().getCustomBossEvents().get(resourcelocation);
      if (custombossevent == null) {
         throw ERROR_DOESNT_EXIST.create(resourcelocation.toString());
      } else {
         return custombossevent;
      }
   }
}
