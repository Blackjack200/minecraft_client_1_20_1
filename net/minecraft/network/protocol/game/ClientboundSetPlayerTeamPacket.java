package net.minecraft.network.protocol.game;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.scores.PlayerTeam;

public class ClientboundSetPlayerTeamPacket implements Packet<ClientGamePacketListener> {
   private static final int METHOD_ADD = 0;
   private static final int METHOD_REMOVE = 1;
   private static final int METHOD_CHANGE = 2;
   private static final int METHOD_JOIN = 3;
   private static final int METHOD_LEAVE = 4;
   private static final int MAX_VISIBILITY_LENGTH = 40;
   private static final int MAX_COLLISION_LENGTH = 40;
   private final int method;
   private final String name;
   private final Collection<String> players;
   private final Optional<ClientboundSetPlayerTeamPacket.Parameters> parameters;

   private ClientboundSetPlayerTeamPacket(String s, int i, Optional<ClientboundSetPlayerTeamPacket.Parameters> optional, Collection<String> collection) {
      this.name = s;
      this.method = i;
      this.parameters = optional;
      this.players = ImmutableList.copyOf(collection);
   }

   public static ClientboundSetPlayerTeamPacket createAddOrModifyPacket(PlayerTeam playerteam, boolean flag) {
      return new ClientboundSetPlayerTeamPacket(playerteam.getName(), flag ? 0 : 2, Optional.of(new ClientboundSetPlayerTeamPacket.Parameters(playerteam)), (Collection<String>)(flag ? playerteam.getPlayers() : ImmutableList.of()));
   }

   public static ClientboundSetPlayerTeamPacket createRemovePacket(PlayerTeam playerteam) {
      return new ClientboundSetPlayerTeamPacket(playerteam.getName(), 1, Optional.empty(), ImmutableList.of());
   }

   public static ClientboundSetPlayerTeamPacket createPlayerPacket(PlayerTeam playerteam, String s, ClientboundSetPlayerTeamPacket.Action clientboundsetplayerteampacket_action) {
      return new ClientboundSetPlayerTeamPacket(playerteam.getName(), clientboundsetplayerteampacket_action == ClientboundSetPlayerTeamPacket.Action.ADD ? 3 : 4, Optional.empty(), ImmutableList.of(s));
   }

   public ClientboundSetPlayerTeamPacket(FriendlyByteBuf friendlybytebuf) {
      this.name = friendlybytebuf.readUtf();
      this.method = friendlybytebuf.readByte();
      if (shouldHaveParameters(this.method)) {
         this.parameters = Optional.of(new ClientboundSetPlayerTeamPacket.Parameters(friendlybytebuf));
      } else {
         this.parameters = Optional.empty();
      }

      if (shouldHavePlayerList(this.method)) {
         this.players = friendlybytebuf.readList(FriendlyByteBuf::readUtf);
      } else {
         this.players = ImmutableList.of();
      }

   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeUtf(this.name);
      friendlybytebuf.writeByte(this.method);
      if (shouldHaveParameters(this.method)) {
         this.parameters.orElseThrow(() -> new IllegalStateException("Parameters not present, but method is" + this.method)).write(friendlybytebuf);
      }

      if (shouldHavePlayerList(this.method)) {
         friendlybytebuf.writeCollection(this.players, FriendlyByteBuf::writeUtf);
      }

   }

   private static boolean shouldHavePlayerList(int i) {
      return i == 0 || i == 3 || i == 4;
   }

   private static boolean shouldHaveParameters(int i) {
      return i == 0 || i == 2;
   }

   @Nullable
   public ClientboundSetPlayerTeamPacket.Action getPlayerAction() {
      switch (this.method) {
         case 0:
         case 3:
            return ClientboundSetPlayerTeamPacket.Action.ADD;
         case 1:
         case 2:
         default:
            return null;
         case 4:
            return ClientboundSetPlayerTeamPacket.Action.REMOVE;
      }
   }

   @Nullable
   public ClientboundSetPlayerTeamPacket.Action getTeamAction() {
      switch (this.method) {
         case 0:
            return ClientboundSetPlayerTeamPacket.Action.ADD;
         case 1:
            return ClientboundSetPlayerTeamPacket.Action.REMOVE;
         default:
            return null;
      }
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleSetPlayerTeamPacket(this);
   }

   public String getName() {
      return this.name;
   }

   public Collection<String> getPlayers() {
      return this.players;
   }

   public Optional<ClientboundSetPlayerTeamPacket.Parameters> getParameters() {
      return this.parameters;
   }

   public static enum Action {
      ADD,
      REMOVE;
   }

   public static class Parameters {
      private final Component displayName;
      private final Component playerPrefix;
      private final Component playerSuffix;
      private final String nametagVisibility;
      private final String collisionRule;
      private final ChatFormatting color;
      private final int options;

      public Parameters(PlayerTeam playerteam) {
         this.displayName = playerteam.getDisplayName();
         this.options = playerteam.packOptions();
         this.nametagVisibility = playerteam.getNameTagVisibility().name;
         this.collisionRule = playerteam.getCollisionRule().name;
         this.color = playerteam.getColor();
         this.playerPrefix = playerteam.getPlayerPrefix();
         this.playerSuffix = playerteam.getPlayerSuffix();
      }

      public Parameters(FriendlyByteBuf friendlybytebuf) {
         this.displayName = friendlybytebuf.readComponent();
         this.options = friendlybytebuf.readByte();
         this.nametagVisibility = friendlybytebuf.readUtf(40);
         this.collisionRule = friendlybytebuf.readUtf(40);
         this.color = friendlybytebuf.readEnum(ChatFormatting.class);
         this.playerPrefix = friendlybytebuf.readComponent();
         this.playerSuffix = friendlybytebuf.readComponent();
      }

      public Component getDisplayName() {
         return this.displayName;
      }

      public int getOptions() {
         return this.options;
      }

      public ChatFormatting getColor() {
         return this.color;
      }

      public String getNametagVisibility() {
         return this.nametagVisibility;
      }

      public String getCollisionRule() {
         return this.collisionRule;
      }

      public Component getPlayerPrefix() {
         return this.playerPrefix;
      }

      public Component getPlayerSuffix() {
         return this.playerSuffix;
      }

      public void write(FriendlyByteBuf friendlybytebuf) {
         friendlybytebuf.writeComponent(this.displayName);
         friendlybytebuf.writeByte(this.options);
         friendlybytebuf.writeUtf(this.nametagVisibility);
         friendlybytebuf.writeUtf(this.collisionRule);
         friendlybytebuf.writeEnum(this.color);
         friendlybytebuf.writeComponent(this.playerPrefix);
         friendlybytebuf.writeComponent(this.playerSuffix);
      }
   }
}
