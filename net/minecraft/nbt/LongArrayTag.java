package net.minecraft.nbt;

import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;

public class LongArrayTag extends CollectionTag<LongTag> {
   private static final int SELF_SIZE_IN_BYTES = 24;
   public static final TagType<LongArrayTag> TYPE = new TagType.VariableSize<LongArrayTag>() {
      public LongArrayTag load(DataInput datainput, int i, NbtAccounter nbtaccounter) throws IOException {
         nbtaccounter.accountBytes(24L);
         int j = datainput.readInt();
         nbtaccounter.accountBytes(8L * (long)j);
         long[] along = new long[j];

         for(int k = 0; k < j; ++k) {
            along[k] = datainput.readLong();
         }

         return new LongArrayTag(along);
      }

      public StreamTagVisitor.ValueResult parse(DataInput datainput, StreamTagVisitor streamtagvisitor) throws IOException {
         int i = datainput.readInt();
         long[] along = new long[i];

         for(int j = 0; j < i; ++j) {
            along[j] = datainput.readLong();
         }

         return streamtagvisitor.visit(along);
      }

      public void skip(DataInput datainput) throws IOException {
         datainput.skipBytes(datainput.readInt() * 8);
      }

      public String getName() {
         return "LONG[]";
      }

      public String getPrettyName() {
         return "TAG_Long_Array";
      }
   };
   private long[] data;

   public LongArrayTag(long[] along) {
      this.data = along;
   }

   public LongArrayTag(LongSet longset) {
      this.data = longset.toLongArray();
   }

   public LongArrayTag(List<Long> list) {
      this(toArray(list));
   }

   private static long[] toArray(List<Long> list) {
      long[] along = new long[list.size()];

      for(int i = 0; i < list.size(); ++i) {
         Long olong = list.get(i);
         along[i] = olong == null ? 0L : olong;
      }

      return along;
   }

   public void write(DataOutput dataoutput) throws IOException {
      dataoutput.writeInt(this.data.length);

      for(long i : this.data) {
         dataoutput.writeLong(i);
      }

   }

   public int sizeInBytes() {
      return 24 + 8 * this.data.length;
   }

   public byte getId() {
      return 12;
   }

   public TagType<LongArrayTag> getType() {
      return TYPE;
   }

   public String toString() {
      return this.getAsString();
   }

   public LongArrayTag copy() {
      long[] along = new long[this.data.length];
      System.arraycopy(this.data, 0, along, 0, this.data.length);
      return new LongArrayTag(along);
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else {
         return object instanceof LongArrayTag && Arrays.equals(this.data, ((LongArrayTag)object).data);
      }
   }

   public int hashCode() {
      return Arrays.hashCode(this.data);
   }

   public void accept(TagVisitor tagvisitor) {
      tagvisitor.visitLongArray(this);
   }

   public long[] getAsLongArray() {
      return this.data;
   }

   public int size() {
      return this.data.length;
   }

   public LongTag get(int i) {
      return LongTag.valueOf(this.data[i]);
   }

   public LongTag set(int i, LongTag longtag) {
      long j = this.data[i];
      this.data[i] = longtag.getAsLong();
      return LongTag.valueOf(j);
   }

   public void add(int i, LongTag longtag) {
      this.data = ArrayUtils.add(this.data, i, longtag.getAsLong());
   }

   public boolean setTag(int i, Tag tag) {
      if (tag instanceof NumericTag) {
         this.data[i] = ((NumericTag)tag).getAsLong();
         return true;
      } else {
         return false;
      }
   }

   public boolean addTag(int i, Tag tag) {
      if (tag instanceof NumericTag) {
         this.data = ArrayUtils.add(this.data, i, ((NumericTag)tag).getAsLong());
         return true;
      } else {
         return false;
      }
   }

   public LongTag remove(int i) {
      long j = this.data[i];
      this.data = ArrayUtils.remove(this.data, i);
      return LongTag.valueOf(j);
   }

   public byte getElementType() {
      return 4;
   }

   public void clear() {
      this.data = new long[0];
   }

   public StreamTagVisitor.ValueResult accept(StreamTagVisitor streamtagvisitor) {
      return streamtagvisitor.visit(this.data);
   }
}
