package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;

public class ShriekParticleOption implements ParticleOptions {
   public static final Codec<ShriekParticleOption> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Codec.INT.fieldOf("delay").forGetter((shriekparticleoption) -> shriekparticleoption.delay)).apply(recordcodecbuilder_instance, ShriekParticleOption::new));
   public static final ParticleOptions.Deserializer<ShriekParticleOption> DESERIALIZER = new ParticleOptions.Deserializer<ShriekParticleOption>() {
      public ShriekParticleOption fromCommand(ParticleType<ShriekParticleOption> particletype, StringReader stringreader) throws CommandSyntaxException {
         stringreader.expect(' ');
         int i = stringreader.readInt();
         return new ShriekParticleOption(i);
      }

      public ShriekParticleOption fromNetwork(ParticleType<ShriekParticleOption> particletype, FriendlyByteBuf friendlybytebuf) {
         return new ShriekParticleOption(friendlybytebuf.readVarInt());
      }
   };
   private final int delay;

   public ShriekParticleOption(int i) {
      this.delay = i;
   }

   public void writeToNetwork(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeVarInt(this.delay);
   }

   public String writeToString() {
      return String.format(Locale.ROOT, "%s %d", BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()), this.delay);
   }

   public ParticleType<ShriekParticleOption> getType() {
      return ParticleTypes.SHRIEK;
   }

   public int getDelay() {
      return this.delay;
   }
}
