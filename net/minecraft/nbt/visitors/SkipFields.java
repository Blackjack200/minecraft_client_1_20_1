package net.minecraft.nbt.visitors;

import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.TagType;

public class SkipFields extends CollectToTag {
   private final Deque<FieldTree> stack = new ArrayDeque<>();

   public SkipFields(FieldSelector... afieldselector) {
      FieldTree fieldtree = FieldTree.createRoot();

      for(FieldSelector fieldselector : afieldselector) {
         fieldtree.addEntry(fieldselector);
      }

      this.stack.push(fieldtree);
   }

   public StreamTagVisitor.EntryResult visitEntry(TagType<?> tagtype, String s) {
      FieldTree fieldtree = this.stack.element();
      if (fieldtree.isSelected(tagtype, s)) {
         return StreamTagVisitor.EntryResult.SKIP;
      } else {
         if (tagtype == CompoundTag.TYPE) {
            FieldTree fieldtree1 = fieldtree.fieldsToRecurse().get(s);
            if (fieldtree1 != null) {
               this.stack.push(fieldtree1);
            }
         }

         return super.visitEntry(tagtype, s);
      }
   }

   public StreamTagVisitor.ValueResult visitContainerEnd() {
      if (this.depth() == this.stack.element().depth()) {
         this.stack.pop();
      }

      return super.visitContainerEnd();
   }
}
