package net.minecraft.server;

import net.minecraft.commands.CommandSourceStack;

public class ConsoleInput {
   public final String msg;
   public final CommandSourceStack source;

   public ConsoleInput(String s, CommandSourceStack commandsourcestack) {
      this.msg = s;
      this.source = commandsourcestack;
   }
}
