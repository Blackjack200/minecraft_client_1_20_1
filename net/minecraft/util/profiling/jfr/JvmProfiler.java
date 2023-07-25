package net.minecraft.util.profiling.jfr;

import com.mojang.logging.LogUtils;
import java.net.SocketAddress;
import java.nio.file.Path;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.jfr.callback.ProfiledDuration;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

public interface JvmProfiler {
   JvmProfiler INSTANCE = (JvmProfiler)(Runtime.class.getModule().getLayer().findModule("jdk.jfr").isPresent() ? JfrProfiler.getInstance() : new JvmProfiler.NoOpProfiler());

   boolean start(Environment environment);

   Path stop();

   boolean isRunning();

   boolean isAvailable();

   void onServerTick(float f);

   void onPacketReceived(int i, int j, SocketAddress socketaddress, int k);

   void onPacketSent(int i, int j, SocketAddress socketaddress, int k);

   @Nullable
   ProfiledDuration onWorldLoadedStarted();

   @Nullable
   ProfiledDuration onChunkGenerate(ChunkPos chunkpos, ResourceKey<Level> resourcekey, String s);

   public static class NoOpProfiler implements JvmProfiler {
      private static final Logger LOGGER = LogUtils.getLogger();
      static final ProfiledDuration noOpCommit = () -> {
      };

      public boolean start(Environment environment) {
         LOGGER.warn("Attempted to start Flight Recorder, but it's not supported on this JVM");
         return false;
      }

      public Path stop() {
         throw new IllegalStateException("Attempted to stop Flight Recorder, but it's not supported on this JVM");
      }

      public boolean isRunning() {
         return false;
      }

      public boolean isAvailable() {
         return false;
      }

      public void onPacketReceived(int i, int j, SocketAddress socketaddress, int k) {
      }

      public void onPacketSent(int i, int j, SocketAddress socketaddress, int k) {
      }

      public void onServerTick(float f) {
      }

      public ProfiledDuration onWorldLoadedStarted() {
         return noOpCommit;
      }

      @Nullable
      public ProfiledDuration onChunkGenerate(ChunkPos chunkpos, ResourceKey<Level> resourcekey, String s) {
         return null;
      }
   }
}
