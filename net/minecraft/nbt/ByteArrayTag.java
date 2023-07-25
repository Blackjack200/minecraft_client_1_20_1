package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;

public class ByteArrayTag extends CollectionTag<ByteTag> {
   private static final int SELF_SIZE_IN_BYTES = 24;
   public static final TagType<ByteArrayTag> TYPE = new TagType.VariableSize<ByteArrayTag>() {
      public ByteArrayTag load(DataInput datainput, int i, NbtAccounter nbtaccounter) throws IOException {
         nbtaccounter.accountBytes(24L);
         int j = datainput.readInt();
         nbtaccounter.accountBytes(1L * (long)j);
         byte[] abyte = new byte[j];
         datainput.readFully(abyte);
         return new ByteArrayTag(abyte);
      }

      public StreamTagVisitor.ValueResult parse(DataInput datainput, StreamTagVisitor streamtagvisitor) throws IOException {
         int i = datainput.readInt();
         byte[] abyte = new byte[i];
         datainput.readFully(abyte);
         return streamtagvisitor.visit(abyte);
      }

      public void skip(DataInput datainput) throws IOException {
         datainput.skipBytes(datainput.readInt() * 1);
      }

      public String getName() {
         return "BYTE[]";
      }

      public String getPrettyName() {
         return "TAG_Byte_Array";
      }
   };
   private byte[] data;

   public ByteArrayTag(byte[] abyte) {
      this.data = abyte;
   }

   public ByteArrayTag(List<Byte> list) {
      this(toArray(list));
   }

   private static byte[] toArray(List<Byte> list) {
      byte[] abyte = new byte[list.size()];

      for(int i = 0; i < list.size(); ++i) {
         Byte obyte = list.get(i);
         abyte[i] = obyte == null ? 0 : obyte;
      }

      return abyte;
   }

   public void write(DataOutput dataoutput) throws IOException {
      dataoutput.writeInt(this.data.length);
      dataoutput.write(this.data);
   }

   public int sizeInBytes() {
      return 24 + 1 * this.data.length;
   }

   public byte getId() {
      return 7;
   }

   public TagType<ByteArrayTag> getType() {
      return TYPE;
   }

   public String toString() {
      return this.getAsString();
   }

   public Tag copy() {
      byte[] abyte = new byte[this.data.length];
      System.arraycopy(this.data, 0, abyte, 0, this.data.length);
      return new ByteArrayTag(abyte);
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else {
         return object instanceof ByteArrayTag && Arrays.equals(this.data, ((ByteArrayTag)object).data);
      }
   }

   public int hashCode() {
      return Arrays.hashCode(this.data);
   }

   public void accept(TagVisitor tagvisitor) {
      tagvisitor.visitByteArray(this);
   }

   public byte[] getAsByteArray() {
      return this.data;
   }

   public int size() {
      return this.data.length;
   }

   public ByteTag get(int i) {
      return ByteTag.valueOf(this.data[i]);
   }

   public ByteTag set(int i, ByteTag bytetag) {
      byte b0 = this.data[i];
      this.data[i] = bytetag.getAsByte();
      return ByteTag.valueOf(b0);
   }

   public void add(int i, ByteTag bytetag) {
      this.data = ArrayUtils.add(this.data, i, bytetag.getAsByte());
   }

   public boolean setTag(int i, Tag tag) {
      if (tag instanceof NumericTag) {
         this.data[i] = ((NumericTag)tag).getAsByte();
         return true;
      } else {
         return false;
      }
   }

   public boolean addTag(int i, Tag tag) {
      if (tag instanceof NumericTag) {
         this.data = ArrayUtils.add(this.data, i, ((NumericTag)tag).getAsByte());
         return true;
      } else {
         return false;
      }
   }

   public ByteTag remove(int i) {
      byte b0 = this.data[i];
      this.data = ArrayUtils.remove(this.data, i);
      return ByteTag.valueOf(b0);
   }

   public byte getElementType() {
      return 1;
   }

   public void clear() {
      this.data = new byte[0];
   }

   public StreamTagVisitor.ValueResult accept(StreamTagVisitor streamtagvisitor) {
      return streamtagvisitor.visit(this.data);
   }
}
