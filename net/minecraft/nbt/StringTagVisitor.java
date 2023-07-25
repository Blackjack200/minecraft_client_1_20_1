package net.minecraft.nbt;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class StringTagVisitor implements TagVisitor {
   private static final Pattern SIMPLE_VALUE = Pattern.compile("[A-Za-z0-9._+-]+");
   private final StringBuilder builder = new StringBuilder();

   public String visit(Tag tag) {
      tag.accept(this);
      return this.builder.toString();
   }

   public void visitString(StringTag stringtag) {
      this.builder.append(StringTag.quoteAndEscape(stringtag.getAsString()));
   }

   public void visitByte(ByteTag bytetag) {
      this.builder.append((Object)bytetag.getAsNumber()).append('b');
   }

   public void visitShort(ShortTag shorttag) {
      this.builder.append((Object)shorttag.getAsNumber()).append('s');
   }

   public void visitInt(IntTag inttag) {
      this.builder.append((Object)inttag.getAsNumber());
   }

   public void visitLong(LongTag longtag) {
      this.builder.append((Object)longtag.getAsNumber()).append('L');
   }

   public void visitFloat(FloatTag floattag) {
      this.builder.append(floattag.getAsFloat()).append('f');
   }

   public void visitDouble(DoubleTag doubletag) {
      this.builder.append(doubletag.getAsDouble()).append('d');
   }

   public void visitByteArray(ByteArrayTag bytearraytag) {
      this.builder.append("[B;");
      byte[] abyte = bytearraytag.getAsByteArray();

      for(int i = 0; i < abyte.length; ++i) {
         if (i != 0) {
            this.builder.append(',');
         }

         this.builder.append((int)abyte[i]).append('B');
      }

      this.builder.append(']');
   }

   public void visitIntArray(IntArrayTag intarraytag) {
      this.builder.append("[I;");
      int[] aint = intarraytag.getAsIntArray();

      for(int i = 0; i < aint.length; ++i) {
         if (i != 0) {
            this.builder.append(',');
         }

         this.builder.append(aint[i]);
      }

      this.builder.append(']');
   }

   public void visitLongArray(LongArrayTag longarraytag) {
      this.builder.append("[L;");
      long[] along = longarraytag.getAsLongArray();

      for(int i = 0; i < along.length; ++i) {
         if (i != 0) {
            this.builder.append(',');
         }

         this.builder.append(along[i]).append('L');
      }

      this.builder.append(']');
   }

   public void visitList(ListTag listtag) {
      this.builder.append('[');

      for(int i = 0; i < listtag.size(); ++i) {
         if (i != 0) {
            this.builder.append(',');
         }

         this.builder.append((new StringTagVisitor()).visit(listtag.get(i)));
      }

      this.builder.append(']');
   }

   public void visitCompound(CompoundTag compoundtag) {
      this.builder.append('{');
      List<String> list = Lists.newArrayList(compoundtag.getAllKeys());
      Collections.sort(list);

      for(String s : list) {
         if (this.builder.length() != 1) {
            this.builder.append(',');
         }

         this.builder.append(handleEscape(s)).append(':').append((new StringTagVisitor()).visit(compoundtag.get(s)));
      }

      this.builder.append('}');
   }

   protected static String handleEscape(String s) {
      return SIMPLE_VALUE.matcher(s).matches() ? s : StringTag.quoteAndEscape(s);
   }

   public void visitEnd(EndTag endtag) {
      this.builder.append("END");
   }
}
