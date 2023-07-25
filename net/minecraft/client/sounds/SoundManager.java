package net.minecraft.client.sounds;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.client.Camera;
import net.minecraft.client.Options;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundEventRegistration;
import net.minecraft.client.resources.sounds.SoundEventRegistrationSerializer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.MultipliedFloats;
import org.slf4j.Logger;

public class SoundManager extends SimplePreparableReloadListener<SoundManager.Preparations> {
   public static final Sound EMPTY_SOUND = new Sound("minecraft:empty", ConstantFloat.of(1.0F), ConstantFloat.of(1.0F), 1, Sound.Type.FILE, false, false, 16);
   public static final ResourceLocation INTENTIONALLY_EMPTY_SOUND_LOCATION = new ResourceLocation("minecraft", "intentionally_empty");
   public static final WeighedSoundEvents INTENTIONALLY_EMPTY_SOUND_EVENT = new WeighedSoundEvents(INTENTIONALLY_EMPTY_SOUND_LOCATION, (String)null);
   public static final Sound INTENTIONALLY_EMPTY_SOUND = new Sound(INTENTIONALLY_EMPTY_SOUND_LOCATION.toString(), ConstantFloat.of(1.0F), ConstantFloat.of(1.0F), 1, Sound.Type.FILE, false, false, 16);
   static final Logger LOGGER = LogUtils.getLogger();
   private static final String SOUNDS_PATH = "sounds.json";
   private static final Gson GSON = (new GsonBuilder()).registerTypeHierarchyAdapter(Component.class, new Component.Serializer()).registerTypeAdapter(SoundEventRegistration.class, new SoundEventRegistrationSerializer()).create();
   private static final TypeToken<Map<String, SoundEventRegistration>> SOUND_EVENT_REGISTRATION_TYPE = new TypeToken<Map<String, SoundEventRegistration>>() {
   };
   private final Map<ResourceLocation, WeighedSoundEvents> registry = Maps.newHashMap();
   private final SoundEngine soundEngine;
   private final Map<ResourceLocation, Resource> soundCache = new HashMap<>();

   public SoundManager(Options options) {
      this.soundEngine = new SoundEngine(this, options, ResourceProvider.fromMap(this.soundCache));
   }

   protected SoundManager.Preparations prepare(ResourceManager resourcemanager, ProfilerFiller profilerfiller) {
      SoundManager.Preparations soundmanager_preparations = new SoundManager.Preparations();
      profilerfiller.startTick();
      profilerfiller.push("list");
      soundmanager_preparations.listResources(resourcemanager);
      profilerfiller.pop();

      for(String s : resourcemanager.getNamespaces()) {
         profilerfiller.push(s);

         try {
            for(Resource resource : resourcemanager.getResourceStack(new ResourceLocation(s, "sounds.json"))) {
               profilerfiller.push(resource.sourcePackId());

               try {
                  Reader reader = resource.openAsReader();

                  try {
                     profilerfiller.push("parse");
                     Map<String, SoundEventRegistration> map = GsonHelper.fromJson(GSON, reader, SOUND_EVENT_REGISTRATION_TYPE);
                     profilerfiller.popPush("register");

                     for(Map.Entry<String, SoundEventRegistration> map_entry : map.entrySet()) {
                        soundmanager_preparations.handleRegistration(new ResourceLocation(s, map_entry.getKey()), map_entry.getValue());
                     }

                     profilerfiller.pop();
                  } catch (Throwable var14) {
                     if (reader != null) {
                        try {
                           reader.close();
                        } catch (Throwable var13) {
                           var14.addSuppressed(var13);
                        }
                     }

                     throw var14;
                  }

                  if (reader != null) {
                     reader.close();
                  }
               } catch (RuntimeException var15) {
                  LOGGER.warn("Invalid {} in resourcepack: '{}'", "sounds.json", resource.sourcePackId(), var15);
               }

               profilerfiller.pop();
            }
         } catch (IOException var16) {
         }

         profilerfiller.pop();
      }

      profilerfiller.endTick();
      return soundmanager_preparations;
   }

   protected void apply(SoundManager.Preparations soundmanager_preparations, ResourceManager resourcemanager, ProfilerFiller profilerfiller) {
      soundmanager_preparations.apply(this.registry, this.soundCache, this.soundEngine);
      if (SharedConstants.IS_RUNNING_IN_IDE) {
         for(ResourceLocation resourcelocation : this.registry.keySet()) {
            WeighedSoundEvents weighedsoundevents = this.registry.get(resourcelocation);
            if (!ComponentUtils.isTranslationResolvable(weighedsoundevents.getSubtitle()) && BuiltInRegistries.SOUND_EVENT.containsKey(resourcelocation)) {
               LOGGER.error("Missing subtitle {} for sound event: {}", weighedsoundevents.getSubtitle(), resourcelocation);
            }
         }
      }

      if (LOGGER.isDebugEnabled()) {
         for(ResourceLocation resourcelocation1 : this.registry.keySet()) {
            if (!BuiltInRegistries.SOUND_EVENT.containsKey(resourcelocation1)) {
               LOGGER.debug("Not having sound event for: {}", (Object)resourcelocation1);
            }
         }
      }

      this.soundEngine.reload();
   }

   public List<String> getAvailableSoundDevices() {
      return this.soundEngine.getAvailableSoundDevices();
   }

   static boolean validateSoundResource(Sound sound, ResourceLocation resourcelocation, ResourceProvider resourceprovider) {
      ResourceLocation resourcelocation1 = sound.getPath();
      if (resourceprovider.getResource(resourcelocation1).isEmpty()) {
         LOGGER.warn("File {} does not exist, cannot add it to event {}", resourcelocation1, resourcelocation);
         return false;
      } else {
         return true;
      }
   }

   @Nullable
   public WeighedSoundEvents getSoundEvent(ResourceLocation resourcelocation) {
      return this.registry.get(resourcelocation);
   }

   public Collection<ResourceLocation> getAvailableSounds() {
      return this.registry.keySet();
   }

   public void queueTickingSound(TickableSoundInstance tickablesoundinstance) {
      this.soundEngine.queueTickingSound(tickablesoundinstance);
   }

   public void play(SoundInstance soundinstance) {
      this.soundEngine.play(soundinstance);
   }

   public void playDelayed(SoundInstance soundinstance, int i) {
      this.soundEngine.playDelayed(soundinstance, i);
   }

   public void updateSource(Camera camera) {
      this.soundEngine.updateSource(camera);
   }

   public void pause() {
      this.soundEngine.pause();
   }

   public void stop() {
      this.soundEngine.stopAll();
   }

   public void destroy() {
      this.soundEngine.destroy();
   }

   public void tick(boolean flag) {
      this.soundEngine.tick(flag);
   }

   public void resume() {
      this.soundEngine.resume();
   }

   public void updateSourceVolume(SoundSource soundsource, float f) {
      if (soundsource == SoundSource.MASTER && f <= 0.0F) {
         this.stop();
      }

      this.soundEngine.updateCategoryVolume(soundsource, f);
   }

   public void stop(SoundInstance soundinstance) {
      this.soundEngine.stop(soundinstance);
   }

   public boolean isActive(SoundInstance soundinstance) {
      return this.soundEngine.isActive(soundinstance);
   }

   public void addListener(SoundEventListener soundeventlistener) {
      this.soundEngine.addEventListener(soundeventlistener);
   }

   public void removeListener(SoundEventListener soundeventlistener) {
      this.soundEngine.removeEventListener(soundeventlistener);
   }

   public void stop(@Nullable ResourceLocation resourcelocation, @Nullable SoundSource soundsource) {
      this.soundEngine.stop(resourcelocation, soundsource);
   }

   public String getDebugString() {
      return this.soundEngine.getDebugString();
   }

   public void reload() {
      this.soundEngine.reload();
   }

   protected static class Preparations {
      final Map<ResourceLocation, WeighedSoundEvents> registry = Maps.newHashMap();
      private Map<ResourceLocation, Resource> soundCache = Map.of();

      void listResources(ResourceManager resourcemanager) {
         this.soundCache = Sound.SOUND_LISTER.listMatchingResources(resourcemanager);
      }

      void handleRegistration(ResourceLocation resourcelocation, SoundEventRegistration soundeventregistration) {
         WeighedSoundEvents weighedsoundevents = this.registry.get(resourcelocation);
         boolean flag = weighedsoundevents == null;
         if (flag || soundeventregistration.isReplace()) {
            if (!flag) {
               SoundManager.LOGGER.debug("Replaced sound event location {}", (Object)resourcelocation);
            }

            weighedsoundevents = new WeighedSoundEvents(resourcelocation, soundeventregistration.getSubtitle());
            this.registry.put(resourcelocation, weighedsoundevents);
         }

         ResourceProvider resourceprovider = ResourceProvider.fromMap(this.soundCache);

         for(final Sound sound : soundeventregistration.getSounds()) {
            final ResourceLocation resourcelocation1 = sound.getLocation();
            Weighted<Sound> weighted1;
            switch (sound.getType()) {
               case FILE:
                  if (!SoundManager.validateSoundResource(sound, resourcelocation, resourceprovider)) {
                     continue;
                  }

                  weighted1 = sound;
                  break;
               case SOUND_EVENT:
                  weighted1 = new Weighted<Sound>() {
                     public int getWeight() {
                        WeighedSoundEvents weighedsoundevents = Preparations.this.registry.get(resourcelocation1);
                        return weighedsoundevents == null ? 0 : weighedsoundevents.getWeight();
                     }

                     public Sound getSound(RandomSource randomsource) {
                        WeighedSoundEvents weighedsoundevents = Preparations.this.registry.get(resourcelocation1);
                        if (weighedsoundevents == null) {
                           return SoundManager.EMPTY_SOUND;
                        } else {
                           Sound sound = weighedsoundevents.getSound(randomsource);
                           return new Sound(sound.getLocation().toString(), new MultipliedFloats(sound.getVolume(), sound.getVolume()), new MultipliedFloats(sound.getPitch(), sound.getPitch()), sound.getWeight(), Sound.Type.FILE, sound.shouldStream() || sound.shouldStream(), sound.shouldPreload(), sound.getAttenuationDistance());
                        }
                     }

                     public void preloadIfRequired(SoundEngine soundengine) {
                        WeighedSoundEvents weighedsoundevents = Preparations.this.registry.get(resourcelocation1);
                        if (weighedsoundevents != null) {
                           weighedsoundevents.preloadIfRequired(soundengine);
                        }
                     }
                  };
                  break;
               default:
                  throw new IllegalStateException("Unknown SoundEventRegistration type: " + sound.getType());
            }

            weighedsoundevents.addSound(weighted1);
         }

      }

      public void apply(Map<ResourceLocation, WeighedSoundEvents> map, Map<ResourceLocation, Resource> map1, SoundEngine soundengine) {
         map.clear();
         map1.clear();
         map1.putAll(this.soundCache);

         for(Map.Entry<ResourceLocation, WeighedSoundEvents> map_entry : this.registry.entrySet()) {
            map.put(map_entry.getKey(), map_entry.getValue());
            map_entry.getValue().preloadIfRequired(soundengine);
         }

      }
   }
}
