package net.minecraft.world.level.chunk;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.IdMap;
import net.minecraft.network.FriendlyByteBuf;

public class GlobalPalette<T> implements Palette<T> {
   private final IdMap<T> registry;

   public GlobalPalette(IdMap<T> idmap) {
      this.registry = idmap;
   }

   public static <A> Palette<A> create(int i, IdMap<A> idmap, PaletteResize<A> paletteresize, List<A> list) {
      return new GlobalPalette<>(idmap);
   }

   public int idFor(T object) {
      int i = this.registry.getId(object);
      return i == -1 ? 0 : i;
   }

   public boolean maybeHas(Predicate<T> predicate) {
      return true;
   }

   public T valueFor(int i) {
      T object = this.registry.byId(i);
      if (object == null) {
         throw new MissingPaletteEntryException(i);
      } else {
         return object;
      }
   }

   public void read(FriendlyByteBuf friendlybytebuf) {
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
   }

   public int getSerializedSize() {
      return FriendlyByteBuf.getVarIntSize(0);
   }

   public int getSize() {
      return this.registry.size();
   }

   public Palette<T> copy() {
      return this;
   }
}
