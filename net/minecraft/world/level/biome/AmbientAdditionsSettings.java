package net.minecraft.world.level.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;

public class AmbientAdditionsSettings {
   public static final Codec<AmbientAdditionsSettings> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(SoundEvent.CODEC.fieldOf("sound").forGetter((ambientadditionssettings1) -> ambientadditionssettings1.soundEvent), Codec.DOUBLE.fieldOf("tick_chance").forGetter((ambientadditionssettings) -> ambientadditionssettings.tickChance)).apply(recordcodecbuilder_instance, AmbientAdditionsSettings::new));
   private final Holder<SoundEvent> soundEvent;
   private final double tickChance;

   public AmbientAdditionsSettings(Holder<SoundEvent> holder, double d0) {
      this.soundEvent = holder;
      this.tickChance = d0;
   }

   public Holder<SoundEvent> getSoundEvent() {
      return this.soundEvent;
   }

   public double getTickChance() {
      return this.tickChance;
   }
}
