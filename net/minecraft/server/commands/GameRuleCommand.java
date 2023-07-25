package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameRules;

public class GameRuleCommand {
   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      final LiteralArgumentBuilder<CommandSourceStack> literalargumentbuilder = Commands.literal("gamerule").requires((commandsourcestack) -> commandsourcestack.hasPermission(2));
      GameRules.visitGameRuleTypes(new GameRules.GameRuleTypeVisitor() {
         public <T extends GameRules.Value<T>> void visit(GameRules.Key<T> gamerules_key, GameRules.Type<T> gamerules_type) {
            literalargumentbuilder.then(Commands.literal(gamerules_key.getId()).executes((commandcontext1) -> GameRuleCommand.queryRule(commandcontext1.getSource(), gamerules_key)).then(gamerules_type.createArgument("value").executes((commandcontext) -> GameRuleCommand.setRule(commandcontext, gamerules_key))));
         }
      });
      commanddispatcher.register(literalargumentbuilder);
   }

   static <T extends GameRules.Value<T>> int setRule(CommandContext<CommandSourceStack> commandcontext, GameRules.Key<T> gamerules_key) {
      CommandSourceStack commandsourcestack = commandcontext.getSource();
      T gamerules_value = commandsourcestack.getServer().getGameRules().getRule(gamerules_key);
      gamerules_value.setFromArgument(commandcontext, "value");
      commandsourcestack.sendSuccess(() -> Component.translatable("commands.gamerule.set", gamerules_key.getId(), gamerules_value.toString()), true);
      return gamerules_value.getCommandResult();
   }

   static <T extends GameRules.Value<T>> int queryRule(CommandSourceStack commandsourcestack, GameRules.Key<T> gamerules_key) {
      T gamerules_value = commandsourcestack.getServer().getGameRules().getRule(gamerules_key);
      commandsourcestack.sendSuccess(() -> Component.translatable("commands.gamerule.query", gamerules_key.getId(), gamerules_value.toString()), false);
      return gamerules_value.getCommandResult();
   }
}
