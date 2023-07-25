package net.minecraft.client.resources.sounds;

import javax.annotation.Nullable;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.Weighted;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.SampledFloat;

public class Sound implements Weighted<Sound> {
   public static final FileToIdConverter SOUND_LISTER = new FileToIdConverter("sounds", ".ogg");
   private final ResourceLocation location;
   private final SampledFloat volume;
   private final SampledFloat pitch;
   private final int weight;
   private final Sound.Type type;
   private final boolean stream;
   private final boolean preload;
   private final int attenuationDistance;

   public Sound(String s, SampledFloat sampledfloat, SampledFloat sampledfloat1, int i, Sound.Type sound_type, boolean flag, boolean flag1, int j) {
      this.location = new ResourceLocation(s);
      this.volume = sampledfloat;
      this.pitch = sampledfloat1;
      this.weight = i;
      this.type = sound_type;
      this.stream = flag;
      this.preload = flag1;
      this.attenuationDistance = j;
   }

   public ResourceLocation getLocation() {
      return this.location;
   }

   public ResourceLocation getPath() {
      return SOUND_LISTER.idToFile(this.location);
   }

   public SampledFloat getVolume() {
      return this.volume;
   }

   public SampledFloat getPitch() {
      return this.pitch;
   }

   public int getWeight() {
      return this.weight;
   }

   public Sound getSound(RandomSource randomsource) {
      return this;
   }

   public void preloadIfRequired(SoundEngine soundengine) {
      if (this.preload) {
         soundengine.requestPreload(this);
      }

   }

   public Sound.Type getType() {
      return this.type;
   }

   public boolean shouldStream() {
      return this.stream;
   }

   public boolean shouldPreload() {
      return this.preload;
   }

   public int getAttenuationDistance() {
      return this.attenuationDistance;
   }

   public String toString() {
      return "Sound[" + this.location + "]";
   }

   public static enum Type {
      FILE("file"),
      SOUND_EVENT("event");

      private final String name;

      private Type(String s) {
         this.name = s;
      }

      @Nullable
      public static Sound.Type getByName(String s) {
         for(Sound.Type sound_type : values()) {
            if (sound_type.name.equals(s)) {
               return sound_type;
            }
         }

         return null;
      }
   }
}
