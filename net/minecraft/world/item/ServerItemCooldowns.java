package net.minecraft.world.item;

import net.minecraft.network.protocol.game.ClientboundCooldownPacket;
import net.minecraft.server.level.ServerPlayer;

public class ServerItemCooldowns extends ItemCooldowns {
   private final ServerPlayer player;

   public ServerItemCooldowns(ServerPlayer serverplayer) {
      this.player = serverplayer;
   }

   protected void onCooldownStarted(Item item, int i) {
      super.onCooldownStarted(item, i);
      this.player.connection.send(new ClientboundCooldownPacket(item, i));
   }

   protected void onCooldownEnded(Item item) {
      super.onCooldownEnded(item);
      this.player.connection.send(new ClientboundCooldownPacket(item, 0));
   }
}
