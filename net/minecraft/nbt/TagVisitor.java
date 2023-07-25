package net.minecraft.nbt;

public interface TagVisitor {
   void visitString(StringTag stringtag);

   void visitByte(ByteTag bytetag);

   void visitShort(ShortTag shorttag);

   void visitInt(IntTag inttag);

   void visitLong(LongTag longtag);

   void visitFloat(FloatTag floattag);

   void visitDouble(DoubleTag doubletag);

   void visitByteArray(ByteArrayTag bytearraytag);

   void visitIntArray(IntArrayTag intarraytag);

   void visitLongArray(LongArrayTag longarraytag);

   void visitList(ListTag listtag);

   void visitCompound(CompoundTag compoundtag);

   void visitEnd(EndTag endtag);
}
