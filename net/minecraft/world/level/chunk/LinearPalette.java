package net.minecraft.world.level.chunk;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.IdMap;
import net.minecraft.network.FriendlyByteBuf;
import org.apache.commons.lang3.Validate;

public class LinearPalette<T> implements Palette<T> {
   private final IdMap<T> registry;
   private final T[] values;
   private final PaletteResize<T> resizeHandler;
   private final int bits;
   private int size;

   private LinearPalette(IdMap<T> idmap, int i, PaletteResize<T> paletteresize, List<T> list) {
      this.registry = idmap;
      this.values = (T[])(new Object[1 << i]);
      this.bits = i;
      this.resizeHandler = paletteresize;
      Validate.isTrue(list.size() <= this.values.length, "Can't initialize LinearPalette of size %d with %d entries", this.values.length, list.size());

      for(int j = 0; j < list.size(); ++j) {
         this.values[j] = list.get(j);
      }

      this.size = list.size();
   }

   private LinearPalette(IdMap<T> idmap, T[] aobject, PaletteResize<T> paletteresize, int i, int j) {
      this.registry = idmap;
      this.values = aobject;
      this.resizeHandler = paletteresize;
      this.bits = i;
      this.size = j;
   }

   public static <A> Palette<A> create(int i, IdMap<A> idmap, PaletteResize<A> paletteresize, List<A> list) {
      return new LinearPalette<>(idmap, i, paletteresize, list);
   }

   public int idFor(T object) {
      for(int i = 0; i < this.size; ++i) {
         if (this.values[i] == object) {
            return i;
         }
      }

      int j = this.size;
      if (j < this.values.length) {
         this.values[j] = object;
         ++this.size;
         return j;
      } else {
         return this.resizeHandler.onResize(this.bits + 1, object);
      }
   }

   public boolean maybeHas(Predicate<T> predicate) {
      for(int i = 0; i < this.size; ++i) {
         if (predicate.test(this.values[i])) {
            return true;
         }
      }

      return false;
   }

   public T valueFor(int i) {
      if (i >= 0 && i < this.size) {
         return this.values[i];
      } else {
         throw new MissingPaletteEntryException(i);
      }
   }

   public void read(FriendlyByteBuf friendlybytebuf) {
      this.size = friendlybytebuf.readVarInt();

      for(int i = 0; i < this.size; ++i) {
         this.values[i] = this.registry.byIdOrThrow(friendlybytebuf.readVarInt());
      }

   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeVarInt(this.size);

      for(int i = 0; i < this.size; ++i) {
         friendlybytebuf.writeVarInt(this.registry.getId(this.values[i]));
      }

   }

   public int getSerializedSize() {
      int i = FriendlyByteBuf.getVarIntSize(this.getSize());

      for(int j = 0; j < this.getSize(); ++j) {
         i += FriendlyByteBuf.getVarIntSize(this.registry.getId(this.values[j]));
      }

      return i;
   }

   public int getSize() {
      return this.size;
   }

   public Palette<T> copy() {
      return new LinearPalette<>(this.registry, (T[])((Object[])this.values.clone()), this.resizeHandler, this.bits, this.size);
   }
}
