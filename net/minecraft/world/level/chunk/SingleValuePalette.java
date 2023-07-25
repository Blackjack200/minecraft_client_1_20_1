package net.minecraft.world.level.chunk;

import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.IdMap;
import net.minecraft.network.FriendlyByteBuf;
import org.apache.commons.lang3.Validate;

public class SingleValuePalette<T> implements Palette<T> {
   private final IdMap<T> registry;
   @Nullable
   private T value;
   private final PaletteResize<T> resizeHandler;

   public SingleValuePalette(IdMap<T> idmap, PaletteResize<T> paletteresize, List<T> list) {
      this.registry = idmap;
      this.resizeHandler = paletteresize;
      if (list.size() > 0) {
         Validate.isTrue(list.size() <= 1, "Can't initialize SingleValuePalette with %d values.", (long)list.size());
         this.value = list.get(0);
      }

   }

   public static <A> Palette<A> create(int i, IdMap<A> idmap, PaletteResize<A> paletteresize, List<A> list) {
      return new SingleValuePalette<>(idmap, paletteresize, list);
   }

   public int idFor(T object) {
      if (this.value != null && this.value != object) {
         return this.resizeHandler.onResize(1, object);
      } else {
         this.value = object;
         return 0;
      }
   }

   public boolean maybeHas(Predicate<T> predicate) {
      if (this.value == null) {
         throw new IllegalStateException("Use of an uninitialized palette");
      } else {
         return predicate.test(this.value);
      }
   }

   public T valueFor(int i) {
      if (this.value != null && i == 0) {
         return this.value;
      } else {
         throw new IllegalStateException("Missing Palette entry for id " + i + ".");
      }
   }

   public void read(FriendlyByteBuf friendlybytebuf) {
      this.value = this.registry.byIdOrThrow(friendlybytebuf.readVarInt());
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      if (this.value == null) {
         throw new IllegalStateException("Use of an uninitialized palette");
      } else {
         friendlybytebuf.writeVarInt(this.registry.getId(this.value));
      }
   }

   public int getSerializedSize() {
      if (this.value == null) {
         throw new IllegalStateException("Use of an uninitialized palette");
      } else {
         return FriendlyByteBuf.getVarIntSize(this.registry.getId(this.value));
      }
   }

   public int getSize() {
      return 1;
   }

   public Palette<T> copy() {
      if (this.value == null) {
         throw new IllegalStateException("Use of an uninitialized palette");
      } else {
         return this;
      }
   }
}
