package net.minecraft.client.multiplayer.chat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

public class ChatLog {
   private final LoggedChatEvent[] buffer;
   private int nextId;

   public static Codec<ChatLog> codec(int i) {
      return Codec.list(LoggedChatEvent.CODEC).comapFlatMap((list1) -> {
         int l = list1.size();
         return l > i ? DataResult.error(() -> "Expected: a buffer of size less than or equal to " + i + " but: " + l + " is greater than " + i) : DataResult.success(new ChatLog(i, list1));
      }, ChatLog::loggedChatEvents);
   }

   public ChatLog(int i) {
      this.buffer = new LoggedChatEvent[i];
   }

   private ChatLog(int i, List<LoggedChatEvent> list) {
      this.buffer = list.toArray((k) -> new LoggedChatEvent[i]);
      this.nextId = list.size();
   }

   private List<LoggedChatEvent> loggedChatEvents() {
      List<LoggedChatEvent> list = new ArrayList<>(this.size());

      for(int j = this.start(); j <= this.end(); ++j) {
         list.add(this.lookup(j));
      }

      return list;
   }

   public void push(LoggedChatEvent loggedchatevent) {
      this.buffer[this.index(this.nextId++)] = loggedchatevent;
   }

   @Nullable
   public LoggedChatEvent lookup(int i) {
      return i >= this.start() && i <= this.end() ? this.buffer[this.index(i)] : null;
   }

   private int index(int i) {
      return i % this.buffer.length;
   }

   public int start() {
      return Math.max(this.nextId - this.buffer.length, 0);
   }

   public int end() {
      return this.nextId - 1;
   }

   private int size() {
      return this.end() - this.start() + 1;
   }
}
