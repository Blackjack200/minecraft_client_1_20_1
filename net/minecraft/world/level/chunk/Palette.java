package net.minecraft.world.level.chunk;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.IdMap;
import net.minecraft.network.FriendlyByteBuf;

public interface Palette<T> {
   int idFor(T object);

   boolean maybeHas(Predicate<T> predicate);

   T valueFor(int i);

   void read(FriendlyByteBuf friendlybytebuf);

   void write(FriendlyByteBuf friendlybytebuf);

   int getSerializedSize();

   int getSize();

   Palette<T> copy();

   public interface Factory {
      <A> Palette<A> create(int i, IdMap<A> idmap, PaletteResize<A> paletteresize, List<A> list);
   }
}
