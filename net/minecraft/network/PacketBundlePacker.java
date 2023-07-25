package net.minecraft.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.protocol.BundlerInfo;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;

public class PacketBundlePacker extends MessageToMessageDecoder<Packet<?>> {
   @Nullable
   private BundlerInfo.Bundler currentBundler;
   @Nullable
   private BundlerInfo infoForCurrentBundler;
   private final PacketFlow flow;

   public PacketBundlePacker(PacketFlow packetflow) {
      this.flow = packetflow;
   }

   protected void decode(ChannelHandlerContext channelhandlercontext, Packet<?> packet, List<Object> list) throws Exception {
      BundlerInfo.Provider bundlerinfo_provider = channelhandlercontext.channel().attr(BundlerInfo.BUNDLER_PROVIDER).get();
      if (bundlerinfo_provider == null) {
         throw new DecoderException("Bundler not configured: " + packet);
      } else {
         BundlerInfo bundlerinfo = bundlerinfo_provider.getBundlerInfo(this.flow);
         if (this.currentBundler != null) {
            if (this.infoForCurrentBundler != bundlerinfo) {
               throw new DecoderException("Bundler handler changed during bundling");
            }

            Packet<?> packet1 = this.currentBundler.addPacket(packet);
            if (packet1 != null) {
               this.infoForCurrentBundler = null;
               this.currentBundler = null;
               list.add(packet1);
            }
         } else {
            BundlerInfo.Bundler bundlerinfo_bundler = bundlerinfo.startPacketBundling(packet);
            if (bundlerinfo_bundler != null) {
               this.currentBundler = bundlerinfo_bundler;
               this.infoForCurrentBundler = bundlerinfo;
            } else {
               list.add(packet);
            }
         }

      }
   }
}
