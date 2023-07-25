package net.minecraft.network.protocol.game;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundEditBookPacket implements Packet<ServerGamePacketListener> {
   public static final int MAX_BYTES_PER_CHAR = 4;
   private static final int TITLE_MAX_CHARS = 128;
   private static final int PAGE_MAX_CHARS = 8192;
   private static final int MAX_PAGES_COUNT = 200;
   private final int slot;
   private final List<String> pages;
   private final Optional<String> title;

   public ServerboundEditBookPacket(int i, List<String> list, Optional<String> optional) {
      this.slot = i;
      this.pages = ImmutableList.copyOf(list);
      this.title = optional;
   }

   public ServerboundEditBookPacket(FriendlyByteBuf friendlybytebuf) {
      this.slot = friendlybytebuf.readVarInt();
      this.pages = friendlybytebuf.readCollection(FriendlyByteBuf.limitValue(Lists::newArrayListWithCapacity, 200), (friendlybytebuf2) -> friendlybytebuf2.readUtf(8192));
      this.title = friendlybytebuf.readOptional((friendlybytebuf1) -> friendlybytebuf1.readUtf(128));
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeVarInt(this.slot);
      friendlybytebuf.writeCollection(this.pages, (friendlybytebuf2, s1) -> friendlybytebuf2.writeUtf(s1, 8192));
      friendlybytebuf.writeOptional(this.title, (friendlybytebuf1, s) -> friendlybytebuf1.writeUtf(s, 128));
   }

   public void handle(ServerGamePacketListener servergamepacketlistener) {
      servergamepacketlistener.handleEditBook(this);
   }

   public List<String> getPages() {
      return this.pages;
   }

   public Optional<String> getTitle() {
      return this.title;
   }

   public int getSlot() {
      return this.slot;
   }
}
