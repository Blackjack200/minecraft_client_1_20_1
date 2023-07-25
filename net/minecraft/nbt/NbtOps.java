package net.minecraft.nbt;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import javax.annotation.Nullable;

public class NbtOps implements DynamicOps<Tag> {
   public static final NbtOps INSTANCE = new NbtOps();
   private static final String WRAPPER_MARKER = "";

   protected NbtOps() {
   }

   public Tag empty() {
      return EndTag.INSTANCE;
   }

   public <U> U convertTo(DynamicOps<U> dynamicops, Tag tag) {
      switch (tag.getId()) {
         case 0:
            return dynamicops.empty();
         case 1:
            return dynamicops.createByte(((NumericTag)tag).getAsByte());
         case 2:
            return dynamicops.createShort(((NumericTag)tag).getAsShort());
         case 3:
            return dynamicops.createInt(((NumericTag)tag).getAsInt());
         case 4:
            return dynamicops.createLong(((NumericTag)tag).getAsLong());
         case 5:
            return dynamicops.createFloat(((NumericTag)tag).getAsFloat());
         case 6:
            return dynamicops.createDouble(((NumericTag)tag).getAsDouble());
         case 7:
            return dynamicops.createByteList(ByteBuffer.wrap(((ByteArrayTag)tag).getAsByteArray()));
         case 8:
            return dynamicops.createString(tag.getAsString());
         case 9:
            return this.convertList(dynamicops, tag);
         case 10:
            return this.convertMap(dynamicops, tag);
         case 11:
            return dynamicops.createIntList(Arrays.stream(((IntArrayTag)tag).getAsIntArray()));
         case 12:
            return dynamicops.createLongList(Arrays.stream(((LongArrayTag)tag).getAsLongArray()));
         default:
            throw new IllegalStateException("Unknown tag type: " + tag);
      }
   }

   public DataResult<Number> getNumberValue(Tag tag) {
      if (tag instanceof NumericTag numerictag) {
         return DataResult.success(numerictag.getAsNumber());
      } else {
         return DataResult.error(() -> "Not a number");
      }
   }

   public Tag createNumeric(Number number) {
      return DoubleTag.valueOf(number.doubleValue());
   }

   public Tag createByte(byte b0) {
      return ByteTag.valueOf(b0);
   }

   public Tag createShort(short short0) {
      return ShortTag.valueOf(short0);
   }

   public Tag createInt(int i) {
      return IntTag.valueOf(i);
   }

   public Tag createLong(long i) {
      return LongTag.valueOf(i);
   }

   public Tag createFloat(float f) {
      return FloatTag.valueOf(f);
   }

   public Tag createDouble(double d0) {
      return DoubleTag.valueOf(d0);
   }

   public Tag createBoolean(boolean flag) {
      return ByteTag.valueOf(flag);
   }

   public DataResult<String> getStringValue(Tag tag) {
      if (tag instanceof StringTag stringtag) {
         return DataResult.success(stringtag.getAsString());
      } else {
         return DataResult.error(() -> "Not a string");
      }
   }

   public Tag createString(String s) {
      return StringTag.valueOf(s);
   }

   public DataResult<Tag> mergeToList(Tag tag, Tag tag1) {
      return createCollector(tag).map((nbtops_listcollector) -> DataResult.success(nbtops_listcollector.accept(tag1).result())).orElseGet(() -> DataResult.error(() -> "mergeToList called with not a list: " + tag, tag));
   }

   public DataResult<Tag> mergeToList(Tag tag, List<Tag> list) {
      return createCollector(tag).map((nbtops_listcollector) -> DataResult.success(nbtops_listcollector.acceptAll(list).result())).orElseGet(() -> DataResult.error(() -> "mergeToList called with not a list: " + tag, tag));
   }

   public DataResult<Tag> mergeToMap(Tag tag, Tag tag1, Tag tag2) {
      if (!(tag instanceof CompoundTag) && !(tag instanceof EndTag)) {
         return DataResult.error(() -> "mergeToMap called with not a map: " + tag, tag);
      } else if (!(tag1 instanceof StringTag)) {
         return DataResult.error(() -> "key is not a string: " + tag1, tag);
      } else {
         CompoundTag compoundtag = new CompoundTag();
         if (tag instanceof CompoundTag) {
            CompoundTag compoundtag1 = (CompoundTag)tag;
            compoundtag1.getAllKeys().forEach((s) -> compoundtag.put(s, compoundtag1.get(s)));
         }

         compoundtag.put(tag1.getAsString(), tag2);
         return DataResult.success(compoundtag);
      }
   }

   public DataResult<Tag> mergeToMap(Tag tag, MapLike<Tag> maplike) {
      if (!(tag instanceof CompoundTag) && !(tag instanceof EndTag)) {
         return DataResult.error(() -> "mergeToMap called with not a map: " + tag, tag);
      } else {
         CompoundTag compoundtag = new CompoundTag();
         if (tag instanceof CompoundTag) {
            CompoundTag compoundtag1 = (CompoundTag)tag;
            compoundtag1.getAllKeys().forEach((s) -> compoundtag.put(s, compoundtag1.get(s)));
         }

         List<Tag> list = Lists.newArrayList();
         maplike.entries().forEach((pair) -> {
            Tag tag1 = pair.getFirst();
            if (!(tag1 instanceof StringTag)) {
               list.add(tag1);
            } else {
               compoundtag.put(tag1.getAsString(), pair.getSecond());
            }
         });
         return !list.isEmpty() ? DataResult.error(() -> "some keys are not strings: " + list, compoundtag) : DataResult.success(compoundtag);
      }
   }

   public DataResult<Stream<Pair<Tag, Tag>>> getMapValues(Tag tag) {
      if (tag instanceof CompoundTag compoundtag) {
         return DataResult.success(compoundtag.getAllKeys().stream().map((s) -> Pair.of(this.createString(s), compoundtag.get(s))));
      } else {
         return DataResult.error(() -> "Not a map: " + tag);
      }
   }

   public DataResult<Consumer<BiConsumer<Tag, Tag>>> getMapEntries(Tag tag) {
      if (tag instanceof CompoundTag compoundtag) {
         return DataResult.success((biconsumer) -> compoundtag.getAllKeys().forEach((s) -> biconsumer.accept(this.createString(s), compoundtag.get(s))));
      } else {
         return DataResult.error(() -> "Not a map: " + tag);
      }
   }

   public DataResult<MapLike<Tag>> getMap(Tag tag) {
      if (tag instanceof final CompoundTag compoundtag) {
         return DataResult.success(new MapLike<Tag>() {
            @Nullable
            public Tag get(Tag tag) {
               return compoundtag.get(tag.getAsString());
            }

            @Nullable
            public Tag get(String s) {
               return compoundtag.get(s);
            }

            public Stream<Pair<Tag, Tag>> entries() {
               return compoundtag.getAllKeys().stream().map((s) -> Pair.of(NbtOps.this.createString(s), compoundtag.get(s)));
            }

            public String toString() {
               return "MapLike[" + compoundtag + "]";
            }
         });
      } else {
         return DataResult.error(() -> "Not a map: " + tag);
      }
   }

   public Tag createMap(Stream<Pair<Tag, Tag>> stream) {
      CompoundTag compoundtag = new CompoundTag();
      stream.forEach((pair) -> compoundtag.put(pair.getFirst().getAsString(), pair.getSecond()));
      return compoundtag;
   }

   private static Tag tryUnwrap(CompoundTag compoundtag) {
      if (compoundtag.size() == 1) {
         Tag tag = compoundtag.get("");
         if (tag != null) {
            return tag;
         }
      }

      return compoundtag;
   }

   public DataResult<Stream<Tag>> getStream(Tag tag) {
      if (tag instanceof ListTag listtag) {
         return listtag.getElementType() == 10 ? DataResult.success(listtag.stream().map((tag2) -> tryUnwrap((CompoundTag)tag2))) : DataResult.success(listtag.stream());
      } else if (tag instanceof CollectionTag<?> collectiontag) {
         return DataResult.success(collectiontag.stream().map((tag1) -> tag1));
      } else {
         return DataResult.error(() -> "Not a list");
      }
   }

   public DataResult<Consumer<Consumer<Tag>>> getList(Tag tag) {
      if (tag instanceof ListTag listtag) {
         return listtag.getElementType() == 10 ? DataResult.success((consumer) -> listtag.forEach((tag2) -> consumer.accept(tryUnwrap((CompoundTag)tag2)))) : DataResult.success(listtag::forEach);
      } else if (tag instanceof CollectionTag<?> collectiontag) {
         return DataResult.success(collectiontag::forEach);
      } else {
         return DataResult.error(() -> "Not a list: " + tag);
      }
   }

   public DataResult<ByteBuffer> getByteBuffer(Tag tag) {
      if (tag instanceof ByteArrayTag bytearraytag) {
         return DataResult.success(ByteBuffer.wrap(bytearraytag.getAsByteArray()));
      } else {
         return DynamicOps.super.getByteBuffer(tag);
      }
   }

   public Tag createByteList(ByteBuffer bytebuffer) {
      return new ByteArrayTag(DataFixUtils.toArray(bytebuffer));
   }

   public DataResult<IntStream> getIntStream(Tag tag) {
      if (tag instanceof IntArrayTag intarraytag) {
         return DataResult.success(Arrays.stream(intarraytag.getAsIntArray()));
      } else {
         return DynamicOps.super.getIntStream(tag);
      }
   }

   public Tag createIntList(IntStream intstream) {
      return new IntArrayTag(intstream.toArray());
   }

   public DataResult<LongStream> getLongStream(Tag tag) {
      if (tag instanceof LongArrayTag longarraytag) {
         return DataResult.success(Arrays.stream(longarraytag.getAsLongArray()));
      } else {
         return DynamicOps.super.getLongStream(tag);
      }
   }

   public Tag createLongList(LongStream longstream) {
      return new LongArrayTag(longstream.toArray());
   }

   public Tag createList(Stream<Tag> stream) {
      return NbtOps.InitialListCollector.INSTANCE.acceptAll(stream).result();
   }

   public Tag remove(Tag tag, String s) {
      if (tag instanceof CompoundTag compoundtag) {
         CompoundTag compoundtag1 = new CompoundTag();
         compoundtag.getAllKeys().stream().filter((s3) -> !Objects.equals(s3, s)).forEach((s1) -> compoundtag1.put(s1, compoundtag.get(s1)));
         return compoundtag1;
      } else {
         return tag;
      }
   }

   public String toString() {
      return "NBT";
   }

   public RecordBuilder<Tag> mapBuilder() {
      return new NbtOps.NbtRecordBuilder();
   }

   private static Optional<NbtOps.ListCollector> createCollector(Tag tag) {
      if (tag instanceof EndTag) {
         return Optional.of(NbtOps.InitialListCollector.INSTANCE);
      } else {
         if (tag instanceof CollectionTag) {
            CollectionTag<?> collectiontag = (CollectionTag)tag;
            if (collectiontag.isEmpty()) {
               return Optional.of(NbtOps.InitialListCollector.INSTANCE);
            }

            if (collectiontag instanceof ListTag) {
               ListTag listtag = (ListTag)collectiontag;
               Optional var10000;
               switch (listtag.getElementType()) {
                  case 0:
                     var10000 = Optional.of(NbtOps.InitialListCollector.INSTANCE);
                     break;
                  case 10:
                     var10000 = Optional.of(new NbtOps.HeterogenousListCollector(listtag));
                     break;
                  default:
                     var10000 = Optional.of(new NbtOps.HomogenousListCollector(listtag));
               }

               return var10000;
            }

            if (collectiontag instanceof ByteArrayTag) {
               ByteArrayTag bytearraytag = (ByteArrayTag)collectiontag;
               return Optional.of(new NbtOps.ByteListCollector(bytearraytag.getAsByteArray()));
            }

            if (collectiontag instanceof IntArrayTag) {
               IntArrayTag intarraytag = (IntArrayTag)collectiontag;
               return Optional.of(new NbtOps.IntListCollector(intarraytag.getAsIntArray()));
            }

            if (collectiontag instanceof LongArrayTag) {
               LongArrayTag longarraytag = (LongArrayTag)collectiontag;
               return Optional.of(new NbtOps.LongListCollector(longarraytag.getAsLongArray()));
            }
         }

         return Optional.empty();
      }
   }

   static class ByteListCollector implements NbtOps.ListCollector {
      private final ByteArrayList values = new ByteArrayList();

      public ByteListCollector(byte b0) {
         this.values.add(b0);
      }

      public ByteListCollector(byte[] abyte) {
         this.values.addElements(0, abyte);
      }

      public NbtOps.ListCollector accept(Tag tag) {
         if (tag instanceof ByteTag bytetag) {
            this.values.add(bytetag.getAsByte());
            return this;
         } else {
            return (new NbtOps.HeterogenousListCollector(this.values)).accept(tag);
         }
      }

      public Tag result() {
         return new ByteArrayTag(this.values.toByteArray());
      }
   }

   static class HeterogenousListCollector implements NbtOps.ListCollector {
      private final ListTag result = new ListTag();

      public HeterogenousListCollector() {
      }

      public HeterogenousListCollector(Collection<Tag> collection) {
         this.result.addAll(collection);
      }

      public HeterogenousListCollector(IntArrayList intarraylist) {
         intarraylist.forEach((i) -> this.result.add(wrapElement(IntTag.valueOf(i))));
      }

      public HeterogenousListCollector(ByteArrayList bytearraylist) {
         bytearraylist.forEach((b0) -> this.result.add(wrapElement(ByteTag.valueOf(b0))));
      }

      public HeterogenousListCollector(LongArrayList longarraylist) {
         longarraylist.forEach((i) -> this.result.add(wrapElement(LongTag.valueOf(i))));
      }

      private static boolean isWrapper(CompoundTag compoundtag) {
         return compoundtag.size() == 1 && compoundtag.contains("");
      }

      private static Tag wrapIfNeeded(Tag tag) {
         if (tag instanceof CompoundTag compoundtag) {
            if (!isWrapper(compoundtag)) {
               return compoundtag;
            }
         }

         return wrapElement(tag);
      }

      private static CompoundTag wrapElement(Tag tag) {
         CompoundTag compoundtag = new CompoundTag();
         compoundtag.put("", tag);
         return compoundtag;
      }

      public NbtOps.ListCollector accept(Tag tag) {
         this.result.add(wrapIfNeeded(tag));
         return this;
      }

      public Tag result() {
         return this.result;
      }
   }

   static class HomogenousListCollector implements NbtOps.ListCollector {
      private final ListTag result = new ListTag();

      HomogenousListCollector(Tag tag) {
         this.result.add(tag);
      }

      HomogenousListCollector(ListTag listtag) {
         this.result.addAll(listtag);
      }

      public NbtOps.ListCollector accept(Tag tag) {
         if (tag.getId() != this.result.getElementType()) {
            return (new NbtOps.HeterogenousListCollector()).acceptAll(this.result).accept(tag);
         } else {
            this.result.add(tag);
            return this;
         }
      }

      public Tag result() {
         return this.result;
      }
   }

   static class InitialListCollector implements NbtOps.ListCollector {
      public static final NbtOps.InitialListCollector INSTANCE = new NbtOps.InitialListCollector();

      private InitialListCollector() {
      }

      public NbtOps.ListCollector accept(Tag tag) {
         if (tag instanceof CompoundTag compoundtag) {
            return (new NbtOps.HeterogenousListCollector()).accept(compoundtag);
         } else if (tag instanceof ByteTag bytetag) {
            return new NbtOps.ByteListCollector(bytetag.getAsByte());
         } else if (tag instanceof IntTag inttag) {
            return new NbtOps.IntListCollector(inttag.getAsInt());
         } else if (tag instanceof LongTag longtag) {
            return new NbtOps.LongListCollector(longtag.getAsLong());
         } else {
            return new NbtOps.HomogenousListCollector(tag);
         }
      }

      public Tag result() {
         return new ListTag();
      }
   }

   static class IntListCollector implements NbtOps.ListCollector {
      private final IntArrayList values = new IntArrayList();

      public IntListCollector(int i) {
         this.values.add(i);
      }

      public IntListCollector(int[] aint) {
         this.values.addElements(0, aint);
      }

      public NbtOps.ListCollector accept(Tag tag) {
         if (tag instanceof IntTag inttag) {
            this.values.add(inttag.getAsInt());
            return this;
         } else {
            return (new NbtOps.HeterogenousListCollector(this.values)).accept(tag);
         }
      }

      public Tag result() {
         return new IntArrayTag(this.values.toIntArray());
      }
   }

   interface ListCollector {
      NbtOps.ListCollector accept(Tag tag);

      default NbtOps.ListCollector acceptAll(Iterable<Tag> iterable) {
         NbtOps.ListCollector nbtops_listcollector = this;

         for(Tag tag : iterable) {
            nbtops_listcollector = nbtops_listcollector.accept(tag);
         }

         return nbtops_listcollector;
      }

      default NbtOps.ListCollector acceptAll(Stream<Tag> stream) {
         return this.acceptAll(stream::iterator);
      }

      Tag result();
   }

   static class LongListCollector implements NbtOps.ListCollector {
      private final LongArrayList values = new LongArrayList();

      public LongListCollector(long i) {
         this.values.add(i);
      }

      public LongListCollector(long[] along) {
         this.values.addElements(0, along);
      }

      public NbtOps.ListCollector accept(Tag tag) {
         if (tag instanceof LongTag longtag) {
            this.values.add(longtag.getAsLong());
            return this;
         } else {
            return (new NbtOps.HeterogenousListCollector(this.values)).accept(tag);
         }
      }

      public Tag result() {
         return new LongArrayTag(this.values.toLongArray());
      }
   }

   class NbtRecordBuilder extends RecordBuilder.AbstractStringBuilder<Tag, CompoundTag> {
      protected NbtRecordBuilder() {
         super(NbtOps.this);
      }

      protected CompoundTag initBuilder() {
         return new CompoundTag();
      }

      protected CompoundTag append(String s, Tag tag, CompoundTag compoundtag) {
         compoundtag.put(s, tag);
         return compoundtag;
      }

      protected DataResult<Tag> build(CompoundTag compoundtag, Tag tag) {
         if (tag != null && tag != EndTag.INSTANCE) {
            if (!(tag instanceof CompoundTag)) {
               return DataResult.error(() -> "mergeToMap called with not a map: " + tag, tag);
            } else {
               CompoundTag compoundtag1 = (CompoundTag)tag;
               CompoundTag compoundtag2 = new CompoundTag(Maps.newHashMap(compoundtag1.entries()));

               for(Map.Entry<String, Tag> map_entry : compoundtag.entries().entrySet()) {
                  compoundtag2.put(map_entry.getKey(), map_entry.getValue());
               }

               return DataResult.success(compoundtag2);
            }
         } else {
            return DataResult.success(compoundtag);
         }
      }
   }
}
