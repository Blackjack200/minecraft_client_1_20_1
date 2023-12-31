package net.minecraft.commands;

import net.minecraft.network.chat.Component;

public interface CommandSource {
   CommandSource NULL = new CommandSource() {
      public void sendSystemMessage(Component component) {
      }

      public boolean acceptsSuccess() {
         return false;
      }

      public boolean acceptsFailure() {
         return false;
      }

      public boolean shouldInformAdmins() {
         return false;
      }
   };

   void sendSystemMessage(Component component);

   boolean acceptsSuccess();

   boolean acceptsFailure();

   boolean shouldInformAdmins();

   default boolean alwaysAccepts() {
      return false;
   }
}
