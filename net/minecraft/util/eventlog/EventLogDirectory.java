package net.minecraft.util.eventlog;

import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.annotation.Nullable;
import org.slf4j.Logger;

public class EventLogDirectory {
   static final Logger LOGGER = LogUtils.getLogger();
   private static final int COMPRESS_BUFFER_SIZE = 4096;
   private static final String COMPRESSED_EXTENSION = ".gz";
   private final Path root;
   private final String extension;

   private EventLogDirectory(Path path, String s) {
      this.root = path;
      this.extension = s;
   }

   public static EventLogDirectory open(Path path, String s) throws IOException {
      Files.createDirectories(path);
      return new EventLogDirectory(path, s);
   }

   public EventLogDirectory.FileList listFiles() throws IOException {
      Stream<Path> stream = Files.list(this.root);

      EventLogDirectory.FileList var2;
      try {
         var2 = new EventLogDirectory.FileList(stream.filter((path1) -> Files.isRegularFile(path1)).map(this::parseFile).filter(Objects::nonNull).toList());
      } catch (Throwable var5) {
         if (stream != null) {
            try {
               stream.close();
            } catch (Throwable var4) {
               var5.addSuppressed(var4);
            }
         }

         throw var5;
      }

      if (stream != null) {
         stream.close();
      }

      return var2;
   }

   @Nullable
   private EventLogDirectory.File parseFile(Path path) {
      String s = path.getFileName().toString();
      int i = s.indexOf(46);
      if (i == -1) {
         return null;
      } else {
         EventLogDirectory.FileId eventlogdirectory_fileid = EventLogDirectory.FileId.parse(s.substring(0, i));
         if (eventlogdirectory_fileid != null) {
            String s1 = s.substring(i);
            if (s1.equals(this.extension)) {
               return new EventLogDirectory.RawFile(path, eventlogdirectory_fileid);
            }

            if (s1.equals(this.extension + ".gz")) {
               return new EventLogDirectory.CompressedFile(path, eventlogdirectory_fileid);
            }
         }

         return null;
      }
   }

   static void tryCompress(Path path, Path path1) throws IOException {
      if (Files.exists(path1)) {
         throw new IOException("Compressed target file already exists: " + path1);
      } else {
         FileChannel filechannel = FileChannel.open(path, StandardOpenOption.WRITE, StandardOpenOption.READ);

         try {
            FileLock filelock = filechannel.tryLock();
            if (filelock == null) {
               throw new IOException("Raw log file is already locked, cannot compress: " + path);
            }

            writeCompressed(filechannel, path1);
            filechannel.truncate(0L);
         } catch (Throwable var6) {
            if (filechannel != null) {
               try {
                  filechannel.close();
               } catch (Throwable var5) {
                  var6.addSuppressed(var5);
               }
            }

            throw var6;
         }

         if (filechannel != null) {
            filechannel.close();
         }

         Files.delete(path);
      }
   }

   private static void writeCompressed(ReadableByteChannel readablebytechannel, Path path) throws IOException {
      OutputStream outputstream = new GZIPOutputStream(Files.newOutputStream(path));

      try {
         byte[] abyte = new byte[4096];
         ByteBuffer bytebuffer = ByteBuffer.wrap(abyte);

         while(readablebytechannel.read(bytebuffer) >= 0) {
            bytebuffer.flip();
            outputstream.write(abyte, 0, bytebuffer.limit());
            bytebuffer.clear();
         }
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

   public EventLogDirectory.RawFile createNewFile(LocalDate localdate) throws IOException {
      int i = 1;
      Set<EventLogDirectory.FileId> set = this.listFiles().ids();

      EventLogDirectory.FileId eventlogdirectory_fileid;
      do {
         eventlogdirectory_fileid = new EventLogDirectory.FileId(localdate, i++);
      } while(set.contains(eventlogdirectory_fileid));

      EventLogDirectory.RawFile eventlogdirectory_rawfile = new EventLogDirectory.RawFile(this.root.resolve(eventlogdirectory_fileid.toFileName(this.extension)), eventlogdirectory_fileid);
      Files.createFile(eventlogdirectory_rawfile.path());
      return eventlogdirectory_rawfile;
   }

   public static record CompressedFile(Path path, EventLogDirectory.FileId id) implements EventLogDirectory.File {
      @Nullable
      public Reader openReader() throws IOException {
         return !Files.exists(this.path) ? null : new BufferedReader(new InputStreamReader(new GZIPInputStream(Files.newInputStream(this.path))));
      }

      public EventLogDirectory.CompressedFile compress() {
         return this;
      }
   }

   public interface File {
      Path path();

      EventLogDirectory.FileId id();

      @Nullable
      Reader openReader() throws IOException;

      EventLogDirectory.CompressedFile compress() throws IOException;
   }

   public static record FileId(LocalDate date, int index) {
      private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

      @Nullable
      public static EventLogDirectory.FileId parse(String s) {
         int i = s.indexOf("-");
         if (i == -1) {
            return null;
         } else {
            String s1 = s.substring(0, i);
            String s2 = s.substring(i + 1);

            try {
               return new EventLogDirectory.FileId(LocalDate.parse(s1, DATE_FORMATTER), Integer.parseInt(s2));
            } catch (DateTimeParseException | NumberFormatException var5) {
               return null;
            }
         }
      }

      public String toString() {
         return DATE_FORMATTER.format(this.date) + "-" + this.index;
      }

      public String toFileName(String s) {
         return this + s;
      }
   }

   public static class FileList implements Iterable<EventLogDirectory.File> {
      private final List<EventLogDirectory.File> files;

      FileList(List<EventLogDirectory.File> list) {
         this.files = new ArrayList<>(list);
      }

      public EventLogDirectory.FileList prune(LocalDate localdate, int i) {
         this.files.removeIf((eventlogdirectory_file) -> {
            EventLogDirectory.FileId eventlogdirectory_fileid = eventlogdirectory_file.id();
            LocalDate localdate2 = eventlogdirectory_fileid.date().plusDays((long)i);
            if (!localdate.isBefore(localdate2)) {
               try {
                  Files.delete(eventlogdirectory_file.path());
                  return true;
               } catch (IOException var6) {
                  EventLogDirectory.LOGGER.warn("Failed to delete expired event log file: {}", eventlogdirectory_file.path(), var6);
               }
            }

            return false;
         });
         return this;
      }

      public EventLogDirectory.FileList compressAll() {
         ListIterator<EventLogDirectory.File> listiterator = this.files.listIterator();

         while(listiterator.hasNext()) {
            EventLogDirectory.File eventlogdirectory_file = listiterator.next();

            try {
               listiterator.set(eventlogdirectory_file.compress());
            } catch (IOException var4) {
               EventLogDirectory.LOGGER.warn("Failed to compress event log file: {}", eventlogdirectory_file.path(), var4);
            }
         }

         return this;
      }

      public Iterator<EventLogDirectory.File> iterator() {
         return this.files.iterator();
      }

      public Stream<EventLogDirectory.File> stream() {
         return this.files.stream();
      }

      public Set<EventLogDirectory.FileId> ids() {
         return this.files.stream().map(EventLogDirectory.File::id).collect(Collectors.toSet());
      }
   }

   public static record RawFile(Path path, EventLogDirectory.FileId id) implements EventLogDirectory.File {
      public FileChannel openChannel() throws IOException {
         return FileChannel.open(this.path, StandardOpenOption.WRITE, StandardOpenOption.READ);
      }

      @Nullable
      public Reader openReader() throws IOException {
         return Files.exists(this.path) ? Files.newBufferedReader(this.path) : null;
      }

      public EventLogDirectory.CompressedFile compress() throws IOException {
         Path path = this.path.resolveSibling(this.path.getFileName().toString() + ".gz");
         EventLogDirectory.tryCompress(this.path, path);
         return new EventLogDirectory.CompressedFile(path, this.id);
      }
   }
}
