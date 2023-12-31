package net.minecraft.world.level.gameevent;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public interface PositionSourceType<T extends PositionSource> {
   PositionSourceType<BlockPositionSource> BLOCK = register("block", new BlockPositionSource.Type());
   PositionSourceType<EntityPositionSource> ENTITY = register("entity", new EntityPositionSource.Type());

   T read(FriendlyByteBuf friendlybytebuf);

   void write(FriendlyByteBuf friendlybytebuf, T positionsource);

   Codec<T> codec();

   static <S extends PositionSourceType<T>, T extends PositionSource> S register(String s, S positionsourcetype) {
      return Registry.register(BuiltInRegistries.POSITION_SOURCE_TYPE, s, positionsourcetype);
   }

   static PositionSource fromNetwork(FriendlyByteBuf friendlybytebuf) {
      ResourceLocation resourcelocation = friendlybytebuf.readResourceLocation();
      return BuiltInRegistries.POSITION_SOURCE_TYPE.getOptional(resourcelocation).orElseThrow(() -> new IllegalArgumentException("Unknown position source type " + resourcelocation)).read(friendlybytebuf);
   }

   static <T extends PositionSource> void toNetwork(T positionsource, FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeResourceLocation(BuiltInRegistries.POSITION_SOURCE_TYPE.getKey(positionsource.getType()));
      positionsource.getType().write(friendlybytebuf, positionsource);
   }
}
