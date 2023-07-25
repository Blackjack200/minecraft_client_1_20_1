package com.mojang.blaze3d.audio;

import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.nio.IntBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.OptionalLong;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.util.Mth;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALC11;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;
import org.lwjgl.openal.ALUtil;
import org.lwjgl.openal.SOFTHRTF;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;

public class Library {
   static final Logger LOGGER = LogUtils.getLogger();
   private static final int NO_DEVICE = 0;
   private static final int DEFAULT_CHANNEL_COUNT = 30;
   private long currentDevice;
   private long context;
   private boolean supportsDisconnections;
   @Nullable
   private String defaultDeviceName;
   private static final Library.ChannelPool EMPTY = new Library.ChannelPool() {
      @Nullable
      public Channel acquire() {
         return null;
      }

      public boolean release(Channel channel) {
         return false;
      }

      public void cleanup() {
      }

      public int getMaxCount() {
         return 0;
      }

      public int getUsedCount() {
         return 0;
      }
   };
   private Library.ChannelPool staticChannels = EMPTY;
   private Library.ChannelPool streamingChannels = EMPTY;
   private final Listener listener = new Listener();

   public Library() {
      this.defaultDeviceName = getDefaultDeviceName();
   }

   public void init(@Nullable String s, boolean flag) {
      this.currentDevice = openDeviceOrFallback(s);
      this.supportsDisconnections = ALC10.alcIsExtensionPresent(this.currentDevice, "ALC_EXT_disconnect");
      ALCCapabilities alccapabilities = ALC.createCapabilities(this.currentDevice);
      if (OpenAlUtil.checkALCError(this.currentDevice, "Get capabilities")) {
         throw new IllegalStateException("Failed to get OpenAL capabilities");
      } else if (!alccapabilities.OpenALC11) {
         throw new IllegalStateException("OpenAL 1.1 not supported");
      } else {
         this.setHrtf(alccapabilities.ALC_SOFT_HRTF && flag);
         this.context = ALC10.alcCreateContext(this.currentDevice, (IntBuffer)null);
         ALC10.alcMakeContextCurrent(this.context);
         int i = this.getChannelCount();
         int j = Mth.clamp((int)Mth.sqrt((float)i), 2, 8);
         int k = Mth.clamp(i - j, 8, 255);
         this.staticChannels = new Library.CountingChannelPool(k);
         this.streamingChannels = new Library.CountingChannelPool(j);
         ALCapabilities alcapabilities = AL.createCapabilities(alccapabilities);
         OpenAlUtil.checkALError("Initialization");
         if (!alcapabilities.AL_EXT_source_distance_model) {
            throw new IllegalStateException("AL_EXT_source_distance_model is not supported");
         } else {
            AL10.alEnable(512);
            if (!alcapabilities.AL_EXT_LINEAR_DISTANCE) {
               throw new IllegalStateException("AL_EXT_LINEAR_DISTANCE is not supported");
            } else {
               OpenAlUtil.checkALError("Enable per-source distance models");
               LOGGER.info("OpenAL initialized on device {}", (Object)this.getCurrentDeviceName());
            }
         }
      }
   }

   private void setHrtf(boolean flag) {
      int i = ALC10.alcGetInteger(this.currentDevice, 6548);
      if (i > 0) {
         MemoryStack memorystack = MemoryStack.stackPush();

         try {
            IntBuffer intbuffer = memorystack.callocInt(10).put(6546).put(flag ? 1 : 0).put(6550).put(0).put(0).flip();
            if (!SOFTHRTF.alcResetDeviceSOFT(this.currentDevice, intbuffer)) {
               LOGGER.warn("Failed to reset device: {}", (Object)ALC10.alcGetString(this.currentDevice, ALC10.alcGetError(this.currentDevice)));
            }
         } catch (Throwable var7) {
            if (memorystack != null) {
               try {
                  memorystack.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }
            }

            throw var7;
         }

         if (memorystack != null) {
            memorystack.close();
         }
      }

   }

   private int getChannelCount() {
      MemoryStack memorystack = MemoryStack.stackPush();

      int var7;
      label58: {
         try {
            int i = ALC10.alcGetInteger(this.currentDevice, 4098);
            if (OpenAlUtil.checkALCError(this.currentDevice, "Get attributes size")) {
               throw new IllegalStateException("Failed to get OpenAL attributes");
            }

            IntBuffer intbuffer = memorystack.mallocInt(i);
            ALC10.alcGetIntegerv(this.currentDevice, 4099, intbuffer);
            if (OpenAlUtil.checkALCError(this.currentDevice, "Get attributes")) {
               throw new IllegalStateException("Failed to get OpenAL attributes");
            }

            int j = 0;

            while(j < i) {
               int k = intbuffer.get(j++);
               if (k == 0) {
                  break;
               }

               int l = intbuffer.get(j++);
               if (k == 4112) {
                  var7 = l;
                  break label58;
               }
            }
         } catch (Throwable var9) {
            if (memorystack != null) {
               try {
                  memorystack.close();
               } catch (Throwable var8) {
                  var9.addSuppressed(var8);
               }
            }

            throw var9;
         }

         if (memorystack != null) {
            memorystack.close();
         }

         return 30;
      }

      if (memorystack != null) {
         memorystack.close();
      }

      return var7;
   }

   @Nullable
   public static String getDefaultDeviceName() {
      if (!ALC10.alcIsExtensionPresent(0L, "ALC_ENUMERATE_ALL_EXT")) {
         return null;
      } else {
         ALUtil.getStringList(0L, 4115);
         return ALC10.alcGetString(0L, 4114);
      }
   }

   public String getCurrentDeviceName() {
      String s = ALC10.alcGetString(this.currentDevice, 4115);
      if (s == null) {
         s = ALC10.alcGetString(this.currentDevice, 4101);
      }

      if (s == null) {
         s = "Unknown";
      }

      return s;
   }

   public synchronized boolean hasDefaultDeviceChanged() {
      String s = getDefaultDeviceName();
      if (Objects.equals(this.defaultDeviceName, s)) {
         return false;
      } else {
         this.defaultDeviceName = s;
         return true;
      }
   }

   private static long openDeviceOrFallback(@Nullable String s) {
      OptionalLong optionallong = OptionalLong.empty();
      if (s != null) {
         optionallong = tryOpenDevice(s);
      }

      if (optionallong.isEmpty()) {
         optionallong = tryOpenDevice(getDefaultDeviceName());
      }

      if (optionallong.isEmpty()) {
         optionallong = tryOpenDevice((String)null);
      }

      if (optionallong.isEmpty()) {
         throw new IllegalStateException("Failed to open OpenAL device");
      } else {
         return optionallong.getAsLong();
      }
   }

   private static OptionalLong tryOpenDevice(@Nullable String s) {
      long i = ALC10.alcOpenDevice(s);
      return i != 0L && !OpenAlUtil.checkALCError(i, "Open device") ? OptionalLong.of(i) : OptionalLong.empty();
   }

   public void cleanup() {
      this.staticChannels.cleanup();
      this.streamingChannels.cleanup();
      ALC10.alcDestroyContext(this.context);
      if (this.currentDevice != 0L) {
         ALC10.alcCloseDevice(this.currentDevice);
      }

   }

   public Listener getListener() {
      return this.listener;
   }

   @Nullable
   public Channel acquireChannel(Library.Pool library_pool) {
      return (library_pool == Library.Pool.STREAMING ? this.streamingChannels : this.staticChannels).acquire();
   }

   public void releaseChannel(Channel channel) {
      if (!this.staticChannels.release(channel) && !this.streamingChannels.release(channel)) {
         throw new IllegalStateException("Tried to release unknown channel");
      }
   }

   public String getDebugString() {
      return String.format(Locale.ROOT, "Sounds: %d/%d + %d/%d", this.staticChannels.getUsedCount(), this.staticChannels.getMaxCount(), this.streamingChannels.getUsedCount(), this.streamingChannels.getMaxCount());
   }

   public List<String> getAvailableSoundDevices() {
      List<String> list = ALUtil.getStringList(0L, 4115);
      return list == null ? Collections.emptyList() : list;
   }

   public boolean isCurrentDeviceDisconnected() {
      return this.supportsDisconnections && ALC11.alcGetInteger(this.currentDevice, 787) == 0;
   }

   interface ChannelPool {
      @Nullable
      Channel acquire();

      boolean release(Channel channel);

      void cleanup();

      int getMaxCount();

      int getUsedCount();
   }

   static class CountingChannelPool implements Library.ChannelPool {
      private final int limit;
      private final Set<Channel> activeChannels = Sets.newIdentityHashSet();

      public CountingChannelPool(int i) {
         this.limit = i;
      }

      @Nullable
      public Channel acquire() {
         if (this.activeChannels.size() >= this.limit) {
            if (SharedConstants.IS_RUNNING_IN_IDE) {
               Library.LOGGER.warn("Maximum sound pool size {} reached", (int)this.limit);
            }

            return null;
         } else {
            Channel channel = Channel.create();
            if (channel != null) {
               this.activeChannels.add(channel);
            }

            return channel;
         }
      }

      public boolean release(Channel channel) {
         if (!this.activeChannels.remove(channel)) {
            return false;
         } else {
            channel.destroy();
            return true;
         }
      }

      public void cleanup() {
         this.activeChannels.forEach(Channel::destroy);
         this.activeChannels.clear();
      }

      public int getMaxCount() {
         return this.limit;
      }

      public int getUsedCount() {
         return this.activeChannels.size();
      }
   }

   public static enum Pool {
      STATIC,
      STREAMING;
   }
}
