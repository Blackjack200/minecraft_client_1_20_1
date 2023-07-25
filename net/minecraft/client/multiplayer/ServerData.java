package net.minecraft.client.multiplayer;

import com.mojang.logging.LogUtils;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.status.ServerStatus;
import org.slf4j.Logger;

public class ServerData {
   private static final Logger LOGGER = LogUtils.getLogger();
   public String name;
   public String ip;
   public Component status;
   public Component motd;
   @Nullable
   public ServerStatus.Players players;
   public long ping;
   public int protocol = SharedConstants.getCurrentVersion().getProtocolVersion();
   public Component version = Component.literal(SharedConstants.getCurrentVersion().getName());
   public boolean pinged;
   public List<Component> playerList = Collections.emptyList();
   private ServerData.ServerPackStatus packStatus = ServerData.ServerPackStatus.PROMPT;
   @Nullable
   private byte[] iconBytes;
   private boolean lan;
   private boolean enforcesSecureChat;

   public ServerData(String s, String s1, boolean flag) {
      this.name = s;
      this.ip = s1;
      this.lan = flag;
   }

   public CompoundTag write() {
      CompoundTag compoundtag = new CompoundTag();
      compoundtag.putString("name", this.name);
      compoundtag.putString("ip", this.ip);
      if (this.iconBytes != null) {
         compoundtag.putString("icon", Base64.getEncoder().encodeToString(this.iconBytes));
      }

      if (this.packStatus == ServerData.ServerPackStatus.ENABLED) {
         compoundtag.putBoolean("acceptTextures", true);
      } else if (this.packStatus == ServerData.ServerPackStatus.DISABLED) {
         compoundtag.putBoolean("acceptTextures", false);
      }

      return compoundtag;
   }

   public ServerData.ServerPackStatus getResourcePackStatus() {
      return this.packStatus;
   }

   public void setResourcePackStatus(ServerData.ServerPackStatus serverdata_serverpackstatus) {
      this.packStatus = serverdata_serverpackstatus;
   }

   public static ServerData read(CompoundTag compoundtag) {
      ServerData serverdata = new ServerData(compoundtag.getString("name"), compoundtag.getString("ip"), false);
      if (compoundtag.contains("icon", 8)) {
         try {
            serverdata.setIconBytes(Base64.getDecoder().decode(compoundtag.getString("icon")));
         } catch (IllegalArgumentException var3) {
            LOGGER.warn("Malformed base64 server icon", (Throwable)var3);
         }
      }

      if (compoundtag.contains("acceptTextures", 1)) {
         if (compoundtag.getBoolean("acceptTextures")) {
            serverdata.setResourcePackStatus(ServerData.ServerPackStatus.ENABLED);
         } else {
            serverdata.setResourcePackStatus(ServerData.ServerPackStatus.DISABLED);
         }
      } else {
         serverdata.setResourcePackStatus(ServerData.ServerPackStatus.PROMPT);
      }

      return serverdata;
   }

   @Nullable
   public byte[] getIconBytes() {
      return this.iconBytes;
   }

   public void setIconBytes(@Nullable byte[] abyte) {
      this.iconBytes = abyte;
   }

   public boolean isLan() {
      return this.lan;
   }

   public void setEnforcesSecureChat(boolean flag) {
      this.enforcesSecureChat = flag;
   }

   public boolean enforcesSecureChat() {
      return this.enforcesSecureChat;
   }

   public void copyNameIconFrom(ServerData serverdata) {
      this.ip = serverdata.ip;
      this.name = serverdata.name;
      this.iconBytes = serverdata.iconBytes;
   }

   public void copyFrom(ServerData serverdata) {
      this.copyNameIconFrom(serverdata);
      this.setResourcePackStatus(serverdata.getResourcePackStatus());
      this.lan = serverdata.lan;
      this.enforcesSecureChat = serverdata.enforcesSecureChat;
   }

   public static enum ServerPackStatus {
      ENABLED("enabled"),
      DISABLED("disabled"),
      PROMPT("prompt");

      private final Component name;

      private ServerPackStatus(String s) {
         this.name = Component.translatable("addServer.resourcePack." + s);
      }

      public Component getName() {
         return this.name;
      }
   }
}
