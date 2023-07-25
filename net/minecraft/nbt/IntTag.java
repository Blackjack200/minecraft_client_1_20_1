package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class IntTag extends NumericTag {
   private static final int SELF_SIZE_IN_BYTES = 12;
   public static final TagType<IntTag> TYPE = new TagType.StaticSize<IntTag>() {
      public IntTag load(DataInput datainput, int i, NbtAccounter nbtaccounter) throws IOException {
         nbtaccounter.accountBytes(12L);
         return IntTag.valueOf(datainput.readInt());
      }

      public StreamTagVisitor.ValueResult parse(DataInput datainput, StreamTagVisitor streamtagvisitor) throws IOException {
         return streamtagvisitor.visit(datainput.readInt());
      }

      public int size() {
         return 4;
      }

      public String getName() {
         return "INT";
      }

      public String getPrettyName() {
         return "TAG_Int";
      }

      public boolean isValue() {
         return true;
      }
   };
   private final int data;

   IntTag(int i) {
      this.data = i;
   }

   public static IntTag valueOf(int i) {
      return i >= -128 && i <= 1024 ? IntTag.Cache.cache[i - -128] : new IntTag(i);
   }

   public void write(DataOutput dataoutput) throws IOException {
      dataoutput.writeInt(this.data);
   }

   public int sizeInBytes() {
      return 12;
   }

   public byte getId() {
      return 3;
   }

   public TagType<IntTag> getType() {
      return TYPE;
   }

   public IntTag copy() {
      return this;
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else {
         return object instanceof IntTag && this.data == ((IntTag)object).data;
      }
   }

   public int hashCode() {
      return this.data;
   }

   public void accept(TagVisitor tagvisitor) {
      tagvisitor.visitInt(this);
   }

   public long getAsLong() {
      return (long)this.data;
   }

   public int getAsInt() {
      return this.data;
   }

   public short getAsShort() {
      return (short)(this.data & '\uffff');
   }

   public byte getAsByte() {
      return (byte)(this.data & 255);
   }

   public double getAsDouble() {
      return (double)this.data;
   }

   public float getAsFloat() {
      return (float)this.data;
   }

   public Number getAsNumber() {
      return this.data;
   }

   public StreamTagVisitor.ValueResult accept(StreamTagVisitor streamtagvisitor) {
      return streamtagvisitor.visit(this.data);
   }

   static class Cache {
      private static final int HIGH = 1024;
      private static final int LOW = -128;
      static final IntTag[] cache = new IntTag[1153];

      private Cache() {
      }

      static {
         for(int i = 0; i < cache.length; ++i) {
            cache[i] = new IntTag(-128 + i);
         }

      }
   }
}
