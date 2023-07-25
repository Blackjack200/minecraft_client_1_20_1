package net.minecraft.world.level.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public class AmbientMoodSettings {
   public static final Codec<AmbientMoodSettings> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(SoundEvent.CODEC.fieldOf("sound").forGetter((ambientmoodsettings3) -> ambientmoodsettings3.soundEvent), Codec.INT.fieldOf("tick_delay").forGetter((ambientmoodsettings2) -> ambientmoodsettings2.tickDelay), Codec.INT.fieldOf("block_search_extent").forGetter((ambientmoodsettings1) -> ambientmoodsettings1.blockSearchExtent), Codec.DOUBLE.fieldOf("offset").forGetter((ambientmoodsettings) -> ambientmoodsettings.soundPositionOffset)).apply(recordcodecbuilder_instance, AmbientMoodSettings::new));
   public static final AmbientMoodSettings LEGACY_CAVE_SETTINGS = new AmbientMoodSettings(SoundEvents.AMBIENT_CAVE, 6000, 8, 2.0D);
   private final Holder<SoundEvent> soundEvent;
   private final int tickDelay;
   private final int blockSearchExtent;
   private final double soundPositionOffset;

   public AmbientMoodSettings(Holder<SoundEvent> holder, int i, int j, double d0) {
      this.soundEvent = holder;
      this.tickDelay = i;
      this.blockSearchExtent = j;
      this.soundPositionOffset = d0;
   }

   public Holder<SoundEvent> getSoundEvent() {
      return this.soundEvent;
   }

   public int getTickDelay() {
      return this.tickDelay;
   }

   public int getBlockSearchExtent() {
      return this.blockSearchExtent;
   }

   public double getSoundPositionOffset() {
      return this.soundPositionOffset;
   }
}
