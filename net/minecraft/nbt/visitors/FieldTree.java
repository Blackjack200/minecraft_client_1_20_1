package net.minecraft.nbt.visitors;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.nbt.TagType;

public record FieldTree(int depth, Map<String, TagType<?>> selectedFields, Map<String, FieldTree> fieldsToRecurse) {
   private FieldTree(int i) {
      this(i, new HashMap<>(), new HashMap<>());
   }

   public static FieldTree createRoot() {
      return new FieldTree(1);
   }

   public void addEntry(FieldSelector fieldselector) {
      if (this.depth <= fieldselector.path().size()) {
         this.fieldsToRecurse.computeIfAbsent(fieldselector.path().get(this.depth - 1), (s) -> new FieldTree(this.depth + 1)).addEntry(fieldselector);
      } else {
         this.selectedFields.put(fieldselector.name(), fieldselector.type());
      }

   }

   public boolean isSelected(TagType<?> tagtype, String s) {
      return tagtype.equals(this.selectedFields().get(s));
   }
}
