package net.minecraft.server.network;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.logging.LogUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.RateKickingConnection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.LazyLoadedValue;
import org.slf4j.Logger;

public class ServerConnectionListener {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final LazyLoadedValue<NioEventLoopGroup> SERVER_EVENT_GROUP = new LazyLoadedValue<>(() -> new NioEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Server IO #%d").setDaemon(true).build()));
   public static final LazyLoadedValue<EpollEventLoopGroup> SERVER_EPOLL_EVENT_GROUP = new LazyLoadedValue<>(() -> new EpollEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Epoll Server IO #%d").setDaemon(true).build()));
   final MinecraftServer server;
   public volatile boolean running;
   private final List<ChannelFuture> channels = Collections.synchronizedList(Lists.newArrayList());
   final List<Connection> connections = Collections.synchronizedList(Lists.newArrayList());

   public ServerConnectionListener(MinecraftServer minecraftserver) {
      this.server = minecraftserver;
      this.running = true;
   }

   public void startTcpServerListener(@Nullable InetAddress inetaddress, int i) throws IOException {
      synchronized(this.channels) {
         Class<? extends ServerSocketChannel> oclass;
         LazyLoadedValue<? extends EventLoopGroup> lazyloadedvalue;
         if (Epoll.isAvailable() && this.server.isEpollEnabled()) {
            oclass = EpollServerSocketChannel.class;
            lazyloadedvalue = SERVER_EPOLL_EVENT_GROUP;
            LOGGER.info("Using epoll channel type");
         } else {
            oclass = NioServerSocketChannel.class;
            lazyloadedvalue = SERVER_EVENT_GROUP;
            LOGGER.info("Using default channel type");
         }

         this.channels.add((new ServerBootstrap()).channel(oclass).childHandler(new ChannelInitializer<Channel>() {
            protected void initChannel(Channel channel) {
               try {
                  channel.config().setOption(ChannelOption.TCP_NODELAY, true);
               } catch (ChannelException var5) {
               }

               ChannelPipeline channelpipeline = channel.pipeline().addLast("timeout", new ReadTimeoutHandler(30)).addLast("legacy_query", new LegacyQueryHandler(ServerConnectionListener.this));
               Connection.configureSerialization(channelpipeline, PacketFlow.SERVERBOUND);
               int i = ServerConnectionListener.this.server.getRateLimitPacketsPerSecond();
               Connection connection = (Connection)(i > 0 ? new RateKickingConnection(i) : new Connection(PacketFlow.SERVERBOUND));
               ServerConnectionListener.this.connections.add(connection);
               channelpipeline.addLast("packet_handler", connection);
               connection.setListener(new ServerHandshakePacketListenerImpl(ServerConnectionListener.this.server, connection));
            }
         }).group(lazyloadedvalue.get()).localAddress(inetaddress, i).bind().syncUninterruptibly());
      }
   }

   public SocketAddress startMemoryChannel() {
      ChannelFuture channelfuture;
      synchronized(this.channels) {
         channelfuture = (new ServerBootstrap()).channel(LocalServerChannel.class).childHandler(new ChannelInitializer<Channel>() {
            protected void initChannel(Channel channel) {
               Connection connection = new Connection(PacketFlow.SERVERBOUND);
               connection.setListener(new MemoryServerHandshakePacketListenerImpl(ServerConnectionListener.this.server, connection));
               ServerConnectionListener.this.connections.add(connection);
               ChannelPipeline channelpipeline = channel.pipeline();
               channelpipeline.addLast("packet_handler", connection);
            }
         }).group(SERVER_EVENT_GROUP.get()).localAddress(LocalAddress.ANY).bind().syncUninterruptibly();
         this.channels.add(channelfuture);
      }

      return channelfuture.channel().localAddress();
   }

   public void stop() {
      this.running = false;

      for(ChannelFuture channelfuture : this.channels) {
         try {
            channelfuture.channel().close().sync();
         } catch (InterruptedException var4) {
            LOGGER.error("Interrupted whilst closing channel");
         }
      }

   }

   public void tick() {
      synchronized(this.connections) {
         Iterator<Connection> iterator = this.connections.iterator();

         while(iterator.hasNext()) {
            Connection connection = iterator.next();
            if (!connection.isConnecting()) {
               if (connection.isConnected()) {
                  try {
                     connection.tick();
                  } catch (Exception var7) {
                     if (connection.isMemoryConnection()) {
                        throw new ReportedException(CrashReport.forThrowable(var7, "Ticking memory connection"));
                     }

                     LOGGER.warn("Failed to handle packet for {}", connection.getRemoteAddress(), var7);
                     Component component = Component.literal("Internal server error");
                     connection.send(new ClientboundDisconnectPacket(component), PacketSendListener.thenRun(() -> connection.disconnect(component)));
                     connection.setReadOnly();
                  }
               } else {
                  iterator.remove();
                  connection.handleDisconnection();
               }
            }
         }

      }
   }

   public MinecraftServer getServer() {
      return this.server;
   }

   public List<Connection> getConnections() {
      return this.connections;
   }

   static class LatencySimulator extends ChannelInboundHandlerAdapter {
      private static final Timer TIMER = new HashedWheelTimer();
      private final int delay;
      private final int jitter;
      private final List<ServerConnectionListener.LatencySimulator.DelayedMessage> queuedMessages = Lists.newArrayList();

      public LatencySimulator(int i, int j) {
         this.delay = i;
         this.jitter = j;
      }

      public void channelRead(ChannelHandlerContext channelhandlercontext, Object object) {
         this.delayDownstream(channelhandlercontext, object);
      }

      private void delayDownstream(ChannelHandlerContext channelhandlercontext, Object object) {
         int i = this.delay + (int)(Math.random() * (double)this.jitter);
         this.queuedMessages.add(new ServerConnectionListener.LatencySimulator.DelayedMessage(channelhandlercontext, object));
         TIMER.newTimeout(this::onTimeout, (long)i, TimeUnit.MILLISECONDS);
      }

      private void onTimeout(Timeout timeout) {
         ServerConnectionListener.LatencySimulator.DelayedMessage serverconnectionlistener_latencysimulator_delayedmessage = this.queuedMessages.remove(0);
         serverconnectionlistener_latencysimulator_delayedmessage.ctx.fireChannelRead(serverconnectionlistener_latencysimulator_delayedmessage.msg);
      }

      static class DelayedMessage {
         public final ChannelHandlerContext ctx;
         public final Object msg;

         public DelayedMessage(ChannelHandlerContext channelhandlercontext, Object object) {
            this.ctx = channelhandlercontext;
            this.msg = object;
         }
      }
   }
}
