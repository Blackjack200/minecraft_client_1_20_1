package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class DustColorTransitionOptions extends DustParticleOptionsBase {
   public static final Vector3f SCULK_PARTICLE_COLOR = Vec3.fromRGB24(3790560).toVector3f();
   public static final DustColorTransitionOptions SCULK_TO_REDSTONE = new DustColorTransitionOptions(SCULK_PARTICLE_COLOR, DustParticleOptions.REDSTONE_PARTICLE_COLOR, 1.0F);
   public static final Codec<DustColorTransitionOptions> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(ExtraCodecs.VECTOR3F.fieldOf("fromColor").forGetter((dustcolortransitionoptions2) -> dustcolortransitionoptions2.color), ExtraCodecs.VECTOR3F.fieldOf("toColor").forGetter((dustcolortransitionoptions1) -> dustcolortransitionoptions1.toColor), Codec.FLOAT.fieldOf("scale").forGetter((dustcolortransitionoptions) -> dustcolortransitionoptions.scale)).apply(recordcodecbuilder_instance, DustColorTransitionOptions::new));
   public static final ParticleOptions.Deserializer<DustColorTransitionOptions> DESERIALIZER = new ParticleOptions.Deserializer<DustColorTransitionOptions>() {
      public DustColorTransitionOptions fromCommand(ParticleType<DustColorTransitionOptions> particletype, StringReader stringreader) throws CommandSyntaxException {
         Vector3f vector3f = DustParticleOptionsBase.readVector3f(stringreader);
         stringreader.expect(' ');
         float f = stringreader.readFloat();
         Vector3f vector3f1 = DustParticleOptionsBase.readVector3f(stringreader);
         return new DustColorTransitionOptions(vector3f, vector3f1, f);
      }

      public DustColorTransitionOptions fromNetwork(ParticleType<DustColorTransitionOptions> particletype, FriendlyByteBuf friendlybytebuf) {
         Vector3f vector3f = DustParticleOptionsBase.readVector3f(friendlybytebuf);
         float f = friendlybytebuf.readFloat();
         Vector3f vector3f1 = DustParticleOptionsBase.readVector3f(friendlybytebuf);
         return new DustColorTransitionOptions(vector3f, vector3f1, f);
      }
   };
   private final Vector3f toColor;

   public DustColorTransitionOptions(Vector3f vector3f, Vector3f vector3f1, float f) {
      super(vector3f, f);
      this.toColor = vector3f1;
   }

   public Vector3f getFromColor() {
      return this.color;
   }

   public Vector3f getToColor() {
      return this.toColor;
   }

   public void writeToNetwork(FriendlyByteBuf friendlybytebuf) {
      super.writeToNetwork(friendlybytebuf);
      friendlybytebuf.writeFloat(this.toColor.x());
      friendlybytebuf.writeFloat(this.toColor.y());
      friendlybytebuf.writeFloat(this.toColor.z());
   }

   public String writeToString() {
      return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %.2f %.2f %.2f %.2f", BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()), this.color.x(), this.color.y(), this.color.z(), this.scale, this.toColor.x(), this.toColor.y(), this.toColor.z());
   }

   public ParticleType<DustColorTransitionOptions> getType() {
      return ParticleTypes.DUST_COLOR_TRANSITION;
   }
}
