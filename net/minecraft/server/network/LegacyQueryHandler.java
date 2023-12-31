package net.minecraft.server.network;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

public class LegacyQueryHandler extends ChannelInboundHandlerAdapter {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final int FAKE_PROTOCOL_VERSION = 127;
   private final ServerConnectionListener serverConnectionListener;

   public LegacyQueryHandler(ServerConnectionListener serverconnectionlistener) {
      this.serverConnectionListener = serverconnectionlistener;
   }

   public void channelRead(ChannelHandlerContext channelhandlercontext, Object object) {
      ByteBuf bytebuf = (ByteBuf)object;
      bytebuf.markReaderIndex();
      boolean flag = true;

      try {
         if (bytebuf.readUnsignedByte() == 254) {
            InetSocketAddress inetsocketaddress = (InetSocketAddress)channelhandlercontext.channel().remoteAddress();
            MinecraftServer minecraftserver = this.serverConnectionListener.getServer();
            int i = bytebuf.readableBytes();
            switch (i) {
               case 0:
                  LOGGER.debug("Ping: (<1.3.x) from {}:{}", inetsocketaddress.getAddress(), inetsocketaddress.getPort());
                  String s = String.format(Locale.ROOT, "%s\u00a7%d\u00a7%d", minecraftserver.getMotd(), minecraftserver.getPlayerCount(), minecraftserver.getMaxPlayers());
                  this.sendFlushAndClose(channelhandlercontext, this.createReply(s));
                  break;
               case 1:
                  if (bytebuf.readUnsignedByte() != 1) {
                     return;
                  }

                  LOGGER.debug("Ping: (1.4-1.5.x) from {}:{}", inetsocketaddress.getAddress(), inetsocketaddress.getPort());
                  String s1 = String.format(Locale.ROOT, "\u00a71\u0000%d\u0000%s\u0000%s\u0000%d\u0000%d", 127, minecraftserver.getServerVersion(), minecraftserver.getMotd(), minecraftserver.getPlayerCount(), minecraftserver.getMaxPlayers());
                  this.sendFlushAndClose(channelhandlercontext, this.createReply(s1));
                  break;
               default:
                  boolean flag1 = bytebuf.readUnsignedByte() == 1;
                  flag1 &= bytebuf.readUnsignedByte() == 250;
                  flag1 &= "MC|PingHost".equals(new String(bytebuf.readBytes(bytebuf.readShort() * 2).array(), StandardCharsets.UTF_16BE));
                  int j = bytebuf.readUnsignedShort();
                  flag1 &= bytebuf.readUnsignedByte() >= 73;
                  flag1 &= 3 + bytebuf.readBytes(bytebuf.readShort() * 2).array().length + 4 == j;
                  flag1 &= bytebuf.readInt() <= 65535;
                  flag1 &= bytebuf.readableBytes() == 0;
                  if (!flag1) {
                     return;
                  }

                  LOGGER.debug("Ping: (1.6) from {}:{}", inetsocketaddress.getAddress(), inetsocketaddress.getPort());
                  String s2 = String.format(Locale.ROOT, "\u00a71\u0000%d\u0000%s\u0000%s\u0000%d\u0000%d", 127, minecraftserver.getServerVersion(), minecraftserver.getMotd(), minecraftserver.getPlayerCount(), minecraftserver.getMaxPlayers());
                  ByteBuf bytebuf1 = this.createReply(s2);

                  try {
                     this.sendFlushAndClose(channelhandlercontext, bytebuf1);
                  } finally {
                     bytebuf1.release();
                  }
            }

            bytebuf.release();
            flag = false;
            return;
         }
      } catch (RuntimeException var21) {
         return;
      } finally {
         if (flag) {
            bytebuf.resetReaderIndex();
            channelhandlercontext.channel().pipeline().remove("legacy_query");
            channelhandlercontext.fireChannelRead(object);
         }

      }

   }

   private void sendFlushAndClose(ChannelHandlerContext channelhandlercontext, ByteBuf bytebuf) {
      channelhandlercontext.pipeline().firstContext().writeAndFlush(bytebuf).addListener(ChannelFutureListener.CLOSE);
   }

   private ByteBuf createReply(String s) {
      ByteBuf bytebuf = Unpooled.buffer();
      bytebuf.writeByte(255);
      char[] achar = s.toCharArray();
      bytebuf.writeShort(achar.length);

      for(char c0 : achar) {
         bytebuf.writeChar(c0);
      }

      return bytebuf;
   }
}
