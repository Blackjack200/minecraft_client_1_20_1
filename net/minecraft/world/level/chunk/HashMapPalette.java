package net.minecraft.world.level.chunk;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.IdMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;

public class HashMapPalette<T> implements Palette<T> {
   private final IdMap<T> registry;
   private final CrudeIncrementalIntIdentityHashBiMap<T> values;
   private final PaletteResize<T> resizeHandler;
   private final int bits;

   public HashMapPalette(IdMap<T> idmap, int i, PaletteResize<T> paletteresize, List<T> list) {
      this(idmap, i, paletteresize);
      list.forEach(this.values::add);
   }

   public HashMapPalette(IdMap<T> idmap, int i, PaletteResize<T> paletteresize) {
      this(idmap, i, paletteresize, CrudeIncrementalIntIdentityHashBiMap.create(1 << i));
   }

   private HashMapPalette(IdMap<T> idmap, int i, PaletteResize<T> paletteresize, CrudeIncrementalIntIdentityHashBiMap<T> crudeincrementalintidentityhashbimap) {
      this.registry = idmap;
      this.bits = i;
      this.resizeHandler = paletteresize;
      this.values = crudeincrementalintidentityhashbimap;
   }

   public static <A> Palette<A> create(int i, IdMap<A> idmap, PaletteResize<A> paletteresize, List<A> list) {
      return new HashMapPalette<>(idmap, i, paletteresize, list);
   }

   public int idFor(T object) {
      int i = this.values.getId(object);
      if (i == -1) {
         i = this.values.add(object);
         if (i >= 1 << this.bits) {
            i = this.resizeHandler.onResize(this.bits + 1, object);
         }
      }

      return i;
   }

   public boolean maybeHas(Predicate<T> predicate) {
      for(int i = 0; i < this.getSize(); ++i) {
         if (predicate.test(this.values.byId(i))) {
            return true;
         }
      }

      return false;
   }

   public T valueFor(int i) {
      T object = this.values.byId(i);
      if (object == null) {
         throw new MissingPaletteEntryException(i);
      } else {
         return object;
      }
   }

   public void read(FriendlyByteBuf friendlybytebuf) {
      this.values.clear();
      int i = friendlybytebuf.readVarInt();

      for(int j = 0; j < i; ++j) {
         this.values.add(this.registry.byIdOrThrow(friendlybytebuf.readVarInt()));
      }

   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      int i = this.getSize();
      friendlybytebuf.writeVarInt(i);

      for(int j = 0; j < i; ++j) {
         friendlybytebuf.writeVarInt(this.registry.getId(this.values.byId(j)));
      }

   }

   public int getSerializedSize() {
      int i = FriendlyByteBuf.getVarIntSize(this.getSize());

      for(int j = 0; j < this.getSize(); ++j) {
         i += FriendlyByteBuf.getVarIntSize(this.registry.getId(this.values.byId(j)));
      }

      return i;
   }

   public List<T> getEntries() {
      ArrayList<T> arraylist = new ArrayList<>();
      this.values.iterator().forEachRemaining(arraylist::add);
      return arraylist;
   }

   public int getSize() {
      return this.values.size();
   }

   public Palette<T> copy() {
      return new HashMapPalette<>(this.registry, this.bits, this.resizeHandler, this.values.copy());
   }
}
