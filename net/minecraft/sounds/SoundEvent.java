package net.minecraft.sounds;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;

public class SoundEvent {
   public static final Codec<SoundEvent> DIRECT_CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(ResourceLocation.CODEC.fieldOf("sound_id").forGetter(SoundEvent::getLocation), Codec.FLOAT.optionalFieldOf("range").forGetter(SoundEvent::fixedRange)).apply(recordcodecbuilder_instance, SoundEvent::create));
   public static final Codec<Holder<SoundEvent>> CODEC = RegistryFileCodec.create(Registries.SOUND_EVENT, DIRECT_CODEC);
   private static final float DEFAULT_RANGE = 16.0F;
   private final ResourceLocation location;
   private final float range;
   private final boolean newSystem;

   private static SoundEvent create(ResourceLocation resourcelocation, Optional<Float> optional) {
      return optional.map((ofloat) -> createFixedRangeEvent(resourcelocation, ofloat)).orElseGet(() -> createVariableRangeEvent(resourcelocation));
   }

   public static SoundEvent createVariableRangeEvent(ResourceLocation resourcelocation) {
      return new SoundEvent(resourcelocation, 16.0F, false);
   }

   public static SoundEvent createFixedRangeEvent(ResourceLocation resourcelocation, float f) {
      return new SoundEvent(resourcelocation, f, true);
   }

   private SoundEvent(ResourceLocation resourcelocation, float f, boolean flag) {
      this.location = resourcelocation;
      this.range = f;
      this.newSystem = flag;
   }

   public ResourceLocation getLocation() {
      return this.location;
   }

   public float getRange(float f) {
      if (this.newSystem) {
         return this.range;
      } else {
         return f > 1.0F ? 16.0F * f : 16.0F;
      }
   }

   private Optional<Float> fixedRange() {
      return this.newSystem ? Optional.of(this.range) : Optional.empty();
   }

   public void writeToNetwork(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeResourceLocation(this.location);
      friendlybytebuf.writeOptional(this.fixedRange(), FriendlyByteBuf::writeFloat);
   }

   public static SoundEvent readFromNetwork(FriendlyByteBuf friendlybytebuf) {
      ResourceLocation resourcelocation = friendlybytebuf.readResourceLocation();
      Optional<Float> optional = friendlybytebuf.readOptional(FriendlyByteBuf::readFloat);
      return create(resourcelocation, optional);
   }
}
