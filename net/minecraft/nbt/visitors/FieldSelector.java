package net.minecraft.nbt.visitors;

import java.util.List;
import net.minecraft.nbt.TagType;

public record FieldSelector(List<String> path, TagType<?> type, String name) {
   public FieldSelector(TagType<?> tagtype, String s) {
      this(List.of(), tagtype, s);
   }

   public FieldSelector(String s, TagType<?> tagtype, String s1) {
      this(List.of(s), tagtype, s1);
   }

   public FieldSelector(String s, String s1, TagType<?> tagtype, String s2) {
      this(List.of(s, s1), tagtype, s2);
   }
}
