package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.MessageToByteEncoder;

@Sharable
public class Varint21LengthFieldPrepender extends MessageToByteEncoder<ByteBuf> {
   private static final int MAX_BYTES = 3;

   protected void encode(ChannelHandlerContext channelhandlercontext, ByteBuf bytebuf, ByteBuf bytebuf1) {
      int i = bytebuf.readableBytes();
      int j = FriendlyByteBuf.getVarIntSize(i);
      if (j > 3) {
         throw new IllegalArgumentException("unable to fit " + i + " into 3");
      } else {
         FriendlyByteBuf friendlybytebuf = new FriendlyByteBuf(bytebuf1);
         friendlybytebuf.ensureWritable(j + i);
         friendlybytebuf.writeVarInt(i);
         friendlybytebuf.writeBytes(bytebuf, bytebuf.readerIndex(), i);
      }
   }
}
