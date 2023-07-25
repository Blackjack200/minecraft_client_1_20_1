package net.minecraft.network.protocol.game;

import com.google.common.base.MoreObjects;
import com.mojang.authlib.GameProfile;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Optionull;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

public class ClientboundPlayerInfoUpdatePacket implements Packet<ClientGamePacketListener> {
   private final EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions;
   private final List<ClientboundPlayerInfoUpdatePacket.Entry> entries;

   public ClientboundPlayerInfoUpdatePacket(EnumSet<ClientboundPlayerInfoUpdatePacket.Action> enumset, Collection<ServerPlayer> collection) {
      this.actions = enumset;
      this.entries = collection.stream().map(ClientboundPlayerInfoUpdatePacket.Entry::new).toList();
   }

   public ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action clientboundplayerinfoupdatepacket_action, ServerPlayer serverplayer) {
      this.actions = EnumSet.of(clientboundplayerinfoupdatepacket_action);
      this.entries = List.of(new ClientboundPlayerInfoUpdatePacket.Entry(serverplayer));
   }

   public static ClientboundPlayerInfoUpdatePacket createPlayerInitializing(Collection<ServerPlayer> collection) {
      EnumSet<ClientboundPlayerInfoUpdatePacket.Action> enumset = EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, ClientboundPlayerInfoUpdatePacket.Action.INITIALIZE_CHAT, ClientboundPlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE, ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED, ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LATENCY, ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME);
      return new ClientboundPlayerInfoUpdatePacket(enumset, collection);
   }

   public ClientboundPlayerInfoUpdatePacket(FriendlyByteBuf friendlybytebuf) {
      this.actions = friendlybytebuf.readEnumSet(ClientboundPlayerInfoUpdatePacket.Action.class);
      this.entries = friendlybytebuf.readList((friendlybytebuf1) -> {
         ClientboundPlayerInfoUpdatePacket.EntryBuilder clientboundplayerinfoupdatepacket_entrybuilder = new ClientboundPlayerInfoUpdatePacket.EntryBuilder(friendlybytebuf1.readUUID());

         for(ClientboundPlayerInfoUpdatePacket.Action clientboundplayerinfoupdatepacket_action : this.actions) {
            clientboundplayerinfoupdatepacket_action.reader.read(clientboundplayerinfoupdatepacket_entrybuilder, friendlybytebuf1);
         }

         return clientboundplayerinfoupdatepacket_entrybuilder.build();
      });
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeEnumSet(this.actions, ClientboundPlayerInfoUpdatePacket.Action.class);
      friendlybytebuf.writeCollection(this.entries, (friendlybytebuf1, clientboundplayerinfoupdatepacket_entry) -> {
         friendlybytebuf1.writeUUID(clientboundplayerinfoupdatepacket_entry.profileId());

         for(ClientboundPlayerInfoUpdatePacket.Action clientboundplayerinfoupdatepacket_action : this.actions) {
            clientboundplayerinfoupdatepacket_action.writer.write(friendlybytebuf1, clientboundplayerinfoupdatepacket_entry);
         }

      });
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handlePlayerInfoUpdate(this);
   }

   public EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions() {
      return this.actions;
   }

   public List<ClientboundPlayerInfoUpdatePacket.Entry> entries() {
      return this.entries;
   }

   public List<ClientboundPlayerInfoUpdatePacket.Entry> newEntries() {
      return this.actions.contains(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER) ? this.entries : List.of();
   }

   public String toString() {
      return MoreObjects.toStringHelper(this).add("actions", this.actions).add("entries", this.entries).toString();
   }

   public static enum Action {
      ADD_PLAYER((clientboundplayerinfoupdatepacket_entrybuilder, friendlybytebuf) -> {
         GameProfile gameprofile = new GameProfile(clientboundplayerinfoupdatepacket_entrybuilder.profileId, friendlybytebuf.readUtf(16));
         gameprofile.getProperties().putAll(friendlybytebuf.readGameProfileProperties());
         clientboundplayerinfoupdatepacket_entrybuilder.profile = gameprofile;
      }, (friendlybytebuf, clientboundplayerinfoupdatepacket_entry) -> {
         friendlybytebuf.writeUtf(clientboundplayerinfoupdatepacket_entry.profile().getName(), 16);
         friendlybytebuf.writeGameProfileProperties(clientboundplayerinfoupdatepacket_entry.profile().getProperties());
      }),
      INITIALIZE_CHAT((clientboundplayerinfoupdatepacket_entrybuilder, friendlybytebuf) -> clientboundplayerinfoupdatepacket_entrybuilder.chatSession = friendlybytebuf.readNullable(RemoteChatSession.Data::read), (friendlybytebuf, clientboundplayerinfoupdatepacket_entry) -> friendlybytebuf.writeNullable(clientboundplayerinfoupdatepacket_entry.chatSession, RemoteChatSession.Data::write)),
      UPDATE_GAME_MODE((clientboundplayerinfoupdatepacket_entrybuilder, friendlybytebuf) -> clientboundplayerinfoupdatepacket_entrybuilder.gameMode = GameType.byId(friendlybytebuf.readVarInt()), (friendlybytebuf, clientboundplayerinfoupdatepacket_entry) -> friendlybytebuf.writeVarInt(clientboundplayerinfoupdatepacket_entry.gameMode().getId())),
      UPDATE_LISTED((clientboundplayerinfoupdatepacket_entrybuilder, friendlybytebuf) -> clientboundplayerinfoupdatepacket_entrybuilder.listed = friendlybytebuf.readBoolean(), (friendlybytebuf, clientboundplayerinfoupdatepacket_entry) -> friendlybytebuf.writeBoolean(clientboundplayerinfoupdatepacket_entry.listed())),
      UPDATE_LATENCY((clientboundplayerinfoupdatepacket_entrybuilder, friendlybytebuf) -> clientboundplayerinfoupdatepacket_entrybuilder.latency = friendlybytebuf.readVarInt(), (friendlybytebuf, clientboundplayerinfoupdatepacket_entry) -> friendlybytebuf.writeVarInt(clientboundplayerinfoupdatepacket_entry.latency())),
      UPDATE_DISPLAY_NAME((clientboundplayerinfoupdatepacket_entrybuilder, friendlybytebuf) -> clientboundplayerinfoupdatepacket_entrybuilder.displayName = friendlybytebuf.readNullable(FriendlyByteBuf::readComponent), (friendlybytebuf, clientboundplayerinfoupdatepacket_entry) -> friendlybytebuf.writeNullable(clientboundplayerinfoupdatepacket_entry.displayName(), FriendlyByteBuf::writeComponent));

      final ClientboundPlayerInfoUpdatePacket.Action.Reader reader;
      final ClientboundPlayerInfoUpdatePacket.Action.Writer writer;

      private Action(ClientboundPlayerInfoUpdatePacket.Action.Reader clientboundplayerinfoupdatepacket_action_reader, ClientboundPlayerInfoUpdatePacket.Action.Writer clientboundplayerinfoupdatepacket_action_writer) {
         this.reader = clientboundplayerinfoupdatepacket_action_reader;
         this.writer = clientboundplayerinfoupdatepacket_action_writer;
      }

      public interface Reader {
         void read(ClientboundPlayerInfoUpdatePacket.EntryBuilder clientboundplayerinfoupdatepacket_entrybuilder, FriendlyByteBuf friendlybytebuf);
      }

      public interface Writer {
         void write(FriendlyByteBuf friendlybytebuf, ClientboundPlayerInfoUpdatePacket.Entry clientboundplayerinfoupdatepacket_entry);
      }
   }

   public static record Entry(UUID profileId, GameProfile profile, boolean listed, int latency, GameType gameMode, @Nullable Component displayName, @Nullable RemoteChatSession.Data chatSession) {
      @Nullable
      final RemoteChatSession.Data chatSession;

      Entry(ServerPlayer serverplayer) {
         this(serverplayer.getUUID(), serverplayer.getGameProfile(), true, serverplayer.latency, serverplayer.gameMode.getGameModeForPlayer(), serverplayer.getTabListDisplayName(), Optionull.map(serverplayer.getChatSession(), RemoteChatSession::asData));
      }
   }

   static class EntryBuilder {
      final UUID profileId;
      GameProfile profile;
      boolean listed;
      int latency;
      GameType gameMode = GameType.DEFAULT_MODE;
      @Nullable
      Component displayName;
      @Nullable
      RemoteChatSession.Data chatSession;

      EntryBuilder(UUID uuid) {
         this.profileId = uuid;
         this.profile = new GameProfile(uuid, (String)null);
      }

      ClientboundPlayerInfoUpdatePacket.Entry build() {
         return new ClientboundPlayerInfoUpdatePacket.Entry(this.profileId, this.profile, this.listed, this.latency, this.gameMode, this.displayName, this.chatSession);
      }
   }
}
