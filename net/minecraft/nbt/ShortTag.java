package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ShortTag extends NumericTag {
   private static final int SELF_SIZE_IN_BYTES = 10;
   public static final TagType<ShortTag> TYPE = new TagType.StaticSize<ShortTag>() {
      public ShortTag load(DataInput datainput, int i, NbtAccounter nbtaccounter) throws IOException {
         nbtaccounter.accountBytes(10L);
         return ShortTag.valueOf(datainput.readShort());
      }

      public StreamTagVisitor.ValueResult parse(DataInput datainput, StreamTagVisitor streamtagvisitor) throws IOException {
         return streamtagvisitor.visit(datainput.readShort());
      }

      public int size() {
         return 2;
      }

      public String getName() {
         return "SHORT";
      }

      public String getPrettyName() {
         return "TAG_Short";
      }

      public boolean isValue() {
         return true;
      }
   };
   private final short data;

   ShortTag(short short0) {
      this.data = short0;
   }

   public static ShortTag valueOf(short short0) {
      return short0 >= -128 && short0 <= 1024 ? ShortTag.Cache.cache[short0 - -128] : new ShortTag(short0);
   }

   public void write(DataOutput dataoutput) throws IOException {
      dataoutput.writeShort(this.data);
   }

   public int sizeInBytes() {
      return 10;
   }

   public byte getId() {
      return 2;
   }

   public TagType<ShortTag> getType() {
      return TYPE;
   }

   public ShortTag copy() {
      return this;
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else {
         return object instanceof ShortTag && this.data == ((ShortTag)object).data;
      }
   }

   public int hashCode() {
      return this.data;
   }

   public void accept(TagVisitor tagvisitor) {
      tagvisitor.visitShort(this);
   }

   public long getAsLong() {
      return (long)this.data;
   }

   public int getAsInt() {
      return this.data;
   }

   public short getAsShort() {
      return this.data;
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
      static final ShortTag[] cache = new ShortTag[1153];

      private Cache() {
      }

      static {
         for(int i = 0; i < cache.length; ++i) {
            cache[i] = new ShortTag((short)(-128 + i));
         }

      }
   }
}
