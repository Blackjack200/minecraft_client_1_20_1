package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.PositionSourceType;
import net.minecraft.world.phys.Vec3;

public class VibrationParticleOption implements ParticleOptions {
   public static final Codec<VibrationParticleOption> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(PositionSource.CODEC.fieldOf("destination").forGetter((vibrationparticleoption1) -> vibrationparticleoption1.destination), Codec.INT.fieldOf("arrival_in_ticks").forGetter((vibrationparticleoption) -> vibrationparticleoption.arrivalInTicks)).apply(recordcodecbuilder_instance, VibrationParticleOption::new));
   public static final ParticleOptions.Deserializer<VibrationParticleOption> DESERIALIZER = new ParticleOptions.Deserializer<VibrationParticleOption>() {
      public VibrationParticleOption fromCommand(ParticleType<VibrationParticleOption> particletype, StringReader stringreader) throws CommandSyntaxException {
         stringreader.expect(' ');
         float f = (float)stringreader.readDouble();
         stringreader.expect(' ');
         float f1 = (float)stringreader.readDouble();
         stringreader.expect(' ');
         float f2 = (float)stringreader.readDouble();
         stringreader.expect(' ');
         int i = stringreader.readInt();
         BlockPos blockpos = BlockPos.containing((double)f, (double)f1, (double)f2);
         return new VibrationParticleOption(new BlockPositionSource(blockpos), i);
      }

      public VibrationParticleOption fromNetwork(ParticleType<VibrationParticleOption> particletype, FriendlyByteBuf friendlybytebuf) {
         PositionSource positionsource = PositionSourceType.fromNetwork(friendlybytebuf);
         int i = friendlybytebuf.readVarInt();
         return new VibrationParticleOption(positionsource, i);
      }
   };
   private final PositionSource destination;
   private final int arrivalInTicks;

   public VibrationParticleOption(PositionSource positionsource, int i) {
      this.destination = positionsource;
      this.arrivalInTicks = i;
   }

   public void writeToNetwork(FriendlyByteBuf friendlybytebuf) {
      PositionSourceType.toNetwork(this.destination, friendlybytebuf);
      friendlybytebuf.writeVarInt(this.arrivalInTicks);
   }

   public String writeToString() {
      Vec3 vec3 = this.destination.getPosition((Level)null).get();
      double d0 = vec3.x();
      double d1 = vec3.y();
      double d2 = vec3.z();
      return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %d", BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()), d0, d1, d2, this.arrivalInTicks);
   }

   public ParticleType<VibrationParticleOption> getType() {
      return ParticleTypes.VIBRATION;
   }

   public PositionSource getDestination() {
      return this.destination;
   }

   public int getArrivalInTicks() {
      return this.arrivalInTicks;
   }
}
