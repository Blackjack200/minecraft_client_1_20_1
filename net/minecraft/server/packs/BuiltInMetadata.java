package net.minecraft.server.packs;

import java.util.Map;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;

public class BuiltInMetadata {
   private static final BuiltInMetadata EMPTY = new BuiltInMetadata(Map.of());
   private final Map<MetadataSectionSerializer<?>, ?> values;

   private BuiltInMetadata(Map<MetadataSectionSerializer<?>, ?> map) {
      this.values = map;
   }

   public <T> T get(MetadataSectionSerializer<T> metadatasectionserializer) {
      return (T)this.values.get(metadatasectionserializer);
   }

   public static BuiltInMetadata of() {
      return EMPTY;
   }

   public static <T> BuiltInMetadata of(MetadataSectionSerializer<T> metadatasectionserializer, T object) {
      return new BuiltInMetadata(Map.of(metadatasectionserializer, object));
   }

   public static <T1, T2> BuiltInMetadata of(MetadataSectionSerializer<T1> metadatasectionserializer, T1 object, MetadataSectionSerializer<T2> metadatasectionserializer1, T2 object1) {
      return new BuiltInMetadata(Map.of(metadatasectionserializer, object, metadatasectionserializer1, (T1)object1));
   }
}
