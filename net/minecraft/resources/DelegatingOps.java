package net.minecraft.resources;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.ListBuilder;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public abstract class DelegatingOps<T> implements DynamicOps<T> {
   protected final DynamicOps<T> delegate;

   protected DelegatingOps(DynamicOps<T> dynamicops) {
      this.delegate = dynamicops;
   }

   public T empty() {
      return this.delegate.empty();
   }

   public <U> U convertTo(DynamicOps<U> dynamicops, T object) {
      return this.delegate.convertTo(dynamicops, object);
   }

   public DataResult<Number> getNumberValue(T object) {
      return this.delegate.getNumberValue(object);
   }

   public T createNumeric(Number number) {
      return this.delegate.createNumeric(number);
   }

   public T createByte(byte b0) {
      return this.delegate.createByte(b0);
   }

   public T createShort(short short0) {
      return this.delegate.createShort(short0);
   }

   public T createInt(int i) {
      return this.delegate.createInt(i);
   }

   public T createLong(long i) {
      return this.delegate.createLong(i);
   }

   public T createFloat(float f) {
      return this.delegate.createFloat(f);
   }

   public T createDouble(double d0) {
      return this.delegate.createDouble(d0);
   }

   public DataResult<Boolean> getBooleanValue(T object) {
      return this.delegate.getBooleanValue(object);
   }

   public T createBoolean(boolean flag) {
      return this.delegate.createBoolean(flag);
   }

   public DataResult<String> getStringValue(T object) {
      return this.delegate.getStringValue(object);
   }

   public T createString(String s) {
      return this.delegate.createString(s);
   }

   public DataResult<T> mergeToList(T object, T object1) {
      return this.delegate.mergeToList(object, object1);
   }

   public DataResult<T> mergeToList(T object, List<T> list) {
      return this.delegate.mergeToList(object, list);
   }

   public DataResult<T> mergeToMap(T object, T object1, T object2) {
      return this.delegate.mergeToMap(object, object1, object2);
   }

   public DataResult<T> mergeToMap(T object, MapLike<T> maplike) {
      return this.delegate.mergeToMap(object, maplike);
   }

   public DataResult<Stream<Pair<T, T>>> getMapValues(T object) {
      return this.delegate.getMapValues(object);
   }

   public DataResult<Consumer<BiConsumer<T, T>>> getMapEntries(T object) {
      return this.delegate.getMapEntries(object);
   }

   public T createMap(Stream<Pair<T, T>> stream) {
      return this.delegate.createMap(stream);
   }

   public DataResult<MapLike<T>> getMap(T object) {
      return this.delegate.getMap(object);
   }

   public DataResult<Stream<T>> getStream(T object) {
      return this.delegate.getStream(object);
   }

   public DataResult<Consumer<Consumer<T>>> getList(T object) {
      return this.delegate.getList(object);
   }

   public T createList(Stream<T> stream) {
      return this.delegate.createList(stream);
   }

   public DataResult<ByteBuffer> getByteBuffer(T object) {
      return this.delegate.getByteBuffer(object);
   }

   public T createByteList(ByteBuffer bytebuffer) {
      return this.delegate.createByteList(bytebuffer);
   }

   public DataResult<IntStream> getIntStream(T object) {
      return this.delegate.getIntStream(object);
   }

   public T createIntList(IntStream intstream) {
      return this.delegate.createIntList(intstream);
   }

   public DataResult<LongStream> getLongStream(T object) {
      return this.delegate.getLongStream(object);
   }

   public T createLongList(LongStream longstream) {
      return this.delegate.createLongList(longstream);
   }

   public T remove(T object, String s) {
      return this.delegate.remove(object, s);
   }

   public boolean compressMaps() {
      return this.delegate.compressMaps();
   }

   public ListBuilder<T> listBuilder() {
      return new ListBuilder.Builder<>(this);
   }

   public RecordBuilder<T> mapBuilder() {
      return new RecordBuilder.MapBuilder<>(this);
   }
}
