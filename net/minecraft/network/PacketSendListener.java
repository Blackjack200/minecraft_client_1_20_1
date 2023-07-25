package net.minecraft.network;

import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.network.protocol.Packet;

public interface PacketSendListener {
   static PacketSendListener thenRun(final Runnable runnable) {
      return new PacketSendListener() {
         public void onSuccess() {
            runnable.run();
         }

         @Nullable
         public Packet<?> onFailure() {
            runnable.run();
            return null;
         }
      };
   }

   static PacketSendListener exceptionallySend(final Supplier<Packet<?>> supplier) {
      return new PacketSendListener() {
         @Nullable
         public Packet<?> onFailure() {
            return supplier.get();
         }
      };
   }

   default void onSuccess() {
   }

   @Nullable
   default Packet<?> onFailure() {
      return null;
   }
}
