package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.util.zip.Deflater;

public class CompressionEncoder extends MessageToByteEncoder<ByteBuf> {
   private final byte[] encodeBuf = new byte[8192];
   private final Deflater deflater;
   private int threshold;

   public CompressionEncoder(int i) {
      this.threshold = i;
      this.deflater = new Deflater();
   }

   protected void encode(ChannelHandlerContext channelhandlercontext, ByteBuf bytebuf, ByteBuf bytebuf1) {
      int i = bytebuf.readableBytes();
      FriendlyByteBuf friendlybytebuf = new FriendlyByteBuf(bytebuf1);
      if (i < this.threshold) {
         friendlybytebuf.writeVarInt(0);
         friendlybytebuf.writeBytes(bytebuf);
      } else {
         byte[] abyte = new byte[i];
         bytebuf.readBytes(abyte);
         friendlybytebuf.writeVarInt(abyte.length);
         this.deflater.setInput(abyte, 0, i);
         this.deflater.finish();

         while(!this.deflater.finished()) {
            int j = this.deflater.deflate(this.encodeBuf);
            friendlybytebuf.writeBytes(this.encodeBuf, 0, j);
         }

         this.deflater.reset();
      }

   }

   public int getThreshold() {
      return this.threshold;
   }

   public void setThreshold(int i) {
      this.threshold = i;
   }
}
