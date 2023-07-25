package net.minecraft.commands;

import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.network.chat.PlayerChatMessage;

public interface CommandSigningContext {
   CommandSigningContext ANONYMOUS = new CommandSigningContext() {
      @Nullable
      public PlayerChatMessage getArgument(String s) {
         return null;
      }
   };

   @Nullable
   PlayerChatMessage getArgument(String s);

   public static record SignedArguments(Map<String, PlayerChatMessage> arguments) implements CommandSigningContext {
      @Nullable
      public PlayerChatMessage getArgument(String s) {
         return this.arguments.get(s);
      }
   }
}
