package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import java.util.List;

public class Varint21FrameDecoder extends ByteToMessageDecoder {
   protected void decode(ChannelHandlerContext channelhandlercontext, ByteBuf bytebuf, List<Object> list) {
      bytebuf.markReaderIndex();
      byte[] abyte = new byte[3];

      for(int i = 0; i < abyte.length; ++i) {
         if (!bytebuf.isReadable()) {
            bytebuf.resetReaderIndex();
            return;
         }

         abyte[i] = bytebuf.readByte();
         if (abyte[i] >= 0) {
            FriendlyByteBuf friendlybytebuf = new FriendlyByteBuf(Unpooled.wrappedBuffer(abyte));

            try {
               int j = friendlybytebuf.readVarInt();
               if (bytebuf.readableBytes() >= j) {
                  list.add(bytebuf.readBytes(j));
                  return;
               }

               bytebuf.resetReaderIndex();
            } finally {
               friendlybytebuf.release();
            }

            return;
         }
      }

      throw new CorruptedFrameException("length wider than 21-bit");
   }
}
