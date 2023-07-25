package net.minecraft.nbt.visitors;

import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.TagType;

public interface SkipAll extends StreamTagVisitor {
   SkipAll INSTANCE = new SkipAll() {
   };

   default StreamTagVisitor.ValueResult visitEnd() {
      return StreamTagVisitor.ValueResult.CONTINUE;
   }

   default StreamTagVisitor.ValueResult visit(String s) {
      return StreamTagVisitor.ValueResult.CONTINUE;
   }

   default StreamTagVisitor.ValueResult visit(byte b0) {
      return StreamTagVisitor.ValueResult.CONTINUE;
   }

   default StreamTagVisitor.ValueResult visit(short short0) {
      return StreamTagVisitor.ValueResult.CONTINUE;
   }

   default StreamTagVisitor.ValueResult visit(int i) {
      return StreamTagVisitor.ValueResult.CONTINUE;
   }

   default StreamTagVisitor.ValueResult visit(long i) {
      return StreamTagVisitor.ValueResult.CONTINUE;
   }

   default StreamTagVisitor.ValueResult visit(float f) {
      return StreamTagVisitor.ValueResult.CONTINUE;
   }

   default StreamTagVisitor.ValueResult visit(double d0) {
      return StreamTagVisitor.ValueResult.CONTINUE;
   }

   default StreamTagVisitor.ValueResult visit(byte[] abyte) {
      return StreamTagVisitor.ValueResult.CONTINUE;
   }

   default StreamTagVisitor.ValueResult visit(int[] aint) {
      return StreamTagVisitor.ValueResult.CONTINUE;
   }

   default StreamTagVisitor.ValueResult visit(long[] along) {
      return StreamTagVisitor.ValueResult.CONTINUE;
   }

   default StreamTagVisitor.ValueResult visitList(TagType<?> tagtype, int i) {
      return StreamTagVisitor.ValueResult.CONTINUE;
   }

   default StreamTagVisitor.EntryResult visitElement(TagType<?> tagtype, int i) {
      return StreamTagVisitor.EntryResult.SKIP;
   }

   default StreamTagVisitor.EntryResult visitEntry(TagType<?> tagtype) {
      return StreamTagVisitor.EntryResult.SKIP;
   }

   default StreamTagVisitor.EntryResult visitEntry(TagType<?> tagtype, String s) {
      return StreamTagVisitor.EntryResult.SKIP;
   }

   default StreamTagVisitor.ValueResult visitContainerEnd() {
      return StreamTagVisitor.ValueResult.CONTINUE;
   }

   default StreamTagVisitor.ValueResult visitRootEntry(TagType<?> tagtype) {
      return StreamTagVisitor.ValueResult.CONTINUE;
   }
}
