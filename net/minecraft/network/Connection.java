package net.minecraft.network;

import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.logging.LogUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.TimeoutException;
import io.netty.util.AttributeKey;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Queue;
import java.util.concurrent.RejectedExecutionException;
import javax.annotation.Nullable;
import javax.crypto.Cipher;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.BundlerInfo;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.server.RunningOnDifferentThreadException;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class Connection extends SimpleChannelInboundHandler<Packet<?>> {
   private static final float AVERAGE_PACKETS_SMOOTHING = 0.75F;
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final Marker ROOT_MARKER = MarkerFactory.getMarker("NETWORK");
   public static final Marker PACKET_MARKER = Util.make(MarkerFactory.getMarker("NETWORK_PACKETS"), (marker) -> marker.add(ROOT_MARKER));
   public static final Marker PACKET_RECEIVED_MARKER = Util.make(MarkerFactory.getMarker("PACKET_RECEIVED"), (marker) -> marker.add(PACKET_MARKER));
   public static final Marker PACKET_SENT_MARKER = Util.make(MarkerFactory.getMarker("PACKET_SENT"), (marker) -> marker.add(PACKET_MARKER));
   public static final AttributeKey<ConnectionProtocol> ATTRIBUTE_PROTOCOL = AttributeKey.valueOf("protocol");
   public static final LazyLoadedValue<NioEventLoopGroup> NETWORK_WORKER_GROUP = new LazyLoadedValue<>(() -> new NioEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Client IO #%d").setDaemon(true).build()));
   public static final LazyLoadedValue<EpollEventLoopGroup> NETWORK_EPOLL_WORKER_GROUP = new LazyLoadedValue<>(() -> new EpollEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Epoll Client IO #%d").setDaemon(true).build()));
   public static final LazyLoadedValue<DefaultEventLoopGroup> LOCAL_WORKER_GROUP = new LazyLoadedValue<>(() -> new DefaultEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Local Client IO #%d").setDaemon(true).build()));
   private final PacketFlow receiving;
   private final Queue<Connection.PacketHolder> queue = Queues.newConcurrentLinkedQueue();
   private Channel channel;
   private SocketAddress address;
   private PacketListener packetListener;
   private Component disconnectedReason;
   private boolean encrypted;
   private boolean disconnectionHandled;
   private int receivedPackets;
   private int sentPackets;
   private float averageReceivedPackets;
   private float averageSentPackets;
   private int tickCount;
   private boolean handlingFault;
   @Nullable
   private volatile Component delayedDisconnect;

   public Connection(PacketFlow packetflow) {
      this.receiving = packetflow;
   }

   public void channelActive(ChannelHandlerContext channelhandlercontext) throws Exception {
      super.channelActive(channelhandlercontext);
      this.channel = channelhandlercontext.channel();
      this.address = this.channel.remoteAddress();

      try {
         this.setProtocol(ConnectionProtocol.HANDSHAKING);
      } catch (Throwable var3) {
         LOGGER.error(LogUtils.FATAL_MARKER, "Failed to change protocol to handshake", var3);
      }

      if (this.delayedDisconnect != null) {
         this.disconnect(this.delayedDisconnect);
      }

   }

   public void setProtocol(ConnectionProtocol connectionprotocol) {
      this.channel.attr(ATTRIBUTE_PROTOCOL).set(connectionprotocol);
      this.channel.attr(BundlerInfo.BUNDLER_PROVIDER).set(connectionprotocol);
      this.channel.config().setAutoRead(true);
      LOGGER.debug("Enabled auto read");
   }

   public void channelInactive(ChannelHandlerContext channelhandlercontext) {
      this.disconnect(Component.translatable("disconnect.endOfStream"));
   }

   public void exceptionCaught(ChannelHandlerContext channelhandlercontext, Throwable throwable) {
      if (throwable instanceof SkipPacketException) {
         LOGGER.debug("Skipping packet due to errors", throwable.getCause());
      } else {
         boolean flag = !this.handlingFault;
         this.handlingFault = true;
         if (this.channel.isOpen()) {
            if (throwable instanceof TimeoutException) {
               LOGGER.debug("Timeout", throwable);
               this.disconnect(Component.translatable("disconnect.timeout"));
            } else {
               Component component = Component.translatable("disconnect.genericReason", "Internal Exception: " + throwable);
               if (flag) {
                  LOGGER.debug("Failed to sent packet", throwable);
                  ConnectionProtocol connectionprotocol = this.getCurrentProtocol();
                  Packet<?> packet = (Packet<?>)(connectionprotocol == ConnectionProtocol.LOGIN ? new ClientboundLoginDisconnectPacket(component) : new ClientboundDisconnectPacket(component));
                  this.send(packet, PacketSendListener.thenRun(() -> this.disconnect(component)));
                  this.setReadOnly();
               } else {
                  LOGGER.debug("Double fault", throwable);
                  this.disconnect(component);
               }
            }

         }
      }
   }

   protected void channelRead0(ChannelHandlerContext channelhandlercontext, Packet<?> packet) {
      if (this.channel.isOpen()) {
         try {
            genericsFtw(packet, this.packetListener);
         } catch (RunningOnDifferentThreadException var4) {
         } catch (RejectedExecutionException var5) {
            this.disconnect(Component.translatable("multiplayer.disconnect.server_shutdown"));
         } catch (ClassCastException var6) {
            LOGGER.error("Received {} that couldn't be processed", packet.getClass(), var6);
            this.disconnect(Component.translatable("multiplayer.disconnect.invalid_packet"));
         }

         ++this.receivedPackets;
      }

   }

   private static <T extends PacketListener> void genericsFtw(Packet<T> packet, PacketListener packetlistener) {
      packet.handle((T)packetlistener);
   }

   public void setListener(PacketListener packetlistener) {
      Validate.notNull(packetlistener, "packetListener");
      this.packetListener = packetlistener;
   }

   public void send(Packet<?> packet) {
      this.send(packet, (PacketSendListener)null);
   }

   public void send(Packet<?> packet, @Nullable PacketSendListener packetsendlistener) {
      if (this.isConnected()) {
         this.flushQueue();
         this.sendPacket(packet, packetsendlistener);
      } else {
         this.queue.add(new Connection.PacketHolder(packet, packetsendlistener));
      }

   }

   private void sendPacket(Packet<?> packet, @Nullable PacketSendListener packetsendlistener) {
      ConnectionProtocol connectionprotocol = ConnectionProtocol.getProtocolForPacket(packet);
      ConnectionProtocol connectionprotocol1 = this.getCurrentProtocol();
      ++this.sentPackets;
      if (connectionprotocol1 != connectionprotocol) {
         if (connectionprotocol == null) {
            throw new IllegalStateException("Encountered packet without set protocol: " + packet);
         }

         LOGGER.debug("Disabled auto read");
         this.channel.config().setAutoRead(false);
      }

      if (this.channel.eventLoop().inEventLoop()) {
         this.doSendPacket(packet, packetsendlistener, connectionprotocol, connectionprotocol1);
      } else {
         this.channel.eventLoop().execute(() -> this.doSendPacket(packet, packetsendlistener, connectionprotocol, connectionprotocol1));
      }

   }

   private void doSendPacket(Packet<?> packet, @Nullable PacketSendListener packetsendlistener, ConnectionProtocol connectionprotocol, ConnectionProtocol connectionprotocol1) {
      if (connectionprotocol != connectionprotocol1) {
         this.setProtocol(connectionprotocol);
      }

      ChannelFuture channelfuture = this.channel.writeAndFlush(packet);
      if (packetsendlistener != null) {
         channelfuture.addListener((future) -> {
            if (future.isSuccess()) {
               packetsendlistener.onSuccess();
            } else {
               Packet<?> packet1 = packetsendlistener.onFailure();
               if (packet1 != null) {
                  ChannelFuture channelfuture1 = this.channel.writeAndFlush(packet1);
                  channelfuture1.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
               }
            }

         });
      }

      channelfuture.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
   }

   private ConnectionProtocol getCurrentProtocol() {
      return this.channel.attr(ATTRIBUTE_PROTOCOL).get();
   }

   private void flushQueue() {
      if (this.channel != null && this.channel.isOpen()) {
         synchronized(this.queue) {
            Connection.PacketHolder connection_packetholder;
            while((connection_packetholder = this.queue.poll()) != null) {
               this.sendPacket(connection_packetholder.packet, connection_packetholder.listener);
            }

         }
      }
   }

   public void tick() {
      this.flushQueue();
      PacketListener var2 = this.packetListener;
      if (var2 instanceof TickablePacketListener tickablepacketlistener) {
         tickablepacketlistener.tick();
      }

      if (!this.isConnected() && !this.disconnectionHandled) {
         this.handleDisconnection();
      }

      if (this.channel != null) {
         this.channel.flush();
      }

      if (this.tickCount++ % 20 == 0) {
         this.tickSecond();
      }

   }

   protected void tickSecond() {
      this.averageSentPackets = Mth.lerp(0.75F, (float)this.sentPackets, this.averageSentPackets);
      this.averageReceivedPackets = Mth.lerp(0.75F, (float)this.receivedPackets, this.averageReceivedPackets);
      this.sentPackets = 0;
      this.receivedPackets = 0;
   }

   public SocketAddress getRemoteAddress() {
      return this.address;
   }

   public void disconnect(Component component) {
      if (this.channel == null) {
         this.delayedDisconnect = component;
      }

      if (this.isConnected()) {
         this.channel.close().awaitUninterruptibly();
         this.disconnectedReason = component;
      }

   }

   public boolean isMemoryConnection() {
      return this.channel instanceof LocalChannel || this.channel instanceof LocalServerChannel;
   }

   public PacketFlow getReceiving() {
      return this.receiving;
   }

   public PacketFlow getSending() {
      return this.receiving.getOpposite();
   }

   public static Connection connectToServer(InetSocketAddress inetsocketaddress, boolean flag) {
      Connection connection = new Connection(PacketFlow.CLIENTBOUND);
      ChannelFuture channelfuture = connect(inetsocketaddress, flag, connection);
      channelfuture.syncUninterruptibly();
      return connection;
   }

   public static ChannelFuture connect(InetSocketAddress inetsocketaddress, boolean flag, final Connection connection) {
      Class<? extends SocketChannel> oclass;
      LazyLoadedValue<? extends EventLoopGroup> lazyloadedvalue;
      if (Epoll.isAvailable() && flag) {
         oclass = EpollSocketChannel.class;
         lazyloadedvalue = NETWORK_EPOLL_WORKER_GROUP;
      } else {
         oclass = NioSocketChannel.class;
         lazyloadedvalue = NETWORK_WORKER_GROUP;
      }

      return (new Bootstrap()).group(lazyloadedvalue.get()).handler(new ChannelInitializer<Channel>() {
         protected void initChannel(Channel channel) {
            try {
               channel.config().setOption(ChannelOption.TCP_NODELAY, true);
            } catch (ChannelException var3) {
            }

            ChannelPipeline channelpipeline = channel.pipeline().addLast("timeout", new ReadTimeoutHandler(30));
            Connection.configureSerialization(channelpipeline, PacketFlow.CLIENTBOUND);
            channelpipeline.addLast("packet_handler", connection);
         }
      }).channel(oclass).connect(inetsocketaddress.getAddress(), inetsocketaddress.getPort());
   }

   public static void configureSerialization(ChannelPipeline channelpipeline, PacketFlow packetflow) {
      PacketFlow packetflow1 = packetflow.getOpposite();
      channelpipeline.addLast("splitter", new Varint21FrameDecoder()).addLast("decoder", new PacketDecoder(packetflow)).addLast("prepender", new Varint21LengthFieldPrepender()).addLast("encoder", new PacketEncoder(packetflow1)).addLast("unbundler", new PacketBundleUnpacker(packetflow1)).addLast("bundler", new PacketBundlePacker(packetflow));
   }

   public static Connection connectToLocalServer(SocketAddress socketaddress) {
      final Connection connection = new Connection(PacketFlow.CLIENTBOUND);
      (new Bootstrap()).group(LOCAL_WORKER_GROUP.get()).handler(new ChannelInitializer<Channel>() {
         protected void initChannel(Channel channel) {
            ChannelPipeline channelpipeline = channel.pipeline();
            channelpipeline.addLast("packet_handler", connection);
         }
      }).channel(LocalChannel.class).connect(socketaddress).syncUninterruptibly();
      return connection;
   }

   public void setEncryptionKey(Cipher cipher, Cipher cipher1) {
      this.encrypted = true;
      this.channel.pipeline().addBefore("splitter", "decrypt", new CipherDecoder(cipher));
      this.channel.pipeline().addBefore("prepender", "encrypt", new CipherEncoder(cipher1));
   }

   public boolean isEncrypted() {
      return this.encrypted;
   }

   public boolean isConnected() {
      return this.channel != null && this.channel.isOpen();
   }

   public boolean isConnecting() {
      return this.channel == null;
   }

   public PacketListener getPacketListener() {
      return this.packetListener;
   }

   @Nullable
   public Component getDisconnectedReason() {
      return this.disconnectedReason;
   }

   public void setReadOnly() {
      if (this.channel != null) {
         this.channel.config().setAutoRead(false);
      }

   }

   public void setupCompression(int i, boolean flag) {
      if (i >= 0) {
         if (this.channel.pipeline().get("decompress") instanceof CompressionDecoder) {
            ((CompressionDecoder)this.channel.pipeline().get("decompress")).setThreshold(i, flag);
         } else {
            this.channel.pipeline().addBefore("decoder", "decompress", new CompressionDecoder(i, flag));
         }

         if (this.channel.pipeline().get("compress") instanceof CompressionEncoder) {
            ((CompressionEncoder)this.channel.pipeline().get("compress")).setThreshold(i);
         } else {
            this.channel.pipeline().addBefore("encoder", "compress", new CompressionEncoder(i));
         }
      } else {
         if (this.channel.pipeline().get("decompress") instanceof CompressionDecoder) {
            this.channel.pipeline().remove("decompress");
         }

         if (this.channel.pipeline().get("compress") instanceof CompressionEncoder) {
            this.channel.pipeline().remove("compress");
         }
      }

   }

   public void handleDisconnection() {
      if (this.channel != null && !this.channel.isOpen()) {
         if (this.disconnectionHandled) {
            LOGGER.warn("handleDisconnection() called twice");
         } else {
            this.disconnectionHandled = true;
            if (this.getDisconnectedReason() != null) {
               this.getPacketListener().onDisconnect(this.getDisconnectedReason());
            } else if (this.getPacketListener() != null) {
               this.getPacketListener().onDisconnect(Component.translatable("multiplayer.disconnect.generic"));
            }
         }

      }
   }

   public float getAverageReceivedPackets() {
      return this.averageReceivedPackets;
   }

   public float getAverageSentPackets() {
      return this.averageSentPackets;
   }

   static class PacketHolder {
      final Packet<?> packet;
      @Nullable
      final PacketSendListener listener;

      public PacketHolder(Packet<?> packet, @Nullable PacketSendListener packetsendlistener) {
         this.packet = packet;
         this.listener = packetsendlistener;
      }
   }
}
