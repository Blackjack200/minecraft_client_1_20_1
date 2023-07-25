package net.minecraft.world.level.chunk.storage;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.logging.LogUtils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.world.level.ChunkPos;
import org.slf4j.Logger;

public class RegionFile implements AutoCloseable {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int SECTOR_BYTES = 4096;
   @VisibleForTesting
   protected static final int SECTOR_INTS = 1024;
   private static final int CHUNK_HEADER_SIZE = 5;
   private static final int HEADER_OFFSET = 0;
   private static final ByteBuffer PADDING_BUFFER = ByteBuffer.allocateDirect(1);
   private static final String EXTERNAL_FILE_EXTENSION = ".mcc";
   private static final int EXTERNAL_STREAM_FLAG = 128;
   private static final int EXTERNAL_CHUNK_THRESHOLD = 256;
   private static final int CHUNK_NOT_PRESENT = 0;
   private final FileChannel file;
   private final Path externalFileDir;
   final RegionFileVersion version;
   private final ByteBuffer header = ByteBuffer.allocateDirect(8192);
   private final IntBuffer offsets;
   private final IntBuffer timestamps;
   @VisibleForTesting
   protected final RegionBitmap usedSectors = new RegionBitmap();

   public RegionFile(Path path, Path path1, boolean flag) throws IOException {
      this(path, path1, RegionFileVersion.VERSION_DEFLATE, flag);
   }

   public RegionFile(Path path, Path path1, RegionFileVersion regionfileversion, boolean flag) throws IOException {
      this.version = regionfileversion;
      if (!Files.isDirectory(path1)) {
         throw new IllegalArgumentException("Expected directory, got " + path1.toAbsolutePath());
      } else {
         this.externalFileDir = path1;
         this.offsets = this.header.asIntBuffer();
         this.offsets.limit(1024);
         this.header.position(4096);
         this.timestamps = this.header.asIntBuffer();
         if (flag) {
            this.file = FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.DSYNC);
         } else {
            this.file = FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);
         }

         this.usedSectors.force(0, 2);
         this.header.position(0);
         int i = this.file.read(this.header, 0L);
         if (i != -1) {
            if (i != 8192) {
               LOGGER.warn("Region file {} has truncated header: {}", path, i);
            }

            long j = Files.size(path);

            for(int k = 0; k < 1024; ++k) {
               int l = this.offsets.get(k);
               if (l != 0) {
                  int i1 = getSectorNumber(l);
                  int j1 = getNumSectors(l);
                  if (i1 < 2) {
                     LOGGER.warn("Region file {} has invalid sector at index: {}; sector {} overlaps with header", path, k, i1);
                     this.offsets.put(k, 0);
                  } else if (j1 == 0) {
                     LOGGER.warn("Region file {} has an invalid sector at index: {}; size has to be > 0", path, k);
                     this.offsets.put(k, 0);
                  } else if ((long)i1 * 4096L > j) {
                     LOGGER.warn("Region file {} has an invalid sector at index: {}; sector {} is out of bounds", path, k, i1);
                     this.offsets.put(k, 0);
                  } else {
                     this.usedSectors.force(i1, j1);
                  }
               }
            }
         }

      }
   }

   private Path getExternalChunkPath(ChunkPos chunkpos) {
      String s = "c." + chunkpos.x + "." + chunkpos.z + ".mcc";
      return this.externalFileDir.resolve(s);
   }

   @Nullable
   public synchronized DataInputStream getChunkDataInputStream(ChunkPos chunkpos) throws IOException {
      int i = this.getOffset(chunkpos);
      if (i == 0) {
         return null;
      } else {
         int j = getSectorNumber(i);
         int k = getNumSectors(i);
         int l = k * 4096;
         ByteBuffer bytebuffer = ByteBuffer.allocate(l);
         this.file.read(bytebuffer, (long)(j * 4096));
         bytebuffer.flip();
         if (bytebuffer.remaining() < 5) {
            LOGGER.error("Chunk {} header is truncated: expected {} but read {}", chunkpos, l, bytebuffer.remaining());
            return null;
         } else {
            int i1 = bytebuffer.getInt();
            byte b0 = bytebuffer.get();
            if (i1 == 0) {
               LOGGER.warn("Chunk {} is allocated, but stream is missing", (Object)chunkpos);
               return null;
            } else {
               int j1 = i1 - 1;
               if (isExternalStreamChunk(b0)) {
                  if (j1 != 0) {
                     LOGGER.warn("Chunk has both internal and external streams");
                  }

                  return this.createExternalChunkInputStream(chunkpos, getExternalChunkVersion(b0));
               } else if (j1 > bytebuffer.remaining()) {
                  LOGGER.error("Chunk {} stream is truncated: expected {} but read {}", chunkpos, j1, bytebuffer.remaining());
                  return null;
               } else if (j1 < 0) {
                  LOGGER.error("Declared size {} of chunk {} is negative", i1, chunkpos);
                  return null;
               } else {
                  return this.createChunkInputStream(chunkpos, b0, createStream(bytebuffer, j1));
               }
            }
         }
      }
   }

   private static int getTimestamp() {
      return (int)(Util.getEpochMillis() / 1000L);
   }

   private static boolean isExternalStreamChunk(byte b0) {
      return (b0 & 128) != 0;
   }

   private static byte getExternalChunkVersion(byte b0) {
      return (byte)(b0 & -129);
   }

   @Nullable
   private DataInputStream createChunkInputStream(ChunkPos chunkpos, byte b0, InputStream inputstream) throws IOException {
      RegionFileVersion regionfileversion = RegionFileVersion.fromId(b0);
      if (regionfileversion == null) {
         LOGGER.error("Chunk {} has invalid chunk stream version {}", chunkpos, b0);
         return null;
      } else {
         return new DataInputStream(regionfileversion.wrap(inputstream));
      }
   }

   @Nullable
   private DataInputStream createExternalChunkInputStream(ChunkPos chunkpos, byte b0) throws IOException {
      Path path = this.getExternalChunkPath(chunkpos);
      if (!Files.isRegularFile(path)) {
         LOGGER.error("External chunk path {} is not file", (Object)path);
         return null;
      } else {
         return this.createChunkInputStream(chunkpos, b0, Files.newInputStream(path));
      }
   }

   private static ByteArrayInputStream createStream(ByteBuffer bytebuffer, int i) {
      return new ByteArrayInputStream(bytebuffer.array(), bytebuffer.position(), i);
   }

   private int packSectorOffset(int i, int j) {
      return i << 8 | j;
   }

   private static int getNumSectors(int i) {
      return i & 255;
   }

   private static int getSectorNumber(int i) {
      return i >> 8 & 16777215;
   }

   private static int sizeToSectors(int i) {
      return (i + 4096 - 1) / 4096;
   }

   public boolean doesChunkExist(ChunkPos chunkpos) {
      int i = this.getOffset(chunkpos);
      if (i == 0) {
         return false;
      } else {
         int j = getSectorNumber(i);
         int k = getNumSectors(i);
         ByteBuffer bytebuffer = ByteBuffer.allocate(5);

         try {
            this.file.read(bytebuffer, (long)(j * 4096));
            bytebuffer.flip();
            if (bytebuffer.remaining() != 5) {
               return false;
            } else {
               int l = bytebuffer.getInt();
               byte b0 = bytebuffer.get();
               if (isExternalStreamChunk(b0)) {
                  if (!RegionFileVersion.isValidVersion(getExternalChunkVersion(b0))) {
                     return false;
                  }

                  if (!Files.isRegularFile(this.getExternalChunkPath(chunkpos))) {
                     return false;
                  }
               } else {
                  if (!RegionFileVersion.isValidVersion(b0)) {
                     return false;
                  }

                  if (l == 0) {
                     return false;
                  }

                  int i1 = l - 1;
                  if (i1 < 0 || i1 > 4096 * k) {
                     return false;
                  }
               }

               return true;
            }
         } catch (IOException var9) {
            return false;
         }
      }
   }

   public DataOutputStream getChunkDataOutputStream(ChunkPos chunkpos) throws IOException {
      return new DataOutputStream(this.version.wrap(new RegionFile.ChunkBuffer(chunkpos)));
   }

   public void flush() throws IOException {
      this.file.force(true);
   }

   public void clear(ChunkPos chunkpos) throws IOException {
      int i = getOffsetIndex(chunkpos);
      int j = this.offsets.get(i);
      if (j != 0) {
         this.offsets.put(i, 0);
         this.timestamps.put(i, getTimestamp());
         this.writeHeader();
         Files.deleteIfExists(this.getExternalChunkPath(chunkpos));
         this.usedSectors.free(getSectorNumber(j), getNumSectors(j));
      }
   }

   protected synchronized void write(ChunkPos chunkpos, ByteBuffer bytebuffer) throws IOException {
      int i = getOffsetIndex(chunkpos);
      int j = this.offsets.get(i);
      int k = getSectorNumber(j);
      int l = getNumSectors(j);
      int i1 = bytebuffer.remaining();
      int j1 = sizeToSectors(i1);
      int k1;
      RegionFile.CommitOp regionfile_commitop;
      if (j1 >= 256) {
         Path path = this.getExternalChunkPath(chunkpos);
         LOGGER.warn("Saving oversized chunk {} ({} bytes} to external file {}", chunkpos, i1, path);
         j1 = 1;
         k1 = this.usedSectors.allocate(j1);
         regionfile_commitop = this.writeToExternalFile(path, bytebuffer);
         ByteBuffer bytebuffer1 = this.createExternalStub();
         this.file.write(bytebuffer1, (long)(k1 * 4096));
      } else {
         k1 = this.usedSectors.allocate(j1);
         regionfile_commitop = () -> Files.deleteIfExists(this.getExternalChunkPath(chunkpos));
         this.file.write(bytebuffer, (long)(k1 * 4096));
      }

      this.offsets.put(i, this.packSectorOffset(k1, j1));
      this.timestamps.put(i, getTimestamp());
      this.writeHeader();
      regionfile_commitop.run();
      if (k != 0) {
         this.usedSectors.free(k, l);
      }

   }

   private ByteBuffer createExternalStub() {
      ByteBuffer bytebuffer = ByteBuffer.allocate(5);
      bytebuffer.putInt(1);
      bytebuffer.put((byte)(this.version.getId() | 128));
      bytebuffer.flip();
      return bytebuffer;
   }

   private RegionFile.CommitOp writeToExternalFile(Path path, ByteBuffer bytebuffer) throws IOException {
      Path path1 = Files.createTempFile(this.externalFileDir, "tmp", (String)null);
      FileChannel filechannel = FileChannel.open(path1, StandardOpenOption.CREATE, StandardOpenOption.WRITE);

      try {
         bytebuffer.position(5);
         filechannel.write(bytebuffer);
      } catch (Throwable var8) {
         if (filechannel != null) {
            try {
               filechannel.close();
            } catch (Throwable var7) {
               var8.addSuppressed(var7);
            }
         }

         throw var8;
      }

      if (filechannel != null) {
         filechannel.close();
      }

      return () -> Files.move(path1, path, StandardCopyOption.REPLACE_EXISTING);
   }

   private void writeHeader() throws IOException {
      this.header.position(0);
      this.file.write(this.header, 0L);
   }

   private int getOffset(ChunkPos chunkpos) {
      return this.offsets.get(getOffsetIndex(chunkpos));
   }

   public boolean hasChunk(ChunkPos chunkpos) {
      return this.getOffset(chunkpos) != 0;
   }

   private static int getOffsetIndex(ChunkPos chunkpos) {
      return chunkpos.getRegionLocalX() + chunkpos.getRegionLocalZ() * 32;
   }

   public void close() throws IOException {
      try {
         this.padToFullSector();
      } finally {
         try {
            this.file.force(true);
         } finally {
            this.file.close();
         }
      }

   }

   private void padToFullSector() throws IOException {
      int i = (int)this.file.size();
      int j = sizeToSectors(i) * 4096;
      if (i != j) {
         ByteBuffer bytebuffer = PADDING_BUFFER.duplicate();
         bytebuffer.position(0);
         this.file.write(bytebuffer, (long)(j - 1));
      }

   }

   class ChunkBuffer extends ByteArrayOutputStream {
      private final ChunkPos pos;

      public ChunkBuffer(ChunkPos chunkpos) {
         super(8096);
         super.write(0);
         super.write(0);
         super.write(0);
         super.write(0);
         super.write(RegionFile.this.version.getId());
         this.pos = chunkpos;
      }

      public void close() throws IOException {
         ByteBuffer bytebuffer = ByteBuffer.wrap(this.buf, 0, this.count);
         bytebuffer.putInt(0, this.count - 5 + 1);
         RegionFile.this.write(this.pos, bytebuffer);
      }
   }

   interface CommitOp {
      void run() throws IOException;
   }
}
