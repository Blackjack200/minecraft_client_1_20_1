package net.minecraft.nbt;

import java.io.DataInput;
import java.io.IOException;

public interface TagType<T extends Tag> {
   T load(DataInput datainput, int i, NbtAccounter nbtaccounter) throws IOException;

   StreamTagVisitor.ValueResult parse(DataInput datainput, StreamTagVisitor streamtagvisitor) throws IOException;

   default void parseRoot(DataInput datainput, StreamTagVisitor streamtagvisitor) throws IOException {
      switch (streamtagvisitor.visitRootEntry(this)) {
         case CONTINUE:
            this.parse(datainput, streamtagvisitor);
         case HALT:
         default:
            break;
         case BREAK:
            this.skip(datainput);
      }

   }

   void skip(DataInput datainput, int i) throws IOException;

   void skip(DataInput datainput) throws IOException;

   default boolean isValue() {
      return false;
   }

   String getName();

   String getPrettyName();

   static TagType<EndTag> createInvalid(final int i) {
      return new TagType<EndTag>() {
         private IOException createException() {
            return new IOException("Invalid tag id: " + i);
         }

         public EndTag load(DataInput datainput, int ix, NbtAccounter nbtaccounter) throws IOException {
            throw this.createException();
         }

         public StreamTagVisitor.ValueResult parse(DataInput datainput, StreamTagVisitor streamtagvisitor) throws IOException {
            throw this.createException();
         }

         public void skip(DataInput datainput, int ix) throws IOException {
            throw this.createException();
         }

         public void skip(DataInput datainput) throws IOException {
            throw this.createException();
         }

         public String getName() {
            return "INVALID[" + i + "]";
         }

         public String getPrettyName() {
            return "UNKNOWN_" + i;
         }
      };
   }

   public interface StaticSize<T extends Tag> extends TagType<T> {
      default void skip(DataInput datainput) throws IOException {
         datainput.skipBytes(this.size());
      }

      default void skip(DataInput datainput, int i) throws IOException {
         datainput.skipBytes(this.size() * i);
      }

      int size();
   }

   public interface VariableSize<T extends Tag> extends TagType<T> {
      default void skip(DataInput datainput, int i) throws IOException {
         for(int j = 0; j < i; ++j) {
            this.skip(datainput);
         }

      }
   }
}
