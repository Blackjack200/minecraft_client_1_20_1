package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;

public class SimpleParticleType extends ParticleType<SimpleParticleType> implements ParticleOptions {
   private static final ParticleOptions.Deserializer<SimpleParticleType> DESERIALIZER = new ParticleOptions.Deserializer<SimpleParticleType>() {
      public SimpleParticleType fromCommand(ParticleType<SimpleParticleType> particletype, StringReader stringreader) {
         return (SimpleParticleType)particletype;
      }

      public SimpleParticleType fromNetwork(ParticleType<SimpleParticleType> particletype, FriendlyByteBuf friendlybytebuf) {
         return (SimpleParticleType)particletype;
      }
   };
   private final Codec<SimpleParticleType> codec = Codec.unit(this::getType);

   protected SimpleParticleType(boolean flag) {
      super(flag, DESERIALIZER);
   }

   public SimpleParticleType getType() {
      return this;
   }

   public Codec<SimpleParticleType> codec() {
      return this.codec;
   }

   public void writeToNetwork(FriendlyByteBuf friendlybytebuf) {
   }

   public String writeToString() {
      return BuiltInRegistries.PARTICLE_TYPE.getKey(this).toString();
   }
}
