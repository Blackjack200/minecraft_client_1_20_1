package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;

public record SculkChargeParticleOptions(float roll) implements ParticleOptions {
   public static final Codec<SculkChargeParticleOptions> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Codec.FLOAT.fieldOf("roll").forGetter((sculkchargeparticleoptions) -> sculkchargeparticleoptions.roll)).apply(recordcodecbuilder_instance, SculkChargeParticleOptions::new));
   public static final ParticleOptions.Deserializer<SculkChargeParticleOptions> DESERIALIZER = new ParticleOptions.Deserializer<SculkChargeParticleOptions>() {
      public SculkChargeParticleOptions fromCommand(ParticleType<SculkChargeParticleOptions> particletype, StringReader stringreader) throws CommandSyntaxException {
         stringreader.expect(' ');
         float f = stringreader.readFloat();
         return new SculkChargeParticleOptions(f);
      }

      public SculkChargeParticleOptions fromNetwork(ParticleType<SculkChargeParticleOptions> particletype, FriendlyByteBuf friendlybytebuf) {
         return new SculkChargeParticleOptions(friendlybytebuf.readFloat());
      }
   };

   public ParticleType<SculkChargeParticleOptions> getType() {
      return ParticleTypes.SCULK_CHARGE;
   }

   public void writeToNetwork(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeFloat(this.roll);
   }

   public String writeToString() {
      return String.format(Locale.ROOT, "%s %.2f", BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()), this.roll);
   }
}
