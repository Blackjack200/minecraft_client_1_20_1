package net.minecraft.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.HolderSet;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.mutable.MutableObject;
import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class ExtraCodecs {
   public static final Codec<JsonElement> JSON = Codec.PASSTHROUGH.xmap((dynamic) -> dynamic.convert(JsonOps.INSTANCE).getValue(), (jsonelement) -> new Dynamic<>(JsonOps.INSTANCE, jsonelement));
   public static final Codec<Component> COMPONENT = JSON.flatXmap((jsonelement) -> {
      try {
         return DataResult.success(Component.Serializer.fromJson(jsonelement));
      } catch (JsonParseException var2) {
         return DataResult.error(var2::getMessage);
      }
   }, (component) -> {
      try {
         return DataResult.success(Component.Serializer.toJsonTree(component));
      } catch (IllegalArgumentException var2) {
         return DataResult.error(var2::getMessage);
      }
   });
   public static final Codec<Component> FLAT_COMPONENT = Codec.STRING.flatXmap((s) -> {
      try {
         return DataResult.success(Component.Serializer.fromJson(s));
      } catch (JsonParseException var2) {
         return DataResult.error(var2::getMessage);
      }
   }, (component) -> {
      try {
         return DataResult.success(Component.Serializer.toJson(component));
      } catch (IllegalArgumentException var2) {
         return DataResult.error(var2::getMessage);
      }
   });
   public static final Codec<Vector3f> VECTOR3F = Codec.FLOAT.listOf().comapFlatMap((list) -> Util.fixedSize(list, 3).map((list1) -> new Vector3f(list1.get(0), list1.get(1), list1.get(2))), (vector3f) -> List.of(vector3f.x(), vector3f.y(), vector3f.z()));
   public static final Codec<Quaternionf> QUATERNIONF_COMPONENTS = Codec.FLOAT.listOf().comapFlatMap((list) -> Util.fixedSize(list, 4).map((list1) -> new Quaternionf(list1.get(0), list1.get(1), list1.get(2), list1.get(3))), (quaternionf) -> List.of(quaternionf.x, quaternionf.y, quaternionf.z, quaternionf.w));
   public static final Codec<AxisAngle4f> AXISANGLE4F = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Codec.FLOAT.fieldOf("angle").forGetter((axisangle4f1) -> axisangle4f1.angle), VECTOR3F.fieldOf("axis").forGetter((axisangle4f) -> new Vector3f(axisangle4f.x, axisangle4f.y, axisangle4f.z))).apply(recordcodecbuilder_instance, AxisAngle4f::new));
   public static final Codec<Quaternionf> QUATERNIONF = Codec.either(QUATERNIONF_COMPONENTS, AXISANGLE4F.xmap(Quaternionf::new, AxisAngle4f::new)).xmap((either) -> either.map((quaternionf1) -> quaternionf1, (quaternionf) -> quaternionf), Either::left);
   public static Codec<Matrix4f> MATRIX4F = Codec.FLOAT.listOf().comapFlatMap((list) -> Util.fixedSize(list, 16).map((list1) -> {
         Matrix4f matrix4f = new Matrix4f();

         for(int i = 0; i < list1.size(); ++i) {
            matrix4f.setRowColumn(i >> 2, i & 3, list1.get(i));
         }

         return matrix4f.determineProperties();
      }), (matrix4f) -> {
      FloatList floatlist = new FloatArrayList(16);

      for(int i = 0; i < 16; ++i) {
         floatlist.add(matrix4f.getRowColumn(i >> 2, i & 3));
      }

      return floatlist;
   });
   public static final Codec<Integer> NON_NEGATIVE_INT = intRangeWithMessage(0, Integer.MAX_VALUE, (integer) -> "Value must be non-negative: " + integer);
   public static final Codec<Integer> POSITIVE_INT = intRangeWithMessage(1, Integer.MAX_VALUE, (integer) -> "Value must be positive: " + integer);
   public static final Codec<Float> POSITIVE_FLOAT = floatRangeMinExclusiveWithMessage(0.0F, Float.MAX_VALUE, (ofloat) -> "Value must be positive: " + ofloat);
   public static final Codec<Pattern> PATTERN = Codec.STRING.comapFlatMap((s) -> {
      try {
         return DataResult.success(Pattern.compile(s));
      } catch (PatternSyntaxException var2) {
         return DataResult.error(() -> "Invalid regex pattern '" + s + "': " + var2.getMessage());
      }
   }, Pattern::pattern);
   public static final Codec<Instant> INSTANT_ISO8601 = instantCodec(DateTimeFormatter.ISO_INSTANT);
   public static final Codec<byte[]> BASE64_STRING = Codec.STRING.comapFlatMap((s) -> {
      try {
         return DataResult.success(Base64.getDecoder().decode(s));
      } catch (IllegalArgumentException var2) {
         return DataResult.error(() -> "Malformed base64 string");
      }
   }, (abyte) -> Base64.getEncoder().encodeToString(abyte));
   public static final Codec<ExtraCodecs.TagOrElementLocation> TAG_OR_ELEMENT_ID = Codec.STRING.comapFlatMap((s) -> s.startsWith("#") ? ResourceLocation.read(s.substring(1)).map((resourcelocation1) -> new ExtraCodecs.TagOrElementLocation(resourcelocation1, true)) : ResourceLocation.read(s).map((resourcelocation) -> new ExtraCodecs.TagOrElementLocation(resourcelocation, false)), ExtraCodecs.TagOrElementLocation::decoratedId);
   public static final Function<Optional<Long>, OptionalLong> toOptionalLong = (optional) -> optional.map(OptionalLong::of).orElseGet(OptionalLong::empty);
   public static final Function<OptionalLong, Optional<Long>> fromOptionalLong = (optionallong) -> optionallong.isPresent() ? Optional.of(optionallong.getAsLong()) : Optional.empty();
   public static final Codec<BitSet> BIT_SET = Codec.LONG_STREAM.xmap((longstream) -> BitSet.valueOf(longstream.toArray()), (bitset) -> Arrays.stream(bitset.toLongArray()));
   private static final Codec<Property> PROPERTY = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Codec.STRING.fieldOf("name").forGetter(Property::getName), Codec.STRING.fieldOf("value").forGetter(Property::getValue), Codec.STRING.optionalFieldOf("signature").forGetter((property) -> Optional.ofNullable(property.getSignature()))).apply(recordcodecbuilder_instance, (s, s1, optional) -> new Property(s, s1, optional.orElse((String)null))));
   @VisibleForTesting
   public static final Codec<PropertyMap> PROPERTY_MAP = Codec.either(Codec.unboundedMap(Codec.STRING, Codec.STRING.listOf()), PROPERTY.listOf()).xmap((either) -> {
      PropertyMap propertymap = new PropertyMap();
      either.ifLeft((map) -> map.forEach((s, list1) -> {
            for(String s1 : list1) {
               propertymap.put(s, new Property(s, s1));
            }

         })).ifRight((list) -> {
         for(Property property : list) {
            propertymap.put(property.getName(), property);
         }

      });
      return propertymap;
   }, (propertymap) -> Either.right(propertymap.values().stream().toList()));
   public static final Codec<GameProfile> GAME_PROFILE = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Codec.mapPair(UUIDUtil.AUTHLIB_CODEC.xmap(Optional::of, (optional1) -> optional1.orElse((UUID)null)).optionalFieldOf("id", Optional.empty()), Codec.STRING.xmap(Optional::of, (optional) -> optional.orElse((String)null)).optionalFieldOf("name", Optional.empty())).flatXmap(ExtraCodecs::mapIdNameToGameProfile, ExtraCodecs::mapGameProfileToIdName).forGetter(Function.identity()), PROPERTY_MAP.optionalFieldOf("properties", new PropertyMap()).forGetter(GameProfile::getProperties)).apply(recordcodecbuilder_instance, (gameprofile, propertymap) -> {
         propertymap.forEach((s, property) -> gameprofile.getProperties().put(s, property));
         return gameprofile;
      }));
   public static final Codec<String> NON_EMPTY_STRING = validate(Codec.STRING, (s) -> s.isEmpty() ? DataResult.error(() -> "Expected non-empty string") : DataResult.success(s));
   public static final Codec<Integer> CODEPOINT = Codec.STRING.comapFlatMap((s) -> {
      int[] aint = s.codePoints().toArray();
      return aint.length != 1 ? DataResult.error(() -> "Expected one codepoint, got: " + s) : DataResult.success(aint[0]);
   }, Character::toString);

   public static <F, S> Codec<Either<F, S>> xor(Codec<F> codec, Codec<S> codec1) {
      return new ExtraCodecs.XorCodec<>(codec, codec1);
   }

   public static <P, I> Codec<I> intervalCodec(Codec<P> codec, String s, String s1, BiFunction<P, P, DataResult<I>> bifunction, Function<I, P> function, Function<I, P> function1) {
      Codec<I> codec1 = Codec.list(codec).comapFlatMap((list) -> Util.fixedSize(list, 2).flatMap((list1) -> {
            P object8 = list1.get(0);
            P object9 = list1.get(1);
            return bifunction.apply(object8, object9);
         }), (object7) -> ImmutableList.of(function.apply(object7), function1.apply(object7)));
      Codec<I> codec2 = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(codec.fieldOf(s).forGetter(Pair::getFirst), codec.fieldOf(s1).forGetter(Pair::getSecond)).apply(recordcodecbuilder_instance, Pair::of)).comapFlatMap((pair) -> bifunction.apply((P)pair.getFirst(), (P)pair.getSecond()), (object6) -> Pair.of(function.apply(object6), function1.apply(object6)));
      Codec<I> codec3 = (new ExtraCodecs.EitherCodec<>(codec1, codec2)).xmap((either1) -> either1.map((object5) -> object5, (object4) -> object4), Either::left);
      return Codec.either(codec, codec3).comapFlatMap((either) -> either.map((object3) -> bifunction.apply(object3, object3), DataResult::success), (object) -> {
         P object1 = function.apply(object);
         P object2 = function1.apply(object);
         return Objects.equals(object1, object2) ? Either.left(object1) : Either.right(object);
      });
   }

   public static <A> Codec.ResultFunction<A> orElsePartial(final A object) {
      return new Codec.ResultFunction<A>() {
         public <T> DataResult<Pair<A, T>> apply(DynamicOps<T> dynamicops, T objectx, DataResult<Pair<A, T>> dataresult) {
            MutableObject<String> mutableobject = new MutableObject<>();
            Optional<Pair<A, T>> optional = dataresult.resultOrPartial(mutableobject::setValue);
            return optional.isPresent() ? dataresult : DataResult.error(() -> "(" + (String)mutableobject.getValue() + " -> using default)", Pair.of(object, object));
         }

         public <T> DataResult<T> coApply(DynamicOps<T> dynamicops, A objectx, DataResult<T> dataresult) {
            return dataresult;
         }

         public String toString() {
            return "OrElsePartial[" + object + "]";
         }
      };
   }

   public static <E> Codec<E> idResolverCodec(ToIntFunction<E> tointfunction, IntFunction<E> intfunction, int i) {
      return Codec.INT.flatXmap((integer) -> Optional.ofNullable(intfunction.apply(integer)).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Unknown element id: " + integer)), (object) -> {
         int k = tointfunction.applyAsInt(object);
         return k == i ? DataResult.error(() -> "Element with unknown id: " + object) : DataResult.success(k);
      });
   }

   public static <E> Codec<E> stringResolverCodec(Function<E, String> function, Function<String, E> function1) {
      return Codec.STRING.flatXmap((s) -> Optional.ofNullable(function1.apply(s)).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Unknown element name:" + s)), (object) -> Optional.ofNullable(function.apply(object)).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Element with unknown name: " + object)));
   }

   public static <E> Codec<E> orCompressed(final Codec<E> codec, final Codec<E> codec1) {
      return new Codec<E>() {
         public <T> DataResult<T> encode(E object, DynamicOps<T> dynamicops, T object1) {
            return dynamicops.compressMaps() ? codec1.encode(object, dynamicops, object1) : codec.encode(object, dynamicops, object1);
         }

         public <T> DataResult<Pair<E, T>> decode(DynamicOps<T> dynamicops, T object) {
            return dynamicops.compressMaps() ? codec1.decode(dynamicops, object) : codec.decode(dynamicops, object);
         }

         public String toString() {
            return codec + " orCompressed " + codec1;
         }
      };
   }

   public static <E> Codec<E> overrideLifecycle(Codec<E> codec, final Function<E, Lifecycle> function, final Function<E, Lifecycle> function1) {
      return codec.mapResult(new Codec.ResultFunction<E>() {
         public <T> DataResult<Pair<E, T>> apply(DynamicOps<T> dynamicops, T object, DataResult<Pair<E, T>> dataresult) {
            return dataresult.result().map((pair) -> dataresult.setLifecycle(function.apply(pair.getFirst()))).orElse(dataresult);
         }

         public <T> DataResult<T> coApply(DynamicOps<T> dynamicops, E object, DataResult<T> dataresult) {
            return dataresult.setLifecycle(function1.apply(object));
         }

         public String toString() {
            return "WithLifecycle[" + function + " " + function1 + "]";
         }
      });
   }

   public static <T> Codec<T> validate(Codec<T> codec, Function<T, DataResult<T>> function) {
      return codec.flatXmap(function, function);
   }

   public static <T> MapCodec<T> validate(MapCodec<T> mapcodec, Function<T, DataResult<T>> function) {
      return mapcodec.flatXmap(function, function);
   }

   private static Codec<Integer> intRangeWithMessage(int i, int j, Function<Integer, String> function) {
      return validate(Codec.INT, (integer) -> integer.compareTo(i) >= 0 && integer.compareTo(j) <= 0 ? DataResult.success(integer) : DataResult.error(() -> function.apply(integer)));
   }

   public static Codec<Integer> intRange(int i, int j) {
      return intRangeWithMessage(i, j, (integer) -> "Value must be within range [" + i + ";" + j + "]: " + integer);
   }

   private static Codec<Float> floatRangeMinExclusiveWithMessage(float f, float f1, Function<Float, String> function) {
      return validate(Codec.FLOAT, (ofloat) -> ofloat.compareTo(f) > 0 && ofloat.compareTo(f1) <= 0 ? DataResult.success(ofloat) : DataResult.error(() -> function.apply(ofloat)));
   }

   public static <T> Codec<List<T>> nonEmptyList(Codec<List<T>> codec) {
      return validate(codec, (list) -> list.isEmpty() ? DataResult.error(() -> "List must have contents") : DataResult.success(list));
   }

   public static <T> Codec<HolderSet<T>> nonEmptyHolderSet(Codec<HolderSet<T>> codec) {
      return validate(codec, (holderset) -> holderset.unwrap().right().filter(List::isEmpty).isPresent() ? DataResult.error(() -> "List must have contents") : DataResult.success(holderset));
   }

   public static <A> Codec<A> lazyInitializedCodec(Supplier<Codec<A>> supplier) {
      return new ExtraCodecs.LazyInitializedCodec<>(supplier);
   }

   public static <E> MapCodec<E> retrieveContext(final Function<DynamicOps<?>, DataResult<E>> function) {
      class ContextRetrievalCodec extends MapCodec<E> {
         public <T> RecordBuilder<T> encode(E object, DynamicOps<T> dynamicops, RecordBuilder<T> recordbuilder) {
            return recordbuilder;
         }

         public <T> DataResult<E> decode(DynamicOps<T> dynamicops, MapLike<T> maplike) {
            return function.apply(dynamicops);
         }

         public String toString() {
            return "ContextRetrievalCodec[" + function + "]";
         }

         public <T> Stream<T> keys(DynamicOps<T> dynamicops) {
            return Stream.empty();
         }
      }

      return new ContextRetrievalCodec();
   }

   public static <E, L extends Collection<E>, T> Function<L, DataResult<L>> ensureHomogenous(Function<E, T> function) {
      return (collection) -> {
         Iterator<E> iterator = collection.iterator();
         if (iterator.hasNext()) {
            T object = function.apply(iterator.next());

            while(iterator.hasNext()) {
               E object1 = iterator.next();
               T object2 = function.apply(object1);
               if (object2 != object) {
                  return DataResult.error(() -> "Mixed type list: element " + object1 + " had type " + object2 + ", but list is of type " + object);
               }
            }
         }

         return DataResult.success(collection, Lifecycle.stable());
      };
   }

   public static <A> Codec<A> catchDecoderException(final Codec<A> codec) {
      return Codec.of(codec, new Decoder<A>() {
         public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> dynamicops, T object) {
            try {
               return codec.decode(dynamicops, object);
            } catch (Exception var4) {
               return DataResult.error(() -> "Caught exception decoding " + object + ": " + var4.getMessage());
            }
         }
      });
   }

   public static Codec<Instant> instantCodec(DateTimeFormatter datetimeformatter) {
      return Codec.STRING.comapFlatMap((s) -> {
         try {
            return DataResult.success(Instant.from(datetimeformatter.parse(s)));
         } catch (Exception var3) {
            return DataResult.error(var3::getMessage);
         }
      }, datetimeformatter::format);
   }

   public static MapCodec<OptionalLong> asOptionalLong(MapCodec<Optional<Long>> mapcodec) {
      return mapcodec.xmap(toOptionalLong, fromOptionalLong);
   }

   private static DataResult<GameProfile> mapIdNameToGameProfile(Pair<Optional<UUID>, Optional<String>> pair) {
      try {
         return DataResult.success(new GameProfile(pair.getFirst().orElse((UUID)null), pair.getSecond().orElse((String)null)));
      } catch (Throwable var2) {
         return DataResult.error(var2::getMessage);
      }
   }

   private static DataResult<Pair<Optional<UUID>, Optional<String>>> mapGameProfileToIdName(GameProfile gameprofile) {
      return DataResult.success(Pair.of(Optional.ofNullable(gameprofile.getId()), Optional.ofNullable(gameprofile.getName())));
   }

   public static Codec<String> sizeLimitedString(int i, int j) {
      return validate(Codec.STRING, (s) -> {
         int i1 = s.length();
         if (i1 < i) {
            return DataResult.error(() -> "String \"" + s + "\" is too short: " + i1 + ", expected range [" + i + "-" + j + "]");
         } else {
            return i1 > j ? DataResult.error(() -> "String \"" + s + "\" is too long: " + i1 + ", expected range [" + i + "-" + j + "]") : DataResult.success(s);
         }
      });
   }

   static final class EitherCodec<F, S> implements Codec<Either<F, S>> {
      private final Codec<F> first;
      private final Codec<S> second;

      public EitherCodec(Codec<F> codec, Codec<S> codec1) {
         this.first = codec;
         this.second = codec1;
      }

      public <T> DataResult<Pair<Either<F, S>, T>> decode(DynamicOps<T> dynamicops, T object) {
         DataResult<Pair<Either<F, S>, T>> dataresult = this.first.decode(dynamicops, object).map((pair3) -> pair3.mapFirst(Either::left));
         if (!dataresult.error().isPresent()) {
            return dataresult;
         } else {
            DataResult<Pair<Either<F, S>, T>> dataresult1 = this.second.decode(dynamicops, object).map((pair2) -> pair2.mapFirst(Either::right));
            return !dataresult1.error().isPresent() ? dataresult1 : dataresult.apply2((pair, pair1) -> pair1, dataresult1);
         }
      }

      public <T> DataResult<T> encode(Either<F, S> either, DynamicOps<T> dynamicops, T object) {
         return either.map((object4) -> this.first.encode(object4, dynamicops, object), (object2) -> this.second.encode(object2, dynamicops, object));
      }

      public boolean equals(Object object) {
         if (this == object) {
            return true;
         } else if (object != null && this.getClass() == object.getClass()) {
            ExtraCodecs.EitherCodec<?, ?> extracodecs_eithercodec = (ExtraCodecs.EitherCodec)object;
            return Objects.equals(this.first, extracodecs_eithercodec.first) && Objects.equals(this.second, extracodecs_eithercodec.second);
         } else {
            return false;
         }
      }

      public int hashCode() {
         return Objects.hash(this.first, this.second);
      }

      public String toString() {
         return "EitherCodec[" + this.first + ", " + this.second + "]";
      }
   }

   static record LazyInitializedCodec<A>(Supplier<Codec<A>> delegate) implements Codec<A> {
      LazyInitializedCodec {
         supplier = Suppliers.memoize(supplier::get);
      }

      public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> dynamicops, T object) {
         return this.delegate.get().decode(dynamicops, object);
      }

      public <T> DataResult<T> encode(A object, DynamicOps<T> dynamicops, T object1) {
         return this.delegate.get().encode(object, dynamicops, object1);
      }
   }

   public static record TagOrElementLocation(ResourceLocation id, boolean tag) {
      public String toString() {
         return this.decoratedId();
      }

      private String decoratedId() {
         return this.tag ? "#" + this.id : this.id.toString();
      }
   }

   static final class XorCodec<F, S> implements Codec<Either<F, S>> {
      private final Codec<F> first;
      private final Codec<S> second;

      public XorCodec(Codec<F> codec, Codec<S> codec1) {
         this.first = codec;
         this.second = codec1;
      }

      public <T> DataResult<Pair<Either<F, S>, T>> decode(DynamicOps<T> dynamicops, T object) {
         DataResult<Pair<Either<F, S>, T>> dataresult = this.first.decode(dynamicops, object).map((pair1) -> pair1.mapFirst(Either::left));
         DataResult<Pair<Either<F, S>, T>> dataresult1 = this.second.decode(dynamicops, object).map((pair) -> pair.mapFirst(Either::right));
         Optional<Pair<Either<F, S>, T>> optional = dataresult.result();
         Optional<Pair<Either<F, S>, T>> optional1 = dataresult1.result();
         if (optional.isPresent() && optional1.isPresent()) {
            return DataResult.error(() -> "Both alternatives read successfully, can not pick the correct one; first: " + optional.get() + " second: " + optional1.get(), optional.get());
         } else {
            return optional.isPresent() ? dataresult : dataresult1;
         }
      }

      public <T> DataResult<T> encode(Either<F, S> either, DynamicOps<T> dynamicops, T object) {
         return either.map((object4) -> this.first.encode(object4, dynamicops, object), (object2) -> this.second.encode(object2, dynamicops, object));
      }

      public boolean equals(Object object) {
         if (this == object) {
            return true;
         } else if (object != null && this.getClass() == object.getClass()) {
            ExtraCodecs.XorCodec<?, ?> extracodecs_xorcodec = (ExtraCodecs.XorCodec)object;
            return Objects.equals(this.first, extracodecs_xorcodec.first) && Objects.equals(this.second, extracodecs_xorcodec.second);
         } else {
            return false;
         }
      }

      public int hashCode() {
         return Objects.hash(this.first, this.second);
      }

      public String toString() {
         return "XorCodec[" + this.first + ", " + this.second + "]";
      }
   }
}
