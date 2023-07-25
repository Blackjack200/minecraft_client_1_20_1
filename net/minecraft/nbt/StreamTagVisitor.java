package net.minecraft.nbt;

public interface StreamTagVisitor {
   StreamTagVisitor.ValueResult visitEnd();

   StreamTagVisitor.ValueResult visit(String s);

   StreamTagVisitor.ValueResult visit(byte b0);

   StreamTagVisitor.ValueResult visit(short short0);

   StreamTagVisitor.ValueResult visit(int i);

   StreamTagVisitor.ValueResult visit(long i);

   StreamTagVisitor.ValueResult visit(float f);

   StreamTagVisitor.ValueResult visit(double d0);

   StreamTagVisitor.ValueResult visit(byte[] abyte);

   StreamTagVisitor.ValueResult visit(int[] aint);

   StreamTagVisitor.ValueResult visit(long[] along);

   StreamTagVisitor.ValueResult visitList(TagType<?> tagtype, int i);

   StreamTagVisitor.EntryResult visitEntry(TagType<?> tagtype);

   StreamTagVisitor.EntryResult visitEntry(TagType<?> tagtype, String s);

   StreamTagVisitor.EntryResult visitElement(TagType<?> tagtype, int i);

   StreamTagVisitor.ValueResult visitContainerEnd();

   StreamTagVisitor.ValueResult visitRootEntry(TagType<?> tagtype);

   public static enum EntryResult {
      ENTER,
      SKIP,
      BREAK,
      HALT;
   }

   public static enum ValueResult {
      CONTINUE,
      BREAK,
      HALT;
   }
}
