package net.minecraft.client.sounds;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.audio.Channel;
import com.mojang.blaze3d.audio.Library;
import com.mojang.blaze3d.audio.Listener;
import com.mojang.logging.LogUtils;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Options;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class SoundEngine {
   private static final Marker MARKER = MarkerFactory.getMarker("SOUNDS");
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final float PITCH_MIN = 0.5F;
   private static final float PITCH_MAX = 2.0F;
   private static final float VOLUME_MIN = 0.0F;
   private static final float VOLUME_MAX = 1.0F;
   private static final int MIN_SOURCE_LIFETIME = 20;
   private static final Set<ResourceLocation> ONLY_WARN_ONCE = Sets.newHashSet();
   private static final long DEFAULT_DEVICE_CHECK_INTERVAL_MS = 1000L;
   public static final String MISSING_SOUND = "FOR THE DEBUG!";
   public static final String OPEN_AL_SOFT_PREFIX = "OpenAL Soft on ";
   public static final int OPEN_AL_SOFT_PREFIX_LENGTH = "OpenAL Soft on ".length();
   private final SoundManager soundManager;
   private final Options options;
   private boolean loaded;
   private final Library library = new Library();
   private final Listener listener = this.library.getListener();
   private final SoundBufferLibrary soundBuffers;
   private final SoundEngineExecutor executor = new SoundEngineExecutor();
   private final ChannelAccess channelAccess = new ChannelAccess(this.library, this.executor);
   private int tickCount;
   private long lastDeviceCheckTime;
   private final AtomicReference<SoundEngine.DeviceCheckState> devicePoolState = new AtomicReference<>(SoundEngine.DeviceCheckState.NO_CHANGE);
   private final Map<SoundInstance, ChannelAccess.ChannelHandle> instanceToChannel = Maps.newHashMap();
   private final Multimap<SoundSource, SoundInstance> instanceBySource = HashMultimap.create();
   private final List<TickableSoundInstance> tickingSounds = Lists.newArrayList();
   private final Map<SoundInstance, Integer> queuedSounds = Maps.newHashMap();
   private final Map<SoundInstance, Integer> soundDeleteTime = Maps.newHashMap();
   private final List<SoundEventListener> listeners = Lists.newArrayList();
   private final List<TickableSoundInstance> queuedTickableSounds = Lists.newArrayList();
   private final List<Sound> preloadQueue = Lists.newArrayList();

   public SoundEngine(SoundManager soundmanager, Options options, ResourceProvider resourceprovider) {
      this.soundManager = soundmanager;
      this.options = options;
      this.soundBuffers = new SoundBufferLibrary(resourceprovider);
   }

   public void reload() {
      ONLY_WARN_ONCE.clear();

      for(SoundEvent soundevent : BuiltInRegistries.SOUND_EVENT) {
         if (soundevent != SoundEvents.EMPTY) {
            ResourceLocation resourcelocation = soundevent.getLocation();
            if (this.soundManager.getSoundEvent(resourcelocation) == null) {
               LOGGER.warn("Missing sound for event: {}", (Object)BuiltInRegistries.SOUND_EVENT.getKey(soundevent));
               ONLY_WARN_ONCE.add(resourcelocation);
            }
         }
      }

      this.destroy();
      this.loadLibrary();
   }

   private synchronized void loadLibrary() {
      if (!this.loaded) {
         try {
            String s = this.options.soundDevice().get();
            this.library.init("".equals(s) ? null : s, this.options.directionalAudio().get());
            this.listener.reset();
            this.listener.setGain(this.options.getSoundSourceVolume(SoundSource.MASTER));
            this.soundBuffers.preload(this.preloadQueue).thenRun(this.preloadQueue::clear);
            this.loaded = true;
            LOGGER.info(MARKER, "Sound engine started");
         } catch (RuntimeException var2) {
            LOGGER.error(MARKER, "Error starting SoundSystem. Turning off sounds & music", (Throwable)var2);
         }

      }
   }

   private float getVolume(@Nullable SoundSource soundsource) {
      return soundsource != null && soundsource != SoundSource.MASTER ? this.options.getSoundSourceVolume(soundsource) : 1.0F;
   }

   public void updateCategoryVolume(SoundSource soundsource, float f) {
      if (this.loaded) {
         if (soundsource == SoundSource.MASTER) {
            this.listener.setGain(f);
         } else {
            this.instanceToChannel.forEach((soundinstance, channelaccess_channelhandle) -> {
               float f1 = this.calculateVolume(soundinstance);
               channelaccess_channelhandle.execute((channel) -> {
                  if (f1 <= 0.0F) {
                     channel.stop();
                  } else {
                     channel.setVolume(f1);
                  }

               });
            });
         }
      }
   }

   public void destroy() {
      if (this.loaded) {
         this.stopAll();
         this.soundBuffers.clear();
         this.library.cleanup();
         this.loaded = false;
      }

   }

   public void stop(SoundInstance soundinstance) {
      if (this.loaded) {
         ChannelAccess.ChannelHandle channelaccess_channelhandle = this.instanceToChannel.get(soundinstance);
         if (channelaccess_channelhandle != null) {
            channelaccess_channelhandle.execute(Channel::stop);
         }
      }

   }

   public void stopAll() {
      if (this.loaded) {
         this.executor.flush();
         this.instanceToChannel.values().forEach((channelaccess_channelhandle) -> channelaccess_channelhandle.execute(Channel::stop));
         this.instanceToChannel.clear();
         this.channelAccess.clear();
         this.queuedSounds.clear();
         this.tickingSounds.clear();
         this.instanceBySource.clear();
         this.soundDeleteTime.clear();
         this.queuedTickableSounds.clear();
      }

   }

   public void addEventListener(SoundEventListener soundeventlistener) {
      this.listeners.add(soundeventlistener);
   }

   public void removeEventListener(SoundEventListener soundeventlistener) {
      this.listeners.remove(soundeventlistener);
   }

   private boolean shouldChangeDevice() {
      if (this.library.isCurrentDeviceDisconnected()) {
         LOGGER.info("Audio device was lost!");
         return true;
      } else {
         long i = Util.getMillis();
         boolean flag = i - this.lastDeviceCheckTime >= 1000L;
         if (flag) {
            this.lastDeviceCheckTime = i;
            if (this.devicePoolState.compareAndSet(SoundEngine.DeviceCheckState.NO_CHANGE, SoundEngine.DeviceCheckState.ONGOING)) {
               String s = this.options.soundDevice().get();
               Util.ioPool().execute(() -> {
                  if ("".equals(s)) {
                     if (this.library.hasDefaultDeviceChanged()) {
                        LOGGER.info("System default audio device has changed!");
                        this.devicePoolState.compareAndSet(SoundEngine.DeviceCheckState.ONGOING, SoundEngine.DeviceCheckState.CHANGE_DETECTED);
                     }
                  } else if (!this.library.getCurrentDeviceName().equals(s) && this.library.getAvailableSoundDevices().contains(s)) {
                     LOGGER.info("Preferred audio device has become available!");
                     this.devicePoolState.compareAndSet(SoundEngine.DeviceCheckState.ONGOING, SoundEngine.DeviceCheckState.CHANGE_DETECTED);
                  }

                  this.devicePoolState.compareAndSet(SoundEngine.DeviceCheckState.ONGOING, SoundEngine.DeviceCheckState.NO_CHANGE);
               });
            }
         }

         return this.devicePoolState.compareAndSet(SoundEngine.DeviceCheckState.CHANGE_DETECTED, SoundEngine.DeviceCheckState.NO_CHANGE);
      }
   }

   public void tick(boolean flag) {
      if (this.shouldChangeDevice()) {
         this.reload();
      }

      if (!flag) {
         this.tickNonPaused();
      }

      this.channelAccess.scheduleTick();
   }

   private void tickNonPaused() {
      ++this.tickCount;
      this.queuedTickableSounds.stream().filter(SoundInstance::canPlaySound).forEach(this::play);
      this.queuedTickableSounds.clear();

      for(TickableSoundInstance tickablesoundinstance : this.tickingSounds) {
         if (!tickablesoundinstance.canPlaySound()) {
            this.stop(tickablesoundinstance);
         }

         tickablesoundinstance.tick();
         if (tickablesoundinstance.isStopped()) {
            this.stop(tickablesoundinstance);
         } else {
            float f = this.calculateVolume(tickablesoundinstance);
            float f1 = this.calculatePitch(tickablesoundinstance);
            Vec3 vec3 = new Vec3(tickablesoundinstance.getX(), tickablesoundinstance.getY(), tickablesoundinstance.getZ());
            ChannelAccess.ChannelHandle channelaccess_channelhandle = this.instanceToChannel.get(tickablesoundinstance);
            if (channelaccess_channelhandle != null) {
               channelaccess_channelhandle.execute((channel) -> {
                  channel.setVolume(f);
                  channel.setPitch(f1);
                  channel.setSelfPosition(vec3);
               });
            }
         }
      }

      Iterator<Map.Entry<SoundInstance, ChannelAccess.ChannelHandle>> iterator = this.instanceToChannel.entrySet().iterator();

      while(iterator.hasNext()) {
         Map.Entry<SoundInstance, ChannelAccess.ChannelHandle> map_entry = iterator.next();
         ChannelAccess.ChannelHandle channelaccess_channelhandle1 = map_entry.getValue();
         SoundInstance soundinstance = map_entry.getKey();
         float f2 = this.options.getSoundSourceVolume(soundinstance.getSource());
         if (f2 <= 0.0F) {
            channelaccess_channelhandle1.execute(Channel::stop);
            iterator.remove();
         } else if (channelaccess_channelhandle1.isStopped()) {
            int i = this.soundDeleteTime.get(soundinstance);
            if (i <= this.tickCount) {
               if (shouldLoopManually(soundinstance)) {
                  this.queuedSounds.put(soundinstance, this.tickCount + soundinstance.getDelay());
               }

               iterator.remove();
               LOGGER.debug(MARKER, "Removed channel {} because it's not playing anymore", (Object)channelaccess_channelhandle1);
               this.soundDeleteTime.remove(soundinstance);

               try {
                  this.instanceBySource.remove(soundinstance.getSource(), soundinstance);
               } catch (RuntimeException var8) {
               }

               if (soundinstance instanceof TickableSoundInstance) {
                  this.tickingSounds.remove(soundinstance);
               }
            }
         }
      }

      Iterator<Map.Entry<SoundInstance, Integer>> iterator1 = this.queuedSounds.entrySet().iterator();

      while(iterator1.hasNext()) {
         Map.Entry<SoundInstance, Integer> map_entry1 = iterator1.next();
         if (this.tickCount >= map_entry1.getValue()) {
            SoundInstance soundinstance1 = map_entry1.getKey();
            if (soundinstance1 instanceof TickableSoundInstance) {
               ((TickableSoundInstance)soundinstance1).tick();
            }

            this.play(soundinstance1);
            iterator1.remove();
         }
      }

   }

   private static boolean requiresManualLooping(SoundInstance soundinstance) {
      return soundinstance.getDelay() > 0;
   }

   private static boolean shouldLoopManually(SoundInstance soundinstance) {
      return soundinstance.isLooping() && requiresManualLooping(soundinstance);
   }

   private static boolean shouldLoopAutomatically(SoundInstance soundinstance) {
      return soundinstance.isLooping() && !requiresManualLooping(soundinstance);
   }

   public boolean isActive(SoundInstance soundinstance) {
      if (!this.loaded) {
         return false;
      } else {
         return this.soundDeleteTime.containsKey(soundinstance) && this.soundDeleteTime.get(soundinstance) <= this.tickCount ? true : this.instanceToChannel.containsKey(soundinstance);
      }
   }

   public void play(SoundInstance soundinstance) {
      if (this.loaded) {
         if (soundinstance.canPlaySound()) {
            WeighedSoundEvents weighedsoundevents = soundinstance.resolve(this.soundManager);
            ResourceLocation resourcelocation = soundinstance.getLocation();
            if (weighedsoundevents == null) {
               if (ONLY_WARN_ONCE.add(resourcelocation)) {
                  LOGGER.warn(MARKER, "Unable to play unknown soundEvent: {}", (Object)resourcelocation);
               }

            } else {
               Sound sound = soundinstance.getSound();
               if (sound != SoundManager.INTENTIONALLY_EMPTY_SOUND) {
                  if (sound == SoundManager.EMPTY_SOUND) {
                     if (ONLY_WARN_ONCE.add(resourcelocation)) {
                        LOGGER.warn(MARKER, "Unable to play empty soundEvent: {}", (Object)resourcelocation);
                     }

                  } else {
                     float f = soundinstance.getVolume();
                     float f1 = Math.max(f, 1.0F) * (float)sound.getAttenuationDistance();
                     SoundSource soundsource = soundinstance.getSource();
                     float f2 = this.calculateVolume(f, soundsource);
                     float f3 = this.calculatePitch(soundinstance);
                     SoundInstance.Attenuation soundinstance_attenuation = soundinstance.getAttenuation();
                     boolean flag = soundinstance.isRelative();
                     if (f2 == 0.0F && !soundinstance.canStartSilent()) {
                        LOGGER.debug(MARKER, "Skipped playing sound {}, volume was zero.", (Object)sound.getLocation());
                     } else {
                        Vec3 vec3 = new Vec3(soundinstance.getX(), soundinstance.getY(), soundinstance.getZ());
                        if (!this.listeners.isEmpty()) {
                           boolean flag1 = flag || soundinstance_attenuation == SoundInstance.Attenuation.NONE || this.listener.getListenerPosition().distanceToSqr(vec3) < (double)(f1 * f1);
                           if (flag1) {
                              for(SoundEventListener soundeventlistener : this.listeners) {
                                 soundeventlistener.onPlaySound(soundinstance, weighedsoundevents);
                              }
                           } else {
                              LOGGER.debug(MARKER, "Did not notify listeners of soundEvent: {}, it is too far away to hear", (Object)resourcelocation);
                           }
                        }

                        if (this.listener.getGain() <= 0.0F) {
                           LOGGER.debug(MARKER, "Skipped playing soundEvent: {}, master volume was zero", (Object)resourcelocation);
                        } else {
                           boolean flag2 = shouldLoopAutomatically(soundinstance);
                           boolean flag3 = sound.shouldStream();
                           CompletableFuture<ChannelAccess.ChannelHandle> completablefuture = this.channelAccess.createHandle(sound.shouldStream() ? Library.Pool.STREAMING : Library.Pool.STATIC);
                           ChannelAccess.ChannelHandle channelaccess_channelhandle = completablefuture.join();
                           if (channelaccess_channelhandle == null) {
                              if (SharedConstants.IS_RUNNING_IN_IDE) {
                                 LOGGER.warn("Failed to create new sound handle");
                              }

                           } else {
                              LOGGER.debug(MARKER, "Playing sound {} for event {}", sound.getLocation(), resourcelocation);
                              this.soundDeleteTime.put(soundinstance, this.tickCount + 20);
                              this.instanceToChannel.put(soundinstance, channelaccess_channelhandle);
                              this.instanceBySource.put(soundsource, soundinstance);
                              channelaccess_channelhandle.execute((channel2) -> {
                                 channel2.setPitch(f3);
                                 channel2.setVolume(f2);
                                 if (soundinstance_attenuation == SoundInstance.Attenuation.LINEAR) {
                                    channel2.linearAttenuation(f1);
                                 } else {
                                    channel2.disableAttenuation();
                                 }

                                 channel2.setLooping(flag2 && !flag3);
                                 channel2.setSelfPosition(vec3);
                                 channel2.setRelative(flag);
                              });
                              if (!flag3) {
                                 this.soundBuffers.getCompleteBuffer(sound.getPath()).thenAccept((soundbuffer) -> channelaccess_channelhandle.execute((channel1) -> {
                                       channel1.attachStaticBuffer(soundbuffer);
                                       channel1.play();
                                    }));
                              } else {
                                 this.soundBuffers.getStream(sound.getPath(), flag2).thenAccept((audiostream) -> channelaccess_channelhandle.execute((channel) -> {
                                       channel.attachBufferStream(audiostream);
                                       channel.play();
                                    }));
                              }

                              if (soundinstance instanceof TickableSoundInstance) {
                                 this.tickingSounds.add((TickableSoundInstance)soundinstance);
                              }

                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public void queueTickingSound(TickableSoundInstance tickablesoundinstance) {
      this.queuedTickableSounds.add(tickablesoundinstance);
   }

   public void requestPreload(Sound sound) {
      this.preloadQueue.add(sound);
   }

   private float calculatePitch(SoundInstance soundinstance) {
      return Mth.clamp(soundinstance.getPitch(), 0.5F, 2.0F);
   }

   private float calculateVolume(SoundInstance soundinstance) {
      return this.calculateVolume(soundinstance.getVolume(), soundinstance.getSource());
   }

   private float calculateVolume(float f, SoundSource soundsource) {
      return Mth.clamp(f * this.getVolume(soundsource), 0.0F, 1.0F);
   }

   public void pause() {
      if (this.loaded) {
         this.channelAccess.executeOnChannels((stream) -> stream.forEach(Channel::pause));
      }

   }

   public void resume() {
      if (this.loaded) {
         this.channelAccess.executeOnChannels((stream) -> stream.forEach(Channel::unpause));
      }

   }

   public void playDelayed(SoundInstance soundinstance, int i) {
      this.queuedSounds.put(soundinstance, this.tickCount + i);
   }

   public void updateSource(Camera camera) {
      if (this.loaded && camera.isInitialized()) {
         Vec3 vec3 = camera.getPosition();
         Vector3f vector3f = camera.getLookVector();
         Vector3f vector3f1 = camera.getUpVector();
         this.executor.execute(() -> {
            this.listener.setListenerPosition(vec3);
            this.listener.setListenerOrientation(vector3f, vector3f1);
         });
      }
   }

   public void stop(@Nullable ResourceLocation resourcelocation, @Nullable SoundSource soundsource) {
      if (soundsource != null) {
         for(SoundInstance soundinstance : this.instanceBySource.get(soundsource)) {
            if (resourcelocation == null || soundinstance.getLocation().equals(resourcelocation)) {
               this.stop(soundinstance);
            }
         }
      } else if (resourcelocation == null) {
         this.stopAll();
      } else {
         for(SoundInstance soundinstance1 : this.instanceToChannel.keySet()) {
            if (soundinstance1.getLocation().equals(resourcelocation)) {
               this.stop(soundinstance1);
            }
         }
      }

   }

   public String getDebugString() {
      return this.library.getDebugString();
   }

   public List<String> getAvailableSoundDevices() {
      return this.library.getAvailableSoundDevices();
   }

   static enum DeviceCheckState {
      ONGOING,
      CHANGE_DETECTED,
      NO_CHANGE;
   }
}
