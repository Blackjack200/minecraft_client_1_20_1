package net.minecraft.client.multiplayer.chat;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.util.StringRepresentable;

public interface LoggedChatEvent {
   Codec<LoggedChatEvent> CODEC = StringRepresentable.fromEnum(LoggedChatEvent.Type::values).dispatch(LoggedChatEvent::type, LoggedChatEvent.Type::codec);

   LoggedChatEvent.Type type();

   public static enum Type implements StringRepresentable {
      PLAYER("player", () -> LoggedChatMessage.Player.CODEC),
      SYSTEM("system", () -> LoggedChatMessage.System.CODEC);

      private final String serializedName;
      private final Supplier<Codec<? extends LoggedChatEvent>> codec;

      private Type(String s, Supplier<Codec<? extends LoggedChatEvent>> supplier) {
         this.serializedName = s;
         this.codec = supplier;
      }

      private Codec<? extends LoggedChatEvent> codec() {
         return this.codec.get();
      }

      public String getSerializedName() {
         return this.serializedName;
      }
   }
}
