package net.minecraft.server.packs;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.IoSupplier;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class FilePackResources extends AbstractPackResources {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final Splitter SPLITTER = Splitter.on('/').omitEmptyStrings().limit(3);
   private final File file;
   @Nullable
   private ZipFile zipFile;
   private boolean failedToLoad;

   public FilePackResources(String s, File file, boolean flag) {
      super(s, flag);
      this.file = file;
   }

   @Nullable
   private ZipFile getOrCreateZipFile() {
      if (this.failedToLoad) {
         return null;
      } else {
         if (this.zipFile == null) {
            try {
               this.zipFile = new ZipFile(this.file);
            } catch (IOException var2) {
               LOGGER.error("Failed to open pack {}", this.file, var2);
               this.failedToLoad = true;
               return null;
            }
         }

         return this.zipFile;
      }
   }

   private static String getPathFromLocation(PackType packtype, ResourceLocation resourcelocation) {
      return String.format(Locale.ROOT, "%s/%s/%s", packtype.getDirectory(), resourcelocation.getNamespace(), resourcelocation.getPath());
   }

   @Nullable
   public IoSupplier<InputStream> getRootResource(String... astring) {
      return this.getResource(String.join("/", astring));
   }

   public IoSupplier<InputStream> getResource(PackType packtype, ResourceLocation resourcelocation) {
      return this.getResource(getPathFromLocation(packtype, resourcelocation));
   }

   @Nullable
   private IoSupplier<InputStream> getResource(String s) {
      ZipFile zipfile = this.getOrCreateZipFile();
      if (zipfile == null) {
         return null;
      } else {
         ZipEntry zipentry = zipfile.getEntry(s);
         return zipentry == null ? null : IoSupplier.create(zipfile, zipentry);
      }
   }

   public Set<String> getNamespaces(PackType packtype) {
      ZipFile zipfile = this.getOrCreateZipFile();
      if (zipfile == null) {
         return Set.of();
      } else {
         Enumeration<? extends ZipEntry> enumeration = zipfile.entries();
         Set<String> set = Sets.newHashSet();

         while(enumeration.hasMoreElements()) {
            ZipEntry zipentry = enumeration.nextElement();
            String s = zipentry.getName();
            if (s.startsWith(packtype.getDirectory() + "/")) {
               List<String> list = Lists.newArrayList(SPLITTER.split(s));
               if (list.size() > 1) {
                  String s1 = list.get(1);
                  if (s1.equals(s1.toLowerCase(Locale.ROOT))) {
                     set.add(s1);
                  } else {
                     LOGGER.warn("Ignored non-lowercase namespace: {} in {}", s1, this.file);
                  }
               }
            }
         }

         return set;
      }
   }

   protected void finalize() throws Throwable {
      this.close();
      super.finalize();
   }

   public void close() {
      if (this.zipFile != null) {
         IOUtils.closeQuietly((Closeable)this.zipFile);
         this.zipFile = null;
      }

   }

   public void listResources(PackType packtype, String s, String s1, PackResources.ResourceOutput packresources_resourceoutput) {
      ZipFile zipfile = this.getOrCreateZipFile();
      if (zipfile != null) {
         Enumeration<? extends ZipEntry> enumeration = zipfile.entries();
         String s2 = packtype.getDirectory() + "/" + s + "/";
         String s3 = s2 + s1 + "/";

         while(enumeration.hasMoreElements()) {
            ZipEntry zipentry = enumeration.nextElement();
            if (!zipentry.isDirectory()) {
               String s4 = zipentry.getName();
               if (s4.startsWith(s3)) {
                  String s5 = s4.substring(s2.length());
                  ResourceLocation resourcelocation = ResourceLocation.tryBuild(s, s5);
                  if (resourcelocation != null) {
                     packresources_resourceoutput.accept(resourcelocation, IoSupplier.create(zipfile, zipentry));
                  } else {
                     LOGGER.warn("Invalid path in datapack: {}:{}, ignoring", s, s5);
                  }
               }
            }
         }

      }
   }
}
