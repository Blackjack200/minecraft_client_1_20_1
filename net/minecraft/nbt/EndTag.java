package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class EndTag implements Tag {
   private static final int SELF_SIZE_IN_BYTES = 8;
   public static final TagType<EndTag> TYPE = new TagType<EndTag>() {
      public EndTag load(DataInput datainput, int i, NbtAccounter nbtaccounter) {
         nbtaccounter.accountBytes(8L);
         return EndTag.INSTANCE;
      }

      public StreamTagVisitor.ValueResult parse(DataInput datainput, StreamTagVisitor streamtagvisitor) {
         return streamtagvisitor.visitEnd();
      }

      public void skip(DataInput datainput, int i) {
      }

      public void skip(DataInput datainput) {
      }

      public String getName() {
         return "END";
      }

      public String getPrettyName() {
         return "TAG_End";
      }

      public boolean isValue() {
         return true;
      }
   };
   public static final EndTag INSTANCE = new EndTag();

   private EndTag() {
   }

   public void write(DataOutput dataoutput) throws IOException {
   }

   public int sizeInBytes() {
      return 8;
   }

   public byte getId() {
      return 0;
   }

   public TagType<EndTag> getType() {
      return TYPE;
   }

   public String toString() {
      return this.getAsString();
   }

   public EndTag copy() {
      return this;
   }

   public void accept(TagVisitor tagvisitor) {
      tagvisitor.visitEnd(this);
   }

   public StreamTagVisitor.ValueResult accept(StreamTagVisitor streamtagvisitor) {
      return streamtagvisitor.visitEnd();
   }
}
