package net.minecraft.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToMessageEncoder;
import java.util.List;
import net.minecraft.network.protocol.BundlerInfo;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;

public class PacketBundleUnpacker extends MessageToMessageEncoder<Packet<?>> {
   private final PacketFlow flow;

   public PacketBundleUnpacker(PacketFlow packetflow) {
      this.flow = packetflow;
   }

   protected void encode(ChannelHandlerContext channelhandlercontext, Packet<?> packet, List<Object> list) throws Exception {
      BundlerInfo.Provider bundlerinfo_provider = channelhandlercontext.channel().attr(BundlerInfo.BUNDLER_PROVIDER).get();
      if (bundlerinfo_provider == null) {
         throw new EncoderException("Bundler not configured: " + packet);
      } else {
         bundlerinfo_provider.getBundlerInfo(this.flow).unbundlePacket(packet, list::add);
      }
   }
}
