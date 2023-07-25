package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.UTFDataFormatException;
import java.util.Objects;
import net.minecraft.Util;

public class StringTag implements Tag {
   private static final int SELF_SIZE_IN_BYTES = 36;
   public static final TagType<StringTag> TYPE = new TagType.VariableSize<StringTag>() {
      public StringTag load(DataInput datainput, int i, NbtAccounter nbtaccounter) throws IOException {
         nbtaccounter.accountBytes(36L);
         String s = datainput.readUTF();
         nbtaccounter.accountBytes((long)(2 * s.length()));
         return StringTag.valueOf(s);
      }

      public StreamTagVisitor.ValueResult parse(DataInput datainput, StreamTagVisitor streamtagvisitor) throws IOException {
         return streamtagvisitor.visit(datainput.readUTF());
      }

      public void skip(DataInput datainput) throws IOException {
         StringTag.skipString(datainput);
      }

      public String getName() {
         return "STRING";
      }

      public String getPrettyName() {
         return "TAG_String";
      }

      public boolean isValue() {
         return true;
      }
   };
   private static final StringTag EMPTY = new StringTag("");
   private static final char DOUBLE_QUOTE = '"';
   private static final char SINGLE_QUOTE = '\'';
   private static final char ESCAPE = '\\';
   private static final char NOT_SET = '\u0000';
   private final String data;

   public static void skipString(DataInput datainput) throws IOException {
      datainput.skipBytes(datainput.readUnsignedShort());
   }

   private StringTag(String s) {
      Objects.requireNonNull(s, "Null string not allowed");
      this.data = s;
   }

   public static StringTag valueOf(String s) {
      return s.isEmpty() ? EMPTY : new StringTag(s);
   }

   public void write(DataOutput dataoutput) throws IOException {
      try {
         dataoutput.writeUTF(this.data);
      } catch (UTFDataFormatException var3) {
         Util.logAndPauseIfInIde("Failed to write NBT String", var3);
         dataoutput.writeUTF("");
      }

   }

   public int sizeInBytes() {
      return 36 + 2 * this.data.length();
   }

   public byte getId() {
      return 8;
   }

   public TagType<StringTag> getType() {
      return TYPE;
   }

   public String toString() {
      return Tag.super.getAsString();
   }

   public StringTag copy() {
      return this;
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else {
         return object instanceof StringTag && Objects.equals(this.data, ((StringTag)object).data);
      }
   }

   public int hashCode() {
      return this.data.hashCode();
   }

   public String getAsString() {
      return this.data;
   }

   public void accept(TagVisitor tagvisitor) {
      tagvisitor.visitString(this);
   }

   public static String quoteAndEscape(String s) {
      StringBuilder stringbuilder = new StringBuilder(" ");
      char c0 = 0;

      for(int i = 0; i < s.length(); ++i) {
         char c1 = s.charAt(i);
         if (c1 == '\\') {
            stringbuilder.append('\\');
         } else if (c1 == '"' || c1 == '\'') {
            if (c0 == 0) {
               c0 = (char)(c1 == '"' ? 39 : 34);
            }

            if (c0 == c1) {
               stringbuilder.append('\\');
            }
         }

         stringbuilder.append(c1);
      }

      if (c0 == 0) {
         c0 = '"';
      }

      stringbuilder.setCharAt(0, c0);
      stringbuilder.append(c0);
      return stringbuilder.toString();
   }

   public StreamTagVisitor.ValueResult accept(StreamTagVisitor streamtagvisitor) {
      return streamtagvisitor.visit(this.data);
   }
}
