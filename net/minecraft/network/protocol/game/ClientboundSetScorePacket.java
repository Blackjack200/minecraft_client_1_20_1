package net.minecraft.network.protocol.game;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.ServerScoreboard;

public class ClientboundSetScorePacket implements Packet<ClientGamePacketListener> {
   private final String owner;
   @Nullable
   private final String objectiveName;
   private final int score;
   private final ServerScoreboard.Method method;

   public ClientboundSetScorePacket(ServerScoreboard.Method serverscoreboard_method, @Nullable String s, String s1, int i) {
      if (serverscoreboard_method != ServerScoreboard.Method.REMOVE && s == null) {
         throw new IllegalArgumentException("Need an objective name");
      } else {
         this.owner = s1;
         this.objectiveName = s;
         this.score = i;
         this.method = serverscoreboard_method;
      }
   }

   public ClientboundSetScorePacket(FriendlyByteBuf friendlybytebuf) {
      this.owner = friendlybytebuf.readUtf();
      this.method = friendlybytebuf.readEnum(ServerScoreboard.Method.class);
      String s = friendlybytebuf.readUtf();
      this.objectiveName = Objects.equals(s, "") ? null : s;
      if (this.method != ServerScoreboard.Method.REMOVE) {
         this.score = friendlybytebuf.readVarInt();
      } else {
         this.score = 0;
      }

   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeUtf(this.owner);
      friendlybytebuf.writeEnum(this.method);
      friendlybytebuf.writeUtf(this.objectiveName == null ? "" : this.objectiveName);
      if (this.method != ServerScoreboard.Method.REMOVE) {
         friendlybytebuf.writeVarInt(this.score);
      }

   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleSetScore(this);
   }

   public String getOwner() {
      return this.owner;
   }

   @Nullable
   public String getObjectiveName() {
      return this.objectiveName;
   }

   public int getScore() {
      return this.score;
   }

   public ServerScoreboard.Method getMethod() {
      return this.method;
   }
}
