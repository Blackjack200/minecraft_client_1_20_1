package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.network.FriendlyByteBuf;

public interface ParticleOptions {
   ParticleType<?> getType();

   void writeToNetwork(FriendlyByteBuf friendlybytebuf);

   String writeToString();

   /** @deprecated */
   @Deprecated
   public interface Deserializer<T extends ParticleOptions> {
      T fromCommand(ParticleType<T> particletype, StringReader stringreader) throws CommandSyntaxException;

      T fromNetwork(ParticleType<T> particletype, FriendlyByteBuf friendlybytebuf);
   }
}
