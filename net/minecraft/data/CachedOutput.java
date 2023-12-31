package net.minecraft.data;

import com.google.common.hash.HashCode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public interface CachedOutput {
   CachedOutput NO_CACHE = (path, abyte, hashcode) -> {
      Files.createDirectories(path.getParent());
      Files.write(path, abyte);
   };

   void writeIfNeeded(Path path, byte[] abyte, HashCode hashcode) throws IOException;
}
