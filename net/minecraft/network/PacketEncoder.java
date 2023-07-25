package net.minecraft.network;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.io.IOException;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import org.slf4j.Logger;

public class PacketEncoder extends MessageToByteEncoder<Packet<?>> {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final PacketFlow flow;

   public PacketEncoder(PacketFlow packetflow) {
      this.flow = packetflow;
   }

   protected void encode(ChannelHandlerContext channelhandlercontext, Packet<?> packet, ByteBuf bytebuf) throws Exception {
      ConnectionProtocol connectionprotocol = channelhandlercontext.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get();
      if (connectionprotocol == null) {
         throw new RuntimeException("ConnectionProtocol unknown: " + packet);
      } else {
         int i = connectionprotocol.getPacketId(this.flow, packet);
         if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(Connection.PACKET_SENT_MARKER, "OUT: [{}:{}] {}", channelhandlercontext.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get(), i, packet.getClass().getName());
         }

         if (i == -1) {
            throw new IOException("Can't serialize unregistered packet");
         } else {
            FriendlyByteBuf friendlybytebuf = new FriendlyByteBuf(bytebuf);
            friendlybytebuf.writeVarInt(i);

            try {
               int j = friendlybytebuf.writerIndex();
               packet.write(friendlybytebuf);
               int k = friendlybytebuf.writerIndex() - j;
               if (k > 8388608) {
                  throw new IllegalArgumentException("Packet too big (is " + k + ", should be less than 8388608): " + packet);
               } else {
                  int l = channelhandlercontext.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get().getId();
                  JvmProfiler.INSTANCE.onPacketSent(l, i, channelhandlercontext.channel().remoteAddress(), k);
               }
            } catch (Throwable var10) {
               LOGGER.error("Error receiving packet {}", i, var10);
               if (packet.isSkippable()) {
                  throw new SkipPacketException(var10);
               } else {
                  throw var10;
               }
            }
         }
      }
   }
}
