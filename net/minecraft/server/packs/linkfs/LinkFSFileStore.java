package net.minecraft.server.packs.linkfs;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;
import javax.annotation.Nullable;

class LinkFSFileStore extends FileStore {
   private final String name;

   public LinkFSFileStore(String s) {
      this.name = s;
   }

   public String name() {
      return this.name;
   }

   public String type() {
      return "index";
   }

   public boolean isReadOnly() {
      return true;
   }

   public long getTotalSpace() {
      return 0L;
   }

   public long getUsableSpace() {
      return 0L;
   }

   public long getUnallocatedSpace() {
      return 0L;
   }

   public boolean supportsFileAttributeView(Class<? extends FileAttributeView> oclass) {
      return oclass == BasicFileAttributeView.class;
   }

   public boolean supportsFileAttributeView(String s) {
      return "basic".equals(s);
   }

   @Nullable
   public <V extends FileStoreAttributeView> V getFileStoreAttributeView(Class<V> oclass) {
      return (V)null;
   }

   public Object getAttribute(String s) throws IOException {
      throw new UnsupportedOperationException();
   }
}
