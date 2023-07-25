package net.minecraft.util;

import com.google.common.base.Charsets;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.AccessDeniedException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import net.minecraft.FileUtil;

public class DirectoryLock implements AutoCloseable {
   public static final String LOCK_FILE = "session.lock";
   private final FileChannel lockFile;
   private final FileLock lock;
   private static final ByteBuffer DUMMY;

   public static DirectoryLock create(Path path) throws IOException {
      Path path1 = path.resolve("session.lock");
      FileUtil.createDirectoriesSafe(path);
      FileChannel filechannel = FileChannel.open(path1, StandardOpenOption.CREATE, StandardOpenOption.WRITE);

      try {
         filechannel.write(DUMMY.duplicate());
         filechannel.force(true);
         FileLock filelock = filechannel.tryLock();
         if (filelock == null) {
            throw DirectoryLock.LockException.alreadyLocked(path1);
         } else {
            return new DirectoryLock(filechannel, filelock);
         }
      } catch (IOException var6) {
         try {
            filechannel.close();
         } catch (IOException var5) {
            var6.addSuppressed(var5);
         }

         throw var6;
      }
   }

   private DirectoryLock(FileChannel filechannel, FileLock filelock) {
      this.lockFile = filechannel;
      this.lock = filelock;
   }

   public void close() throws IOException {
      try {
         if (this.lock.isValid()) {
            this.lock.release();
         }
      } finally {
         if (this.lockFile.isOpen()) {
            this.lockFile.close();
         }

      }

   }

   public boolean isValid() {
      return this.lock.isValid();
   }

   public static boolean isLocked(Path path) throws IOException {
      Path path1 = path.resolve("session.lock");

      try {
         FileChannel filechannel = FileChannel.open(path1, StandardOpenOption.WRITE);

         boolean var4;
         try {
            FileLock filelock = filechannel.tryLock();

            try {
               var4 = filelock == null;
            } catch (Throwable var8) {
               if (filelock != null) {
                  try {
                     filelock.close();
                  } catch (Throwable var7) {
                     var8.addSuppressed(var7);
                  }
               }

               throw var8;
            }

            if (filelock != null) {
               filelock.close();
            }
         } catch (Throwable var9) {
            if (filechannel != null) {
               try {
                  filechannel.close();
               } catch (Throwable var6) {
                  var9.addSuppressed(var6);
               }
            }

            throw var9;
         }

         if (filechannel != null) {
            filechannel.close();
         }

         return var4;
      } catch (AccessDeniedException var10) {
         return true;
      } catch (NoSuchFileException var11) {
         return false;
      }
   }

   static {
      byte[] abyte = "\u2603".getBytes(Charsets.UTF_8);
      DUMMY = ByteBuffer.allocateDirect(abyte.length);
      DUMMY.put(abyte);
      DUMMY.flip();
   }

   public static class LockException extends IOException {
      private LockException(Path path, String s) {
         super(path.toAbsolutePath() + ": " + s);
      }

      public static DirectoryLock.LockException alreadyLocked(Path path) {
         return new DirectoryLock.LockException(path, "already locked (possibly by other Minecraft instance?)");
      }
   }
}
