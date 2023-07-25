package net.minecraft.nbt.visitors;

import com.google.common.collect.ImmutableSet;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.TagType;

public class CollectFields extends CollectToTag {
   private int fieldsToGetCount;
   private final Set<TagType<?>> wantedTypes;
   private final Deque<FieldTree> stack = new ArrayDeque<>();

   public CollectFields(FieldSelector... afieldselector) {
      this.fieldsToGetCount = afieldselector.length;
      ImmutableSet.Builder<TagType<?>> immutableset_builder = ImmutableSet.builder();
      FieldTree fieldtree = FieldTree.createRoot();

      for(FieldSelector fieldselector : afieldselector) {
         fieldtree.addEntry(fieldselector);
         immutableset_builder.add(fieldselector.type());
      }

      this.stack.push(fieldtree);
      immutableset_builder.add(CompoundTag.TYPE);
      this.wantedTypes = immutableset_builder.build();
   }

   public StreamTagVisitor.ValueResult visitRootEntry(TagType<?> tagtype) {
      return tagtype != CompoundTag.TYPE ? StreamTagVisitor.ValueResult.HALT : super.visitRootEntry(tagtype);
   }

   public StreamTagVisitor.EntryResult visitEntry(TagType<?> tagtype) {
      FieldTree fieldtree = this.stack.element();
      if (this.depth() > fieldtree.depth()) {
         return super.visitEntry(tagtype);
      } else if (this.fieldsToGetCount <= 0) {
         return StreamTagVisitor.EntryResult.HALT;
      } else {
         return !this.wantedTypes.contains(tagtype) ? StreamTagVisitor.EntryResult.SKIP : super.visitEntry(tagtype);
      }
   }

   public StreamTagVisitor.EntryResult visitEntry(TagType<?> tagtype, String s) {
      FieldTree fieldtree = this.stack.element();
      if (this.depth() > fieldtree.depth()) {
         return super.visitEntry(tagtype, s);
      } else if (fieldtree.selectedFields().remove(s, tagtype)) {
         --this.fieldsToGetCount;
         return super.visitEntry(tagtype, s);
      } else {
         if (tagtype == CompoundTag.TYPE) {
            FieldTree fieldtree1 = fieldtree.fieldsToRecurse().get(s);
            if (fieldtree1 != null) {
               this.stack.push(fieldtree1);
               return super.visitEntry(tagtype, s);
            }
         }

         return StreamTagVisitor.EntryResult.SKIP;
      }
   }

   public StreamTagVisitor.ValueResult visitContainerEnd() {
      if (this.depth() == this.stack.element().depth()) {
         this.stack.pop();
      }

      return super.visitContainerEnd();
   }

   public int getMissingFieldCount() {
      return this.fieldsToGetCount;
   }
}
