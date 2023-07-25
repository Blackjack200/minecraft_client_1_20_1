package net.minecraft.network.protocol.game;

import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.protocol.Packet;

public class ClientboundCommandSuggestionsPacket implements Packet<ClientGamePacketListener> {
   private final int id;
   private final Suggestions suggestions;

   public ClientboundCommandSuggestionsPacket(int i, Suggestions suggestions) {
      this.id = i;
      this.suggestions = suggestions;
   }

   public ClientboundCommandSuggestionsPacket(FriendlyByteBuf friendlybytebuf) {
      this.id = friendlybytebuf.readVarInt();
      int i = friendlybytebuf.readVarInt();
      int j = friendlybytebuf.readVarInt();
      StringRange stringrange = StringRange.between(i, i + j);
      List<Suggestion> list = friendlybytebuf.readList((friendlybytebuf1) -> {
         String s = friendlybytebuf1.readUtf();
         Component component = friendlybytebuf1.readNullable(FriendlyByteBuf::readComponent);
         return new Suggestion(stringrange, s, component);
      });
      this.suggestions = new Suggestions(stringrange, list);
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeVarInt(this.id);
      friendlybytebuf.writeVarInt(this.suggestions.getRange().getStart());
      friendlybytebuf.writeVarInt(this.suggestions.getRange().getLength());
      friendlybytebuf.writeCollection(this.suggestions.getList(), (friendlybytebuf1, suggestion) -> {
         friendlybytebuf1.writeUtf(suggestion.getText());
         friendlybytebuf1.writeNullable(suggestion.getTooltip(), (friendlybytebuf2, message) -> friendlybytebuf2.writeComponent(ComponentUtils.fromMessage(message)));
      });
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleCommandSuggestions(this);
   }

   public int getId() {
      return this.id;
   }

   public Suggestions getSuggestions() {
      return this.suggestions;
   }
}
