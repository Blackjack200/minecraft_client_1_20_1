package net.minecraft.network.protocol;

import com.mojang.logging.LogUtils;
import net.minecraft.network.PacketListener;
import net.minecraft.server.RunningOnDifferentThreadException;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.thread.BlockableEventLoop;
import org.slf4j.Logger;

public class PacketUtils {
   private static final Logger LOGGER = LogUtils.getLogger();

   public static <T extends PacketListener> void ensureRunningOnSameThread(Packet<T> packet, T packetlistener, ServerLevel serverlevel) throws RunningOnDifferentThreadException {
      ensureRunningOnSameThread(packet, packetlistener, serverlevel.getServer());
   }

   public static <T extends PacketListener> void ensureRunningOnSameThread(Packet<T> packet, T packetlistener, BlockableEventLoop<?> blockableeventloop) throws RunningOnDifferentThreadException {
      if (!blockableeventloop.isSameThread()) {
         blockableeventloop.executeIfPossible(() -> {
            if (packetlistener.isAcceptingMessages()) {
               try {
                  packet.handle(packetlistener);
               } catch (Exception var3) {
                  if (packetlistener.shouldPropagateHandlingExceptions()) {
                     throw var3;
                  }

                  LOGGER.error("Failed to handle packet {}, suppressing error", packet, var3);
               }
            } else {
               LOGGER.debug("Ignoring packet due to disconnection: {}", (Object)packet);
            }

         });
         throw RunningOnDifferentThreadException.RUNNING_ON_DIFFERENT_THREAD;
      }
   }
}
