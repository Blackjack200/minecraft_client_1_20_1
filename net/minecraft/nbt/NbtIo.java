package net.minecraft.nbt;

import java.io.BufferedOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.util.FastBufferedInputStream;

public class NbtIo {
   public static CompoundTag readCompressed(File file) throws IOException {
      InputStream inputstream = new FileInputStream(file);

      CompoundTag var2;
      try {
         var2 = readCompressed(inputstream);
      } catch (Throwable var5) {
         try {
            inputstream.close();
         } catch (Throwable var4) {
            var5.addSuppressed(var4);
         }

         throw var5;
      }

      inputstream.close();
      return var2;
   }

   private static DataInputStream createDecompressorStream(InputStream inputstream) throws IOException {
      return new DataInputStream(new FastBufferedInputStream(new GZIPInputStream(inputstream)));
   }

   public static CompoundTag readCompressed(InputStream inputstream) throws IOException {
      DataInputStream datainputstream = createDecompressorStream(inputstream);

      CompoundTag var2;
      try {
         var2 = read(datainputstream, NbtAccounter.UNLIMITED);
      } catch (Throwable var5) {
         if (datainputstream != null) {
            try {
               datainputstream.close();
            } catch (Throwable var4) {
               var5.addSuppressed(var4);
            }
         }

         throw var5;
      }

      if (datainputstream != null) {
         datainputstream.close();
      }

      return var2;
   }

   public static void parseCompressed(File file, StreamTagVisitor streamtagvisitor) throws IOException {
      InputStream inputstream = new FileInputStream(file);

      try {
         parseCompressed(inputstream, streamtagvisitor);
      } catch (Throwable var6) {
         try {
            inputstream.close();
         } catch (Throwable var5) {
            var6.addSuppressed(var5);
         }

         throw var6;
      }

      inputstream.close();
   }

   public static void parseCompressed(InputStream inputstream, StreamTagVisitor streamtagvisitor) throws IOException {
      DataInputStream datainputstream = createDecompressorStream(inputstream);

      try {
         parse(datainputstream, streamtagvisitor);
      } catch (Throwable var6) {
         if (datainputstream != null) {
            try {
               datainputstream.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }
         }

         throw var6;
      }

      if (datainputstream != null) {
         datainputstream.close();
      }

   }

   public static void writeCompressed(CompoundTag compoundtag, File file) throws IOException {
      OutputStream outputstream = new FileOutputStream(file);

      try {
         writeCompressed(compoundtag, outputstream);
      } catch (Throwable var6) {
         try {
            outputstream.close();
         } catch (Throwable var5) {
            var6.addSuppressed(var5);
         }

         throw var6;
      }

      outputstream.close();
   }

   public static void writeCompressed(CompoundTag compoundtag, OutputStream outputstream) throws IOException {
      DataOutputStream dataoutputstream = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(outputstream)));

      try {
         write(compoundtag, dataoutputstream);
      } catch (Throwable var6) {
         try {
            dataoutputstream.close();
         } catch (Throwable var5) {
            var6.addSuppressed(var5);
         }

         throw var6;
      }

      dataoutputstream.close();
   }

   public static void write(CompoundTag compoundtag, File file) throws IOException {
      FileOutputStream fileoutputstream = new FileOutputStream(file);

      try {
         DataOutputStream dataoutputstream = new DataOutputStream(fileoutputstream);

         try {
            write(compoundtag, dataoutputstream);
         } catch (Throwable var8) {
            try {
               dataoutputstream.close();
            } catch (Throwable var7) {
               var8.addSuppressed(var7);
            }

            throw var8;
         }

         dataoutputstream.close();
      } catch (Throwable var9) {
         try {
            fileoutputstream.close();
         } catch (Throwable var6) {
            var9.addSuppressed(var6);
         }

         throw var9;
      }

      fileoutputstream.close();
   }

   @Nullable
   public static CompoundTag read(File file) throws IOException {
      if (!file.exists()) {
         return null;
      } else {
         FileInputStream fileinputstream = new FileInputStream(file);

         CompoundTag var3;
         try {
            DataInputStream datainputstream = new DataInputStream(fileinputstream);

            try {
               var3 = read(datainputstream, NbtAccounter.UNLIMITED);
            } catch (Throwable var7) {
               try {
                  datainputstream.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }

               throw var7;
            }

            datainputstream.close();
         } catch (Throwable var8) {
            try {
               fileinputstream.close();
            } catch (Throwable var5) {
               var8.addSuppressed(var5);
            }

            throw var8;
         }

         fileinputstream.close();
         return var3;
      }
   }

   public static CompoundTag read(DataInput datainput) throws IOException {
      return read(datainput, NbtAccounter.UNLIMITED);
   }

   public static CompoundTag read(DataInput datainput, NbtAccounter nbtaccounter) throws IOException {
      Tag tag = readUnnamedTag(datainput, 0, nbtaccounter);
      if (tag instanceof CompoundTag) {
         return (CompoundTag)tag;
      } else {
         throw new IOException("Root tag must be a named compound tag");
      }
   }

   public static void write(CompoundTag compoundtag, DataOutput dataoutput) throws IOException {
      writeUnnamedTag(compoundtag, dataoutput);
   }

   public static void parse(DataInput datainput, StreamTagVisitor streamtagvisitor) throws IOException {
      TagType<?> tagtype = TagTypes.getType(datainput.readByte());
      if (tagtype == EndTag.TYPE) {
         if (streamtagvisitor.visitRootEntry(EndTag.TYPE) == StreamTagVisitor.ValueResult.CONTINUE) {
            streamtagvisitor.visitEnd();
         }

      } else {
         switch (streamtagvisitor.visitRootEntry(tagtype)) {
            case HALT:
            default:
               break;
            case BREAK:
               StringTag.skipString(datainput);
               tagtype.skip(datainput);
               break;
            case CONTINUE:
               StringTag.skipString(datainput);
               tagtype.parse(datainput, streamtagvisitor);
         }

      }
   }

   public static void writeUnnamedTag(Tag tag, DataOutput dataoutput) throws IOException {
      dataoutput.writeByte(tag.getId());
      if (tag.getId() != 0) {
         dataoutput.writeUTF("");
         tag.write(dataoutput);
      }
   }

   private static Tag readUnnamedTag(DataInput datainput, int i, NbtAccounter nbtaccounter) throws IOException {
      byte b0 = datainput.readByte();
      if (b0 == 0) {
         return EndTag.INSTANCE;
      } else {
         StringTag.skipString(datainput);

         try {
            return TagTypes.getType(b0).load(datainput, i, nbtaccounter);
         } catch (IOException var7) {
            CrashReport crashreport = CrashReport.forThrowable(var7, "Loading NBT data");
            CrashReportCategory crashreportcategory = crashreport.addCategory("NBT Tag");
            crashreportcategory.setDetail("Tag type", b0);
            throw new ReportedException(crashreport);
         }
      }
   }
}
