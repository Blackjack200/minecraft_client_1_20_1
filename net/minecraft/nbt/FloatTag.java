package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.util.Mth;

public class FloatTag extends NumericTag {
   private static final int SELF_SIZE_IN_BYTES = 12;
   public static final FloatTag ZERO = new FloatTag(0.0F);
   public static final TagType<FloatTag> TYPE = new TagType.StaticSize<FloatTag>() {
      public FloatTag load(DataInput datainput, int i, NbtAccounter nbtaccounter) throws IOException {
         nbtaccounter.accountBytes(12L);
         return FloatTag.valueOf(datainput.readFloat());
      }

      public StreamTagVisitor.ValueResult parse(DataInput datainput, StreamTagVisitor streamtagvisitor) throws IOException {
         return streamtagvisitor.visit(datainput.readFloat());
      }

      public int size() {
         return 4;
      }

      public String getName() {
         return "FLOAT";
      }

      public String getPrettyName() {
         return "TAG_Float";
      }

      public boolean isValue() {
         return true;
      }
   };
   private final float data;

   private FloatTag(float f) {
      this.data = f;
   }

   public static FloatTag valueOf(float f) {
      return f == 0.0F ? ZERO : new FloatTag(f);
   }

   public void write(DataOutput dataoutput) throws IOException {
      dataoutput.writeFloat(this.data);
   }

   public int sizeInBytes() {
      return 12;
   }

   public byte getId() {
      return 5;
   }

   public TagType<FloatTag> getType() {
      return TYPE;
   }

   public FloatTag copy() {
      return this;
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else {
         return object instanceof FloatTag && this.data == ((FloatTag)object).data;
      }
   }

   public int hashCode() {
      return Float.floatToIntBits(this.data);
   }

   public void accept(TagVisitor tagvisitor) {
      tagvisitor.visitFloat(this);
   }

   public long getAsLong() {
      return (long)this.data;
   }

   public int getAsInt() {
      return Mth.floor(this.data);
   }

   public short getAsShort() {
      return (short)(Mth.floor(this.data) & '\uffff');
   }

   public byte getAsByte() {
      return (byte)(Mth.floor(this.data) & 255);
   }

   public double getAsDouble() {
      return (double)this.data;
   }

   public float getAsFloat() {
      return this.data;
   }

   public Number getAsNumber() {
      return this.data;
   }

   public StreamTagVisitor.ValueResult accept(StreamTagVisitor streamtagvisitor) {
      return streamtagvisitor.visit(this.data);
   }
}
