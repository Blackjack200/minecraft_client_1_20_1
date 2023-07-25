package net.minecraft.network.protocol;

import io.netty.util.AttributeKey;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.network.PacketListener;

public interface BundlerInfo {
   AttributeKey<BundlerInfo.Provider> BUNDLER_PROVIDER = AttributeKey.valueOf("bundler");
   int BUNDLE_SIZE_LIMIT = 4096;
   BundlerInfo EMPTY = new BundlerInfo() {
      public void unbundlePacket(Packet<?> packet, Consumer<Packet<?>> consumer) {
         consumer.accept(packet);
      }

      @Nullable
      public BundlerInfo.Bundler startPacketBundling(Packet<?> packet) {
         return null;
      }
   };

   static <T extends PacketListener, P extends BundlePacket<T>> BundlerInfo createForPacket(final Class<P> oclass, final Function<Iterable<Packet<T>>, P> function, final BundleDelimiterPacket<T> bundledelimiterpacket) {
      return new BundlerInfo() {
         public void unbundlePacket(Packet<?> packet, Consumer<Packet<?>> consumer) {
            if (packet.getClass() == oclass) {
               P bundlepacket = (P)(packet);
               consumer.accept(bundledelimiterpacket);
               bundlepacket.subPackets().forEach(consumer);
               consumer.accept(bundledelimiterpacket);
            } else {
               consumer.accept(packet);
            }

         }

         @Nullable
         public BundlerInfo.Bundler startPacketBundling(Packet<?> packet) {
            return packet == bundledelimiterpacket ? new BundlerInfo.Bundler() {
               private final List<Packet<T>> bundlePackets = new ArrayList<>();

               @Nullable
               public Packet<?> addPacket(Packet<?> packet) {
                  if (packet == bundledelimiterpacket) {
                     return function.apply(this.bundlePackets);
                  } else if (this.bundlePackets.size() >= 4096) {
                     throw new IllegalStateException("Too many packets in a bundle");
                  } else {
                     this.bundlePackets.add(packet);
                     return null;
                  }
               }
            } : null;
         }
      };
   }

   void unbundlePacket(Packet<?> packet, Consumer<Packet<?>> consumer);

   @Nullable
   BundlerInfo.Bundler startPacketBundling(Packet<?> packet);

   public interface Bundler {
      @Nullable
      Packet<?> addPacket(Packet<?> packet);
   }

   public interface Provider {
      BundlerInfo getBundlerInfo(PacketFlow packetflow);
   }
}
