package net.minecraft.network.chat;

import net.minecraft.server.level.ServerPlayer;

public interface OutgoingChatMessage {
   Component content();

   void sendToPlayer(ServerPlayer serverplayer, boolean flag, ChatType.Bound chattype_bound);

   static OutgoingChatMessage create(PlayerChatMessage playerchatmessage) {
      return (OutgoingChatMessage)(playerchatmessage.isSystem() ? new OutgoingChatMessage.Disguised(playerchatmessage.decoratedContent()) : new OutgoingChatMessage.Player(playerchatmessage));
   }

   public static record Disguised(Component content) implements OutgoingChatMessage {
      public void sendToPlayer(ServerPlayer serverplayer, boolean flag, ChatType.Bound chattype_bound) {
         serverplayer.connection.sendDisguisedChatMessage(this.content, chattype_bound);
      }
   }

   public static record Player(PlayerChatMessage message) implements OutgoingChatMessage {
      public Component content() {
         return this.message.decoratedContent();
      }

      public void sendToPlayer(ServerPlayer serverplayer, boolean flag, ChatType.Bound chattype_bound) {
         PlayerChatMessage playerchatmessage = this.message.filter(flag);
         if (!playerchatmessage.isFullyFiltered()) {
            serverplayer.connection.sendPlayerChatMessage(playerchatmessage, chattype_bound);
         }

      }
   }
}
