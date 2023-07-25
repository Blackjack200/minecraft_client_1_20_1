package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;

public class IntArrayTag extends CollectionTag<IntTag> {
   private static final int SELF_SIZE_IN_BYTES = 24;
   public static final TagType<IntArrayTag> TYPE = new TagType.VariableSize<IntArrayTag>() {
      public IntArrayTag load(DataInput datainput, int i, NbtAccounter nbtaccounter) throws IOException {
         nbtaccounter.accountBytes(24L);
         int j = datainput.readInt();
         nbtaccounter.accountBytes(4L * (long)j);
         int[] aint = new int[j];

         for(int k = 0; k < j; ++k) {
            aint[k] = datainput.readInt();
         }

         return new IntArrayTag(aint);
      }

      public StreamTagVisitor.ValueResult parse(DataInput datainput, StreamTagVisitor streamtagvisitor) throws IOException {
         int i = datainput.readInt();
         int[] aint = new int[i];

         for(int j = 0; j < i; ++j) {
            aint[j] = datainput.readInt();
         }

         return streamtagvisitor.visit(aint);
      }

      public void skip(DataInput datainput) throws IOException {
         datainput.skipBytes(datainput.readInt() * 4);
      }

      public String getName() {
         return "INT[]";
      }

      public String getPrettyName() {
         return "TAG_Int_Array";
      }
   };
   private int[] data;

   public IntArrayTag(int[] aint) {
      this.data = aint;
   }

   public IntArrayTag(List<Integer> list) {
      this(toArray(list));
   }

   private static int[] toArray(List<Integer> list) {
      int[] aint = new int[list.size()];

      for(int i = 0; i < list.size(); ++i) {
         Integer integer = list.get(i);
         aint[i] = integer == null ? 0 : integer;
      }

      return aint;
   }

   public void write(DataOutput dataoutput) throws IOException {
      dataoutput.writeInt(this.data.length);

      for(int i : this.data) {
         dataoutput.writeInt(i);
      }

   }

   public int sizeInBytes() {
      return 24 + 4 * this.data.length;
   }

   public byte getId() {
      return 11;
   }

   public TagType<IntArrayTag> getType() {
      return TYPE;
   }

   public String toString() {
      return this.getAsString();
   }

   public IntArrayTag copy() {
      int[] aint = new int[this.data.length];
      System.arraycopy(this.data, 0, aint, 0, this.data.length);
      return new IntArrayTag(aint);
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else {
         return object instanceof IntArrayTag && Arrays.equals(this.data, ((IntArrayTag)object).data);
      }
   }

   public int hashCode() {
      return Arrays.hashCode(this.data);
   }

   public int[] getAsIntArray() {
      return this.data;
   }

   public void accept(TagVisitor tagvisitor) {
      tagvisitor.visitIntArray(this);
   }

   public int size() {
      return this.data.length;
   }

   public IntTag get(int i) {
      return IntTag.valueOf(this.data[i]);
   }

   public IntTag set(int i, IntTag inttag) {
      int j = this.data[i];
      this.data[i] = inttag.getAsInt();
      return IntTag.valueOf(j);
   }

   public void add(int i, IntTag inttag) {
      this.data = ArrayUtils.add(this.data, i, inttag.getAsInt());
   }

   public boolean setTag(int i, Tag tag) {
      if (tag instanceof NumericTag) {
         this.data[i] = ((NumericTag)tag).getAsInt();
         return true;
      } else {
         return false;
      }
   }

   public boolean addTag(int i, Tag tag) {
      if (tag instanceof NumericTag) {
         this.data = ArrayUtils.add(this.data, i, ((NumericTag)tag).getAsInt());
         return true;
      } else {
         return false;
      }
   }

   public IntTag remove(int i) {
      int j = this.data[i];
      this.data = ArrayUtils.remove(this.data, i);
      return IntTag.valueOf(j);
   }

   public byte getElementType() {
      return 3;
   }

   public void clear() {
      this.data = new int[0];
   }

   public StreamTagVisitor.ValueResult accept(StreamTagVisitor streamtagvisitor) {
      return streamtagvisitor.visit(this.data);
   }
}
