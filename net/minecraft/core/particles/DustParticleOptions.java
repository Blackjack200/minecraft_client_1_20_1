package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class DustParticleOptions extends DustParticleOptionsBase {
   public static final Vector3f REDSTONE_PARTICLE_COLOR = Vec3.fromRGB24(16711680).toVector3f();
   public static final DustParticleOptions REDSTONE = new DustParticleOptions(REDSTONE_PARTICLE_COLOR, 1.0F);
   public static final Codec<DustParticleOptions> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(ExtraCodecs.VECTOR3F.fieldOf("color").forGetter((dustparticleoptions1) -> dustparticleoptions1.color), Codec.FLOAT.fieldOf("scale").forGetter((dustparticleoptions) -> dustparticleoptions.scale)).apply(recordcodecbuilder_instance, DustParticleOptions::new));
   public static final ParticleOptions.Deserializer<DustParticleOptions> DESERIALIZER = new ParticleOptions.Deserializer<DustParticleOptions>() {
      public DustParticleOptions fromCommand(ParticleType<DustParticleOptions> particletype, StringReader stringreader) throws CommandSyntaxException {
         Vector3f vector3f = DustParticleOptionsBase.readVector3f(stringreader);
         stringreader.expect(' ');
         float f = stringreader.readFloat();
         return new DustParticleOptions(vector3f, f);
      }

      public DustParticleOptions fromNetwork(ParticleType<DustParticleOptions> particletype, FriendlyByteBuf friendlybytebuf) {
         return new DustParticleOptions(DustParticleOptionsBase.readVector3f(friendlybytebuf), friendlybytebuf.readFloat());
      }
   };

   public DustParticleOptions(Vector3f vector3f, float f) {
      super(vector3f, f);
   }

   public ParticleType<DustParticleOptions> getType() {
      return ParticleTypes.DUST;
   }
}
