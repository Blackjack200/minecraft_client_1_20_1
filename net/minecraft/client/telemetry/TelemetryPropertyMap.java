package net.minecraft.client.telemetry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;

public class TelemetryPropertyMap {
   final Map<TelemetryProperty<?>, Object> entries;

   TelemetryPropertyMap(Map<TelemetryProperty<?>, Object> map) {
      this.entries = map;
   }

   public static TelemetryPropertyMap.Builder builder() {
      return new TelemetryPropertyMap.Builder();
   }

   public static Codec<TelemetryPropertyMap> createCodec(final List<TelemetryProperty<?>> list) {
      return (new MapCodec<TelemetryPropertyMap>() {
         public <T> RecordBuilder<T> encode(TelemetryPropertyMap telemetrypropertymap, DynamicOps<T> dynamicops, RecordBuilder<T> recordbuilder) {
            RecordBuilder<T> recordbuilder1 = recordbuilder;

            for(TelemetryProperty<?> telemetryproperty : list) {
               recordbuilder1 = this.encodeProperty(telemetrypropertymap, recordbuilder1, telemetryproperty);
            }

            return recordbuilder1;
         }

         private <T, V> RecordBuilder<T> encodeProperty(TelemetryPropertyMap telemetrypropertymap, RecordBuilder<T> recordbuilder, TelemetryProperty<V> telemetryproperty) {
            V object = telemetrypropertymap.get(telemetryproperty);
            return object != null ? recordbuilder.add(telemetryproperty.id(), object, telemetryproperty.codec()) : recordbuilder;
         }

         public <T> DataResult<TelemetryPropertyMap> decode(DynamicOps<T> dynamicops, MapLike<T> maplike) {
            DataResult<TelemetryPropertyMap.Builder> dataresult = DataResult.success(new TelemetryPropertyMap.Builder());

            for(TelemetryProperty<?> telemetryproperty : list) {
               dataresult = this.decodeProperty(dataresult, dynamicops, maplike, telemetryproperty);
            }

            return dataresult.map(TelemetryPropertyMap.Builder::build);
         }

         private <T, V> DataResult<TelemetryPropertyMap.Builder> decodeProperty(DataResult<TelemetryPropertyMap.Builder> dataresult, DynamicOps<T> dynamicops, MapLike<T> maplike, TelemetryProperty<V> telemetryproperty) {
            T object = maplike.get(telemetryproperty.id());
            if (object != null) {
               DataResult<V> dataresult1 = telemetryproperty.codec().parse(dynamicops, object);
               return dataresult.apply2stable((telemetrypropertymap_builder, object1) -> telemetrypropertymap_builder.put(telemetryproperty, (T)object1), dataresult1);
            } else {
               return dataresult;
            }
         }

         public <T> Stream<T> keys(DynamicOps<T> dynamicops) {
            return list.stream().map(TelemetryProperty::id).map(dynamicops::createString);
         }
      }).codec();
   }

   @Nullable
   public <T> T get(TelemetryProperty<T> telemetryproperty) {
      return (T)this.entries.get(telemetryproperty);
   }

   public String toString() {
      return this.entries.toString();
   }

   public Set<TelemetryProperty<?>> propertySet() {
      return this.entries.keySet();
   }

   public static class Builder {
      private final Map<TelemetryProperty<?>, Object> entries = new Reference2ObjectOpenHashMap<>();

      Builder() {
      }

      public <T> TelemetryPropertyMap.Builder put(TelemetryProperty<T> telemetryproperty, T object) {
         this.entries.put(telemetryproperty, object);
         return this;
      }

      public <T> TelemetryPropertyMap.Builder putIfNotNull(TelemetryProperty<T> telemetryproperty, @Nullable T object) {
         if (object != null) {
            this.entries.put(telemetryproperty, object);
         }

         return this;
      }

      public TelemetryPropertyMap.Builder putAll(TelemetryPropertyMap telemetrypropertymap) {
         this.entries.putAll(telemetrypropertymap.entries);
         return this;
      }

      public TelemetryPropertyMap build() {
         return new TelemetryPropertyMap(this.entries);
      }
   }
}
