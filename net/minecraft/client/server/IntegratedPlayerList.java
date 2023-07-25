package net.minecraft.client.server;

import com.mojang.authlib.GameProfile;
import java.net.SocketAddress;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.storage.PlayerDataStorage;

public class IntegratedPlayerList extends PlayerList {
   private CompoundTag playerData;

   public IntegratedPlayerList(IntegratedServer integratedserver, LayeredRegistryAccess<RegistryLayer> layeredregistryaccess, PlayerDataStorage playerdatastorage) {
      super(integratedserver, layeredregistryaccess, playerdatastorage, 8);
      this.setViewDistance(10);
   }

   protected void save(ServerPlayer serverplayer) {
      if (this.getServer().isSingleplayerOwner(serverplayer.getGameProfile())) {
         this.playerData = serverplayer.saveWithoutId(new CompoundTag());
      }

      super.save(serverplayer);
   }

   public Component canPlayerLogin(SocketAddress socketaddress, GameProfile gameprofile) {
      return (Component)(this.getServer().isSingleplayerOwner(gameprofile) && this.getPlayerByName(gameprofile.getName()) != null ? Component.translatable("multiplayer.disconnect.name_taken") : super.canPlayerLogin(socketaddress, gameprofile));
   }

   public IntegratedServer getServer() {
      return (IntegratedServer)super.getServer();
   }

   public CompoundTag getSingleplayerData() {
      return this.playerData;
   }
}
