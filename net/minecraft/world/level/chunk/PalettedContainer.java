package net.minecraft.world.level.chunk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;
import java.util.stream.LongStream;
import javax.annotation.Nullable;
import net.minecraft.core.IdMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.BitStorage;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.SimpleBitStorage;
import net.minecraft.util.ThreadingDetector;
import net.minecraft.util.ZeroBitStorage;

public class PalettedContainer<T> implements PaletteResize<T>, PalettedContainerRO<T> {
   private static final int MIN_PALETTE_BITS = 0;
   private final PaletteResize<T> dummyPaletteResize = (i, objectx) -> 0;
   private final IdMap<T> registry;
   private volatile PalettedContainer.Data<T> data;
   private final PalettedContainer.Strategy strategy;
   private final ThreadingDetector threadingDetector = new ThreadingDetector("PalettedContainer");

   public void acquire() {
      this.threadingDetector.checkAndLock();
   }

   public void release() {
      this.threadingDetector.checkAndUnlock();
   }

   public static <T> Codec<PalettedContainer<T>> codecRW(IdMap<T> idmap, Codec<T> codec, PalettedContainer.Strategy palettedcontainer_strategy, T object) {
      PalettedContainerRO.Unpacker<T, PalettedContainer<T>> palettedcontainerro_unpacker = PalettedContainer::unpack;
      return codec(idmap, codec, palettedcontainer_strategy, object, palettedcontainerro_unpacker);
   }

   public static <T> Codec<PalettedContainerRO<T>> codecRO(IdMap<T> idmap, Codec<T> codec, PalettedContainer.Strategy palettedcontainer_strategy, T object) {
      PalettedContainerRO.Unpacker<T, PalettedContainerRO<T>> palettedcontainerro_unpacker = (idmap1, palettedcontainer_strategy1, palettedcontainerro_packeddata) -> unpack(idmap1, palettedcontainer_strategy1, palettedcontainerro_packeddata).map((palettedcontainer) -> palettedcontainer);
      return codec(idmap, codec, palettedcontainer_strategy, object, palettedcontainerro_unpacker);
   }

   private static <T, C extends PalettedContainerRO<T>> Codec<C> codec(IdMap<T> idmap, Codec<T> codec, PalettedContainer.Strategy palettedcontainer_strategy, T object, PalettedContainerRO.Unpacker<T, C> palettedcontainerro_unpacker) {
      return RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(codec.mapResult(ExtraCodecs.orElsePartial(object)).listOf().fieldOf("palette").forGetter(PalettedContainerRO.PackedData::paletteEntries), Codec.LONG_STREAM.optionalFieldOf("data").forGetter(PalettedContainerRO.PackedData::storage)).apply(recordcodecbuilder_instance, PalettedContainerRO.PackedData::new)).comapFlatMap((palettedcontainerro_packeddata) -> palettedcontainerro_unpacker.read(idmap, palettedcontainer_strategy, palettedcontainerro_packeddata), (palettedcontainerro) -> palettedcontainerro.pack(idmap, palettedcontainer_strategy));
   }

   public PalettedContainer(IdMap<T> idmap, PalettedContainer.Strategy palettedcontainer_strategy, PalettedContainer.Configuration<T> palettedcontainer_configuration, BitStorage bitstorage, List<T> list) {
      this.registry = idmap;
      this.strategy = palettedcontainer_strategy;
      this.data = new PalettedContainer.Data<>(palettedcontainer_configuration, bitstorage, palettedcontainer_configuration.factory().create(palettedcontainer_configuration.bits(), idmap, this, list));
   }

   private PalettedContainer(IdMap<T> idmap, PalettedContainer.Strategy palettedcontainer_strategy, PalettedContainer.Data<T> palettedcontainer_data) {
      this.registry = idmap;
      this.strategy = palettedcontainer_strategy;
      this.data = palettedcontainer_data;
   }

   public PalettedContainer(IdMap<T> idmap, T object, PalettedContainer.Strategy palettedcontainer_strategy) {
      this.strategy = palettedcontainer_strategy;
      this.registry = idmap;
      this.data = this.createOrReuseData((PalettedContainer.Data<T>)null, 0);
      this.data.palette.idFor(object);
   }

   private PalettedContainer.Data<T> createOrReuseData(@Nullable PalettedContainer.Data<T> palettedcontainer_data, int i) {
      PalettedContainer.Configuration<T> palettedcontainer_configuration = this.strategy.getConfiguration(this.registry, i);
      return palettedcontainer_data != null && palettedcontainer_configuration.equals(palettedcontainer_data.configuration()) ? palettedcontainer_data : palettedcontainer_configuration.createData(this.registry, this, this.strategy.size());
   }

   public int onResize(int i, T object) {
      PalettedContainer.Data<T> palettedcontainer_data = this.data;
      PalettedContainer.Data<T> palettedcontainer_data1 = this.createOrReuseData(palettedcontainer_data, i);
      palettedcontainer_data1.copyFrom(palettedcontainer_data.palette, palettedcontainer_data.storage);
      this.data = palettedcontainer_data1;
      return palettedcontainer_data1.palette.idFor(object);
   }

   public T getAndSet(int i, int j, int k, T object) {
      this.acquire();

      Object var5;
      try {
         var5 = this.getAndSet(this.strategy.getIndex(i, j, k), object);
      } finally {
         this.release();
      }

      return (T)var5;
   }

   public T getAndSetUnchecked(int i, int j, int k, T object) {
      return this.getAndSet(this.strategy.getIndex(i, j, k), object);
   }

   private T getAndSet(int i, T object) {
      int j = this.data.palette.idFor(object);
      int k = this.data.storage.getAndSet(i, j);
      return this.data.palette.valueFor(k);
   }

   public void set(int i, int j, int k, T object) {
      this.acquire();

      try {
         this.set(this.strategy.getIndex(i, j, k), object);
      } finally {
         this.release();
      }

   }

   private void set(int i, T object) {
      int j = this.data.palette.idFor(object);
      this.data.storage.set(i, j);
   }

   public T get(int i, int j, int k) {
      return this.get(this.strategy.getIndex(i, j, k));
   }

   protected T get(int i) {
      PalettedContainer.Data<T> palettedcontainer_data = this.data;
      return palettedcontainer_data.palette.valueFor(palettedcontainer_data.storage.get(i));
   }

   public void getAll(Consumer<T> consumer) {
      Palette<T> palette = this.data.palette();
      IntSet intset = new IntArraySet();
      this.data.storage.getAll(intset::add);
      intset.forEach((i) -> consumer.accept(palette.valueFor(i)));
   }

   public void read(FriendlyByteBuf friendlybytebuf) {
      this.acquire();

      try {
         int i = friendlybytebuf.readByte();
         PalettedContainer.Data<T> palettedcontainer_data = this.createOrReuseData(this.data, i);
         palettedcontainer_data.palette.read(friendlybytebuf);
         friendlybytebuf.readLongArray(palettedcontainer_data.storage.getRaw());
         this.data = palettedcontainer_data;
      } finally {
         this.release();
      }

   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      this.acquire();

      try {
         this.data.write(friendlybytebuf);
      } finally {
         this.release();
      }

   }

   private static <T> DataResult<PalettedContainer<T>> unpack(IdMap<T> idmap1, PalettedContainer.Strategy palettedcontainer_strategy1, PalettedContainerRO.PackedData<T> palettedcontainerro_packeddata) {
      List<T> list = palettedcontainerro_packeddata.paletteEntries();
      int i = palettedcontainer_strategy1.size();
      int j = palettedcontainer_strategy1.calculateBitsForSerialization(idmap1, list.size());
      PalettedContainer.Configuration<T> palettedcontainer_configuration = palettedcontainer_strategy1.getConfiguration(idmap1, j);
      BitStorage bitstorage;
      if (j == 0) {
         bitstorage = new ZeroBitStorage(i);
      } else {
         Optional<LongStream> optional = palettedcontainerro_packeddata.storage();
         if (optional.isEmpty()) {
            return DataResult.error(() -> "Missing values for non-zero storage");
         }

         long[] along = optional.get().toArray();

         try {
            if (palettedcontainer_configuration.factory() == PalettedContainer.Strategy.GLOBAL_PALETTE_FACTORY) {
               Palette<T> palette = new HashMapPalette<>(idmap1, j, (l, object1) -> 0, list);
               SimpleBitStorage simplebitstorage = new SimpleBitStorage(j, i, along);
               int[] aint = new int[i];
               simplebitstorage.unpack(aint);
               swapPalette(aint, (k) -> idmap1.getId(palette.valueFor(k)));
               bitstorage = new SimpleBitStorage(palettedcontainer_configuration.bits(), i, aint);
            } else {
               bitstorage = new SimpleBitStorage(palettedcontainer_configuration.bits(), i, along);
            }
         } catch (SimpleBitStorage.InitializationException var13) {
            return DataResult.error(() -> "Failed to read PalettedContainer: " + var13.getMessage());
         }
      }

      return DataResult.success(new PalettedContainer<>(idmap1, palettedcontainer_strategy1, palettedcontainer_configuration, bitstorage, list));
   }

   public PalettedContainerRO.PackedData<T> pack(IdMap<T> idmap, PalettedContainer.Strategy palettedcontainer_strategy) {
      this.acquire();

      PalettedContainerRO.PackedData var12;
      try {
         HashMapPalette<T> hashmappalette = new HashMapPalette<>(idmap, this.data.storage.getBits(), this.dummyPaletteResize);
         int i = palettedcontainer_strategy.size();
         int[] aint = new int[i];
         this.data.storage.unpack(aint);
         swapPalette(aint, (k) -> hashmappalette.idFor(this.data.palette.valueFor(k)));
         int j = palettedcontainer_strategy.calculateBitsForSerialization(idmap, hashmappalette.getSize());
         Optional<LongStream> optional;
         if (j != 0) {
            SimpleBitStorage simplebitstorage = new SimpleBitStorage(j, i, aint);
            optional = Optional.of(Arrays.stream(simplebitstorage.getRaw()));
         } else {
            optional = Optional.empty();
         }

         var12 = new PalettedContainerRO.PackedData<>(hashmappalette.getEntries(), optional);
      } finally {
         this.release();
      }

      return var12;
   }

   private static <T> void swapPalette(int[] aint, IntUnaryOperator intunaryoperator) {
      int i = -1;
      int j = -1;

      for(int k = 0; k < aint.length; ++k) {
         int l = aint[k];
         if (l != i) {
            i = l;
            j = intunaryoperator.applyAsInt(l);
         }

         aint[k] = j;
      }

   }

   public int getSerializedSize() {
      return this.data.getSerializedSize();
   }

   public boolean maybeHas(Predicate<T> predicate) {
      return this.data.palette.maybeHas(predicate);
   }

   public PalettedContainer<T> copy() {
      return new PalettedContainer<>(this.registry, this.strategy, this.data.copy());
   }

   public PalettedContainer<T> recreate() {
      return new PalettedContainer<>(this.registry, this.data.palette.valueFor(0), this.strategy);
   }

   public void count(PalettedContainer.CountConsumer<T> palettedcontainer_countconsumer) {
      if (this.data.palette.getSize() == 1) {
         palettedcontainer_countconsumer.accept(this.data.palette.valueFor(0), this.data.storage.getSize());
      } else {
         Int2IntOpenHashMap int2intopenhashmap = new Int2IntOpenHashMap();
         this.data.storage.getAll((i) -> int2intopenhashmap.addTo(i, 1));
         int2intopenhashmap.int2IntEntrySet().forEach((int2intmap_entry) -> palettedcontainer_countconsumer.accept(this.data.palette.valueFor(int2intmap_entry.getIntKey()), int2intmap_entry.getIntValue()));
      }
   }

   static record Configuration<T>(Palette.Factory factory, int bits) {
      public PalettedContainer.Data<T> createData(IdMap<T> idmap, PaletteResize<T> paletteresize, int i) {
         BitStorage bitstorage = (BitStorage)(this.bits == 0 ? new ZeroBitStorage(i) : new SimpleBitStorage(this.bits, i));
         Palette<T> palette = this.factory.create(this.bits, idmap, paletteresize, List.of());
         return new PalettedContainer.Data<>(this, bitstorage, palette);
      }
   }

   @FunctionalInterface
   public interface CountConsumer<T> {
      void accept(T object, int i);
   }

   static record Data<T>(PalettedContainer.Configuration<T> configuration, BitStorage storage, Palette<T> palette) {
      final BitStorage storage;
      final Palette<T> palette;

      public void copyFrom(Palette<T> palette, BitStorage bitstorage) {
         for(int i = 0; i < bitstorage.getSize(); ++i) {
            T object = palette.valueFor(bitstorage.get(i));
            this.storage.set(i, this.palette.idFor(object));
         }

      }

      public int getSerializedSize() {
         return 1 + this.palette.getSerializedSize() + FriendlyByteBuf.getVarIntSize(this.storage.getSize()) + this.storage.getRaw().length * 8;
      }

      public void write(FriendlyByteBuf friendlybytebuf) {
         friendlybytebuf.writeByte(this.storage.getBits());
         this.palette.write(friendlybytebuf);
         friendlybytebuf.writeLongArray(this.storage.getRaw());
      }

      public PalettedContainer.Data<T> copy() {
         return new PalettedContainer.Data<>(this.configuration, this.storage.copy(), this.palette.copy());
      }
   }

   public abstract static class Strategy {
      public static final Palette.Factory SINGLE_VALUE_PALETTE_FACTORY = SingleValuePalette::create;
      public static final Palette.Factory LINEAR_PALETTE_FACTORY = LinearPalette::create;
      public static final Palette.Factory HASHMAP_PALETTE_FACTORY = HashMapPalette::create;
      static final Palette.Factory GLOBAL_PALETTE_FACTORY = GlobalPalette::create;
      public static final PalettedContainer.Strategy SECTION_STATES = new PalettedContainer.Strategy(4) {
         public <A> PalettedContainer.Configuration<A> getConfiguration(IdMap<A> idmap, int i) {
            PalettedContainer.Configuration var10000;
            switch (i) {
               case 0:
                  var10000 = new PalettedContainer.Configuration(SINGLE_VALUE_PALETTE_FACTORY, i);
                  break;
               case 1:
               case 2:
               case 3:
               case 4:
                  var10000 = new PalettedContainer.Configuration(LINEAR_PALETTE_FACTORY, 4);
                  break;
               case 5:
               case 6:
               case 7:
               case 8:
                  var10000 = new PalettedContainer.Configuration(HASHMAP_PALETTE_FACTORY, i);
                  break;
               default:
                  var10000 = new PalettedContainer.Configuration(PalettedContainer.Strategy.GLOBAL_PALETTE_FACTORY, Mth.ceillog2(idmap.size()));
            }

            return var10000;
         }
      };
      public static final PalettedContainer.Strategy SECTION_BIOMES = new PalettedContainer.Strategy(2) {
         public <A> PalettedContainer.Configuration<A> getConfiguration(IdMap<A> idmap, int i) {
            PalettedContainer.Configuration var10000;
            switch (i) {
               case 0:
                  var10000 = new PalettedContainer.Configuration(SINGLE_VALUE_PALETTE_FACTORY, i);
                  break;
               case 1:
               case 2:
               case 3:
                  var10000 = new PalettedContainer.Configuration(LINEAR_PALETTE_FACTORY, i);
                  break;
               default:
                  var10000 = new PalettedContainer.Configuration(PalettedContainer.Strategy.GLOBAL_PALETTE_FACTORY, Mth.ceillog2(idmap.size()));
            }

            return var10000;
         }
      };
      private final int sizeBits;

      Strategy(int i) {
         this.sizeBits = i;
      }

      public int size() {
         return 1 << this.sizeBits * 3;
      }

      public int getIndex(int i, int j, int k) {
         return (j << this.sizeBits | k) << this.sizeBits | i;
      }

      public abstract <A> PalettedContainer.Configuration<A> getConfiguration(IdMap<A> idmap, int i);

      <A> int calculateBitsForSerialization(IdMap<A> idmap, int i) {
         int j = Mth.ceillog2(i);
         PalettedContainer.Configuration<A> palettedcontainer_configuration = this.getConfiguration(idmap, j);
         return palettedcontainer_configuration.factory() == GLOBAL_PALETTE_FACTORY ? j : palettedcontainer_configuration.bits();
      }
   }
}
