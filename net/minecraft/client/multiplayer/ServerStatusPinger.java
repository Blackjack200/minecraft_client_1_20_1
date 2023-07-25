package net.minecraft.client.multiplayer;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.resolver.ResolvedServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerNameResolver;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.status.ClientStatusPacketListener;
import net.minecraft.network.protocol.status.ClientboundPongResponsePacket;
import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.network.protocol.status.ServerboundPingRequestPacket;
import net.minecraft.network.protocol.status.ServerboundStatusRequestPacket;
import net.minecraft.util.Mth;
import org.slf4j.Logger;

public class ServerStatusPinger {
   static final Splitter SPLITTER = Splitter.on('\u0000').limit(6);
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Component CANT_CONNECT_MESSAGE = Component.translatable("multiplayer.status.cannot_connect").withStyle((style) -> style.withColor(-65536));
   private final List<Connection> connections = Collections.synchronizedList(Lists.newArrayList());

   public void pingServer(final ServerData serverdata, final Runnable runnable) throws UnknownHostException {
      ServerAddress serveraddress = ServerAddress.parseString(serverdata.ip);
      Optional<InetSocketAddress> optional = ServerNameResolver.DEFAULT.resolveAddress(serveraddress).map(ResolvedServerAddress::asInetSocketAddress);
      if (!optional.isPresent()) {
         this.onPingFailed(ConnectScreen.UNKNOWN_HOST_MESSAGE, serverdata);
      } else {
         final InetSocketAddress inetsocketaddress = optional.get();
         final Connection connection = Connection.connectToServer(inetsocketaddress, false);
         this.connections.add(connection);
         serverdata.motd = Component.translatable("multiplayer.status.pinging");
         serverdata.ping = -1L;
         serverdata.playerList = Collections.emptyList();
         connection.setListener(new ClientStatusPacketListener() {
            private boolean success;
            private boolean receivedPing;
            private long pingStart;

            public void handleStatusResponse(ClientboundStatusResponsePacket clientboundstatusresponsepacket) {
               if (this.receivedPing) {
                  connection.disconnect(Component.translatable("multiplayer.status.unrequested"));
               } else {
                  this.receivedPing = true;
                  ServerStatus serverstatus = clientboundstatusresponsepacket.status();
                  serverdata.motd = serverstatus.description();
                  serverstatus.version().ifPresentOrElse((serverstatus_version) -> {
                     serverdata.version = Component.literal(serverstatus_version.name());
                     serverdata.protocol = serverstatus_version.protocol();
                  }, () -> {
                     serverdata.version = Component.translatable("multiplayer.status.old");
                     serverdata.protocol = 0;
                  });
                  serverstatus.players().ifPresentOrElse((serverstatus_players) -> {
                     serverdata.status = ServerStatusPinger.formatPlayerCount(serverstatus_players.online(), serverstatus_players.max());
                     serverdata.players = serverstatus_players;
                     if (!serverstatus_players.sample().isEmpty()) {
                        List<Component> list = new ArrayList<>(serverstatus_players.sample().size());

                        for(GameProfile gameprofile : serverstatus_players.sample()) {
                           list.add(Component.literal(gameprofile.getName()));
                        }

                        if (serverstatus_players.sample().size() < serverstatus_players.online()) {
                           list.add(Component.translatable("multiplayer.status.and_more", serverstatus_players.online() - serverstatus_players.sample().size()));
                        }

                        serverdata.playerList = list;
                     } else {
                        serverdata.playerList = List.of();
                     }

                  }, () -> serverdata.status = Component.translatable("multiplayer.status.unknown").withStyle(ChatFormatting.DARK_GRAY));
                  serverstatus.favicon().ifPresent((serverstatus_favicon) -> {
                     if (!Arrays.equals(serverstatus_favicon.iconBytes(), serverdata.getIconBytes())) {
                        serverdata.setIconBytes(serverstatus_favicon.iconBytes());
                        runnable.run();
                     }

                  });
                  this.pingStart = Util.getMillis();
                  connection.send(new ServerboundPingRequestPacket(this.pingStart));
                  this.success = true;
               }
            }

            public void handlePongResponse(ClientboundPongResponsePacket clientboundpongresponsepacket) {
               long i = this.pingStart;
               long j = Util.getMillis();
               serverdata.ping = j - i;
               connection.disconnect(Component.translatable("multiplayer.status.finished"));
            }

            public void onDisconnect(Component component) {
               if (!this.success) {
                  ServerStatusPinger.this.onPingFailed(component, serverdata);
                  ServerStatusPinger.this.pingLegacyServer(inetsocketaddress, serverdata);
               }

            }

            public boolean isAcceptingMessages() {
               return connection.isConnected();
            }
         });

         try {
            connection.send(new ClientIntentionPacket(serveraddress.getHost(), serveraddress.getPort(), ConnectionProtocol.STATUS));
            connection.send(new ServerboundStatusRequestPacket());
         } catch (Throwable var8) {
            LOGGER.error("Failed to ping server {}", serveraddress, var8);
         }

      }
   }

   void onPingFailed(Component component, ServerData serverdata) {
      LOGGER.error("Can't ping {}: {}", serverdata.ip, component.getString());
      serverdata.motd = CANT_CONNECT_MESSAGE;
      serverdata.status = CommonComponents.EMPTY;
   }

   void pingLegacyServer(final InetSocketAddress inetsocketaddress, final ServerData serverdata) {
      (new Bootstrap()).group(Connection.NETWORK_WORKER_GROUP.get()).handler(new ChannelInitializer<Channel>() {
         protected void initChannel(Channel channel) {
            try {
               channel.config().setOption(ChannelOption.TCP_NODELAY, true);
            } catch (ChannelException var3) {
            }

            channel.pipeline().addLast(new SimpleChannelInboundHandler<ByteBuf>() {
               public void channelActive(ChannelHandlerContext channelhandlercontext) throws Exception {
                  super.channelActive(channelhandlercontext);
                  ByteBuf bytebuf = Unpooled.buffer();

                  try {
                     bytebuf.writeByte(254);
                     bytebuf.writeByte(1);
                     bytebuf.writeByte(250);
                     char[] achar = "MC|PingHost".toCharArray();
                     bytebuf.writeShort(achar.length);

                     for(char c0 : achar) {
                        bytebuf.writeChar(c0);
                     }

                     bytebuf.writeShort(7 + 2 * inetsocketaddress.getHostName().length());
                     bytebuf.writeByte(127);
                     achar = inetsocketaddress.getHostName().toCharArray();
                     bytebuf.writeShort(achar.length);

                     for(char c1 : achar) {
                        bytebuf.writeChar(c1);
                     }

                     bytebuf.writeInt(inetsocketaddress.getPort());
                     channelhandlercontext.channel().writeAndFlush(bytebuf).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                  } finally {
                     bytebuf.release();
                  }

               }

               protected void channelRead0(ChannelHandlerContext channelhandlercontext, ByteBuf bytebuf) {
                  short short0 = bytebuf.readUnsignedByte();
                  if (short0 == 255) {
                     String s = new String(bytebuf.readBytes(bytebuf.readShort() * 2).array(), StandardCharsets.UTF_16BE);
                     String[] astring = Iterables.toArray(ServerStatusPinger.SPLITTER.split(s), String.class);
                     if ("\u00a71".equals(astring[0])) {
                        int i = Mth.getInt(astring[1], 0);
                        String s1 = astring[2];
                        String s2 = astring[3];
                        int j = Mth.getInt(astring[4], -1);
                        int k = Mth.getInt(astring[5], -1);
                        serverdata.protocol = -1;
                        serverdata.version = Component.literal(s1);
                        serverdata.motd = Component.literal(s2);
                        serverdata.status = ServerStatusPinger.formatPlayerCount(j, k);
                        serverdata.players = new ServerStatus.Players(k, j, List.of());
                     }
                  }

                  channelhandlercontext.close();
               }

               public void exceptionCaught(ChannelHandlerContext channelhandlercontext, Throwable throwable) {
                  channelhandlercontext.close();
               }
            });
         }
      }).channel(NioSocketChannel.class).connect(inetsocketaddress.getAddress(), inetsocketaddress.getPort());
   }

   static Component formatPlayerCount(int i, int j) {
      return Component.literal(Integer.toString(i)).append(Component.literal("/").withStyle(ChatFormatting.DARK_GRAY)).append(Integer.toString(j)).withStyle(ChatFormatting.GRAY);
   }

   public void tick() {
      synchronized(this.connections) {
         Iterator<Connection> iterator = this.connections.iterator();

         while(iterator.hasNext()) {
            Connection connection = iterator.next();
            if (connection.isConnected()) {
               connection.tick();
            } else {
               iterator.remove();
               connection.handleDisconnection();
            }
         }

      }
   }

   public void removeAll() {
      synchronized(this.connections) {
         Iterator<Connection> iterator = this.connections.iterator();

         while(iterator.hasNext()) {
            Connection connection = iterator.next();
            if (connection.isConnected()) {
               iterator.remove();
               connection.disconnect(Component.translatable("multiplayer.status.cancelled"));
            }
         }

      }
   }
}
